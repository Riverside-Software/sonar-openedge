/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
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
import org.prorefactor.macrolevel.PreprocessorEventListener.EditableCodeSection;
import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.proparse.ParserSupport;
import org.prorefactor.proparse.antlr4.DescriptiveErrorListener;
import org.prorefactor.proparse.antlr4.JPNodeVisitor;
import org.prorefactor.proparse.antlr4.ProgressLexer;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.proparse.antlr4.ProparseErrorStrategy;
import org.prorefactor.proparse.antlr4.TreeParser;
import org.prorefactor.refactor.RefactorSession;
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
 * Provides parse unit information, such as the symbol table and a reference to the AST. TreeParser01 calls
 * symbolUsage() in this class in order to build the symbol table.
 */
public class ParseUnit {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParseUnit.class);

  private final RefactorSession session;
  private final File file;
  private final InputStream input;
  private final String relativeName;

  private IntegerIndex<String> fileNameList;
  private ParseTree tree;
  private ProgramRootNode topNode;
  private IncludeRef macroGraph;
  private boolean appBuilderCode;
  private List<EditableCodeSection> sections;
  private TreeParserRootSymbolScope rootScope;
  private JPNodeMetrics metrics;
  private Document doc = null;
  private CrossReference xref = null;
  private ITypeInfo typeInfo = null;
  private List<Integer> trxBlocks;
  private ParserSupport support;

  public ParseUnit(File file, RefactorSession session) {
    this(file, file.getPath(), session);
  }

  public ParseUnit(File file, String relativeName, RefactorSession session) {
    this.file = file;
    this.input = null;
    this.relativeName = relativeName;
    this.session = session;
  }

  public ParseUnit(InputStream input, String relativeName, RefactorSession session) {
    this.file = null;
    this.input = input;
    this.relativeName = relativeName;
    this.session = session;
  }

  public TreeParserRootSymbolScope getRootScope() {
    return rootScope;
  }

  public void setRootScope(TreeParserRootSymbolScope rootScope) {
    this.rootScope = rootScope;
  }

  /** Get the syntax tree top (Program_root) node */
  public ProgramRootNode getTopNode() {
    return topNode;
  }

  public JPNodeMetrics getMetrics() {
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
    return new ProgressLexer(session, getByteSource(), relativeName, true);
  }

  public TokenSource preprocess() {
    ProgressLexer lexer = new ProgressLexer(session, getByteSource(), relativeName, false);
    fileNameList = lexer.getFilenameList();
    macroGraph = lexer.getMacroGraph();
    appBuilderCode = ((PreprocessorEventListener) lexer.getLstListener()).isAppBuilderCode();
    sections = ((PreprocessorEventListener) lexer.getLstListener()).getEditableCodeSections();
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
    ProgressLexer lexer = new ProgressLexer(session, getByteSource(), relativeName, true);
    Token tok = lexer.nextToken();
    while (tok.getType() != Token.EOF) {
      tok = lexer.nextToken();
    }
    this.metrics = lexer.getMetrics();
    LOGGER.trace("Exiting ParseUnit#lex()");
  }

  public void parse() {
    LOGGER.trace("Entering ParseUnit#parse()");

    ProgressLexer lexer = new ProgressLexer(session, getByteSource(), relativeName, false);
    Proparse parser = new Proparse(new CommonTokenStream(lexer));
    parser.initAntlr4(session);
    parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
    parser.setErrorHandler(new BailErrorStrategy());
    parser.removeErrorListeners();
    parser.addErrorListener(new DescriptiveErrorListener());

    try {
      tree = parser.program();
    } catch (ParseCancellationException caught) {
      parser.setErrorHandler(new ProparseErrorStrategy());
      parser.getInterpreter().setPredictionMode(PredictionMode.LL);
      tree = parser.program();
    }
    lexer.parseComplete();
    topNode = (ProgramRootNode) new JPNodeVisitor(parser.getParserSupport(),
        (BufferedTokenStream) parser.getInputStream()).visit(tree).build(parser.getParserSupport());

    fileNameList = lexer.getFilenameList();
    macroGraph = lexer.getMacroGraph();
    appBuilderCode = ((PreprocessorEventListener) lexer.getLstListener()).isAppBuilderCode();
    sections = ((PreprocessorEventListener) lexer.getLstListener()).getEditableCodeSections();
    metrics = lexer.getMetrics();
    support = parser.getParserSupport();

    LOGGER.trace("Exiting ParseUnit#parse()");
  }

  public void treeParser01() {
    if (topNode == null)
      parse();
    ParseTreeWalker walker = new ParseTreeWalker();
    TreeParser parser = new TreeParser(support, session);
    walker.walk(parser, tree);
    rootScope = parser.getRootScope();
  }

  public void attachXref(Document doc) {
    this.doc = doc;
  }

  public void attachXref(CrossReference xref) {
    this.xref = xref;
    if (xref == null)
      return;
    attachXrefToTreeParser(getTopNode(), xref);
  }

  public static void attachXrefToTreeParser(ProgramRootNode root, CrossReference xref) {
    List<JPNode> recordNodes = root.query(ABLNodeType.RECORD_NAME);
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
            try {
              if ((recNode.getStatement().getLine() == ref.getLineNum())
                  && tableName.equalsIgnoreCase(recNode.getTableBuffer().getTargetFullName())
                  && (recNode.attrGet(IConstants.STORETYPE) == tableType)
                  && ((src.getFileNum() == 1 && recNode.getFileIndex() == 0)
                      || Files.isSameFile(srcFile.toPath(), new File(recNode.getStatement().getFileName()).toPath()))) {
                recNode.setLink(IConstants.WHOLE_INDEX, "WHOLE-INDEX".equals(ref.getDetail()));
                recNode.setLink(IConstants.SEARCH_INDEX_NAME, ref.getObjectContext());
                lFound = true;
                break;
              }
            } catch (IOException uncaught) {
              // Nothing
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
            try {
              if ((recNode.getStatement().getLine() == ref.getLineNum())
                  && tableName.equalsIgnoreCase(recNode.getTableBuffer().getTargetFullName())
                  && (recNode.attrGet(IConstants.STORETYPE) == tableType)
                  && ((src.getFileNum() == 1 && recNode.getFileIndex() == 0)
                      || Files.isSameFile(srcFile.toPath(), new File(recNode.getStatement().getFileName()).toPath()))) {
                recNode.setSortAccess(ref.getObjectContext());
                break;
              }
            } catch (IOException uncaught) {
              // Nothing
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

  public RefactorSession getSession() {
    return session;
  }

  public boolean isAppBuilderCode() {
    return appBuilderCode;
  }

  public boolean isInEditableSection(int file, int line) {
    if (!appBuilderCode || (file > 0))
      return true;
    for (EditableCodeSection range : sections) {
      if ((range.getFileNum() == file) && (range.getStartLine() <= line) && (range.getEndLine() >= line))
        return true;
    }
    return false;
  }

  private ByteSource getByteSource() {
    try (InputStream stream = input == null ? new FileInputStream(file) : input) {
      return ByteSource.wrap(ByteStreams.toByteArray(stream));
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }
  }

}
