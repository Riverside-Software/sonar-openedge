/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.treeparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.atn.DecisionInfo;
import org.antlr.v4.runtime.atn.ParseInfo;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.MacroLevel;
import org.prorefactor.macrolevel.MacroRef;
import org.prorefactor.macrolevel.PreprocessorEventListener;
import org.prorefactor.macrolevel.PreprocessorEventListener.CodeSection;
import org.prorefactor.proparse.ABLLexer;
import org.prorefactor.proparse.JPNodeVisitor;
import org.prorefactor.proparse.ProparseErrorListener;
import org.prorefactor.proparse.ProparseErrorStrategy;
import org.prorefactor.proparse.TraceListener;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.proparse.antlr4.ProparseListener;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.proparse.support.IntegerIndex;
import org.prorefactor.proparse.support.ParserSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.progress.xref.CrossReference;
import com.progress.xref.CrossReference.Source;
import com.progress.xref.CrossReference.Source.Reference;

import eu.rssw.pct.elements.ITypeInfo;

/**
 * Executes the lexer, parser and semantic analysis, then provides access to the Abstract Syntax Tree and to the
 * SymbolScope objects.
 */
public class ParseUnit {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParseUnit.class);

  private final IProparseEnvironment session;
  private final File file;
  private final String str;
  private final InputStream input;
  private final String relativeName;

  private IntegerIndex<String> fileNameList;
  private ParseTree tree;
  private ProgramRootNode topNode;
  private IncludeRef macroGraph;
  private boolean appBuilderCode;
  private boolean syntaxError;
  private List<CodeSection> appBuilderSections;

  private TreeParserRootSymbolScope rootScope;
  private JPNodeMetrics metrics;
  private Document doc = null;
  private CrossReference xref = null;
  private ITypeInfo typeInfo = null;
  private List<Integer> trxBlocks;
  private ParserSupport support;

  // ANTLR4 debug and profiler switches
  private boolean profiler;
  private ParseInfo parseInfo;
  private boolean keepStream;
  private CommonTokenStream stream;
  private boolean trace;
  private boolean ambiguityReport;
  private boolean writableTokens;

  // Timings (in ns)
  private long parseTimeSLL;
  private long parseTimeLL;
  private long jpNodeTime;
  private long treeParseTime;
  private long xrefAttachTime;
  private boolean switchToLL;
  private int numTokens;

  private boolean isClass;
  private boolean isInterface;
  private boolean isEnum;
  private boolean isAbstract;
  private String className;

  public ParseUnit(File file, IProparseEnvironment session) {
    this(file, file.getPath(), session);
  }

  public ParseUnit(File file, String relativeName, IProparseEnvironment session) {
    this.file = file;
    this.input = null;
    this.str = null;
    this.relativeName = relativeName;
    this.session = session;
  }

  public ParseUnit(InputStream input, IProparseEnvironment session) {
    this(input, "<unnamed>", session);
  }

  public ParseUnit(InputStream input, String relativeName, IProparseEnvironment session) {
    this.file = null;
    this.input = input;
    this.str = null;
    this.relativeName = relativeName;
    this.session = session;
  }

  public ParseUnit(String str, IProparseEnvironment session) {
    this(str, "<unnamed>", session);
  }

  public ParseUnit(String str, String relativeName, IProparseEnvironment session) {
    this.file = null;
    this.input = null;
    this.str = str;
    this.relativeName = relativeName;
    this.session = session;
  }

  /**
   * Generates WritableProToken instead of immutable ProToken in the lexer phase
   */
  public void enableWritableTokens() {
    this.writableTokens = true;
  }

  /**
   * Enables profiler mode in the parsing phase. Should not be activated in production, CPU intensive
   * @see ParseUnit#getParseInfo()
   */
  public void enableProfiler() {
    profiler = true;
  }

  public void keepStream() {
    keepStream = true;
  }

  public CommonTokenStream getStream() {
    return stream;
  }

  /**
   * Enables trace mode in the parsing phase. Should not be activated in production, extremely verbose
   */
  public void enableTrace() {
    trace = true;
  }

  /**
   * Enables trace mode in the parsing phase. Should not be activated in production, CPU intensive and extremely verbose
   */
  public void reportAmbiguity() {
    ambiguityReport = true;
  }

  /**
   * @return Null if profiler is not enabled, or a ParseInfo object (after #parse() has been called)
   * @see ParseUnit#enableProfiler()
   */
  public @Nullable ParseInfo getParseInfo() {
    return parseInfo;
  }

  public @Nullable TreeParserRootSymbolScope getRootScope() {
    return rootScope;
  }

  /**
   * @return The syntax tree top node, or null if file has not been parsed
   */
  public @Nullable ProgramRootNode getTopNode() {
    return topNode;
  }

  public @Nullable JPNodeMetrics getMetrics() {
    return metrics;
  }

  public String getIncludeFileName(int index) {
    if (fileNameList == null)
      return "";
    return Strings.nullToEmpty(fileNameList.getValue(index));
  }

  /**
   * @return IncludeRef object
   */
  public @Nullable IncludeRef getMacroGraph() {
    return macroGraph;
  }

  /**
   * This is just a shortcut for calling getMacroGraph() and MacroLevel.sourceArray(). Build and return an array of the
   * MacroRef objects, which would map to the SOURCENUM attribute from JPNode. Built simply by walking the tree and
   * adding every MacroRef to the array.
   * 
   * @see org.prorefactor.macrolevel.MacroLevel#sourceArray(MacroRef)
   */
  public @Nonnull MacroRef[] getMacroSourceArray() {
    if (macroGraph == null) {
      return new MacroRef[] {};
    }
    return MacroLevel.sourceArray(macroGraph);
  }

  /**
   * Returns a TokenSource object for the main file. Include files are not expanded, and preprocessor is not used
   * 
   * @throws UncheckedIOException If main file can't be opened
   */
  public TokenSource lex() {
    return new ABLLexer(session, getByteSource(), relativeName, true);
  }

  public TokenSource preprocess() {
    ABLLexer lexer = new ABLLexer(session, getByteSource(), relativeName, false);
    if (writableTokens)
      lexer.enableWritableTokens();
    fileNameList = lexer.getFilenameList();
    macroGraph = lexer.getMacroGraph();
    appBuilderCode = ((PreprocessorEventListener) lexer.getLstListener()).isAppBuilderCode();
    appBuilderSections = ((PreprocessorEventListener) lexer.getLstListener()).getEditableCodeSections();
    metrics = lexer.getMetrics();

    return lexer;
  }

  /**
   * Generate metrics for the main file
   * 
   * @throws UncheckedIOException If main file can't be opened
   */
  public void lexAndGenerateMetrics() {
    LOGGER.trace("Entering ParseUnit#lexAndGenerateMetrics()");
    ABLLexer lexer = new ABLLexer(session, getByteSource(), relativeName, true);
    if (writableTokens)
      lexer.enableWritableTokens();
    Token tok = lexer.nextToken();
    while (tok.getType() != Token.EOF) {
      tok = lexer.nextToken();
    }
    this.metrics = lexer.getMetrics();
    LOGGER.trace("Exiting ParseUnit#lex()");
  }

  public void parse() {
    LOGGER.trace("Entering ParseUnit#parse()");

    ABLLexer lexer = new ABLLexer(session, getByteSource(), relativeName, false);
    if (writableTokens)
      lexer.enableWritableTokens();
    CommonTokenStream tokStream = new CommonTokenStream(lexer);
    Proparse parser = new Proparse(tokStream);
    parser.setTrace(false);
    if (trace) {
      parser.addParseListener(new TraceListener(parser));
    }
    parser.setProfile(profiler);
    parser.initialize(session, xref);
    if (ambiguityReport) {
      parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
      parser.addErrorListener(new DiagnosticErrorListener(true));
      long startTimeNs = System.nanoTime();
      tree = parser.program();
      parseTimeLL = System.nanoTime() - startTimeNs;
    } else {
      // Two-stage parsing
      parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
      parser.setErrorHandler(new BailErrorStrategy());
      parser.removeErrorListeners();

      long startTimeNs = System.nanoTime();
      try {
        tree = parser.program();
        parseTimeSLL = System.nanoTime() - startTimeNs;
      } catch (ParseCancellationException uncaught) {
        Token llToken = tokStream.get(tokStream.index()); // Keep reference to faulty token
        // Not really precise as it includes exception trapping
        parseTimeSLL = System.nanoTime() - startTimeNs;
        switchToLL = true;
        tokStream.seek(0);
        parser.addErrorListener(new ProparseErrorListener());
        parser.setErrorHandler(new ProparseErrorStrategy(session.getProparseSettings().allowAntlrTokenDeletion(),
            session.getProparseSettings().allowAntlrTokenInsertion(), session.getProparseSettings().allowAntlrRecover()));
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        // Another ParseCancellationException can be thrown if recover fails again (only if ProparseSettings.allowRecover is set to false)
        startTimeNs = System.nanoTime();
        try {
          tree = parser.program();
          parseTimeLL = System.nanoTime() - startTimeNs;
          // Display 'Switch to LL' log message only if parsing is successful
          LOGGER.info("File {} - Switching to LL prediction mode - Token: {}", relativeName, llToken);
        } catch (ParseCancellationException uncaught2) {
          parseTimeLL = System.nanoTime() - startTimeNs;
          syntaxError = true;
          throw uncaught2;
        }
      }
    }

    numTokens = tokStream.index();
    lexer.parseComplete();
    long startTimeNs = System.nanoTime();
    JPNodeVisitor visitor = new JPNodeVisitor(parser.getParserSupport(), (BufferedTokenStream) parser.getInputStream());
    topNode = (ProgramRootNode) visitor.visit(tree).build(this, parser.getParserSupport());
    parser.getParserSupport().clearRecordExpressions();
    isClass = visitor.isClass();
    isInterface = visitor.isInterface();
    isEnum = visitor.isEnum();
    isAbstract = visitor.isAbstractClass();
    className = visitor.getClassName();

    jpNodeTime = System.nanoTime() - startTimeNs;

    fileNameList = lexer.getFilenameList();
    macroGraph = lexer.getMacroGraph();
    appBuilderCode = ((PreprocessorEventListener) lexer.getLstListener()).isAppBuilderCode();
    appBuilderSections = ((PreprocessorEventListener) lexer.getLstListener()).getEditableCodeSections();
    metrics = lexer.getMetrics();
    support = parser.getParserSupport();

    if (profiler) {
      parseInfo = parser.getParseInfo();
    }
    if (keepStream) {
      this.stream = tokStream;
    }

    LOGGER.trace("Exiting ParseUnit#parse()");
  }

  public void treeParser01() {
    if (topNode == null)
      parse();

    long startTimeNs = System.nanoTime();
    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(new TreeParserBlocks(this), tree);
    TreeParserVariableDefinition tp02 = new TreeParserVariableDefinition(this);
    walker.walk(tp02, tree);
    TreeParserComputeReferences tp03 = new TreeParserComputeReferences(tp02);
    walker.walk(tp03, tree);
    treeParseTime = System.nanoTime() - startTimeNs;

    startTimeNs = System.nanoTime();
    finalizeXrefInfo();
    xrefAttachTime = System.nanoTime() - startTimeNs;
    xref = null; // No need to keep the entire XREF in memory
  }

  public void treeParser(ProparseListener listener) {
    if (topNode == null)
      parse();

    ParseTreeWalker walker = new ParseTreeWalker();
    long startTimeNs = System.nanoTime();
    walker.walk(listener, tree);
    treeParseTime = System.nanoTime() - startTimeNs;
  }

  public void attachXref(Document doc) {
    this.doc = doc;
  }

  public void attachXref(CrossReference xref) {
    this.xref = xref;
  }

  public void setRootScope(TreeParserRootSymbolScope scope) {
    this.rootScope = scope;
  }

  private static boolean isReferenceAssociatedToRecordNode(RecordNameNode recNode, Source src, Reference ref,
      String tableName, int tableType) {
    // On the same line number
    if (recNode.getStatement().firstNaturalChild().getLine() != ref.getLineNum())
      return false;
    // On the same table
    if ((recNode.getTableBuffer() == null) || !tableName.equalsIgnoreCase(recNode.getTableBuffer().getTargetFullName())
        || (recNode.getStoreType() != tableType))
      return false;
    // Does this statement have multiple RecordName nodes pointing to the same table ?
    // If so, we discard the Reference as it's currently not possible to assign to the right object
    List<JPNode> list = recNode.getStatement().queryCurrentStatement(ABLNodeType.RECORD_NAME);
    for (JPNode ch : list) {
      if ((ch != recNode) && (ch.getTableBuffer() != null)
          && tableName.equalsIgnoreCase(ch.getTableBuffer().getTargetFullName()))
        return false;
    }
    // In the main file ?
    if ((src.getFileNum() == 1) && (recNode.getFileIndex() == 0))
      return true;
    else {
      try {
        // Or in the same include file ?
        return ((src.getFileNum() > 1) && (recNode.getStatement().getFileName() != null) && Files.isSameFile(
            new File(src.getFileName()).toPath(), new File(recNode.getStatement().getFileName()).toPath()));
      } catch (IOException uncaught) {
        return false;
      }
    }
  }

  private void finalizeXrefInfo() {
    if ((topNode == null) || (xref == null))
      return;
    List<JPNode> recordNodes = topNode.query(ABLNodeType.RECORD_NAME);
    for (Source src : xref.getSource()) {
      File srcFile = new File(src.getFileName());
      for (Reference ref : src.getReference()) {
        if ("search".equalsIgnoreCase(ref.getReferenceType())) {
          String tableName = ref.getObjectIdentifier();
          boolean tempTable = "T".equalsIgnoreCase(ref.getTempRef());
          int tableType = tempTable ? IConstants.ST_TTABLE : IConstants.ST_DBTABLE;
          if (tempTable && (tableName.lastIndexOf(':') != -1)) {
            // Temp-table defined in classes are prefixed by the class name
            tableName = tableName.substring(tableName.lastIndexOf(':') + 1);
          }
          if (!tempTable && (tableName.indexOf("._") != -1)) {
            // DBName._Metaschema -> skip
            continue;
          }

          boolean lFound = false;
          for (JPNode node : recordNodes) {
            RecordNameNode recNode = (RecordNameNode) node;
            if (isReferenceAssociatedToRecordNode(recNode, src, ref, tableName, tableType)) {
              recNode.setWholeIndex("WHOLE-INDEX".equals(ref.getDetail()));
              recNode.setSearchIndexName(recNode.getTableBuffer().getTable().getName() + "." + ref.getObjectContext());
              lFound = true;
              break;
            }
          }
          if (!lFound && "WHOLE-INDEX".equals(ref.getDetail())) {
            LOGGER.debug("WHOLE-INDEX search on '{}' with index '{}' couldn't be assigned to {} at line {}", tableName,
                ref.getObjectContext(), srcFile.getPath(), ref.getLineNum());
          }
        } else if ("sort-access".equalsIgnoreCase(ref.getReferenceType())) {
          String tableName = ref.getObjectIdentifier();
          boolean tempTable = "T".equalsIgnoreCase(ref.getTempRef());
          int tableType = tempTable ? IConstants.ST_TTABLE : IConstants.ST_DBTABLE;
          if (tempTable && (tableName.lastIndexOf(':') != -1)) {
            tableName = tableName.substring(tableName.lastIndexOf(':') + 1);
          }
          if (!tempTable && (tableName.indexOf("._") != -1)) {
            // DBName._Metaschema -> skip
            continue;
          }

          for (JPNode node : recordNodes) {
            RecordNameNode recNode = (RecordNameNode) node;
            if (isReferenceAssociatedToRecordNode(recNode, src, ref, tableName, tableType)) {
              recNode.setSortAccess(ref.getObjectContext());
              break;
            }
          }
        }
      }
    }
  }

  public void attachTypeInfo(ITypeInfo unit) {
    this.typeInfo = unit;
  }

  public void attachTransactionBlocks(List<Integer> blocks) {
    this.trxBlocks = blocks;
  }

  @Nullable
  public Document getXRefDocument() {
    return doc;
  }

  @Nullable
  public CrossReference getXref() {
    return xref;
  }

  @Nullable
  public ITypeInfo getTypeInfo() {
    return typeInfo;
  }

  public List<Integer> getTransactionBlocks() {
    return trxBlocks;
  }

  public ParseTree getParseTree() {
    return tree;
  }

  public ParserSupport getSupport() {
    return support;
  }

  public IProparseEnvironment getSession() {
    return session;
  }

  public boolean isAppBuilderCode() {
    return appBuilderCode;
  }

  public long getParseTimeLL() {
    return parseTimeLL / 1000000;
  }

  public long getParseTimeSLL() {
    return parseTimeSLL / 1000000;
  }

  public long getJpNodeTime() {
    return jpNodeTime / 1000000;
  }

  public long getXrefAttachTime() {
    return xrefAttachTime / 1000000;
  }

  public long getTreeParseTime() {
    return treeParseTime / 1000000;
  }

  public boolean hasSwitchedToLL() {
    return switchToLL;
  }

  public boolean hasSyntaxError() {
    return syntaxError;
  }

  public boolean isClass() {
    return isClass;
  }

  public boolean isInterface() {
    return isInterface;
  }

  public boolean isEnum() {
    return isEnum;
  }

  public boolean isAbstractClass() {
    return isAbstract;
  }

  public String getClassName() {
    return className;
  }

  public boolean isInEditableSection(int file, int line) {
    if (!appBuilderCode || (file > 0))
      return true;
    for (CodeSection range : appBuilderSections) {
      if ((range.getFileNum() == file) && (range.getStartLine() <= line) && (range.getEndLine() >= line))
        return true;
    }
    return false;
  }

  private ByteSource getByteSource() {
    if (str != null) {
      return ByteSource.wrap(str.getBytes(session.getCharset()));
    }
    try (InputStream s = input == null ? new FileInputStream(file) : input) {
      if (s.markSupported())
        s.reset();
      return ByteSource.wrap(ByteStreams.toByteArray(s));
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }
  }

  public static void printLongestRules(ParseInfo parseInfo) {
    Arrays.stream(parseInfo.getDecisionInfo()).filter(decision -> decision.SLL_MaxLook > 10).sorted(
        (d1, d2) -> Long.compare(d2.SLL_MaxLook, d1.SLL_MaxLook)).forEach(
            decision -> System.out.println(String.format(
                "Time: %d in %d calls - LL_Lookaheads: %d - Max k: %d - Ambiguities: %d - Errors: %d - Rule: %s - Code: %s",
                decision.timeInPrediction / 1000000, decision.invocations, decision.SLL_TotalLook, decision.SLL_MaxLook,
                decision.ambiguities.size(), decision.errors.size(),
                Proparse.ruleNames[Proparse._ATN.getDecisionState(decision.decision).ruleIndex],
                getCodeFromMaxLookEvent(decision))));
  }

  public static String getCodeFromMaxLookEvent(DecisionInfo info) {
    StringBuilder bldr = new StringBuilder();
    for (int zz = info.SLL_MaxLookEvent.startIndex; zz <= info.SLL_MaxLookEvent.stopIndex; zz++) {
      if (info.SLL_MaxLookEvent.input.get(zz).getChannel() == Token.DEFAULT_CHANNEL)
        bldr.append(info.SLL_MaxLookEvent.input.get(zz).getText()).append(' ');
    }

    return bldr.toString();
  }

}
