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
package org.sonar.plugins.openedge.sensor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.platform.Server;
import org.sonar.api.rule.RuleKey;
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
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(OpenEdgeDB.KEY).name(getClass().getSimpleName());
  }

  @Override
  public void execute(SensorContext context) {
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
            OpenEdgeComponents.configureFields(rule, lint);
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

}
