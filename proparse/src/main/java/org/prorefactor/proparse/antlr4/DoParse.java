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
package org.prorefactor.proparse.antlr4;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.tree.ParseTree;
//import org.prorefactor.core.JPNode;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.refactor.RefactorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public class DoParse {
  private static final Logger LOGGER = LoggerFactory.getLogger(DoParse.class);

  private final RefactorSession session;
  private final String fileName;
  private final DoParse primary;

  private TokenSource filter;
  private boolean proEval = false;
  private int nextNodeNum;
  private IntegerIndex<String> filenameList = new IntegerIndex<>();
  
  BufferedReader inStream;
  boolean preProcessCondition = false;
  boolean preProcessConditionResult = false;
  private JPNodeMetrics metrics;

  public DoParse(RefactorSession sess, String fileName) {
    this(sess, fileName, null);
  }

  DoParse(RefactorSession sess, String filename, DoParse primary) {
    this.session = sess;
    this.fileName = filename;
    this.primary = primary;
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

  public TokenSource getLexerTokenStream() {
    return filter;
  }

  protected void doParse(List<ProToken> tokenVector) throws IOException, TokenStreamException, RecognitionException {
    doParse(false, new TokenVectorIterator(tokenVector, this));
  }

  public void doParse(boolean justLex, TokenVectorIterator tvi) throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering DoParse#doParse({})", justLex);
    if (fileName != null) {
      inStream = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), session.getCharset()));
    }
    Preprocessor prepro = new Preprocessor(fileName, inStream, this);

    try  {
      if (fileName != null) {
        addFilename(fileName);
      }

      // Parsing either a token vector or else regular input stream
      // Parsing either a token vector or else regular input stream
      if (tvi != null) {
        LOGGER.trace("Using TokenVectorIterator filter");
        tvi.trace();
//        filter = new CommonTokenStream(tvi);
      } else {
        LOGGER.trace("Creating Lexer / PostLexer objects");
        if (primary == null && !justLex) {
          LOGGER.trace("... on primary file");
        }
        Lexer lexer = new Lexer(prepro);
        Postlexer postlexer = new Postlexer(prepro, lexer, this);
        TokenSource filter1 = new TokenList(postlexer);
        filter = new MultiChannelTokenList(filter1);
//        ((MultiChannelTokenList) filter).build();
      }
//        filter = new TokenStreamHiddenTokenFilter(tokenlist);
//        metrics = new JPNodeMetrics(lexer.getLoc(), lexer.getCommentedLines());

      // If we're just lexing, let's see the "hidden" tokens too.
      // Otherwise, filter.
//      if (!justLex) {
//        filter.hide(NodeTypes.WS);
//        filter.hide(NodeTypes.COMMENT);
//        filter.hide(NodeTypes.AMPMESSAGE);
//        filter.hide(NodeTypes.AMPANALYZESUSPEND);
//        filter.hide(NodeTypes.AMPANALYZERESUME);
//        filter.hide(NodeTypes.AMPGLOBALDEFINE);
//        filter.hide(NodeTypes.AMPSCOPEDDEFINE);
//        filter.hide(NodeTypes.AMPUNDEFINE);
//      }

      if (justLex) {
        return;
      }

      if (preProcessCondition) {
        LOGGER.trace("Executing ProEval parser on preprocessor");
        PreproEval proeval = new PreproEval();
        PreprocessorParser parser = new PreprocessorParser(new CommonTokenStream(tvi));
        ParseTree tree = parser.preproIfEval();
        LOGGER.trace("Preprocessor expression : " + tree.toStringTree(parser));
        Object o = tree.accept(proeval);
        preProcessConditionResult = ((Boolean) o).booleanValue();
//        preProcessConditionResult = proeval.preproIfEval(parser.getAST());
      }
      
    } catch (Throwable t) {
      throw t;
    }
    finally {
      // List all file indexes.
      int i = 0;
      while (isValidIndex(i)) {
        prepro.getLstListener().fileIndex(i, getFilename(i));
        ++i;
      }
      // Tell the preprocessor we're done. Releases file handles, etc.
      prepro.parseComplete();
      if ((primary == null) && !session.getProparseSettings().isMultiParse()) {
        session.clearSuperCache();
      }
    }
    LOGGER.trace("Exiting DoParse#doParse()");
  }

  public JPNodeMetrics getMetrics() {
    return metrics;
  }

  /**
   * Set parent and prevSibling links, as well as nodeNum. Caller is responsible for setting nodeNum of input node, as
   * well as nextNodeNum value.
   */
  /*private void backLinkAndNodeNum(JPNode r) {
    JPNode currNode = r.firstChild();
    while (currNode != null) {
      currNode.setNodeNum(nextNodeNum++);
      currNode.setParent(r);
      backLinkAndNodeNum(currNode);
      JPNode nextNode = currNode.nextSibling();
      if (nextNode != null)
        nextNode.setPrevSibling(currNode);
      currNode = nextNode;
    }
  }*/

  private static class TokenVectorIterator implements TokenSource {
    private final List<ProToken> tokenVector;
    private int it = 0;
    private DoParse doParse;

    TokenVectorIterator(List<ProToken> tokenVector, DoParse doParse) {
      this.tokenVector = tokenVector;
      this.doParse = doParse;
    }

    @Override
    public Token nextToken() {
      if (it >= tokenVector.size())
        return new ProToken(/*doParse.getFilenameList(),*/ Token.EOF, "");

      return tokenVector.get(it++);
    }

    public void trace() {
      for (ProToken tk : tokenVector) {
        LOGGER.trace(" TVI # " + tk.toString());
      }
    }
    @Override
    public int getLine() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public int getCharPositionInLine() {
      // TODO Auto-generated method stub
      return 0;
    }

    @Override
    public CharStream getInputStream() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public String getSourceName() {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void setTokenFactory(TokenFactory<?> factory) {
      // TODO Auto-generated method stub
      
    }

    @Override
    public TokenFactory<?> getTokenFactory() {
      // TODO Auto-generated method stub
      return null;
    }
  }
}
