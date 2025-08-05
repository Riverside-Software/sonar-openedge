/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.JsonNodeLister;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.Triplet;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.proparse.CognitiveComplexityListener;
import org.prorefactor.proparse.IncludeFileNotFoundException;
import org.prorefactor.proparse.LinesOfCodeVisitor;
import org.prorefactor.proparse.XCodedFileException;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.treeparser.ParseUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.error.NewAnalysisError;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.foundation.CPDCallback;
import org.sonar.plugins.openedge.foundation.IRefactorSessionEnv;
import org.sonar.plugins.openedge.foundation.InputFileUtils;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.progress.xref.CrossReference;
import com.progress.xref.CrossReferenceUtils;
import com.progress.xref.InvalidXMLFilterStream;

import eu.rssw.listing.CodeBlock;
import eu.rssw.listing.ListingParser;

@DependsUpon(value = {"PctDependencies"})
public class OpenEdgeProparseSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeProparseSensor.class);

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
  private int ncLoc;
  private int ncLocL2;

  // Include files LOC
  private Map<String, IntervalSet> incLinesOfOCode = new HashMap<>();
  private Set<InputFile> ncLocPushed = new HashSet<>();

  // Timing statistics
  private Map<String, Long> ruleTime = new HashMap<>();
  private long parseTime = 0L;
  private long xmlParseTime = 0L;
  private long maxParseTime = 0L;
  private Map<Integer, Long> decisionTime = new HashMap<>();
  private Map<Integer, Long> maxK = new HashMap<>();

  // Proparse debug
  List<Triplet<String, String, String>> debugFiles = new ArrayList<>();
  private int varNum = 0;

  public OpenEdgeProparseSensor(OpenEdgeSettings settings, OpenEdgeComponents components) {
    this.settings = settings;
    this.components = components;
    dbFactory = DocumentBuilderFactory.newInstance();

    try {
      dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      dBuilder = dbFactory.newDocumentBuilder();
    } catch (ParserConfigurationException caught) {
      throw new IllegalStateException(caught);
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage(Constants.LANGUAGE_KEY) //
      .name(getClass().getSimpleName()) //
      .onlyWhenConfiguration(config -> !config.getBoolean(Constants.SKIP_PROPARSE_PROPERTY).orElse(false));
  }

  @Override
  public void execute(SensorContext context) {
    if (settings.skipProparseSensor())
      return;

    settings.init();
    components.init(context);
    boolean skipUnchangedFiles = settings.skipUnchangedFiles();
    if (skipUnchangedFiles)
      LOG.info("Unchanged files will be skipped during the analysis (SonarQube DE or more, version 9.9 or more, and sonar.pullrequest.branch is set)");

    for (Map.Entry<ActiveRule, OpenEdgeProparseCheck> entry : components.getProparseRules().entrySet()) {
      ruleTime.put(entry.getKey().ruleKey().toString(), 0L);
    }
    IRefactorSessionEnv sessions = settings.getProparseSessions();
    FilePredicates predicates = context.fileSystem().predicates();

    // Counting total number of files
    long totFiles = StreamSupport.stream(context.fileSystem().inputFiles(
        predicates.and(predicates.hasLanguage(Constants.LANGUAGE_KEY), predicates.hasType(Type.MAIN))).spliterator(),
        false).count();
    long prevMessage = System.currentTimeMillis();
    for (InputFile file : context.fileSystem().inputFiles(
        predicates.and(predicates.hasLanguage(Constants.LANGUAGE_KEY), predicates.hasType(Type.MAIN)))) {
      if (skipUnchangedFiles) {
        if (components.isChanged(context, file))
          LOG.debug("Analyzing {} as it is changed in this branch", file);
        else {
          LOG.debug("Skip {} as it is unchanged in this branch", file);
          continue;
        }
      }
      numFiles++;

      if (System.currentTimeMillis() - prevMessage > 30000L) {
        prevMessage = System.currentTimeMillis();
        LOG.info("{}/{} - Current file: {}", numFiles, totFiles, file);
      }
      IProparseEnvironment session = sessions.getSession(file.toString());
      if (settings.isIncludeFile(file.filename())) {
        parseIncludeFile(context, file, session);
      } else {
        parseMainFile(context, file, session);
      }
      if (context.isCancelled()) {
        LOG.info("Analysis cancelled...");
        return;
      }
    }

    // Publish consolidated LOC-L2 of all include files 
    publishIncLocL2(context);
    computeAnalytics(context);
    if (context.runtime().getProduct() == SonarProduct.SONARQUBE)
      logStatistics();
    generateProparseDebugIndex(context);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void parseIncludeFile(SensorContext context, InputFile file, IProparseEnvironment session) {
    long startTime = System.currentTimeMillis();
    ParseUnit lexUnit = null;
    try {
      lexUnit = new ParseUnit(InputFileUtils.getInputStream(file),
          InputFileUtils.getRelativePath(file, context.fileSystem()), session, file.charset());
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
      ncLoc += lexUnit.getMetrics().getLoc();
      context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMMENT_LINES).withValue(
          lexUnit.getMetrics().getComments()).save();
    }

    if (!settings.useSimpleCPD()) {
      try {
        lexUnit = new ParseUnit(InputFileUtils.getInputStream(file),
            InputFileUtils.getRelativePath(file, context.fileSystem()), session, file.charset());
        TokenSource stream = lexUnit.lex();
        OpenEdgeCPDSensor.processTokenSource(file, context.newCpdTokens().onFile(file), stream);
      } catch (UncheckedIOException | ProparseRuntimeException caught) {
        // Nothing here
      }
    }
  }

  private Document parseXREF(File xrefFile) {
    Document doc = null;
    if ((xrefFile != null) && xrefFile.exists()) {
      LOG.debug("Parsing XML XREF file {}", xrefFile.getAbsolutePath());
      try (InputStream inpStream = new FileInputStream(xrefFile)) {
        doc = dBuilder.parse(new InvalidXMLFilterStream(inpStream));
      } catch (SAXException | IOException caught) {
        LOG.error("Unable to parse XREF file " + xrefFile.getAbsolutePath(), caught);
      }
    }

    return doc;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void parseMainFile(SensorContext context, InputFile file, IProparseEnvironment session) {
    CrossReference xref = null;
    Document doc = null;
    if (context.runtime().getProduct() == SonarProduct.SONARQUBE) {
      long startTime = System.currentTimeMillis();
      xref = CrossReferenceUtils.parseXREF(settings.getXrefFile(file));
      if (settings.parseXrefDocument())
        doc = parseXREF(settings.getXrefFile(file));
      xmlParseTime += (System.currentTimeMillis() - startTime);
    } else if (context.runtime().getProduct() == SonarProduct.SONARLINT) {
      long startTime = System.currentTimeMillis();
      xref = CrossReferenceUtils.parseXREF(settings.getSonarlintXrefFile(file));
      if (settings.parseXrefDocument())
        doc = parseXREF(settings.getSonarlintXrefFile(file));
      xmlParseTime += (System.currentTimeMillis() - startTime);
    }
    if (!xref.getSource().isEmpty())
      numXREF++;
    else
      LOG.debug("Empty XREF file");

    File listingFile = settings.getListingFile(file);
    List<Integer> trxBlocks = new ArrayList<>();
    if ((listingFile != null) && listingFile.exists() && (listingFile.getAbsolutePath().indexOf(' ') == -1)) {
      try {
        ListingParser parser = new ListingParser(listingFile.toPath(),
            InputFileUtils.getRelativePath(file, context.fileSystem()));
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

    // Shared objects
    long numShrTT = xref.getSource().stream().mapToLong(src -> src.getReference().stream().filter(
        ref -> "NEW-SHR-TEMPTABLE".equalsIgnoreCase(ref.getReferenceType())).count()).sum();
    long numShrDS = xref.getSource().stream().mapToLong(src -> src.getReference().stream().filter(
        ref -> "NEW-SHR-DATASET".equalsIgnoreCase(ref.getReferenceType())).count()).sum();
    long numShrVar = xref.getSource().stream().mapToLong(src -> src.getReference().stream().filter(
        ref -> "NEW-SHR-VARIABLE".equalsIgnoreCase(ref.getReferenceType())).count()).sum();

    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.NUM_TRANSACTIONS).withValue(
        trxBlocks.size()).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.SHR_TT).withValue((int) numShrTT).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.SHR_DS).withValue((int) numShrDS).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.SHR_VAR).withValue((int) numShrVar).save();

    ParseUnit unit = null;
    long startTime = System.currentTimeMillis();

    try {
      unit = new ParseUnit(InputFileUtils.getInputStream(file),
          InputFileUtils.getRelativePath(file, context.fileSystem()), session, file.charset());
      unit.attachXref(doc);
      unit.attachXref(xref);
      unit.parse();
      unit.treeParser01();

      unit.attachTransactionBlocks(trxBlocks);
      updateParseTime(System.currentTimeMillis() - startTime);
    } catch (UncheckedIOException caught) {
      numFailures++;
      if (caught.getCause() instanceof XCodedFileException) {
        XCodedFileException cause = (XCodedFileException) caught.getCause();
        LOG.error("Unable to parse {} - Can't read xcode'd file {}", file, cause.getFileName());
      } else if (caught.getCause() instanceof IncludeFileNotFoundException) {
        IncludeFileNotFoundException cause = (IncludeFileNotFoundException) caught.getCause();
        LOG.error("Unable to parse {} - Can't find include file '{}' from '{}'", file, cause.getIncludeName(),
            cause.getFileName());
      } else {
        LOG.error("Unable to parse " + file + " - IOException was caught - Please report this issue", caught);
      }
      return;
    } catch (ParseCancellationException caught) {
      RecognitionException cause = (RecognitionException) caught.getCause();
      ProToken tok = (ProToken) cause.getOffendingToken();
      if (settings.displayStackTraceOnError()) {
        LOG.error("Parser error in '" + file + "' at position " + tok.getFileName() + ":" + tok.getLine()
            + ":" + tok.getCharPositionInLine(), cause);
      } else {
        LOG.error("Parser error in '{}' at position {}:{}:{}", file, tok.getFileName(), tok.getLine(),
            tok.getCharPositionInLine());
      }
      numFailures++;

      TextPointer strt = null;
      TextPointer end = null;
      if (InputFileUtils.getRelativePath(file, context.fileSystem()).equals(tok.getFileName())) {
        try {
          strt = file.newPointer(tok.getLine(), tok.getCharPositionInLine());
          end = file.newPointer(tok.getLine(), tok.getEndCharPositionInLine());
        } catch (IllegalArgumentException uncaught) {
          // Nothing
        }
      }

      if (context.runtime().getProduct() == SonarProduct.SONARLINT) {
        NewAnalysisError analysisError = context.newAnalysisError();
        analysisError.onFile(file);
        analysisError.message(Strings.nullToEmpty(cause.getMessage()) + " in " + tok.getFileName() + ":" + tok.getLine()
            + ":" + tok.getCharPositionInLine());
        if (strt != null)
          analysisError.at(strt);
        analysisError.save();
      } else {
        NewIssue issue = context.newIssue().forRule(
            RuleKey.of(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY));
        NewIssueLocation loc = issue.newLocation().on(file).message(Strings.nullToEmpty(caught.getMessage()) + " in "
            + tok.getFileName() + ":" + tok.getLine() + ":" + tok.getCharPositionInLine());
        if ((strt != null) && (end != null) && (strt.compareTo(end) < 0))
          loc.at(file.newRange(strt, end));
        issue.at(loc);
        issue.save();
      }

      return;
    } catch (RuntimeException caught) {
      LOG.error("Parser error in '" + InputFileUtils.getRelativePath(file, context.fileSystem()) + "'", caught);
      numFailures++;
      NewIssue issue = context.newIssue();
      issue.forRule(RuleKey.of(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY)).at(
          issue.newLocation().on(file).message(Strings.nullToEmpty(caught.getMessage()))).save();
      return;
    }

    if (context.runtime().getProduct() == SonarProduct.SONARQUBE) {
      if (!settings.useSimpleCPD()) {
        computeCpd(context, file, unit);
      }
      computeSimpleMetrics(context, file, unit);
      computeCommonMetrics(context, file, unit);
      computeComplexity(context, file, unit);
      if (!settings.skipCognitiveComplexity())
        computeCognitiveComplexity(context, file, unit);
      computeLOCL2(context, file, unit);
    }

    if (settings.useProparseDebug() && matchDebugInclude(file.toString())) {
        generateProparseDebugFile(context, file, unit);
    }

    for (Map.Entry<ActiveRule, OpenEdgeProparseCheck> entry : components.getProparseRules().entrySet()) {
      try {
        LOG.debug("ActiveRule - Internal key {} - Repository {} - Rule {}", entry.getKey().internalKey(),
            entry.getKey().ruleKey().repository(), entry.getKey().ruleKey().rule());
        startTime = System.currentTimeMillis();
        entry.getValue().sensorExecute(file, unit);
        ruleTime.put(entry.getKey().ruleKey().toString(),
            ruleTime.get(entry.getKey().ruleKey().toString()) + System.currentTimeMillis() - startTime);
      } catch (RuntimeException caught) {
        LOG.error("Error during rule execution for " + file, caught);
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

  private void computeAnalytics(SensorContext context) {
    // Store values in OpenEdgeComponents instance. Used by client-side sensor in rules package.
    components.setAnalytics(String.format(
        "files=%1$d,failures=%2$d,parseTime=%3$d,maxParseTime=%4$d,ncloc=%5$d,nclocl2=%6$d,oeversion=\"%7$s\"",
        numFiles, numFailures, parseTime, maxParseTime, ncLoc, ncLocL2, settings.getOpenEdgePluginVersion()));
    components.setNcLoc(ncLoc);
    components.setNcLocL2(ncLocL2);
    // And make the value available the value available to Compute Engine task
    context.addContextProperty("sonar.oe.ncloc", Integer.toString(ncLoc));
    context.addContextProperty("sonar.oe.nclocl2", Integer.toString(ncLocL2));
  }

  private void logStatistics() {
    LOG.info("{} files proparse'd, {} XREF files, {} listing files, {} failure(s), {} NCLOCs, {} NCLOC-L2", numFiles, numXREF,
        numListings, numFailures, ncLoc, ncLocL2);
    LOG.info("AST Generation | time={} ms", parseTime);
    LOG.info("XREF Parsing   | time={} ms", xmlParseTime);
    LOG.info("Rules          | time={} ms", ruleTime.values().stream().reduce(0L, Long::sum));
    // Sort entries by rule name
    ruleTime.entrySet().stream().sorted(
        (Entry<String, Long> obj1, Entry<String, Long> obj2) -> obj1.getKey().compareTo(obj2.getKey())).forEach(
            (Entry<String, Long> entry) -> LOG.info("Rule {} | time={} ms", entry.getKey(), entry.getValue()));
    if (!decisionTime.isEmpty()) {
      LOG.info("ANTRL4 - 25 longest rules");
      decisionTime.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).limit(25).forEach(
          entry -> LOG.info("Rule {} - {} | time={} ms", entry.getKey(),
              Proparse.ruleNames[Proparse._ATN.getDecisionState(entry.getKey().intValue()).ruleIndex],
              entry.getValue()));
    }
    if (!maxK.isEmpty()) {
      LOG.info("ANTRL4 - 25 Max lookeahead rules");
      maxK.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).limit(25).forEach(
          entry -> LOG.info("Rule {} - {} | Max lookahead: {}", entry.getKey(),
              Proparse.ruleNames[Proparse._ATN.getDecisionState(entry.getKey().intValue()).ruleIndex],
              entry.getValue()));
    }
  }

  private boolean matchDebugInclude(String fName) {
    for (var pattern : settings.getProparseDebugIncludes().split(",")) {
      if (!pattern.isBlank() && SelectorUtils.matchPath(pattern, fName, false))
        return true;
    }
    return false;
  }

  private void generateProparseDebugFile(SensorContext context, InputFile file, ParseUnit unit) {
    var fName = file.toString().replace('/', '_').replace('.', '_');
    var fileName = ".proparse/files/" + fName + ".json";
    var dbgPath = context.fileSystem().baseDir().toPath().resolve(fileName);

    try {
      Files.createDirectories(dbgPath.getParent());
    } catch (IOException caught) {
      LOG.error("Error while creating .proparse/files directory", caught);
      return;
    }

    try (var writer = Files.newBufferedWriter(dbgPath)
        ) {
      writer.write("var var" + varNum + " = ");
      var nodeLister = new JsonNodeLister(unit.getTopNode(), writer, ABLNodeType.LEFTPAREN,
          ABLNodeType.RIGHTPAREN, ABLNodeType.COMMA, ABLNodeType.PERIOD, ABLNodeType.LEXCOLON, ABLNodeType.OBJCOLON,
          ABLNodeType.THEN, ABLNodeType.END);
      nodeLister.print();
      writer.write(";");
      debugFiles.add(Triplet.of(file.toString(), fName, "var" + varNum++));
    } catch (IOException caught) {
      LOG.error("Unable to write proparse debug file", caught);
    }
  }

  private void generateProparseDebugIndex(SensorContext context) {
    if (settings.useProparseDebug()) {
      var outPath = context.fileSystem().baseDir().toPath().resolve(".proparse");
      try {
        Files.createDirectories(outPath);
      } catch (IOException caught) {
        LOG.error("Error while creating .proparse directory", caught);
        return;
      }

      try (var from = getClass().getResourceAsStream("/debug-index.html");
          var to = Files.newOutputStream(outPath.resolve("index.html"))) {
        ByteStreams.copy(from, to);
        for (var dbgFile : debugFiles) {
          var tmp = "<script src=\"files/" + dbgFile.getO2() + ".json\"></script>\n";
          to.write(tmp.getBytes());
        }
        to.write("</body>\n</html>".getBytes());
      } catch (IOException caught) {
        LOG.error("Error while writing index.html", caught);
      }

      try (var writer = Files.newBufferedWriter(outPath.resolve("index.json"))) {
        var first = true;
        writer.write("var data= [");
        writer.newLine();
        for (var dbgFile : debugFiles) {
          if (!first)
            writer.write(',');
          first = false;
          writer.write("{ \"file\": \"" + dbgFile.getO1() + "\", \"var\": \"" + dbgFile.getO3() + " \" }");
          writer.newLine();
        }
        writer.write("]");
      } catch (IOException uncaught) {
        LOG.error("Error while writing debug index", uncaught);
      }
    }
  }

  private void computeCpd(SensorContext context, InputFile file, ParseUnit unit) {
    CPDCallback cpdCallback = new CPDCallback(context, file, settings);
    unit.getTopNode().walk(cpdCallback);
    cpdCallback.getResult().save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeSimpleMetrics(SensorContext context, InputFile file, ParseUnit unit) {
    // Saving LOC, COMMENTS and DIRECTIVES metrics
    ncLoc += unit.getMetrics().getLoc();
    context.newMeasure().on(file) //
      .forMetric((Metric) CoreMetrics.NCLOC) //
      .withValue(unit.getMetrics().getLoc()) //
      .save();
    context.newMeasure().on(file) //
      .forMetric((Metric) CoreMetrics.COMMENT_LINES) //
      .withValue(unit.getMetrics().getComments()) //
      .save();
    context.newMeasure().on(file) //
      .forMetric((Metric) OpenEdgeMetrics.DIRECTIVES) //
      .withValue(unit.getMetrics().getDirectives()) //
      .save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeCommonMetrics(SensorContext context, InputFile file, ParseUnit unit) {
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.STATEMENTS).withValue(
        unit.getTopNode().queryStateHead().size()).save();
    int numProcs = 0;
    int numFuncs = 0;
    int numMethds = 0;

    // Search for nodes starting a procedure, function or method
    Predicate<JPNode> p1 = node -> node.isStateHead() && ((node.getNodeType() == ABLNodeType.PROCEDURE)
        || (node.getNodeType() == ABLNodeType.FUNCTION) || (node.getNodeType() == ABLNodeType.METHOD));
    Predicate<JPNode> p2 = node -> (node.getPreviousNode() == null)
        || (node.getPreviousNode().getNodeType() != ABLNodeType.END);
    for (JPNode node : unit.getTopNode().query2(p1.and(p2))) {
      switch (node.getNodeType()) {
        case PROCEDURE:
          if (node.getDirectChildren(ABLNodeType.IN, ABLNodeType.SUPER, ABLNodeType.EXTERNAL).isEmpty())
            numProcs++;
          break;
        case FUNCTION:
          if (node.getDirectChildren(ABLNodeType.IN, ABLNodeType.FORWARDS).isEmpty())
            numFuncs++;
          break;
        case METHOD:
          numMethds++;
          break;
        default:
          // Nope
          break;
      }
    }

    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.INTERNAL_PROCEDURES).withValue(numProcs).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.INTERNAL_FUNCTIONS).withValue(numFuncs).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.METHODS).withValue(numMethds).save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeComplexity(SensorContext context, InputFile file, ParseUnit unit) {
    // Interfaces don't contribute to complexity
    if (unit.isInterface())
      return;
    int complexity = 0;
    int complexityWithInc = 0;
    // Procedure has a main block, so starting at 1
    if (!unit.isClass()) {
      complexity++;
      complexityWithInc++;
    }

    complexity += unit.getTopNode().queryMainFile(ABLNodeType.IF, ABLNodeType.REPEAT, ABLNodeType.FOR, ABLNodeType.WHEN,
        ABLNodeType.AND, ABLNodeType.OR, ABLNodeType.RETURN, ABLNodeType.PROCEDURE, ABLNodeType.FUNCTION,
        ABLNodeType.METHOD, ABLNodeType.ENUM).size();
    complexityWithInc += unit.getTopNode().query(ABLNodeType.IF, ABLNodeType.REPEAT, ABLNodeType.FOR, ABLNodeType.WHEN,
        ABLNodeType.AND, ABLNodeType.OR, ABLNodeType.RETURN, ABLNodeType.PROCEDURE, ABLNodeType.FUNCTION,
        ABLNodeType.METHOD, ABLNodeType.ENUM).size();
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COMPLEXITY).withValue(complexity).save();
    context.newMeasure().on(file).forMetric((Metric) OpenEdgeMetrics.COMPLEXITY).withValue(complexityWithInc).save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeCognitiveComplexity(SensorContext context, InputFile file, ParseUnit unit) {
    var complexity = 0;
    for (var routine : unit.getRootScope().getRoutines()) {
      var sig = routine.getSignature();
      final var signature = sig.substring(0, sig.lastIndexOf(')') + 1);
      var method = unit.getTypeInfo() == null ? null : unit.getTypeInfo().getMethods().stream().filter(
          it -> it.getSignature().equals(signature)).findFirst().orElse(null);

      var block = routine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
      // Count complexity only on main blocks and on blocks in the main file
      if ((block instanceof ProgramRootNode) || (block.asJPNode().firstNaturalChild().getFileIndex() == 0)) {
        var listener = new CognitiveComplexityListener(block, unit.getTypeInfo(), method);
        listener.walkStatementBlock(block);
        complexity += listener.getMainFileComplexity();
      }
    }
    context.newMeasure().on(file).forMetric((Metric) CoreMetrics.COGNITIVE_COMPLEXITY).withValue(complexity).save();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void computeLOCL2(SensorContext context, InputFile file, ParseUnit unit) {
    var visitor = new LinesOfCodeVisitor();
    visitor.walkStatementBlock(unit.getTopNode());
    var locCountPerFile = visitor.getCounts();

    var locCountMainFile = locCountPerFile.get(0);
    if (locCountMainFile != null) {
      // Store LOC-L2 of main file
      if (unit.isAppBuilderCode()) {
        var editableSections = unit.getCodeSections().get(0);
        if (editableSections != null)
          locCountMainFile = locCountMainFile.and(editableSections);
      }
      ncLocL2 += locCountMainFile.size();
      context.newMeasure().on(file) //
        .forMetric((Metric) OpenEdgeMetrics.OELIC_NCLOC) //
        .withValue(locCountMainFile.size()) //
        .save();
      ncLocPushed.add(file);
    }
    // Then collect LOC-LC2 of all include files
    for (var entry : locCountPerFile.entrySet()) {
      if (entry.getKey() > 0) {
        var set = incLinesOfOCode.computeIfAbsent(unit.getIncludeFileName(entry.getKey()), it -> new IntervalSet());
        for (var ii : entry.getValue().getIntervals()) {
          set.add(ii.a, ii.b);
        }
      }
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void publishIncLocL2(SensorContext context) {
    for (var entry : incLinesOfOCode.entrySet()) {
      var pred1 = context.fileSystem().predicates().hasAbsolutePath(entry.getKey());
      var pred2 = context.fileSystem().predicates().hasRelativePath(entry.getKey());
      var target = context.fileSystem().inputFile(pred1);
      if (target == null) {
        target = context.fileSystem().inputFile(pred2);
      }
      if ((target != null) && Constants.LANGUAGE_KEY.equals(target.language()) && !ncLocPushed.contains(target)) {
        var sz = entry.getValue().size();
        context.newMeasure().on(target) //
          .forMetric((Metric) OpenEdgeMetrics.OELIC_NCLOC) //
          .withValue(sz) //
          .save();
        ncLocL2 += sz;
        ncLocPushed.add(target);
      }
    }
  }

}
