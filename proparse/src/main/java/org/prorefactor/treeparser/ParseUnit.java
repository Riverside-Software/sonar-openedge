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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.ListingParser;
import org.prorefactor.macrolevel.MacroLevel;
import org.prorefactor.macrolevel.MacroRef;
import org.prorefactor.proparse.DoParse;
import org.prorefactor.refactor.FileStuff;
import org.prorefactor.refactor.PUB;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser01.ITreeParserAction;
import org.prorefactor.treeparser01.TreeParser01;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStream;

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
  private PUB pub = null;
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
    if (topNode == null && pub != null)
      setTopNode(pub.getTree());
    return topNode;
  }

  public JPNodeMetrics getMetrics() {
    return metrics;
  }

  /** Set the syntax tree top (Program_root) node. */
  public void setTopNode(JPNode topNode) {
    this.topNode = (ProgramRootNode) topNode;
  }

  /**
   * Get or create a PUB
   */
  public PUB getPUB() {
    if (pub == null) {
      pub = new PUB(session, FileStuff.fullpath(getFile()));
      pub.setParseUnit(this);
    }
    return pub;
  }

  public ParseUnit setPUB(PUB pub) {
    this.pub = pub;
    if (pub.getParseUnit() != this)
      pub.setParseUnit(this);
    return this;
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
   * Get the file index, either from the PUB file or from the parser, whichever was used to get the tree. The return is
   * the array of file names. The file at index zero is always the compile unit. The others are include files. The array
   * index position corresponds to JPNode.getFileIndex().
   * 
   * @see org.prorefactor.core.nodetypes.ProgramRootNode#getFilenames()
   */
  public String[] getFileIndex() {
    if (topNode == null)
      return null;
    return topNode.getFilenames();
  }

  /** This will trigger a parse if the PUB is out of date. */
  public IncludeRef getMacroGraph() throws RefactorException, IOException {
    if (macroGraph != null)
      return macroGraph;
    getPUB();
    if (!pub.isChecked())
      pub.loadTo(PUB.HEADER);
    if (!pub.isCurrent()) {
      // Does the parse, which causes the macroGraph to be built and written to disc.
      pub.build();
    } else {
      try {
        FileInputStream fileIn = new FileInputStream(macroGraphFile());
        ObjectInputStream in = new ObjectInputStream(fileIn);
        macroGraph = (IncludeRef) in.readObject();
        in.close();
        fileIn.close();
      } catch (Exception e) {
        throw new RefactorException(e);
      }
    }
    return macroGraph;
  }

  /**
   * This is just a shortcut for calling getMacroGraph() and MacroLevel.sourceArray(). Build and return an array of the
   * MacroRef objects, which would map to the SOURCENUM attribute from JPNode. Built simply by walking the tree and
   * adding every MacroRef to the array.
   * 
   * @see org.prorefactor.macrolevel.MacroLevel#sourceArray(MacroRef)
   */
  public MacroRef[] getMacroSourceArray() throws RefactorException, IOException {
    return MacroLevel.sourceArray(getMacroGraph());
  }

  /**
   * Load from PUB, or build PUB if it's out of date. TreeParser01 is run in order to build the PUB. If the PUB was up
   * to date, then TreeParser01 is run after the PUB is loaded. (Either way, the symbol tables etc. are available.)
   */
  public void loadOrBuildPUB() throws RefactorException, IOException {
    getPUB();
    if (pub.load()) {
      setTopNode(pub.getTree());
      treeParser01();
    } else {
      pub.build();
    }
  }

  private File macroGraphFile() {
    // .msg = Macro Source Graph. Common source of heartburn.
    return new File(PUB.pubDirFileName(session, file.getAbsolutePath()) + ".msg");
  }

  public TokenStream lex() throws RefactorException {
    LOGGER.trace("Entering ParseUnit#lex()");
    DoParse doParse = new DoParse(session, file.getPath());
    try {
      doParse.doParse(true);
    } catch (ANTLRException | IOException caught) {
      throw new RefactorException(caught);
    }
    this.metrics = doParse.getMetrics();
    LOGGER.trace("Exiting ParseUnit#lex()");

    return doParse.getLexerTokenStream();
  }

  public void parse() throws RefactorException {
    LOGGER.trace("Entering ParseUnit#parse()");
    session.enableParserListing();
    DoParse doParse = new DoParse(session, file.getPath());
    try {
      doParse.doParse();
      ListingParser listingParser = new ListingParser(session.getListingFileName());
      listingParser.parse();
      macroGraph = listingParser.getRoot();
      if (session.getProjectBinariesEnabled()) {
        LOGGER.trace("ParseUnit#parse() - Writing PUB");
        File macroGraphFile = macroGraphFile();
        macroGraphFile.getParentFile().mkdirs();
        FileOutputStream fileOut = new FileOutputStream(macroGraphFile);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(macroGraph);
        out.close();
        fileOut.close();
      }
    } catch (ANTLRException | IOException caught) {
      throw new RefactorException(caught);
    }
    setTopNode(doParse.getTopNode());
    this.metrics = doParse.getMetrics();
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
    action.setParseUnit(this);
    treeParser(tp);
  }

}
