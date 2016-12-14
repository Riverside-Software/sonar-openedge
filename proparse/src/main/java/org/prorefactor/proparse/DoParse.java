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
package org.prorefactor.proparse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.refactor.RefactorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamHiddenTokenFilter;

public class DoParse {
  private static final Logger LOGGER = LoggerFactory.getLogger(DoParse.class);

  private final RefactorSession session;
  private final String fileName;
  private final DoParse primary;

  private TokenStreamHiddenTokenFilter filter;
  private boolean proEval = false;
  private IntegerIndex<String> filenameList = new IntegerIndex<>();
  
  BufferedReader inStream;
  ProParser parser;
  boolean preProcessCondition = false;
  boolean preProcessConditionResult = false;
  private JPNodeMetrics metrics;
  private IncludeRef macroGraph;

  public DoParse(RefactorSession sess, String fileName) {
    this(sess, fileName, null);
  }

  DoParse(RefactorSession sess, String filename, DoParse primary) {
    this.session = sess;
    this.fileName = filename;
    this.primary = primary;
  }

  public ParserSupport getParserSupport() {
    return (parser == null ? null : parser.support);
  }

  public JPNode getTopNode() {
    return (JPNode) parser.getAST();
  }

  public RefactorSession getRefactorSession() {
    return session;
  }

  // A reference to the collection of filenames from the parse
  public IntegerIndex<String> getFilenameList() {
    return filenameList;
  }

  public String getFilename(int fileIndex) {
    return filenameList.getValue(fileIndex);
  }

  protected int addFilename(String filename) {
    return filenameList.add(filename);
  }

  protected boolean isValidIndex(int index) {
    return filenameList.hasIndex(index);
  }

  public TokenStream getLexerTokenStream() {
    return filter;
  }

  protected void doParse(List<ProToken> tokenVector) throws IOException, TokenStreamException, RecognitionException {
    doParse(false, new TokenVectorIterator(tokenVector, this));
  }

  public void doParse() throws IOException, TokenStreamException, RecognitionException {
    doParse(false, null);
  }

  public void doParse(boolean justLex) throws IOException, TokenStreamException, RecognitionException {
    doParse(justLex, null);
  }

  public void doParse(boolean justLex, TokenVectorIterator tvi) throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering DoParse#doParse()");
    if (fileName != null) {
      inStream = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), session.getCharset()));
    }
    Preprocessor prepro = new Preprocessor(fileName, inStream, this);

    try  {
      if (fileName != null) {
        addFilename(fileName);
      }

      // Parsing either a token vector or else regular input stream
      if (tvi != null) {
        LOGGER.trace("Using TokenVectorIterator filter");
        filter = new TokenStreamHiddenTokenFilter(tvi);
      } else {
        LOGGER.trace("Creating Lexer / PostLexer objects");
        Lexer lexer = new Lexer(prepro);
        Postlexer postlexer = new Postlexer(prepro, lexer, this);
        TokenList tokenlist = new TokenList(postlexer);
        tokenlist.build();
        filter = new TokenStreamHiddenTokenFilter(tokenlist);
        metrics = new JPNodeMetrics(lexer.getLoc(), lexer.getCommentedLines());
      }

      // If we're just lexing, let's see the "hidden" tokens too.
      // Otherwise, filter.
      if (!justLex) {
        filter.hide(NodeTypes.WS);
        filter.hide(NodeTypes.COMMENT);
        filter.hide(NodeTypes.AMPMESSAGE);
        filter.hide(NodeTypes.AMPANALYZESUSPEND);
        filter.hide(NodeTypes.AMPANALYZERESUME);
        filter.hide(NodeTypes.AMPGLOBALDEFINE);
        filter.hide(NodeTypes.AMPSCOPEDDEFINE);
        filter.hide(NodeTypes.AMPUNDEFINE);
      }

      if (justLex) {
        return;
      }

      // Create the parser, with the filter as the input.
      parser = new ProParser(filter);
      parser.init(this);

      // Now parse the token stream
      // We might be:
      // - doing an eval of a Progress code chunk
      // - evaluating an &IF preprocessor condition
      // - just doing a regular parse
      if (proEval) {
        LOGGER.trace("Executing ProEval parser on code chunck");
        parser.program();
        ProEval proeval = new ProEval(session.getProparseSettings());
        proeval.program(parser.getAST());
      } else if (preProcessCondition) {
        LOGGER.trace("Executing ProEval parser on preprocessor");
        parser.expression();
        ProEval proeval = new ProEval(session.getProparseSettings());
        preProcessConditionResult = proeval.preproIfEval(parser.getAST());
      } else {
        LOGGER.trace("Executing ProParser");
        parser.program();
        JPNode topNode = (JPNode) parser.getAST();
        backLink(topNode);
        // Deal with trailing hidden tokens
        JPNode.finalizeTrailingHidden((JPNode) parser.getAST());
      }

    } finally {
      // List all file indexes.
      int i = 0;
      while (isValidIndex(i)) {
        prepro.getLstListener().fileIndex(i, getFilename(i));
        ++i;
      }
      // Tell the preprocessor we're done. Releases file handles, etc.
      prepro.parseComplete();
      macroGraph = prepro.getMacroGraph();
      if ((primary == null) && !session.getProparseSettings().isMultiParse()) {
        session.clearSuperCache();
      }
    }
    LOGGER.trace("Entering DoParse#doParse()");
  }

  public JPNodeMetrics getMetrics() {
    return metrics;
  }

  public IncludeRef getMacroGraph() {
    return macroGraph;
  }

  /**
   * Set parent and prevSibling links
   */
  private void backLink(JPNode r) {
    JPNode currNode = r.firstChild();
    while (currNode != null) {
      currNode.setParent(r);
      backLink(currNode);
      JPNode nextNode = currNode.nextSibling();
      if (nextNode != null)
        nextNode.setPrevSibling(currNode);
      currNode = nextNode;
    }
  }

  private static class TokenVectorIterator implements TokenStream {
    private List<ProToken> tokenVector = new ArrayList<>();
    private int it = 0;
    private DoParse doParse;

    TokenVectorIterator(List<ProToken> tokenVector, DoParse doParse) {
      this.tokenVector = tokenVector;
      this.doParse = doParse;
    }

    @Override
    public Token nextToken() {
      if (it >= tokenVector.size())
        return new ProToken(doParse.getFilenameList(), NodeTypes.EOF, "");

      return tokenVector.get(it++);
    }
  }

}
