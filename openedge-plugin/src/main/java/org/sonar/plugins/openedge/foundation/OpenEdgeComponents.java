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
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.rule.RuleKey;
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
import org.sonar.plugins.openedge.api.checks.OpenEdgeDumpFileCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeXrefCheck;
import org.sonar.plugins.openedge.api.com.google.common.base.Strings;

@BatchSide
public class OpenEdgeComponents {
  private static final Logger LOG = Loggers.get(OpenEdgeComponents.class);

  // IoC
  private final IIdProvider idProvider;

  private final List<Class<? extends OpenEdgeCheck>> checkClasses = new ArrayList<>();

  private final Map<ActiveRule, OpenEdgeProparseCheck> ppChecksMap = new HashMap<>();
  private final Map<ActiveRule, OpenEdgeXrefCheck> xrefChecksMap = new HashMap<>();
  private final Map<ActiveRule, OpenEdgeDumpFileCheck> dfChecksMap = new HashMap<>();

  private boolean initialized = false;
  private final List<OpenEdgeProparseCheck> ppChecks = new ArrayList<>();
  private final List<OpenEdgeXrefCheck> xrefChecks = new ArrayList<>();
  private final List<OpenEdgeDumpFileCheck> dfChecks = new ArrayList<>();

  private final Map<String, Licence> licences = new HashMap<>();

  public OpenEdgeComponents(IIdProvider provider) {
    this(provider, null, null);
  }

  public OpenEdgeComponents(IIdProvider provider, CheckRegistrar[] checkRegistrars) {
    this(provider, checkRegistrars, null);
  }

  public OpenEdgeComponents(IIdProvider provider, LicenceRegistrar[] licRegistrars) {
    this(provider, null, licRegistrars);
  }

  public OpenEdgeComponents(IIdProvider provider, CheckRegistrar[] checkRegistrars, LicenceRegistrar[] licRegistrars) {
    this.idProvider = provider;

    if (checkRegistrars != null) {
      registerChecks(checkRegistrars);
    }
    if (licRegistrars != null) {
      registerLicences(licRegistrars, Strings.nullToEmpty(idProvider.getPermanentID()));
    }
  }

  private void registerChecks(CheckRegistrar[] checkRegistrars) {
    for (CheckRegistrar reg : checkRegistrars) {
      CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
      reg.register(registrarContext);
      for (Class<? extends OpenEdgeXrefCheck> analyzer : registrarContext.getXrefCheckClasses()) {
        LOG.debug("{} XREF check registered", analyzer.getName());
        checkClasses.add(analyzer);
      }
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

  private void registerLicences(LicenceRegistrar[] licRegistrars, String permanentId) {
    for (LicenceRegistrar reg : licRegistrars) {
      LicenceRegistrar.Licence lic = new LicenceRegistrar.Licence();
      reg.register(lic);
      if (lic.getRepositoryName().isEmpty()) {
        continue;
      }
      LOG.debug("Found {} licence - Permanent ID '{}' - Customer '{}' - Repository '{}' - Expiration date {}",
          lic.getType().toString(), lic.getPermanentId(), lic.getCustomerName(), lic.getRepositoryName(),
          DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(lic.getExpirationDate())));
      if (!lic.getPermanentId().isEmpty() && !permanentId.equals(lic.getPermanentId())) {
        LOG.debug("Skipped licence as it doesn't match permanent ID '{}'", permanentId);
        continue;
      }
      // Licence with highest expiration date wins
      Licence existingLic = licences.get(lic.getRepositoryName());
      if ((existingLic == null) || (existingLic.getExpirationDate() < lic.getExpirationDate())) {
        licences.put(lic.getRepositoryName(), lic);
        LOG.debug("Installed !");
      } else {
        LOG.debug("Conflict, skipped licence");
      }
    }
    for (Entry<String, Licence> entry : licences.entrySet()) {
      LOG.info(
          "Licence summary - Repository '{}' associated with {} licence permanent ID '{}' - Customer '{}' - Expiration date {}",
          entry.getKey(), entry.getValue().getType().toString(), entry.getValue().getPermanentId(),
          entry.getValue().getCustomerName(), DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(
              new Date(entry.getValue().getExpirationDate())));
    }
  }

  public void initializeChecks(SensorContext context) {
    if (initialized)
      return;

    for (ActiveRule rule : context.activeRules().findByLanguage(Constants.LANGUAGE_KEY)) {
      RuleKey ruleKey = rule.ruleKey();
      // AFAIK, no way to be sure if a rule is based on a template or not
      String clsName = rule.templateRuleKey() == null ? ruleKey.rule() : rule.templateRuleKey();
      OpenEdgeCheck lint = getAnalyzer(clsName, ruleKey, context, getLicence(ruleKey.repository()),
          Strings.nullToEmpty(idProvider.getPermanentID()));
      if (lint != null) {
        configureFields(rule, lint);
        lint.initialize();
        switch (lint.getCheckType()) {
          case DUMP_FILE:
            dfChecks.add((OpenEdgeDumpFileCheck) lint);
            dfChecksMap.put(rule, (OpenEdgeDumpFileCheck) lint);
            break;
          case PROPARSE:
            ppChecks.add((OpenEdgeProparseCheck) lint);
            ppChecksMap.put(rule, (OpenEdgeProparseCheck) lint);
            break;
          case XREF:
            xrefChecks.add((OpenEdgeXrefCheck) lint);
            xrefChecksMap.put(rule, (OpenEdgeXrefCheck) lint);
            break;
        }
      }
    }
    initialized = true;
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

  public Collection<OpenEdgeXrefCheck> getXrefChecks() {
    return Collections.unmodifiableList(xrefChecks);
  }

  public Map<ActiveRule, OpenEdgeXrefCheck> getXrefRules() {
    return Collections.unmodifiableMap(xrefChecksMap);
  }

  public Licence getLicence(String repoName) {
    return licences.get(repoName);
  }

  private OpenEdgeCheck getAnalyzer(String internalKey, RuleKey ruleKey, SensorContext context, Licence licence,
      String permanentId) {
    try {
      for (Class<? extends OpenEdgeCheck> clz : checkClasses) {
        if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
          return clz.getConstructor(RuleKey.class, SensorContext.class, Licence.class, String.class).newInstance(
              ruleKey, context, licence, permanentId);
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
