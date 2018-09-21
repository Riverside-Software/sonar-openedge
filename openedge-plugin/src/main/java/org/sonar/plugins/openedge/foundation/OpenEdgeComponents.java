/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
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

  private final CheckRegistrar checkRegistrar = new CheckRegistrar();
  private final LicenseRegistrar licenseRegistrar = new LicenseRegistrar();
  private boolean initialized = false;

  public OpenEdgeComponents() {
    this(null, null);
  }

  public OpenEdgeComponents(CheckRegistration[] checkRegistrars) {
    this(checkRegistrars, null);
  }

  public OpenEdgeComponents(LicenseRegistration[] licRegistrars) {
    this(null, licRegistrars);
  }

  public OpenEdgeComponents(CheckRegistration[] checkRegistrars, LicenseRegistration[] licRegistrars) {
    if (checkRegistrars != null) {
      registerChecks(checkRegistrars);
    }
    if (licRegistrars != null) {
      registerLicences(licRegistrars);
    }
  }

  private void registerChecks(CheckRegistration[] registrations) {
    for (CheckRegistration registration : registrations) {
      registration.register(checkRegistrar);
    }
  }

  private void registerLicences(LicenseRegistration[] registrations) {
    for (LicenseRegistration registration : registrations) {
      registration.register(licenseRegistrar);
    }
  }

  public Iterable<License> getLicenses() {
    return licenseRegistrar.getLicenses();
  }

  public License getLicense(String repoName, String permId) {
    return licenseRegistrar.getLicense(repoName, permId);
  }

  public void initializeLicense(SensorContext context) {
    String permId = (context.runtime().getProduct() == SonarProduct.SONARLINT ? "sonarlint-" : "")
        + OpenEdgeProjectHelper.getServerId(context);
    for (License entry : licenseRegistrar.getLicenses()) {
      if (permId.equals(entry.getPermanentId())) {
        LOG.info("Repository '{}' associated with {} license permanent ID '{}' - Customer '{}' - Expiration date {}",
            entry.getRepositoryName(), entry.getType().toString(), entry.getPermanentId(), entry.getCustomerName(),
            LocalDateTime.ofEpochSecond(entry.getExpirationDate() / 1000, 0, ZoneOffset.UTC).format(
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));
      }
    }
  }

  public void initializeChecks(SensorContext context) {
    if (initialized)
      return;

    String permId = (context.runtime().getProduct() == SonarProduct.SONARLINT ? "sonarlint-" : "")
        + OpenEdgeProjectHelper.getServerId(context);

    // Proparse and XREF rules
    for (ActiveRule rule : context.activeRules().findByLanguage(Constants.LANGUAGE_KEY)) {
      OpenEdgeCheck<?> lint = initializeCheck(context, rule, permId);
      if ((lint != null) && (lint.getCheckType() == CheckType.PROPARSE)) {
        ppChecksMap.put(rule, (OpenEdgeProparseCheck) lint);
      }
    }
    // DB rules
    for (ActiveRule rule : context.activeRules().findByLanguage(Constants.DB_LANGUAGE_KEY)) {
      OpenEdgeCheck<?> lint = initializeCheck(context, rule, permId);
      if ((lint != null) && (lint.getCheckType() == CheckType.DUMP_FILE)) {
        dfChecksMap.put(rule, (OpenEdgeDumpFileCheck) lint);
      }
    }

    initialized = true;
  }

  public Map<ActiveRule, OpenEdgeProparseCheck> getProparseRules() { 
    return Collections.unmodifiableMap(ppChecksMap);  
  }

  public Map<ActiveRule, OpenEdgeDumpFileCheck> getDumpFileRules() {  
    return Collections.unmodifiableMap(dfChecksMap);  
  }

  private OpenEdgeCheck<?> initializeCheck(SensorContext context, ActiveRule rule, String permId) {
    RuleKey ruleKey = rule.ruleKey();
    // AFAIK, no way to be sure if a rule is based on a template or not
    String clsName = rule.templateRuleKey() == null ? ruleKey.rule() : rule.templateRuleKey();

    try {
      Class<? extends OpenEdgeCheck<?>> clz = checkRegistrar.getCheck(clsName);
      if (clz == null)
        return null;
      OpenEdgeCheck<?> check = clz.getConstructor().newInstance();
      check.setContext(ruleKey, context, getLicense(ruleKey.repository(), permId));
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
      if (StringUtils.isNotBlank(activeRule.param(param))) {
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
      if (propertyAnnotation != null) {
        if (StringUtils.equals(key, field.getName()) || StringUtils.equals(key, propertyAnnotation.key())) {
          return field;
        }
      }
    }
    return null;
  }

  private static class LicenseRegistrar implements LicenseRegistration.Registrar {
    private final Collection<License> licenses = new ArrayList<>();

    public void registerLicense(String permanentId, String customerName, String salt, String repoName,
        LicenseRegistration.LicenseType type, byte[] signature, long expirationDate) {
      if (Strings.isNullOrEmpty(repoName))
        return;
      LOG.debug("Found {} license - Permanent ID '{}' - Customer '{}' - Repository '{}' - Expiration date {}",
          type.toString(), permanentId, customerName, repoName,
          DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(expirationDate)));
      // Only one license per repository / permID
      License existingLic = getLicense(repoName, permanentId);
      License newLic = new License(permanentId, customerName, salt, repoName, type, signature, expirationDate);
      if (existingLic == null) {
        licenses.add(newLic);
      } else if (existingLic.getExpirationDate() < newLic.getExpirationDate()) {
        licenses.remove(existingLic);
        licenses.add(newLic);
      }
    }

    private Iterable<License> getLicenses() {
      return licenses;
    }

    private License getLicense(String repoName, String permId) {
      if ((permId == null) || (repoName == null))
        return null;
      for (License lic : licenses) {
        if (repoName.equals(lic.getRepositoryName()) && permId.equals(lic.getPermanentId()))
          return lic;
      }
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
}
