/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.ParseInfo;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.JsonNodeLister;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.TreeNodeLister;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.proparse.antlr4.DescriptiveErrorListener;
import org.prorefactor.proparse.antlr4.IncludeFileNotFoundException;
import org.prorefactor.proparse.antlr4.JPNode4Visitor;
import org.prorefactor.proparse.antlr4.ProgressLexer;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.proparse.antlr4.XCodedFileException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.foundation.CPDCallback;
import org.sonar.plugins.openedge.foundation.InputFileUtils;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeProjectHelper;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;

import antlr.ANTLRException;
import antlr.RecognitionException;
import eu.rssw.listing.CodeBlock;
import eu.rssw.listing.ListingParser;

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
  private int numListings;
  private int numFailures;
  private int ncLocs;

  // Timing statistics
  private Map<String, Long> ruleTime = new HashMap<>();
  private long parseTime = 0L;
  private long parse4Time = 0L;
  private long parse4Tree = 0L;
  private long xmlParseTime = 0L;
  private long maxParseTime = 0L;
  private Map<Integer, Long> decisionTime = new HashMap<>();
  private Map<Integer, Long> maxK = new HashMap<>();

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
      LOG.debug("Parsing {}", file);
      numFiles++;

      if (settings.isIncludeFile(file.filename())) {
        parseIncludeFile(context, file, session);
      } else {
        parseMainFile(context, file, session);
        if (settings.useANTLR4())
          testAntlr4(context, file, session);
      }
    }

    executeAnalytics(context);
    logStatistics();
    generateProparseDebugIndex();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void parseIncludeFile(SensorContext context, InputFile file, RefactorSession session) {
    long startTime = System.currentTimeMillis();
    ParseUnit lexUnit = null;
    try {
      lexUnit = new ParseUnit(InputFileUtils.getInputStream(file),
          InputFileUtils.getRelativePath(file, context.fileSystem()), session);
      lexUnit.lexAndGenerateMetrics();
    } catch (UncheckedIOException caught) {
      numFailures++;
      if (caught.getCause() instanceof XCodedFileException) {
        LOG.error("Unable to generate file metrics for xcode'd file '{}", file);
      } else {
        LOG.error("Unable to generate file metrics for file '" + file + "'", caught);
      }
      return;
    } catch (ProparseRuntimeException caught) {
      LOG.error("Unable to generate file metrics for file '" + file + "'", caught);
      return;
    }
    updateParseTime(System.currentTimeMillis() - startTime);

    if (lexUnit.getMetrics() != null) {
      // Saving LOC and COMMENTS metrics
      context.newMeasure().on(file).forMetric((Metric) CoreMetrics.NCLOC).withValue(
          lexUnit.getMetrics().getLoc()).save();
      ncLocs += lexUnit.getMetrics().getLoc();
      context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMMENT_LINES).withValue(
          lexUnit.getMetrics().getComments()).save();
    }
  }

  private void parseMainFile(SensorContext context, InputFile file, RefactorSession session) {
    File xrefFile = settings.getXrefFile(file);
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
      settings.parseHierarchy(file);
    }

    File listingFile = settings.getListingFile(file);
    List<Integer> trxBlocks = new ArrayList<>();
    if ((listingFile != null) && listingFile.exists() && (listingFile.getAbsolutePath().indexOf(' ') == -1)) {
      try {
        ListingParser parser = new ListingParser(listingFile, InputFileUtils.getRelativePath(file, context.fileSystem()));
        for (CodeBlock block : parser.getTransactionBlocks()) {
          trxBlocks.add(block.getLineNumber());
        }
        numListings++;
      } catch (IOException caught) {
        LOG.error("Unable to parse listing file for " + file, caught);
      }
    } else {
      LOG.debug("Listing file for '{}' not found or contains space character - Was looking for '{}'", file,
          listingFile);
    }
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.TRANSACTIONS).withValue(
        Joiner.on(",").join(trxBlocks)).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_TRANSACTIONS).withValue(
        trxBlocks.size()).save();

    ParseUnit unit = null;
    long startTime = System.currentTimeMillis();

    try {
      unit = new ParseUnit(InputFileUtils.getInputStream(file), InputFileUtils.getRelativePath(file, context.fileSystem()), session);
      unit.treeParser01();
      unit.attachXref(doc);
      unit.attachTransactionBlocks(trxBlocks);
      unit.attachTypeInfo(session.getTypeInfo(unit.getRootScope().getClassName()));
      updateParseTime(System.currentTimeMillis() - startTime);
    } catch (UncheckedIOException caught) {
      numFailures++;
      if ((caught.getCause() != null) && (caught.getCause() instanceof XCodedFileException)) {
        XCodedFileException cause = (XCodedFileException) caught.getCause();
        LOG.error("Unable to parse {} - Can't read xcode'd file {}", file, cause.getFileName());
      } else if ((caught.getCause() != null) && (caught.getCause() instanceof IncludeFileNotFoundException)) {
        IncludeFileNotFoundException cause = (IncludeFileNotFoundException) caught.getCause();
        LOG.error("Unable to parse {} - Can't find include file '{}' from '{}'", file, cause.getIncludeName(), cause.getFileName());
      } else {
        LOG.error("Unable to parse " + file + " - IOException was caught - Please report this issue", caught);
      }
      return;
    } catch (RecognitionException caught) {
      LOG.error("Error during code parsing for " + file + " at position " + caught.getFilename() + ":"
          + caught.getLine() + ":" + caught.getColumn(), caught);
      numFailures++;
      NewIssue issue = context.newIssue().forRule(
          RuleKey.of(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY));
      NewIssueLocation loc = issue.newLocation().on(file).message(Strings.nullToEmpty(caught.getMessage()) + " in "
          + caught.getFilename() + ":" + caught.getLine() + ":" + caught.getColumn());
      if (InputFileUtils.getRelativePath(file, context.fileSystem()).equals(caught.getFilename())) {
        try {
          TextPointer strt = file.newPointer(caught.getLine(), caught.getColumn() - 1);
          TextPointer end = file.newPointer(caught.getLine(), caught.getColumn());
          loc.at(file.newRange(strt, end));
        } catch (IllegalArgumentException uncaught) { // NO-SONAR
          // Nothing
        }
      }
      issue.at(loc);
      issue.save();
      return;
    } catch (RuntimeException | ANTLRException caught) {
      LOG.error("Error during code parsing for " + InputFileUtils.getRelativePath(file, context.fileSystem()), caught);
      numFailures++;
      NewIssue issue = context.newIssue();
      issue.forRule(RuleKey.of(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY)).at(
          issue.newLocation().on(file).message(Strings.nullToEmpty(caught.getMessage()))).save();
      return;
    }
    if (settings.useANTLR4())
      generateProparseFlatFiles(unit.getTopNode(), false, InputFileUtils.getRelativePath(file, context.fileSystem()));

    if (context.runtime().getProduct() == SonarProduct.SONARQUBE) {
      computeCpd(context, file, unit);
      computeSimpleMetrics(context, file, unit);
      computeCommonMetrics(context, file, unit);
      computeComplexity(context, file, unit);
    }

    if (settings.useProparseDebug()) {
      generateProparseDebugFile(file, unit);
    }

    try {
      for (Map.Entry<ActiveRule, OpenEdgeProparseCheck> entry : components.getProparseRules().entrySet()) {
        LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}", entry.getKey().internalKey(),
            entry.getKey().ruleKey().repository(), entry.getKey().ruleKey().rule());
        startTime = System.currentTimeMillis();
        entry.getValue().sensorExecute(file, unit);
        ruleTime.put(entry.getKey().ruleKey().toString(),
            ruleTime.get(entry.getKey().ruleKey().toString()) + System.currentTimeMillis() - startTime);
      }
    } catch (RuntimeException caught) {
      LOG.error("Error during rule execution for " + file, caught);
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

    StringBuilder data = new StringBuilder(String.format( // NOSONAR Influx requires LF
        "proparse,product=%1$s,sid=%2$s files=%3$d,failures=%4$d,parseTime=%5$d,maxParseTime=%6$d,version=\"%7$s\",ncloc=%8$d\n",
        context.runtime().getProduct().toString().toLowerCase(), OpenEdgeProjectHelper.getServerId(context), numFiles,
        numFailures, parseTime, maxParseTime, context.runtime().getApiVersion().toString(), ncLocs));
    for (Entry<String, Long> entry : ruleTime.entrySet()) {
      data.append(String.format("rule,product=%1$s,sid=%2$s,rulename=%3$s ruleTime=%4$d\n", // NOSONAR
          context.runtime().getProduct().toString().toLowerCase(), OpenEdgeProjectHelper.getServerId(context),
          entry.getKey(), entry.getValue()));
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
    LOG.info("{} files proparse'd, {} XML files, {} listing files, {} failure(s), {} NCLOCs", numFiles, numXREF,
        numListings, numFailures, ncLocs);
    LOG.info("AST Generation | time={} ms", parseTime);
    LOG.info("XML Parsing    | time={} ms", xmlParseTime);
    LOG.info("AST4Generation | time={} ms", parse4Time);
    LOG.info("AST4Tree       | time={} ms", parse4Tree);
    // Sort entries by rule name
    ruleTime.entrySet().stream().sorted(
        (Entry<String, Long> obj1, Entry<String, Long> obj2) -> obj1.getKey().compareTo(obj2.getKey())).forEach(
            (Entry<String, Long> entry) -> LOG.info("Rule {} | time={} ms", entry.getKey(), entry.getValue()));
    if (!decisionTime.isEmpty()) {
      LOG.info("ANTRL4 - 25 longest rules");
      decisionTime.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).limit(25).forEach(
          entry -> LOG.info("Rule {} - {} | time={} ms", entry.getKey().intValue(),
              Proparse.ruleNames[Proparse._ATN.getDecisionState(entry.getKey().intValue()).ruleIndex],
              entry.getValue()));
    }
    if (!maxK.isEmpty()) {
      LOG.info("ANTRL4 - 25 Max lookeahead rules");
      maxK.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).limit(25).forEach(
          entry -> LOG.info("Rule {} - {} | Max lookahead: {}", entry.getKey().intValue(),
              Proparse.ruleNames[Proparse._ATN.getDecisionState(entry.getKey().intValue()).ruleIndex],
              entry.getValue()));
    }
  }

  private void testAntlr4(SensorContext context, InputFile file, RefactorSession session) {
    long startTime = System.currentTimeMillis();
    try {
      ProgressLexer lexer = new ProgressLexer(session, InputFileUtils.getInputStream(file), InputFileUtils.getRelativePath(file, context.fileSystem()), false);
      lexer.setMergeNameDotInId(true);
      Proparse parser = new Proparse(new CommonTokenStream(lexer));
      if (settings.useANTLR4Profiler())
        parser.setProfile(true);
      parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
      parser.removeErrorListeners();
      parser.addErrorListener(new DescriptiveErrorListener());
      parser.setErrorHandler(new BailErrorStrategy());
      
      parser.initAntlr4(session, lexer.getFilenameList());
      ParseTree tree = parser.program();
      long time1 = System.currentTimeMillis() - startTime;
      parse4Time += time1;
      JPNode4Visitor visitor = new JPNode4Visitor(parser.getParserSupport(), (BufferedTokenStream) parser.getInputStream());
      org.prorefactor.proparse.antlr4.ProgramRootNode root4 = (org.prorefactor.proparse.antlr4.ProgramRootNode) visitor.visit(tree).build();
      long time2 = System.currentTimeMillis() - startTime - time1;
      parse4Tree += time2;

      generateProparseFlatFiles(root4, true, InputFileUtils.getRelativePath(file, context.fileSystem()));
      generateAntlr4Stats(InputFileUtils.getRelativePath(file, context.fileSystem()), time1, time2, parser.getParseInfo());

      LOG.info("File {} - {} ms ANTLR4 - {} ms visitor",InputFileUtils.getRelativePath(file, context.fileSystem()), time1, time2);
    } catch (UncheckedIOException caught) {
      if ((caught.getCause() != null) && (caught.getCause() instanceof XCodedFileException)) {
        XCodedFileException cause = (XCodedFileException) caught.getCause();
        LOG.error("Unable to parse {} - Can't read xcode'd file {}", file, cause.getFileName());
      } else if ((caught.getCause() != null) && (caught.getCause() instanceof IncludeFileNotFoundException)) {
        IncludeFileNotFoundException cause = (IncludeFileNotFoundException) caught.getCause();
        LOG.error("Unable to parse {} - Can't find include file '{}' from '{}'", file, cause.getIncludeName(), cause.getFileName());
      } else {
        LOG.error("Unable to parse " + file + " - IOException was caught - Please report this issue", caught);
      }
      return;
    } catch (ParseCancellationException caught) {
      LOG.error("Parse cancelled for {}", InputFileUtils.getRelativePath(file, context.fileSystem()));
    } catch (Throwable caught) {
      LOG.error("Error during code parsing for " + InputFileUtils.getRelativePath(file, context.fileSystem()), caught);
    }
  }

  private void generateProparseFlatFiles(ProgramRootNode rootNode, boolean version, String fileName) {
    File f = new File(".proparse/" + (version ? "antlr4/" : "antlr2/") + fileName.replace('\\', '_').replace('/', '_').replace(':', '_'));
    f.getParentFile().mkdirs();

    try (PrintWriter writer = new PrintWriter(f)) {
      TreeNodeLister nodeLister = new TreeNodeLister(rootNode, writer, ABLNodeType.INVALID_NODE, ABLNodeType.ANNOTATION);
      nodeLister.print();
    } catch (IOException caught) {
      LOG.error("Unable to write proparse debug file", caught);
    }
  }

  private void generateProparseFlatFiles(org.prorefactor.proparse.antlr4.ProgramRootNode rootNode, boolean version, String fileName) {
    File f = new File(".proparse/" + (version ? "antlr4/" : "antlr2/") + fileName.replace('\\', '_').replace('/', '_').replace(':', '_'));
    f.getParentFile().mkdirs();

    try (PrintWriter writer = new PrintWriter(f)) {
      org.prorefactor.proparse.antlr4.TreeNodeLister nodeLister = new org.prorefactor.proparse.antlr4.TreeNodeLister(rootNode, writer, ABLNodeType.INVALID_NODE, ABLNodeType.ANNOTATION);
      nodeLister.print();
    } catch (IOException caught) {
      LOG.error("Unable to write proparse debug file", caught);
    }
  }

  private void generateAntlr4Stats(String fileName, long parseTree, long treeVisitor, ParseInfo info) {
    File f = new File(".proparse/antlr4-timings/" + fileName.replace('\\', '_').replace('/', '_').replace(':', '_'));
    f.getParentFile().mkdirs();
    try (PrintWriter writer = new PrintWriter(f)) {
      writer.println(fileName.replace(':', '_') + " : " + parseTree + " : " + treeVisitor);
      if ((info != null) && (info.getDecisionInfo() != null)) {
        Arrays.stream(info.getDecisionInfo()).filter(decision -> decision.SLL_MaxLook > 0).sorted(
            (d1, d2) -> Long.compare(d2.SLL_MaxLook, d1.SLL_MaxLook)).forEach(
                decision -> writer.println(String.format(
                    "Time: %d in %d calls - LL_Lookaheads: %d Max k: %d Ambiguities: %d Errors: %d Rule: %s",
                    decision.timeInPrediction / 1000000, decision.invocations, decision.SLL_TotalLook,
                    decision.SLL_MaxLook, decision.ambiguities.size(), decision.errors.size(),
                    Proparse.ruleNames[Proparse._ATN.getDecisionState(decision.decision).ruleIndex])));
        // MaxK + prediction time stats
        Arrays.stream(info.getDecisionInfo()).filter(decision -> decision.SLL_MaxLook > 0).forEach(decision -> {
          if ((maxK.get(decision.decision) == null) || (maxK.get(decision.decision) < decision.SLL_MaxLook))
            maxK.put(decision.decision, decision.SLL_MaxLook);
        });
        Arrays.stream(info.getDecisionInfo()).filter(decision -> decision.timeInPrediction > 0).forEach(
            decision -> decisionTime.put(decision.decision, (decision.timeInPrediction / 1000000) 
                + (decisionTime.get(decision.decision) == null ? 0 : decisionTime.get(decision.decision))));
      }
    } catch (IOException caught) {
      LOG.error("Unable to write proparse debug file", caught);
    }
  }

  private void generateProparseDebugFile(InputFile file, ParseUnit unit) {
    String fileName = ".proparse/" + file.relativePath() + ".json";
    File dbgFile = new File(fileName);
    dbgFile.getParentFile().mkdirs();
    try (PrintWriter writer = new PrintWriter(dbgFile)) {
      JsonNodeLister nodeLister = new JsonNodeLister(unit.getTopNode(), writer, ABLNodeType.LEFTPAREN,
          ABLNodeType.RIGHTPAREN, ABLNodeType.COMMA, ABLNodeType.PERIOD, ABLNodeType.LEXCOLON, ABLNodeType.OBJCOLON,
          ABLNodeType.THEN, ABLNodeType.END);
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

  private void computeCpd(SensorContext context, InputFile file, ParseUnit unit) {
    CPDCallback cpdCallback = new CPDCallback(context, file, settings, unit);
    unit.getTopNode().walk(cpdCallback);
    cpdCallback.getResult().save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeSimpleMetrics(SensorContext context, InputFile file, ParseUnit unit) {
    // Saving LOC and COMMENTS metrics
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.NCLOC).withValue(unit.getMetrics().getLoc()).save();
    ncLocs += unit.getMetrics().getLoc();
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
        case ProParserTokenTypes.PROCEDURE:
          boolean externalProc = false;
          for (JPNode node : child.getRootBlock().getNode().getDirectChildren()) {
            if ((node.getType() == ProParserTokenTypes.IN_KW) || (node.getType() == ProParserTokenTypes.SUPER)
                || (node.getType() == ProParserTokenTypes.EXTERNAL)) {
              externalProc = true;
            }
          }
          if (!externalProc) {
            numProcs++;
          }
          break;
        case ProParserTokenTypes.FUNCTION:
          boolean externalFunc = false;
          for (JPNode node : child.getRootBlock().getNode().getDirectChildren()) {
            if ((node.getType() == ProParserTokenTypes.IN_KW) || (node.getType() == ProParserTokenTypes.FORWARDS)) {
              externalFunc = true;
            }
          }
          if (!externalFunc) {
            numFuncs++;
          }
          break;
        case ProParserTokenTypes.METHOD:
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

    complexity += unit.getTopNode().queryMainFile(ABLNodeType.IF, ABLNodeType.REPEAT, ABLNodeType.FOR, ABLNodeType.WHEN,
        ABLNodeType.AND, ABLNodeType.OR, ABLNodeType.RETURN, ABLNodeType.PROCEDURE, ABLNodeType.FUNCTION, ABLNodeType.METHOD,
        ABLNodeType.ENUM).size();
    complexityWithInc += unit.getTopNode().query(ABLNodeType.IF, ABLNodeType.REPEAT, ABLNodeType.FOR, ABLNodeType.WHEN,
        ABLNodeType.AND, ABLNodeType.OR, ABLNodeType.RETURN, ABLNodeType.PROCEDURE, ABLNodeType.FUNCTION, ABLNodeType.METHOD,
        ABLNodeType.ENUM).size();
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMPLEXITY).withValue(complexity).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.COMPLEXITY).withValue(complexityWithInc).save();
  }

}
