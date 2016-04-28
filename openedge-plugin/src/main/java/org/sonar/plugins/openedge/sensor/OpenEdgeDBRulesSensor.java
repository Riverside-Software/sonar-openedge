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
package org.sonar.plugins.openedge.sensor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.platform.Server;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.MessageException;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.IDumpFileAnalyzer;
import org.sonar.plugins.openedge.api.eu.rssw.antlr.database.DumpFileUtils;
import org.sonar.plugins.openedge.api.org.antlr.v4.runtime.tree.ParseTree;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;

public class OpenEdgeDBRulesSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeDBRulesSensor.class);

  private final FileSystem fileSystem;
  private final ActiveRules activeRules;
  private final OpenEdgeComponents components;
  private final Server server;

  public OpenEdgeDBRulesSensor(FileSystem fileSystem, ActiveRules activesRules, OpenEdgeComponents components, Server server) {
    this.fileSystem = fileSystem;
    this.activeRules = activesRules;
    this.components = components;
    this.server = server;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdgeDB.KEY);
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    Map<String, Long> ruleTime = new HashMap<>();
    long parseTime = 0L;

    for (ActiveRule rule : activeRules.findByLanguage(OpenEdge.KEY)) {
      String clsName = rule.templateRuleKey() == null ? rule.ruleKey().rule() : rule.templateRuleKey();
      // If class can be instantiated, then we add an entry 
      if (components.getDFAnalyzer(clsName) != null)
        ruleTime.put(rule.ruleKey().rule(), 0L);
    }

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdgeDB.KEY))) {
      try {
        LOG.debug("Generating ParseTree for dump file {}", file.relativePath());
        long time = System.currentTimeMillis();
        ParseTree tree = DumpFileUtils.getDumpFileParseTree(file.file());
        parseTime += (System.currentTimeMillis() - time);
        
        for (ActiveRule rule : activeRules.findByLanguage(OpenEdge.KEY)) {
          RuleKey ruleKey = rule.ruleKey();
          // AFAIK, no way to be sure if a rule is based on a template or not
          String clsName = (rule.templateRuleKey() == null ? ruleKey.rule() : rule.templateRuleKey());
          IDumpFileAnalyzer lint = components.getDFAnalyzer(clsName);
          if (lint != null) {
            LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}",
                new Object[] {rule.internalKey(), rule.ruleKey().repository(), rule.ruleKey().rule()});
            configureFields(rule, lint);
            long startTime = System.currentTimeMillis();
            lint.execute(tree, context, file, ruleKey, components.getLicence(rule.ruleKey().repository()),
                server.getPermanentServerId() == null ? "" : server.getPermanentServerId());
            ruleTime.put(ruleKey.rule(), ruleTime.get(ruleKey.rule()) + System.currentTimeMillis() - startTime);
          }
        }
      } catch (IOException caught) {
        LOG.error("Unable to analyze {}", file.relativePath(), caught);
      }
    }
    
    LOG.info("AST Generation | time={} ms", parseTime);
    for (Entry<String, Long> entry : ruleTime.entrySet()) {
      LOG.info("Rule {} | time={} ms", new Object[] {entry.getKey(), entry.getValue()});
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
