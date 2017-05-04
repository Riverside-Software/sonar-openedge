/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
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
import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.MacroLevel;
import org.prorefactor.macrolevel.MacroRef;
import org.prorefactor.proparse.ProParser;
import org.prorefactor.proparse.antlr4.ProgressLexer;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser01.ITreeParserAction;
import org.prorefactor.treeparser01.TreeParser01;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;

/**
 * Provides parse unit information, such as the symbol table and a reference to the AST. TreeParser01 calls
 * symbolUsage() in this class in order to build the symbol table.
 * 
 * Main methods : parse() and getTopNode()
 */
public class ParseUnit {
  private static final Logger LOGGER = LoggerFactory.getLogger(ParseUnit.class);

  private final RefactorSession session;
  private File file;
  private IncludeRef macroGraph = null;
  private ProgramRootNode topNode;
  private SymbolScopeRoot rootScope;
  private JPNodeMetrics metrics;

  public ParseUnit(File file, RefactorSession prsession) {
    this.file = file;
    this.session = prsession;
  }

  public SymbolScopeRoot getRootScope() {
    return rootScope;
  }

  public void setRootScope(SymbolScopeRoot rootScope) {
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
    if (file == null) {
      // A lot of old code starts with a string filename, sends that to Proparse, gets the top node
      // handle, builds JPNode, and then runs TreeParser01 from that. (All the stuff ParseUnit does
      // now.) In those cases, this ParseUnit might have been created as an empty shell by TreeParser01
      // itself, and "file" would not be set. In that case, we attempt to find the File from the file index.
      if (topNode == null)
        return null;
      file = new File(topNode.getFilenames()[0]);
    }
    return file;
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

  public TokenStream lex() throws IOException, RefactorException {
    ProgressLexer lexer = new ProgressLexer(session, file.getPath());
    return lexer.getANTLR2TokenStream(false);
  }

  public void lexAndGenerateMetrics() throws RefactorException {
    LOGGER.trace("Entering ParseUnit#lexAndGenerateMetrics()");
    try {
      ProgressLexer lexer = new ProgressLexer(session, file.getPath());
      TokenStream stream = lexer.getANTLR2TokenStream(false);
      try {
        Token tok = stream.nextToken();
        while (tok.getType() != Token.EOF_TYPE) {
          tok = stream.nextToken();
        }
      } catch (TokenStreamException uncaught) {

      }
      this.metrics = lexer.getMetrics();
      LOGGER.trace("Exiting ParseUnit#lex()");
    } catch (IOException caught) {
      throw new RefactorException(caught);
    }
  }

  public void parse() throws RefactorException {
    LOGGER.trace("Entering ParseUnit#parse()");
    
    try {
      ProgressLexer lexer = new ProgressLexer(session, file.getPath());
      ProParser parser = new ProParser(lexer.getANTLR2TokenStream(true));
      parser.initAntlr4(session, lexer.getFilenameList());
      parser.program();
      ((JPNode) parser.getAST()).backLink();

      // Deal with trailing hidden tokens
      JPNode.finalizeTrailingHidden((JPNode) parser.getAST());
      lexer.parseComplete();

      macroGraph = lexer.getMacroGraph();
      setTopNode((JPNode) parser.getAST());
      this.metrics = lexer.getMetrics();
    } catch (ANTLRException | IOException caught) {
      throw new RefactorException(caught);
    }
    LOGGER.trace("Exiting ParseUnit#parse()");
  }

  /**
   * Run any IJPTreeParser against the AST. This will call parse() if the JPNode AST has not already been built.
   */
  public void treeParser(IJPTreeParser tp) throws RefactorException {
    LOGGER.trace("Entering ParseUnit#treeParser()");
    if (this.getTopNode() == null) {
      parse();
    }
    try {
      tp.program(getTopNode());
    } catch (RecognitionException | TreeParserException caught) {
      throw new RefactorException(caught);
    }
    LOGGER.trace("Exiting ParseUnit#treeParser()");
  }

  /**
   * Run TreeParser01. Takes care of calling parse() first, if that has not already been done.
   */
  public void treeParser01() throws RefactorException {
    LOGGER.trace("Entering ParseUnit#treeParser01()");
    if (this.getTopNode() == null) {
      parse();
    }
    TreeParser01 tp = new TreeParser01(session);
    tp.getActionObject().setParseUnit(this);
    treeParser(tp);
    LOGGER.trace("Exiting ParseUnit#treeParser01()");
  }

  /**
   * Run TreeParser01 with any TP01Action object. Takes care of calling parse() first, if that has not already been
   * done.
   */
  public void treeParser01(ITreeParserAction action) throws RefactorException {
    if (this.getTopNode() == null)
      parse();
    TreeParser01 tp = new TreeParser01(session, action);
    tp.getActionObject().setParseUnit(this);
    treeParser(tp);
  }

}
