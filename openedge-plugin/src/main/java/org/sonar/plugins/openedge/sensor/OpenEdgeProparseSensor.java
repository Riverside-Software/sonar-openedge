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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.JsonNodeLister;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.proparse.antlr4.XCodedFileException;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.sonar.api.CoreProperties;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
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
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.foundation.CPDCallback;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

public class OpenEdgeProparseSensor implements Sensor {
  private static final Logger LOG = Loggers.get(OpenEdgeProparseSensor.class);

  // IoC
  private final OpenEdgeSettings settings;
  private final OpenEdgeComponents components;

  // Internal use
  private final DocumentBuilderFactory dbFactory;
  private final DocumentBuilder dBuilder;

  // File statistics
  private int numFiles;
  private int numXREF;
  private int numFailures;
  // Timing statistics
  private Map<String, Long> ruleTime = new HashMap<>();
  private long parseTime = 0L;
  private long xmlParseTime = 0L;
  private long maxParseTime = 0L;
  // Proparse debug
  List<String> debugFiles = new ArrayList<>();

  public OpenEdgeProparseSensor(OpenEdgeSettings settings, OpenEdgeComponents components) {
    this.settings = settings;
    this.components = components;

    this.dbFactory = DocumentBuilderFactory.newInstance();
    try {
      this.dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException caught) {
      throw new IllegalStateException(caught);
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY).name(getClass().getSimpleName());
  }

  @Override
  public void execute(SensorContext context) {
    if (settings.skipProparseSensor())
      return;

    components.initializeChecks(context);
    for (Map.Entry<ActiveRule, OpenEdgeProparseCheck> entry : components.getProparseRules().entrySet()) {
      ruleTime.put(entry.getKey().ruleKey().toString(), 0L);
    }
    RefactorSession session = settings.getProparseSession(context.runtime().getProduct() == SonarProduct.SONARLINT);

    FilePredicates predicates = context.fileSystem().predicates();
    for (InputFile file : context.fileSystem().inputFiles(
        predicates.and(predicates.hasLanguage(Constants.LANGUAGE_KEY), predicates.hasType(Type.MAIN)))) {
      LOG.debug("Parsing {}", file.relativePath());
      numFiles++;

      if ("i".equalsIgnoreCase(Files.getFileExtension(file.relativePath()))) {
        parseIncludeFile(context, file, session);
      } else {
        parseMainFile(context, file, session);
      }
    }

    executeAnalytics(context);
    logStatistics();
    generateProparseDebugIndex();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void parseIncludeFile(SensorContext context, InputFile file, RefactorSession session) {
    long startTime = System.currentTimeMillis();
    ParseUnit lexUnit = new ParseUnit(file.file(), session);
    try {
      lexUnit.lexAndGenerateMetrics();
    } catch (RefactorException | RuntimeException caught) {
      numFailures++;
      if ((caught.getCause() != null) && (caught.getCause() instanceof XCodedFileException)) {
        LOG.error("Unable to analyze xcode'd file " + file.relativePath());
      } else {
        LOG.error("Error during code lexing for " + file.relativePath(), caught);
      }
    }
    updateParseTime(System.currentTimeMillis() - startTime);

    if (lexUnit.getMetrics() != null) {
      // Saving LOC and COMMENTS metrics
      context.newMeasure().on(file).forMetric((Metric) CoreMetrics.NCLOC).withValue(
          lexUnit.getMetrics().getLoc()).save();
      context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMMENT_LINES).withValue(
          lexUnit.getMetrics().getComments()).save();
    }
  }

  private void parseMainFile(SensorContext context, InputFile file, RefactorSession session) {
    File xrefFile = getXrefFile(file.file());
    Document doc = null;
    if ((context.runtime().getProduct() == SonarProduct.SONARQUBE) && (xrefFile != null) && xrefFile.exists()) {
      LOG.debug("Parsing XML XREF file {}", xrefFile.getAbsolutePath());
      try (InputStream inpStream = new FileInputStream(xrefFile)) {
        long startTime = System.currentTimeMillis();
        doc = dBuilder.parse(
            settings.useXrefFilter() ? new InvalidXMLFilterStream(settings.getXrefBytes(), inpStream) : inpStream);
        xmlParseTime += (System.currentTimeMillis() - startTime);
        numXREF++;
      } catch (SAXException | IOException caught) {
        LOG.error("Unable to parse XREF file " + xrefFile.getAbsolutePath(), caught);
      }
    }
    if (context.runtime().getProduct() == SonarProduct.SONARLINT) {
      settings.parseHierarchy(file.relativePath());
    }

    try {
      long startTime = System.currentTimeMillis();
      ParseUnit unit = new ParseUnit(file.file(), session);
      unit.treeParser01();
      unit.attachXref(doc);
      unit.attachTypeInfo(session.getTypeInfo(unit.getRootScope().getClassName()));
      updateParseTime(System.currentTimeMillis() - startTime);

      if (context.runtime().getProduct() == SonarProduct.SONARQUBE) {
        computeCpd(context, file, unit);
        computeSimpleMetrics(context, file, unit);
        computeCommonMetrics(context, file, unit);
        computeComplexity(context, file, unit);
      }

      if (settings.useProparseDebug()) {
        generateProparseDebugFile(file, unit);
      }

      for (Map.Entry<ActiveRule, OpenEdgeProparseCheck> entry : components.getProparseRules().entrySet()) {
        LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}", entry.getKey().internalKey(),
            entry.getKey().ruleKey().repository(), entry.getKey().ruleKey().rule());
        startTime = System.currentTimeMillis();
        entry.getValue().execute(file, unit);
        ruleTime.put(entry.getKey().ruleKey().toString(),
            ruleTime.get(entry.getKey().ruleKey().toString()) + System.currentTimeMillis() - startTime);
      }
    } catch (RefactorException | ProparseRuntimeException caught) {
      LOG.error("Error during code parsing for " + file.relativePath(), caught);
      numFailures++;
      NewIssue issue = context.newIssue();
      issue.forRule(
          RuleKey.of(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY)).at(
              issue.newLocation().on(file).message(caught.getMessage())).save();
    } catch (RuntimeException caught) {
      numFailures++;
      if ((caught.getCause() != null) && (caught.getCause() instanceof XCodedFileException)) {
        LOG.error("Unable to analyze xcode'd file " + file.relativePath());
      } else {
        LOG.error("Runtime exception was caught - Please report this issue : ", caught);
      }
    }
  }

  private void updateParseTime(long elapsedTime) {
    LOG.debug("{} milliseconds to generate ParseUnit", elapsedTime);
    parseTime += elapsedTime;
    if (maxParseTime < elapsedTime) {
      maxParseTime = elapsedTime;
    }
  }

  private void executeAnalytics(SensorContext context) {
    if (!settings.useAnalytics())
      return;

    StringBuilder data = new StringBuilder(String.format(
        "proparse,product=%1$s,sid=%2$s files=%3$d,failures=%4$d,parseTime=%5$d,maxParseTime=%6$d,version=\"%7$s\"\n",
        context.runtime().getProduct().toString().toLowerCase(),
        Strings.nullToEmpty(context.settings().getString(CoreProperties.PERMANENT_SERVER_ID)), numFiles, numFailures,
        parseTime, maxParseTime, context.runtime().getApiVersion().toString()));
    for (Entry<String, Long> entry : ruleTime.entrySet()) {
      data.append(String.format("rule,product=%1$s,sid=%2$s,rulename=%3$s ruleTime=%4$d\n",
          context.runtime().getProduct().toString().toLowerCase(),
          Strings.nullToEmpty(context.settings().getString(CoreProperties.PERMANENT_SERVER_ID)), entry.getKey(),
          entry.getValue()));
    }

    try {
      final URL url = new URL("http://sonar-analytics.rssw.eu/write?db=sonar");
      HttpURLConnection connx = (HttpURLConnection) url.openConnection();
      connx.setRequestMethod("POST");
      connx.setConnectTimeout(2000);
      connx.setDoOutput(true);
      DataOutputStream wr = new DataOutputStream(connx.getOutputStream());
      wr.writeBytes(data.toString());
      wr.flush();
      wr.close();
      connx.getResponseCode();
    } catch (IOException uncaught) {
      LOG.debug("Unable to send analytics: {}", uncaught.getMessage());
    }
  }

  private void logStatistics() {
    LOG.info("{} files proparse'd, {} XML files, {} failure(s)", numFiles, numXREF, numFailures);
    LOG.info("AST Generation | time={} ms", parseTime);
    LOG.info("XML Parsing    | time={} ms", xmlParseTime);
    for (Entry<String, Long> entry : ruleTime.entrySet()) {
      LOG.info("Rule {} | time={} ms", entry.getKey(), entry.getValue());
    }
  }

  private void generateProparseDebugFile(InputFile file, ParseUnit unit) {
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

  private void generateProparseDebugIndex() {
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

  private File getXrefFile(File file) {
    String relPath = OpenEdgeProjectHelper.getPathRelativeToSourceDirs(file, settings.getSourceDirs());
    if (relPath == null)
      return null;
    return new File(settings.getPctDir(), relPath + ".xref");
  }

  private void computeCpd(SensorContext context, InputFile file, ParseUnit unit) {
    CPDCallback cpdCallback = new CPDCallback(context, file, settings, unit);
    unit.getTopNode().walk(cpdCallback);
    cpdCallback.getResult().save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeSimpleMetrics(SensorContext context, InputFile file, ParseUnit unit) {
    // Saving LOC and COMMENTS metrics
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.NCLOC).withValue(unit.getMetrics().getLoc()).save();
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMMENT_LINES).withValue(
        unit.getMetrics().getComments()).save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeCommonMetrics(SensorContext context, InputFile file, ParseUnit unit) {
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.STATEMENTS).withValue(
        unit.getTopNode().queryStateHead().size()).save();
    int numProcs = 0;
    int numFuncs = 0;
    int numMethds = 0;
    for (TreeParserSymbolScope child : unit.getRootScope().getChildScopesDeep()) {
      int scopeType = child.getRootBlock().getNode().getType();
      switch (scopeType) {
        case NodeTypes.PROCEDURE:
          boolean externalProc = false;
          for (JPNode node : child.getRootBlock().getNode().getDirectChildren()) {
            if ((node.getType() == NodeTypes.IN_KW) || (node.getType() == NodeTypes.SUPER)
                || (node.getType() == NodeTypes.EXTERNAL)) {
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

  @SuppressWarnings({"unchecked", "rawtypes"})
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

    complexity += unit.getTopNode().queryMainFile(NodeTypes.IF, NodeTypes.REPEAT, NodeTypes.FOR, NodeTypes.WHEN,
        NodeTypes.AND, NodeTypes.OR, NodeTypes.RETURN, NodeTypes.PROCEDURE, NodeTypes.FUNCTION, NodeTypes.METHOD,
        NodeTypes.ENUM).size();
    complexityWithInc += unit.getTopNode().query(NodeTypes.IF, NodeTypes.REPEAT, NodeTypes.FOR, NodeTypes.WHEN,
        NodeTypes.AND, NodeTypes.OR, NodeTypes.RETURN, NodeTypes.PROCEDURE, NodeTypes.FUNCTION, NodeTypes.METHOD,
        NodeTypes.ENUM).size();
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMPLEXITY).withValue(complexity).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.COMPLEXITY).withValue(complexityWithInc).save();
  }

}
