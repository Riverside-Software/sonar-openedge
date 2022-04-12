/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.foundation;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.prorefactor.proparse.antlr4.ProparseListener;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.platform.Server;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.MessageException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.CheckRegistration;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.InvalidLicenseException;
import org.sonar.plugins.openedge.api.LicenseRegistration;
import org.sonar.plugins.openedge.api.LicenseRegistration.License;
import org.sonar.plugins.openedge.api.LicenseRegistration.LicenseType;
import org.sonar.plugins.openedge.api.TreeParserRegistration;
import org.sonar.plugins.openedge.api.checks.OpenEdgeCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeCheck.CheckType;
import org.sonar.plugins.openedge.api.checks.OpenEdgeDumpFileCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonarsource.api.sonarlint.SonarLintSide;

import com.google.common.base.Strings;

@ScannerSide
@SonarLintSide
@ServerSide
public class OpenEdgeComponents {
  private static final Logger LOG = Loggers.get(OpenEdgeComponents.class);

  private final Map<ActiveRule, OpenEdgeProparseCheck> ppChecksMap = new HashMap<>();
  private final Map<ActiveRule, OpenEdgeDumpFileCheck> dfChecksMap = new HashMap<>();

  private final Server server;
  private final CheckRegistrar checkRegistrar = new CheckRegistrar();
  private final LicenseRegistrar licenseRegistrar = new LicenseRegistrar();
  private final TreeParserRegistrar parserRegistrar = new TreeParserRegistrar();

  private boolean initialized = false;
  private String analytics = "";

  public OpenEdgeComponents() {
    this(null, null, null, null);
  }

  public OpenEdgeComponents(Server server) {
    this(server, null, null, null);
  }

  public OpenEdgeComponents(CheckRegistration[] checkRegistrars) {
    this(null, checkRegistrars, null, null);
  }

  public OpenEdgeComponents(Server server, CheckRegistration[] checkRegistrars) {
    this(server, checkRegistrars, null, null);
  }

  public OpenEdgeComponents(CheckRegistration[] checkRegistrars, LicenseRegistration[] licRegistrars) {
    this(null, checkRegistrars, licRegistrars, null);
  }

  public OpenEdgeComponents(Server server, CheckRegistration[] checkRegistrars, LicenseRegistration[] licRegistrars) {
    this(server, checkRegistrars, licRegistrars, null);
  }

  public OpenEdgeComponents(CheckRegistration[] checkRegistrars, LicenseRegistration[] licRegistrars,
      TreeParserRegistration[] tpRegistrars) {
    this(null, checkRegistrars, licRegistrars, tpRegistrars);
  }

  public OpenEdgeComponents(Server server, CheckRegistration[] checkRegistrars, LicenseRegistration[] licRegistrars,
      TreeParserRegistration[] tpRegistrars) {
    this.server = server;
    if (checkRegistrars != null) {
      for (CheckRegistration registration : checkRegistrars) {
        registration.register(checkRegistrar);
      }
    }
    if (licRegistrars != null) {
      for (LicenseRegistration registration : licRegistrars) {
        registration.register(licenseRegistrar);
      }
    }
    if (tpRegistrars != null) {
      for (TreeParserRegistration registration : tpRegistrars) {
        registration.register(parserRegistrar);
      }
    }
  }

  public Iterable<Class<? extends ProparseListener>> getProparseListeners() {
    return Collections.unmodifiableList(parserRegistrar.allListeners);
  }

  public Collection<License> getLicenses() {
    return licenseRegistrar.getLicenses();
  }

  public License getLicense(SonarProduct product, String permId, String repoName) {
    return licenseRegistrar.getLicense(product, permId, repoName);
  }

  public void init(SensorContext context) {
    if (initialized)
      return;
    initializeChecks(context);
    initialized = true;
  }

  private void initializeChecks(SensorContext context) {
    String permId = getServerId();

    // Proparse and XREF rules
    for (ActiveRule rule : context.activeRules().findByLanguage(Constants.LANGUAGE_KEY)) {
      OpenEdgeCheck<?> lint = initializeCheck(context, rule, context.runtime().getProduct(), permId);
      if ((lint != null) && (lint.getCheckType() == CheckType.PROPARSE)) {
        ppChecksMap.put(rule, (OpenEdgeProparseCheck) lint);
      }
    }
    // DB rules
    for (ActiveRule rule : context.activeRules().findByLanguage(Constants.DB_LANGUAGE_KEY)) {
      OpenEdgeCheck<?> lint = initializeCheck(context, rule, context.runtime().getProduct(), permId);
      if ((lint != null) && (lint.getCheckType() == CheckType.DUMP_FILE)) {
        dfChecksMap.put(rule, (OpenEdgeDumpFileCheck) lint);
      }
    }
  }

  public void setAnalytics(String analytics) {
    this.analytics = analytics;
  }

  public String getAnalytics() {
    return analytics;
  }

  public Map<ActiveRule, OpenEdgeProparseCheck> getProparseRules() {
    return Collections.unmodifiableMap(ppChecksMap);
  }

  public Map<ActiveRule, OpenEdgeDumpFileCheck> getDumpFileRules() {
    return Collections.unmodifiableMap(dfChecksMap);
  }

  private OpenEdgeCheck<?> initializeCheck(SensorContext context, ActiveRule rule, SonarProduct product,
      String permId) {
    RuleKey ruleKey = rule.ruleKey();
    // AFAIK, no way to be sure if a rule is based on a template or not
    String clsName = rule.templateRuleKey() == null ? ruleKey.rule() : rule.templateRuleKey();

    try {
      Class<? extends OpenEdgeCheck<?>> clz = checkRegistrar.getCheck(clsName);
      if (clz == null)
        return null;
      OpenEdgeCheck<?> check = clz.getConstructor().newInstance();
      check.setContext(ruleKey, context, getLicense(product, permId, ruleKey.repository()));
      configureFields(rule, check);
      check.initialize();

      return check;
    } catch (ReflectiveOperationException caught) {
      LOG.error("Unable to instantiate rule " + clsName, caught);
      throw new RuntimeException("Stopping analyzer due to previous exception");
    } catch (InvalidLicenseException caught) {
      LOG.error("Unable to instantiate rule {} - {}", clsName, caught.getMessage());
      throw new RuntimeException("Stopping analyzer due to previous exception");
    }
  }

  private static void configureFields(ActiveRule activeRule, Object check) {
    for (String param : activeRule.params().keySet()) {
      Field field = getField(check, param);
      if (field == null) {
        throw MessageException.of("The field " + param
            + " does not exist or is not annotated with @RuleProperty in the class " + check.getClass().getName());
      }
      if (Strings.nullToEmpty(activeRule.param(param)).trim().length() > 0) {
        configureField(check, field, activeRule.param(param));
      }
    }
  }

  private static void configureField(Object check, Field field, String value) {
    try {
      field.setAccessible(true);

      if (field.getType().equals(String.class)) {
        field.set(check, value);
      } else if (int.class == field.getType()) {
        field.setInt(check, Integer.parseInt(value));
      } else if (short.class == field.getType()) {
        field.setShort(check, Short.parseShort(value));
      } else if (long.class == field.getType()) {
        field.setLong(check, Long.parseLong(value));
      } else if (double.class == field.getType()) {
        field.setDouble(check, Double.parseDouble(value));
      } else if (boolean.class == field.getType()) {
        field.setBoolean(check, Boolean.parseBoolean(value));
      } else if (byte.class == field.getType()) {
        field.setByte(check, Byte.parseByte(value));
      } else if (Integer.class == field.getType()) {
        field.set(check, Integer.parseInt(value));
      } else if (Long.class == field.getType()) {
        field.set(check, Long.parseLong(value));
      } else if (Double.class == field.getType()) {
        field.set(check, Double.parseDouble(value));
      } else if (Boolean.class == field.getType()) {
        field.set(check, Boolean.parseBoolean(value));
      } else {
        throw MessageException.of("The type of the field " + field + " is not supported: " + field.getType());
      }
    } catch (IllegalAccessException e) {
      throw MessageException.of(
          "Can not set the value of the field " + field + " in the class: " + check.getClass().getName(), e);
    }
  }

  private static Field getField(Object check, String key) {
    Field[] fields = check.getClass().getDeclaredFields();
    for (Field field : fields) {
      RuleProperty propertyAnnotation = field.getAnnotation(RuleProperty.class);
      if ((propertyAnnotation != null) && (key.equals(field.getName()) || key.equals(propertyAnnotation.key()))) {
        return field;
      }
    }
    return null;
  }

  public String getServerId() {
    return server == null ? "" : server.getId();
  }

  private static class LicenseRegistrar implements LicenseRegistration.Registrar {
    private final Collection<License> licenses = new ArrayList<>();

    @Override
    public void registerLicense(int version, String permanentId, SonarProduct product, String customerName, String salt,
        String repoName, LicenseRegistration.LicenseType type, byte[] signature, long expirationDate, long lines) {
      if (Strings.isNullOrEmpty(repoName))
        return;
      LOG.debug("Found {} license - Permanent ID '{}' - Customer '{}' - Repository '{}' - Expiration date {}",
          type.toString(), permanentId, customerName, repoName,
          DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(expirationDate)));
      // Only one license per product/ repository / permID
      License existingLic = hasRegisteredLicense(product, repoName, permanentId);
      License newLic = new License.Builder().setVersion(version).setPermanentId(permanentId).setProduct(
          product).setCustomerName(customerName).setSalt(salt).setRepositoryName(repoName).setType(type).setSignature(
              signature).setExpirationDate(expirationDate).setLines(lines).build();
      if (existingLic == null) {
        licenses.add(newLic);
      } else if (existingLic.getExpirationDate() < newLic.getExpirationDate()) {
        licenses.remove(existingLic);
        licenses.add(newLic);
      }
    }

    private Collection<License> getLicenses() {
      return licenses;
    }

    private License hasRegisteredLicense(SonarProduct product, String repoName, String permId) {
      if ((permId == null) || (repoName == null))
        return null;
      for (License lic : licenses) {
        if (((lic.getType() == LicenseType.COMMERCIAL) || (lic.getType() == LicenseType.PARTNER))
            && (lic.getProduct() == product) && repoName.equals(lic.getRepositoryName())
            && permId.equals(lic.getPermanentId()))
          return lic;
      }
      return null;
    }

    private License getLicense(SonarProduct product, String permId, String repoName) {
      if ((permId == null) || (repoName == null))
        return null;
      // TODO Remove this code when old licenses are not used anymore
      String miniPermId = (permId.indexOf('-') == 8) && (permId.length() >= 20)
          ? permId.substring(permId.indexOf('-') + 1) : permId;

      Optional<License> srch = licenses.stream() //
        .filter(lic -> (lic.getType() == LicenseType.COMMERCIAL) || (lic.getType() == LicenseType.PARTNER)) //
        .filter(lic -> lic.getProduct() == product) //
        .filter(lic -> repoName.equals(lic.getRepositoryName())) //
        .filter(lic -> (lic.getVersion() >= 3 && permId.equals(lic.getPermanentId()))
            || miniPermId.equals(lic.getPermanentId())).findFirst();
      if (srch.isPresent())
        return srch.get();
      srch = licenses.stream() //
        .filter(lic -> lic.getType() == LicenseType.EVALUATION) //
        .filter(lic -> lic.getProduct() == product) //
        .filter(lic -> repoName.equals(lic.getRepositoryName())) //
        .filter(lic -> (lic.getVersion() >= 3 && permId.equals(lic.getPermanentId()))
            || miniPermId.equals(lic.getPermanentId())).findFirst();
      if (srch.isPresent())
        return srch.get();

      return null;
    }
  }

  private static class CheckRegistrar implements CheckRegistration.Registrar {
    private final Collection<Class<? extends OpenEdgeCheck<?>>> allChecks = new ArrayList<>();

    @Override
    public void registerParserCheck(Class<? extends OpenEdgeProparseCheck> check) {
      allChecks.add(check);
    }

    @Override
    public void registerDumpFileCheck(Class<? extends OpenEdgeDumpFileCheck> check) {
      allChecks.add(check);
    }

    public Class<? extends OpenEdgeCheck<?>> getCheck(String className) {
      for (Class<? extends OpenEdgeCheck<?>> clz : allChecks) {
        if (clz.getCanonicalName().equalsIgnoreCase(className)) {
          return clz;
        }
      }
      return null;
    }
  }

  private static class TreeParserRegistrar implements TreeParserRegistration.Registrar {
    private final List<Class<? extends ProparseListener>> allListeners = new ArrayList<>();

    @Override
    public void registerTreeParser(Class<? extends ProparseListener> listener) {
      allListeners.add(listener);
    }
  }

}
