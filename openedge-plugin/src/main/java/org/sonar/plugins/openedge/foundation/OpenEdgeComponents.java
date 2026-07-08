/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Status;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonar.api.utils.MessageException;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.InvalidLicenseException;
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
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeComponents.class);

  private final Map<ActiveRule, OpenEdgeProparseCheck> ppChecksMap = new HashMap<>();
  private final Map<ActiveRule, OpenEdgeDumpFileCheck> dfChecksMap = new HashMap<>();

  private final Configuration config;
  private final CheckRegistrar checkRegistrar;
  private final LicenseRegistrar licenseRegistrar;

  private boolean initialized = false;
  private String analytics = "";
  private int ncLoc = 0;
  private int ncLocL2 = 0;
  private Map<String, List<String>> includeDependencies = new HashMap<>();

  public OpenEdgeComponents(Configuration config, CheckRegistrar checkRegistrar, LicenseRegistrar licenseRegistrar) {
    this.config = config;
    this.checkRegistrar = checkRegistrar;
    this.licenseRegistrar = licenseRegistrar;
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

  public void setNcLoc(int ncLoc) {
    this.ncLoc = ncLoc;
  }

  public void setNcLocL2(int ncLocL2) {
    this.ncLocL2 = ncLocL2;
  }

  public int getNcLoc() {
    return ncLoc;
  }

  public int getNcLocL2() {
    return ncLocL2;
  }

  public void addIncludeDependency(String uri, List<String> dependencies) {
    includeDependencies.put(uri, dependencies);
  }

  public List<String> getIncludeDependencies(String uri) {
    return includeDependencies.getOrDefault(uri, Arrays.asList());
  }

  /**
   * Return true if main file or one of its include files has changed
   */
  public boolean isChanged(SensorContext context, InputFile file) {
    if ((file.status() == Status.ADDED) || (file.status() == Status.CHANGED))
      return true;
    for (String str : getIncludeDependencies(file.uri().toString())) {
      InputFile target = context.fileSystem().inputFile(context.fileSystem().predicates().hasRelativePath(str));
      if ((target != null) && ((target.status() == Status.ADDED) || (target.status() == Status.CHANGED)))
        return true;
    }

    return false;
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
      var clz = checkRegistrar.getCheck(clsName);
      if (clz == null)
        return null;
      var check = clz.getConstructor().newInstance();
      check.setContext(ruleKey, context, licenseRegistrar.getLicense(product, permId, ruleKey.repository()));
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
      var method = getSetter(check, field);
      if (method == null) {
        throw MessageException.of(
            "The setter for " + param + " does not exist in the class " + check.getClass().getName());
      }
      if (!Strings.nullToEmpty(activeRule.param(param)).trim().isEmpty()) {
        configureField(check, field, method, activeRule.param(param));
      }
    }
  }

  private static void configureField(Object check, Field field, Method method, String value) {
    Object val = null;
    try {
      if (field.getType().equals(String.class)) {
        val = value;
      } else if ((int.class == field.getType()) || (Integer.class == field.getType())) {
        val = Integer.parseInt(value);
      } else if ((short.class == field.getType()) || (Short.class == field.getType())) {
        val = Short.parseShort(value);
      } else if ((long.class == field.getType()) || (Long.class == field.getType())) {
        val = Long.parseLong(value);
      } else if ((double.class == field.getType()) || (Double.class == field.getType())) {
        val = Double.parseDouble(value);
      } else if ((boolean.class == field.getType()) || (Boolean.class == field.getType())) {
        val = Boolean.parseBoolean(value);
      } else if ((byte.class == field.getType()) || (Byte.class == field.getType())) {
        val = Byte.parseByte(value);
      } else {
        throw MessageException.of("The type of the field " + field + " is not supported: " + field.getType());
      }
      method.invoke(check, val);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
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

  private static Method getSetter(Object check, Field field) {
    var setterName = "set" + field.getName().substring(0, 1).toUpperCase()
        + (field.getName().length() > 1 ? field.getName().substring(1) : "");
    try {
      return check.getClass().getMethod(setterName, field.getType());
    } catch (NoSuchMethodException caught) {
      return null;
    }
  }

  public String getServerId() {
    return config == null ? "" : config.get(CoreProperties.SERVER_ID).orElse("");
  }

}
