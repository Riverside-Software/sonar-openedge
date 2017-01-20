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
package org.sonar.plugins.openedge.sensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.antlr.TokenStream;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.api.com.google.common.io.ByteStreams;
import org.sonar.plugins.openedge.api.com.google.common.io.Files;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JPNode;
import org.sonar.plugins.openedge.api.org.prorefactor.core.JsonNodeLister;
import org.sonar.plugins.openedge.api.org.prorefactor.core.NodeTypes;
import org.sonar.plugins.openedge.api.org.prorefactor.core.ProparseRuntimeException;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorException;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.ParseUnit;
import org.sonar.plugins.openedge.api.org.prorefactor.treeparser.SymbolScope;
import org.sonar.plugins.openedge.foundation.CPDCallback;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;

public class OpenEdgeProparseSensor implements Sensor {
  private static final Logger LOG = Loggers.get(OpenEdgeProparseSensor.class);

  private final FileSystem fileSystem;
  private final OpenEdgeSettings settings;
  private final OpenEdgeComponents components;

  public OpenEdgeProparseSensor(FileSystem fileSystem, OpenEdgeSettings settings, OpenEdgeComponents components) {
    this.fileSystem = fileSystem;
    this.settings = settings;
    this.components = components;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name(getClass().getSimpleName());
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY);
  }

  @Override
  public void execute(SensorContext context) {
    if (settings.skipProparseSensor())
      return;
    int numFiles = 0;
    int numFailures = 0;
    List<String> debugFiles = new ArrayList<>();
    Map<String, Long> ruleTime = new HashMap<>();
    long parseTime = 0L;
    long maxParseTime = 0L;
    components.initializeChecks(context);

    for (Map.Entry<ActiveRule, OpenEdgeProparseCheck> entry : components.getProparseRules().entrySet()) {
      ruleTime.put(entry.getKey().ruleKey().toString(), 0L);
    }

    for (InputFile file : fileSystem.inputFiles(fileSystem.predicates().hasLanguage(Constants.LANGUAGE_KEY))) {
      LOG.debug("Parsing {}", new Object[] {file.relativePath()});
      boolean isIncludeFile = "i".equalsIgnoreCase(Files.getFileExtension(file.relativePath()));
      numFiles++;
      try {
        long startTime = System.currentTimeMillis();
        ParseUnit unit = new ParseUnit(file.file(), settings.getProparseSession());
        ParseUnit lexUnit = new ParseUnit(file.file(), settings.getProparseSession());
        TokenStream stream = lexUnit.lex();
        if (!isIncludeFile) {
          unit.treeParser01();
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        parseTime += elapsedTime;
        if (maxParseTime < elapsedTime) {
          maxParseTime = elapsedTime;
        }
        LOG.debug("{} milliseconds to generate ParseUnit", System.currentTimeMillis() - elapsedTime);

        // Saving LOC and COMMENTS metrics
        context.newMeasure().on(file).forMetric((Metric) CoreMetrics.NCLOC).withValue(
            lexUnit.getMetrics().getLoc()).save();
        context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMMENT_LINES).withValue(
            lexUnit.getMetrics().getComments()).save();

        if (isIncludeFile) {
          // Rules and complexity are not applied on include files
          continue;
        }
        computeCpd(context, file, unit);
        computeCommonMetrics(context, file, unit);
        computeComplexity(context, file, unit);

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

        for (Map.Entry<ActiveRule, OpenEdgeProparseCheck> entry : components.getProparseRules().entrySet()) {
          LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}",
              new Object[] {
                  entry.getKey().internalKey(), entry.getKey().ruleKey().repository(),
                  entry.getKey().ruleKey().rule()});
          startTime = System.currentTimeMillis();
          entry.getValue().execute(file, unit);
          ruleTime.put(entry.getKey().ruleKey().toString(),
              ruleTime.get(entry.getKey().ruleKey().toString()) + System.currentTimeMillis() - startTime);
        }
        
      } catch (RefactorException | ProparseRuntimeException caught ) {
        LOG.error("Error during code parsing for " + file.relativePath(), caught);
        numFailures++;
        NewIssue issue = context.newIssue();
        issue.forRule(
            RuleKey.of(OpenEdgeRulesDefinition.REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY)).at(
                issue.newLocation().on(file).message(caught.getMessage())).save();
      } catch (RuntimeException caught) {
        LOG.error("Runtime exception was caught '{}' - Please report this issue : ", caught.getMessage());
        for (StackTraceElement element : caught.getStackTrace()) {
          LOG.error("  {}", element.toString());
        }
      }
    }
    new File("listingparser.txt").delete();

    if (settings.useAnalytics()) {
      try {
        final URL url = new URL(
            String.format("http://analytics.rssw.eu/oeps.%s.%d.%d.%d.%d.stats", components.getLicence("rssw-oe-main") == null
                ? "none" : components.getLicence("rssw-oe-main").getPermanentId(), numFiles, numFailures, parseTime, maxParseTime));
        URLConnection connx = url.openConnection();
        connx.setConnectTimeout(2000);
        connx.getContentEncoding();
      } catch (IOException uncaught) {
        LOG.info("Unable to send analytics", uncaught);
      }
    }

    LOG.info("{} files proparse'd, {} failure(s)", numFiles, numFailures);
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

  private void computeCpd(SensorContext context, InputFile file, ParseUnit unit) {
    CPDCallback cpdCallback = new CPDCallback(context, file, settings, unit);
    unit.getTopNode().walk(cpdCallback);
    cpdCallback.getResult().save();
  }

  private void computeCommonMetrics(SensorContext context, InputFile file, ParseUnit unit) {
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.STATEMENTS).withValue(unit.getTopNode().queryStateHead().size()).save();
    int numProcs = 0;
    int numFuncs = 0;
    int numMethds = 0;
    for (SymbolScope child : unit.getRootScope().getChildScopesDeep()) {
      int scopeType = child.getRootBlock().getNode().getType();
      switch (scopeType) {
        case NodeTypes.PROCEDURE:
          boolean externalProc = false;
          for (JPNode node : child.getRootBlock().getNode().getDirectChildren()) {
            if ((node.getType() == NodeTypes.IN_KW) || (node.getType() == NodeTypes.SUPER) || (node.getType() == NodeTypes.EXTERNAL)) {
              externalProc = true;
            }
          }
          if (!externalProc) {
            numProcs++;
          }
          break;
        case NodeTypes.FUNCTION:
          boolean externalFunc = false;
          for (JPNode node : child.getRootBlock().getNode().getDirectChildren()) {
            if ((node.getType() == NodeTypes.IN_KW) || (node.getType() == NodeTypes.FORWARDS)) {
              externalFunc = true;
            }
          }
          if (!externalFunc) {
            numFuncs++;
          }
          break;
        case NodeTypes.METHOD:
          numMethds++;
          break;
        default:
            
      }
    }
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.INTERNAL_PROCEDURES).withValue(numProcs).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.INTERNAL_FUNCTIONS).withValue(numFuncs).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.METHODS).withValue(numMethds).save();
  }

  private void computeComplexity(SensorContext context, InputFile file, ParseUnit unit) {
    // Interfaces don't contribute to complexity
    if (unit.getRootScope().isInterface())
      return;
    int complexity = 0;
    int complexityWithInc = 0;
    // Procedure has a main block, so starting at 1
    if (!unit.getRootScope().isClass()) {
      complexity++;
      complexityWithInc++;
    }

    for (JPNode node : unit.getTopNode().queryMainFile(NodeTypes.IF, NodeTypes.REPEAT, NodeTypes.FOR, NodeTypes.WHEN,
        NodeTypes.AND, NodeTypes.OR, NodeTypes.RETURN, NodeTypes.PROCEDURE, NodeTypes.FUNCTION, NodeTypes.METHOD,
        NodeTypes.ENUM)) {
      complexity++;
    }
    for (JPNode node : unit.getTopNode().query(NodeTypes.IF, NodeTypes.REPEAT, NodeTypes.FOR, NodeTypes.WHEN,
        NodeTypes.AND, NodeTypes.OR, NodeTypes.RETURN, NodeTypes.PROCEDURE, NodeTypes.FUNCTION, NodeTypes.METHOD,
        NodeTypes.ENUM)) {
      complexityWithInc++;
    }
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMPLEXITY).withValue(complexity).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.COMPLEXITY).withValue(complexityWithInc).save();
  }

}
