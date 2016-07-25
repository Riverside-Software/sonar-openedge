/*
 * OpenEdge DB plugin for SonarQube
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.platform.Server;
import org.sonar.api.utils.MessageException;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.CheckRegistrar;
import org.sonar.plugins.openedge.api.LicenceRegistrar;
import org.sonar.plugins.openedge.api.LicenceRegistrar.Licence;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.checks.IXrefAnalyzer;
import org.sonar.plugins.openedge.api.checks.IDumpFileAnalyzer;;

@BatchSide
public class OpenEdgeComponents {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeComponents.class);

  private final List<Class<? extends IXrefAnalyzer>> checks = new ArrayList<>();
  private final List<Class<? extends AbstractLintRule>> ppchecks = new ArrayList<>();
  private final List<Class<? extends IDumpFileAnalyzer>> dbChecks = new ArrayList<>();

  private final Map<String, Licence> licences = new HashMap<>();

  public OpenEdgeComponents(Server server, CheckRegistrar[] checkRegistrars, LicenceRegistrar[] licRegistrars) {
    if (checkRegistrars != null) {
      registerChecks(checkRegistrars);
    }
    if (licRegistrars != null) {
      String permanentId = server.getPermanentServerId() == null ? "" : server.getPermanentServerId();
      registerLicences(licRegistrars, permanentId);
    }
  }

  private void registerChecks(CheckRegistrar[] checkRegistrars) {
    for (CheckRegistrar reg : checkRegistrars) {
      CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
      reg.register(registrarContext);
      for (Class<? extends IXrefAnalyzer> analyzer : registrarContext.getXrefCheckClasses()) {
        LOG.debug("{} XREF analyzer registered", analyzer.getName());
        checks.add(analyzer);
      }
      for (Class<? extends AbstractLintRule> analyzer : registrarContext.getProparseCheckClasses()) {
        LOG.debug("{} Proparse analyzer registered", analyzer.getName());
        ppchecks.add(analyzer);
      }
      for (Class<? extends IDumpFileAnalyzer> analyzer : registrarContext.getDbCheckClasses()) {
        LOG.debug("{} DF analyzer registered", analyzer.getName());
        dbChecks.add(analyzer);
      }
    }
  }

  private void registerLicences(LicenceRegistrar[] licRegistrars, String permanentId) {
    for (LicenceRegistrar reg : licRegistrars) {
      LicenceRegistrar.Licence lic = new LicenceRegistrar.Licence();
      reg.register(lic);
      LOG.info("Found {} licence - Permanent ID '{}' - Customer '{}' - Repository '{}' - Expiration date {}",
          lic.getType().toString(), lic.getPermanentId(), lic.getCustomerName(), lic.getRepositoryName(),
          DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date(lic.getExpirationDate())));
      if (!lic.getPermanentId().isEmpty() && !permanentId.equals(lic.getPermanentId())) {
        LOG.info("Skipped licence as it doesn't match permanent ID '{}'", permanentId);
        continue;
      }
      // Licence with highest expiration date wins
      Licence existingLic = licences.get(lic.getRepositoryName());
      if ((existingLic == null) || (existingLic.getExpirationDate() < lic.getExpirationDate())) {
        licences.put(lic.getRepositoryName(), lic);
        LOG.info("Installed !");
      } else {
        LOG.info("Conflict, skipped licence");
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

  public Licence getLicence(String repoName) {
    return licences.get(repoName);
  }

  public List<Class<? extends IXrefAnalyzer>> getChecks() {
    return checks;
  }

  public IDumpFileAnalyzer getDFAnalyzer(String internalKey) {
    try {
      for (Class<? extends IDumpFileAnalyzer> clz : dbChecks) {
        if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
          return clz.newInstance();
        }
      }
      return null;
    } catch (ReflectiveOperationException caught) {
      LOG.error("Unable to instantiate DF rule " + internalKey);
      return null;
    }
  }

  public IXrefAnalyzer getXrefAnalyzer(String internalKey) {
    try {
      for (Class<? extends IXrefAnalyzer> clz : checks) {
        if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
          return clz.newInstance();
        }
      }
      return null;
    } catch (ReflectiveOperationException caught) {
      LOG.error("Unable to instantiate XREF rule " + internalKey);
      return null;
    }
  }

  public AbstractLintRule getProparseAnalyzer(String internalKey) {
    try {
      for (Class<? extends AbstractLintRule> clz : ppchecks) {
        if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
          return clz.newInstance();
        }
      }
      return null;
    } catch (ReflectiveOperationException caught) {
      LOG.error("Unable to instantiate Proparse rule " + internalKey);
      return null;
    }
  }

  public static void configureFields(ActiveRule activeRule, Object check) {
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
      } else if ("int".equals(field.getType().getSimpleName())) {
        field.setInt(check, Integer.parseInt(value));
      } else if ("short".equals(field.getType().getSimpleName())) {
        field.setShort(check, Short.parseShort(value));
      } else if ("long".equals(field.getType().getSimpleName())) {
        field.setLong(check, Long.parseLong(value));
      } else if ("double".equals(field.getType().getSimpleName())) {
        field.setDouble(check, Double.parseDouble(value));
      } else if ("boolean".equals(field.getType().getSimpleName())) {
        field.setBoolean(check, Boolean.parseBoolean(value));
      } else if ("byte".equals(field.getType().getSimpleName())) {
        field.setByte(check, Byte.parseByte(value));
      } else if (field.getType().equals(Integer.class)) {
        field.set(check, new Integer(Integer.parseInt(value)));
      } else if (field.getType().equals(Long.class)) {
        field.set(check, new Long(Long.parseLong(value)));
      } else if (field.getType().equals(Double.class)) {
        field.set(check, new Double(Double.parseDouble(value)));
      } else if (field.getType().equals(Boolean.class)) {
        field.set(check, Boolean.valueOf(Boolean.parseBoolean(value)));
      } else {
        throw MessageException.of("The type of the field " + field + " is not supported: " + field.getType());
      }
    } catch (IllegalAccessException e) {
      throw MessageException.of(
          "Can not set the value of the field " + field + " in the class: " + check.getClass().getName());
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
