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

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.refactor.RefactorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.TokenStream;

public class ProgressLexer implements TokenSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProgressLexer.class);

  private final RefactorSession session;
  private final String fileName;
  private IntegerIndex<String> filenameList = new IntegerIndex<>();

  private TokenSource wrapper;
  
  private JPNodeMetrics metrics;

  public ProgressLexer(RefactorSession sess, String fileName) throws IOException {
    this(sess, fileName, null);
  }

  ProgressLexer(RefactorSession sess, String filename, ProgressLexer primary) throws IOException {
    this.session = sess;
    this.fileName = filename;
    
    if (fileName != null) {
      addFilename(fileName);
    }

    Preprocessor prepro = new Preprocessor(fileName, this);
    Lexer lexer = new Lexer(prepro);
    Postlexer postlexer = new Postlexer(prepro, lexer, this);
    TokenSource filter1 = new TokenList(postlexer);
    wrapper = new MultiChannelTokenSource(filter1);
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

  public JPNodeMetrics getMetrics() {
    return metrics;
  }

  // **********************
  // TokenSource interface
  // **********************

  @Override
  public Token nextToken() {
    return wrapper.nextToken();
  }

  @Override
  public int getLine() {
    return wrapper.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return wrapper.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return wrapper.getInputStream();
  }

  @Override
  public String getSourceName() {
    return wrapper.getSourceName();
  }

  @Override
  public void setTokenFactory(TokenFactory<?> factory) {
    wrapper.setTokenFactory(factory);
  }

  @Override
  public TokenFactory<?> getTokenFactory() {
    return wrapper.getTokenFactory();
  }

  // ****************************
  // End of TokenSource interface
  // ****************************

}
