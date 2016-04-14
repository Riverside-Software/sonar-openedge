/*
 * OpenEdge plugin for SonarQube
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.platform.Server;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.MessageException;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.com.google.common.io.ByteStreams;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.core.ProparseRuntimeException;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorException;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;
import org.sonar.plugins.openedge.api.org.prorefactor.util.JsonNodeLister;
import org.sonar.plugins.openedge.foundation.OpenEdge;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

import com.google.common.io.Files;

public class OpenEdgeProparseSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeProparseSensor.class);

  private final FileSystem fileSystem;
  private final ActiveRules activeRules;
  private final OpenEdgeSettings settings;
  private final OpenEdgeComponents components;
  private final Server server;

  public OpenEdgeProparseSensor(FileSystem fileSystem, ActiveRules activesRules, OpenEdgeSettings settings,
      OpenEdgeComponents components, Server server) {
    this.fileSystem = fileSystem;
    this.activeRules = activesRules;
    this.settings = settings;
    this.components = components;
    this.server = server;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return fileSystem.languages().contains(OpenEdge.KEY) && !settings.skipProparseSensor();
  }

  @Override
  public void analyse(Project project, SensorContext context) {
    List<String> debugFiles = new ArrayList<>();
    Map<String, Long> ruleTime = new HashMap<>();
    long parseTime = 0L;

    for (ActiveRule rule : activeRules.findByLanguage(OpenEdge.KEY)) {
      String clsName = (rule.templateRuleKey() == null ? rule.ruleKey().rule() : rule.templateRuleKey());
      // If class can be instantiated, then we add an entry 
      if (components.getProparseAnalyzer(clsName) != null)
        ruleTime.put(rule.ruleKey().rule(), 0L);
    }

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(OpenEdge.KEY))) {
      LOG.debug("Parsing {}", new Object[] {file.relativePath()});
      boolean isIncludeFile = "i".equalsIgnoreCase(Files.getFileExtension(file.relativePath()));
      try {
        long time = System.currentTimeMillis();

        ParseUnit unit = new ParseUnit(file.file(), settings.getProparseSession());
        long startTime = System.currentTimeMillis();
        if (isIncludeFile) {
          unit.lex();
        } else {
          unit.treeParser01();
        }
        parseTime += (System.currentTimeMillis() - startTime);
        LOG.debug("{} milliseconds to generate ParseUnit", System.currentTimeMillis() - time);

        // Saving LOC and COMMENTS metrics
        context.saveMeasure(file, CoreMetrics.NCLOC, (double) unit.getMetrics().getLoc());
        context.saveMeasure(file, CoreMetrics.COMMENT_LINES, (double) unit.getMetrics().getComments());

        if (isIncludeFile) {
          // Rules are not applied on include files
          continue;
        }

        if (settings.useProparseDebug()) {
          String fileName = ".proparse/" + file.relativePath() + ".json";
          File dbgFile = new File(fileName);
          dbgFile.getParentFile().mkdirs();
          try (PrintWriter writer = new PrintWriter(dbgFile)) {
            JsonNodeLister nodeLister = new JsonNodeLister(unit.getTopNode(), writer,
                new Integer[] {
                    NodeTypes.LEFTPAREN, NodeTypes.RIGHTPAREN, NodeTypes.COMMA, NodeTypes.PERIOD, NodeTypes.LEXCOLON,
                    NodeTypes.OBJCOLON, NodeTypes.THEN, NodeTypes.END});
            nodeLister.print();
            debugFiles.add(file.relativePath() + ".json");
          } catch (IOException caught) {
            LOG.error("Unable to write proparse debug file", caught);
          }
        }

        for (ActiveRule rule : activeRules.findByLanguage(OpenEdge.KEY)) {
          RuleKey ruleKey = rule.ruleKey();
          // AFAIK, no way to be sure if a rule is based on a template or not
          String clsName = (rule.templateRuleKey() == null ? ruleKey.rule() : rule.templateRuleKey());
          AbstractLintRule lint = components.getProparseAnalyzer(clsName);
          if (lint != null) {
            LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}",
                new Object[] {rule.internalKey(), rule.ruleKey().repository(), rule.ruleKey().rule()});
            configureFields(rule, lint);
            startTime = System.currentTimeMillis();
            lint.execute(unit, context, file, ruleKey, components.getLicence(rule.ruleKey().repository()), server.getPermanentServerId());
            ruleTime.put(ruleKey.rule(), ruleTime.get(ruleKey.rule()) + System.currentTimeMillis() - startTime);
          }
        }
      } catch (RefactorException | ProparseRuntimeException caught ) {
        LOG.error("Error during code parsing for " + file.relativePath(), caught);
        NewIssue issue = context.newIssue();
        issue.forRule(
            RuleKey.of(OpenEdgeRulesDefinition.REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY)).at(
                issue.newLocation().on(file).message(caught.getMessage())).save();
      }
    }
    new File("listingparser.txt").delete();

    LOG.info("AST Generation | time={} ms", parseTime);
    for (Entry<String, Long> entry : ruleTime.entrySet()) {
      LOG.info("Rule {} | time={} ms", new Object[] {entry.getKey(), entry.getValue()});
    }

    if (settings.useProparseDebug()) {
      try (InputStream from = this.getClass().getResourceAsStream("/debug-index.html");
          OutputStream to = new FileOutputStream(new File(".proparse/index.html"))) {
        ByteStreams.copy(from, to);
      } catch (IOException caught) {
        LOG.error("Error while writing index.html", caught);
      }
      try (PrintWriter writer = new PrintWriter(new File(".proparse/index.json"))) {
        boolean first = true;
        writer.println("var data= { \"files\": [");
        for (String str : debugFiles) {
          if (!first) {
            writer.write(',');
          } else {
            first = false;
          }
          writer.println("{ \"file\": \"" + str + "\" }");
        }
        writer.println("]}");
      } catch (IOException uncaught) {
        LOG.error("Error while writing debug index", uncaught);
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
      if (propertyAnnotation != null) {
        if (StringUtils.equals(key, field.getName()) || StringUtils.equals(key, propertyAnnotation.key())) {
          return field;
        }
      }
    }
    return null;
  }

}
