/*******************************************************************************
* * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.prorefactor.treeparser;

import java.io.File;
import java.io.UncheckedIOException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.MacroLevel;
import org.prorefactor.macrolevel.MacroRef;
import org.prorefactor.macrolevel.PreprocessorEventListener;
import org.prorefactor.macrolevel.PreprocessorEventListener.EditableCodeSection;
import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.proparse.ProParser;
import org.prorefactor.proparse.antlr4.ProgressLexer;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser01.ITreeParserAction;
import org.prorefactor.treeparser01.TP01Support;
import org.prorefactor.treeparser01.TreeParser01;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.google.common.base.Strings;

import antlr.ANTLRException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import eu.rssw.pct.TypeInfo;

/**
 * Provides parse unit information, such as the symbol table and a reference to the AST. TreeParser01 calls
 * symbolUsage() in this class in order to build the symbol table.
 */
public class ParseUnit {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParseUnit.class);

  private final RefactorSession session;
  private final File file;
  private final IntegerIndex<String> fileNameList;

  private ProgramRootNode topNode;
  private IncludeRef macroGraph;
  private boolean appBuilderCode;
  private List<EditableCodeSection> sections;
  private TreeParserRootSymbolScope rootScope;
  private JPNodeMetrics metrics;
  private Document xref = null;
  private TypeInfo typeInfo = null;
  private List<Integer> trxBlocks;

  public ParseUnit(File file, RefactorSession prsession) {
    this.file = file;
    this.session = prsession;
    this.fileNameList = new IntegerIndex<>();
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

  /** Set the syntax tree top (Program_root) node. */
  public void setTopNode(JPNode topNode) {
    this.topNode = (ProgramRootNode) topNode;
  }

  public File getFile() {
    return file;
  }

  public String getIncludeFileName(int index) {
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
  public TokenSource lex4() {
    return new ProgressLexer(session, file.getPath(), fileNameList, true);
  }

  public TokenSource preprocess4() {
    return new ProgressLexer(session, file.getPath(), fileNameList, false);
  }

  /**
   * Returns a TokenStream object for the main file. Include files are not expanded, and preprocessor is not used
   * 
   * @throws UncheckedIOException If main file can't be opened
   */
  public TokenStream lex() {
    ProgressLexer lexer = new ProgressLexer(session, file.getPath(), fileNameList, true);
    return lexer.getANTLR2TokenStream(false);
  }

  public TokenStream preprocess() {
    ProgressLexer lexer = new ProgressLexer(session, file.getPath(), fileNameList, false);
    return lexer.getANTLR2TokenStream(false);
  }

  /**
   * Generate metrics for the main file
   * 
   * @throws UncheckedIOException If main file can't be opened
   */
  public void lexAndGenerateMetrics() {
    LOGGER.trace("Entering ParseUnit#lexAndGenerateMetrics()");
    ProgressLexer lexer = new ProgressLexer(session, file.getPath(), fileNameList, true);
    TokenStream stream = lexer.getANTLR2TokenStream(false);
    try {
      Token tok = stream.nextToken();
      while (tok.getType() != Token.EOF_TYPE) {
        tok = stream.nextToken();
      }
    } catch (TokenStreamException uncaught) {
      // Never thrown
    }
    this.metrics = lexer.getMetrics();
    LOGGER.trace("Exiting ParseUnit#lex()");
  }

  public void parse() throws ANTLRException {
    LOGGER.trace("Entering ParseUnit#parse()");
    
    ProgressLexer lexer = new ProgressLexer(session, file.getPath(), fileNameList, false);
    ProParser parser = new ProParser(lexer.getANTLR2TokenStream(true));
    parser.initAntlr4(session, lexer.getFilenameList());
    parser.program();
    ((ProgramRootNode) parser.getAST()).backLinkAndFinalize();
    lexer.parseComplete();

    macroGraph = lexer.getMacroGraph();
    appBuilderCode = ((PreprocessorEventListener) lexer.getLstListener()).isAppBuilderCode();
    sections = ((PreprocessorEventListener) lexer.getLstListener()).getEditableCodeSections();
    metrics = lexer.getMetrics();
    setTopNode((JPNode) parser.getAST());
    

    LOGGER.trace("Exiting ParseUnit#parse()");
  }

  /**
   * Run any IJPTreeParser against the AST. This will call parse() if the JPNode AST has not already been built.
   */
  public void treeParser(IJPTreeParser tp) throws ANTLRException {
    LOGGER.trace("Entering ParseUnit#treeParser()");
    if (this.getTopNode() == null)
      parse();
    tp.program(getTopNode());
    LOGGER.trace("Exiting ParseUnit#treeParser()");
  }

  /**
   * Run TreeParser01. Takes care of calling parse() first, if that has not already been done.
   */
  public void treeParser01() throws ANTLRException {
    LOGGER.trace("Entering ParseUnit#treeParser01()");
    if (this.getTopNode() == null)
      parse();
    ITreeParserAction action = new TP01Support(session, this);
    TreeParser01 tp = new TreeParser01(session, action);
    treeParser(tp);
    LOGGER.trace("Exiting ParseUnit#treeParser01()");
  }

  /**
   * Run TreeParser01 with any TP01Action object. Takes care of calling parse() first, if that has not already been
   * done.
   */
  public void treeParser01(ITreeParserAction action) throws ANTLRException {
    if (this.getTopNode() == null)
      parse();
    TreeParser01 tp = new TreeParser01(session, action);
    treeParser(tp);
  }

  public void attachXref(Document xref) {
    this.xref = xref;
  }

  public void attachTypeInfo(TypeInfo unit) {
    this.typeInfo = unit;
  }

  public void attachTransactionBlocks(List<Integer> blocks) {
    this.trxBlocks = blocks;
  }

  @Nullable
  public Document getXref() {
    return xref;
  }

  @Nullable
  public TypeInfo getTypeInfo() {
    return typeInfo;
  }

  public List<Integer> getTransactionBlocks() {
    return trxBlocks;
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

}
