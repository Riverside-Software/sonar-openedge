/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.CoreProperties;
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
import org.sonar.plugins.openedge.api.CheckRegistrar;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.InvalidLicenceException;
import org.sonar.plugins.openedge.api.LicenceRegistrar;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
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

  private final List<Class<? extends OpenEdgeCheck<?>>> checkClasses = new ArrayList<>();
  private final Map<ActiveRule, OpenEdgeProparseCheck> ppChecksMap = new HashMap<>();
  private final Map<ActiveRule, OpenEdgeDumpFileCheck> dfChecksMap = new HashMap<>();

  private boolean initialized = false;
  private final List<OpenEdgeProparseCheck> ppChecks = new ArrayList<>();
  private final List<OpenEdgeDumpFileCheck> dfChecks = new ArrayList<>();

  private final Collection<Licence> licences = new ArrayList<>();

  public OpenEdgeComponents() {
    this(null, null);
  }

  public OpenEdgeComponents(CheckRegistrar[] checkRegistrars) {
    this(checkRegistrars, null);
  }

  public OpenEdgeComponents(LicenceRegistrar[] licRegistrars) {
    this(null, licRegistrars);
  }

  public OpenEdgeComponents(CheckRegistrar[] checkRegistrars, LicenceRegistrar[] licRegistrars) {
    if (checkRegistrars != null) {
      registerChecks(checkRegistrars);
    }
    if (licRegistrars != null) {
      registerLicences(licRegistrars);
    }
  }

  private void registerChecks(CheckRegistrar[] checkRegistrars) {
    for (CheckRegistrar reg : checkRegistrars) {
      CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
      reg.register(registrarContext);
      for (Class<? extends OpenEdgeProparseCheck> analyzer : registrarContext.getProparseCheckClasses()) {
        LOG.debug("{} Proparse check registered", analyzer.getName());
        checkClasses.add(analyzer);
      }
      for (Class<? extends OpenEdgeDumpFileCheck> analyzer : registrarContext.getDbCheckClasses()) {
        LOG.debug("{} DF check registered", analyzer.getName());
        checkClasses.add(analyzer);
      }
    }
  }

  private void registerLicences(LicenceRegistrar[] licRegistrars) {
    for (LicenceRegistrar reg : licRegistrars) {
      LicenceRegistrar.Licence lic = new LicenceRegistrar.Licence();
      reg.register(lic);
      if (lic.getRepositoryName().isEmpty()) {
        continue;
      }
      LOG.debug("Found {} licence - Permanent ID '{}' - Customer '{}' - Repository '{}' - Expiration date {}",
          lic.getType().toString(), lic.getPermanentId(), lic.getCustomerName(), lic.getRepositoryName(),
          DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(lic.getExpirationDate())));

      // Only one licence per repository / permID
      Licence existingLic = getLicence(lic.getRepositoryName(), lic.getPermanentId());
      if (existingLic == null) {
        licences.add(lic);
      } else if (existingLic.getExpirationDate() < lic.getExpirationDate()) {
        licences.remove(existingLic);
        licences.add(lic);
      }
    }
    for (Licence entry : licences) {
      LOG.info(
          "Licence summary - Repository '{}' associated with {} licence permanent ID '{}' - Customer '{}' - Expiration date {}",
          entry.getRepositoryName(),
          entry.getType().toString(), entry.getPermanentId(),
          entry.getCustomerName(), DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(
              new Date(entry.getExpirationDate())));
    }
  }

  public void initializeChecks(SensorContext context) {
    if (initialized)
      return;

    String permId = (context.runtime().getProduct() == SonarProduct.SONARLINT ? "sonarlint-" : "") + Strings.nullToEmpty(context.settings().getString(CoreProperties.PERMANENT_SERVER_ID));

    // Proparse and XREF rules
    for (ActiveRule rule : context.activeRules().findByLanguage(Constants.LANGUAGE_KEY)) {
      OpenEdgeCheck lint = initializeCheck(context, rule, permId);
      if ((lint != null) && (lint.getCheckType() == CheckType.PROPARSE)) {
        ppChecks.add((OpenEdgeProparseCheck) lint);
        ppChecksMap.put(rule, (OpenEdgeProparseCheck) lint);
      }
    }
    // DB rules
    for (ActiveRule rule : context.activeRules().findByLanguage(Constants.DB_LANGUAGE_KEY)) {
      OpenEdgeCheck lint = initializeCheck(context, rule, permId);
      if ((lint != null) && (lint.getCheckType() == CheckType.PROPARSE)) {
        dfChecks.add((OpenEdgeDumpFileCheck) lint);
        dfChecksMap.put(rule, (OpenEdgeDumpFileCheck) lint);
      }
    }

    initialized = true;
  }

  private OpenEdgeCheck initializeCheck(SensorContext context, ActiveRule rule, String permId) {
    RuleKey ruleKey = rule.ruleKey();
    // AFAIK, no way to be sure if a rule is based on a template or not
    String clsName = rule.templateRuleKey() == null ? ruleKey.rule() : rule.templateRuleKey();
    OpenEdgeCheck<?> lint = getAnalyzer(clsName, ruleKey, context, getLicence(ruleKey.repository(), permId));
    if (lint != null) {
      configureFields(rule, lint);
      lint.initialize();
    }

    return lint;
  }

  public Collection<OpenEdgeProparseCheck> getProparseChecks() {
    return Collections.unmodifiableList(ppChecks);
  }

  public Map<ActiveRule, OpenEdgeProparseCheck> getProparseRules() {
    return Collections.unmodifiableMap(ppChecksMap);
  }

  public Collection<OpenEdgeDumpFileCheck> getDumpFileChecks() {
    return Collections.unmodifiableList(dfChecks);
  }

  public Map<ActiveRule, OpenEdgeDumpFileCheck> getDumpFileRules() {
    return Collections.unmodifiableMap(dfChecksMap);
  }

  public Licence getLicence(String repoName, String permId) {
    if (permId == null)
      return null;
    for (Licence lic : licences) {
      if (repoName.equals(lic.getRepositoryName()) && permId.equals(lic.getPermanentId()))
        return lic;
    }
    return null;
  }

  public Collection<Licence> getLicences() {
    return licences;
  }

  private OpenEdgeCheck<?> getAnalyzer(String internalKey, RuleKey ruleKey, SensorContext context, Licence licence) {
    try {
      for (Class<? extends OpenEdgeCheck<?>> clz : checkClasses) {
        if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
          return clz.getConstructor(RuleKey.class, SensorContext.class, Licence.class).newInstance(
              ruleKey, context, licence);
        }
      }
      return null;
    } catch (ReflectiveOperationException caught) {
      if (caught.getCause() instanceof InvalidLicenceException) {
        LOG.error("Unable to instantiate rule {} - {}", internalKey, caught.getCause().getMessage());
      } else {
        LOG.error("Unable to instantiate rule " + internalKey, caught);
      }
      return null;
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
        field.set(check, new Integer(Integer.parseInt(value)));
      } else if (Long.class == field.getType()) {
        field.set(check, new Long(Long.parseLong(value)));
      } else if (Double.class == field.getType()) {
        field.set(check, new Double(Double.parseDouble(value)));
      } else if (Boolean.class == field.getType()) {
        field.set(check, Boolean.valueOf(Boolean.parseBoolean(value)));
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

}
