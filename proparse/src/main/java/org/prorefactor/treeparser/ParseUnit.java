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
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.MacroLevel;
import org.prorefactor.macrolevel.MacroRef;
import org.prorefactor.macrolevel.PreprocessorEventListener;
import org.prorefactor.macrolevel.PreprocessorEventListener.EditableCodeSection;
import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.proparse.ParserSupport;
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
  private ProgramRootNode topNode;
  private IncludeRef macroGraph;
  private boolean appBuilderCode;
  private List<EditableCodeSection> sections;
  private TreeParserRootSymbolScope rootScope;
  private JPNodeMetrics metrics;
  private Document xref = null;
  private ITypeInfo typeInfo = null;
  private List<Integer> trxBlocks;
  // TEMP-ANTLR4
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
  public TokenSource lex4() {
    return new ProgressLexer(session, getInputStream(), relativeName, true);
  }

  public TokenSource preprocess4() {
    return new ProgressLexer(session, getInputStream(), relativeName, false);
  }

  /**
   * Returns a TokenStream object for the main file. Include files are not expanded, and preprocessor is not used
   * 
   * @throws UncheckedIOException If main file can't be opened
   */
  public TokenStream lex() {
    ProgressLexer lexer = new ProgressLexer(session, getInputStream(), relativeName, true);
    return lexer.getANTLR2TokenStream(false);
  }

  public TokenStream preprocess() {
    ProgressLexer lexer = new ProgressLexer(session, getInputStream(), relativeName, false);
    return lexer.getANTLR2TokenStream(true);
  }

  /**
   * Generate metrics for the main file
   * 
   * @throws UncheckedIOException If main file can't be opened
   */
  public void lexAndGenerateMetrics() {
    LOGGER.trace("Entering ParseUnit#lexAndGenerateMetrics()");
    ProgressLexer lexer = new ProgressLexer(session, getInputStream(), relativeName, true);
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
    
    ProgressLexer lexer = new ProgressLexer(session, getInputStream(), relativeName, false);
    ProParser parser = new ProParser(lexer.getANTLR2TokenStream(true));
    parser.initAntlr4(session, lexer.getFilenameList());
    parser.program();
    ((ProgramRootNode) parser.getAST()).backLinkAndFinalize();
    lexer.parseComplete();

    fileNameList = lexer.getFilenameList();
    macroGraph = lexer.getMacroGraph();
    appBuilderCode = ((PreprocessorEventListener) lexer.getLstListener()).isAppBuilderCode();
    sections = ((PreprocessorEventListener) lexer.getLstListener()).getEditableCodeSections();
    metrics = lexer.getMetrics();
    topNode = (ProgramRootNode) parser.getAST();
    support = parser.support;

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
    TreeParser01 tp = new TreeParser01(action);
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
    TreeParser01 tp = new TreeParser01(action);
    treeParser(tp);
  }

  public void attachXref(Document xref) {
    this.xref = xref;
  }

  public void attachTypeInfo(ITypeInfo unit) {
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
  public ITypeInfo getTypeInfo() {
    return typeInfo;
  }

  public List<Integer> getTransactionBlocks() {
    return trxBlocks;
  }

  // TEMP-ANTLR4
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

  /**
   * Careful, returned object can be closed
   */
  private InputStream getInputStream() {
    try {
      if (input == null)
        return new FileInputStream(file);
      else
        return input;
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }
  }
}
