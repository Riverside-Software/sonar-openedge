/*
 * OpenEdge DB plugin for SonarQube
 * Copyright (C) 2013-2014 Riverside Software
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
package org.sonar.plugins.oedb.sensor;

import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.MessageException;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.oedb.api.checks.IDumpFileAnalyzer;
import org.sonar.plugins.oedb.api.eu.rssw.antlr.database.DumpFileUtils;
import org.sonar.plugins.oedb.api.org.antlr.v4.runtime.tree.ParseTree;
import org.sonar.plugins.oedb.foundation.OpenEdgeDB;
import org.sonar.plugins.oedb.foundation.OpenEdgeDBComponents;

public class OpenEdgeDBRulesSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeDBRulesSensor.class);

  private final FileSystem fileSystem;
  private final ActiveRules activeRules;
  private final OpenEdgeDBComponents components;
  
  public OpenEdgeDBRulesSensor(FileSystem fileSystem, ActiveRules activesRules, OpenEdgeDBComponents components) {
    this.fileSystem = fileSystem;
    this.activeRules = activesRules;
    this.components = components;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdgeDB.KEY);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdgeDB.KEY))) {
      try {
        LOG.debug("Generating ParseTree for dump file {}", file.relativePath());
        ParseTree tree = DumpFileUtils.getDumpFileParseTree(file.file());

        for (ActiveRule rule : activeRules.findByLanguage(OpenEdgeDB.KEY)) {
          RuleKey ruleKey = rule.ruleKey();
          // AFAIK, no way to be sure if a rule is based on a template or not
          String clsName = (rule.templateRuleKey() == null ? ruleKey.rule() : rule.templateRuleKey());
          LOG.debug("ActiveRule - Repository {} - Rule {} - Class name {}",
              new Object[] {ruleKey.repository(), ruleKey.rule(), clsName});
          IDumpFileAnalyzer a = components.getAnalyzer(clsName);
          if (a == null) {
            continue;
          }
          configureFields(rule, a);
          a.execute(tree, context, file,  ruleKey);
          LOG.debug("Rule executed");
        }
      } catch (IOException caught) {
        LOG.error("Unable to analyze {}", file.relativePath(), caught);
      } catch (ReflectiveOperationException caught) {
        LOG.error("Unable to analyze {}", file.relativePath(), caught);
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  private void configureFields(ActiveRule activeRule, Object check) {
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

  private void configureField(Object check, Field field, String value) {
    try {
      field.setAccessible(true);
      switch(field.getType().getCanonicalName()) {
        case "java.lang.String":
          field.set(check, value);
          break;
        case "int":
        case "java.lang.Integer":
          field.setInt(check, Integer.parseInt(value));
          break;
        case "short":
        case "java.lang.Short":
          field.setShort(check, Short.parseShort(value));
          break;
        case "long":
        case "java.lang.Long":
          field.setLong(check, Long.parseLong(value));
          break;
        case "double":
        case "java.lang.Double":
          field.setDouble(check, Double.parseDouble(value));
          break;
        case "boolean":
        case "java.lang.Boolean":
          field.setBoolean(check, Boolean.parseBoolean(value));
          break;
        case "byte":
        case "java.lang.Byte":
          field.setByte(check, Byte.parseByte(value));
          break;
        default:
          throw MessageException.of("The type of the field " + field + " is not supported: " + field.getType());
      }
    } catch (IllegalAccessException e) {
      throw MessageException.of(
          "Can not set the value of the field " + field + " in the class: " + check.getClass().getName());
    }
  }

  private Field getField(Object check, String key) {
    Field[] fields = check.getClass().getDeclaredFields();
    for (Field field : fields) {
      RuleProperty propertyAnnotation = field.getAnnotation(RuleProperty.class);
      if ((propertyAnnotation != null)
          && (StringUtils.equals(key, field.getName()) || StringUtils.equals(key, propertyAnnotation.key()))) {
        return field;

      }
    }
    return null;
  }

}
