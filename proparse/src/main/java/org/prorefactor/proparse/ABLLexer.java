/********************************************************************************
 * Copyright (c) 2015-2020 Riverside Software
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
package org.prorefactor.proparse;

import java.io.UncheckedIOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.macrolevel.IPreprocessorEventListener;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.PreprocessorEventListener;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.proparse.support.IntegerIndex;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.IProparseSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteSource;

public class ABLLexer implements TokenSource, IPreprocessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ABLLexer.class);

  // Do we only read tokens ? Or do we expand include files ?
  private final boolean lexOnly;
  private final IProparseSettings ppSettings;
  private final IProparseEnvironment session;
  private final IntegerIndex<String> filenameList = new IntegerIndex<>();
  private final IPreprocessorEventListener lstListener = new PreprocessorEventListener();
  private final Lexer lexer;
  private final TokenSource wrapper;

  // How many levels of &IF FALSE are we currently into?
  private int consuming = 0;

  /**
   * Standard constructor for the ABL lexer. Requires an initialized session object.
   * 
   * @param session Current parser session
   * @param src Byte array of source file
   * @param fileName Referenced under which name
   * @param lexOnly Don't use preprocessor
   * @throws UncheckedIOException
   */
  public ABLLexer(IProparseEnvironment session, ByteSource src, String fileName, boolean lexOnly) {
    LOGGER.trace("New ProgressLexer instance {}", fileName);
    this.ppSettings = session.getProparseSettings();
    this.session = session;
    this.lexOnly = lexOnly;

    lexer = new Lexer(this, src, fileName);
    TokenSource postlexer = lexOnly ? new NoOpPostLexer(lexer) : new PostLexer(this, lexer);
    TokenSource filter0 = new NameDotTokenFilter(postlexer);
    TokenSource filter1 = new TokenList(filter0);
    wrapper = new FunctionKeywordTokenFilter(filter1);
  }

  /**
   * Test-only constructor, no filters added
   */
  protected ABLLexer(IProparseEnvironment session, ByteSource src, String fileName) {
    LOGGER.trace("New ProgressLexer instance {}", fileName);
    this.ppSettings = session.getProparseSettings();
    this.session = session;
    this.lexOnly = false;

    lexer = new Lexer(this, src, fileName);
    wrapper = new NoOpPostLexer(lexer);
  }

  public String getMainFileName() {
    return filenameList.getValue(0);
  }

  public IntegerIndex<String> getFilenameList() {
    return filenameList;
  }

  public String getFilename(int fileIndex) {
    return filenameList.getValue(fileIndex);
  }

  // **********************
  // TokenSource interface
  // **********************

  // Only exposed to unit test classes
  protected TokenSource getTokenSource() {
    return wrapper;
  }

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

  boolean isLexOnly() {
    return lexOnly;
  }

  /**
   * Cleanup work, once the parse is complete.
   */
  public void parseComplete() {
    lexer.parseComplete();
  }

  public boolean isConsuming() {
    return consuming != 0;
  }

  public boolean isConsumingFromOuterIf() {
    return consuming > 1;
  }

  public void incrementConsuming() {
    consuming++;
  }

  public void decrementConsuming() {
    consuming--;
  }

  public IPreprocessorEventListener getLstListener() {
    return lstListener;
  }

  public IProparseEnvironment getRefactorSession() {
    return session;
  }

  public JPNodeMetrics getMetrics() {
    return new JPNodeMetrics(lexer.getLoc(), lexer.getCommentedLines());
  }

  public IncludeRef getMacroGraph() {
    return ((PreprocessorEventListener) lstListener).getMacroGraph();
  }

  public IProparseSettings getProparseSettings(){
    return ppSettings;
  }

  public boolean isAppBuilderCode() {
    return ((PreprocessorEventListener) lstListener).isAppBuilderCode();
  }

  // ****************************
  // IPreprocessor implementation
  // ****************************

  @Override
  public String defined(String argName) {
    return lexer.defined(argName);
  }

  @Override
  public void defGlobal(String argName, String argVal) {
    lexer.defGlobal(argName, argVal);
  }

  @Override
  public void defScoped(String argName, String argVal) {
    lexer.defScoped(argName, argVal);    
  }

  @Override
  public String getArgText(int argNum) {
    return lexer.getArgText(argNum);
  }

  @Override
  public String getArgText(String argName) {
    return lexer.getArgText(argName);
  }

  @Override
  public void undef(String argName) {
    lexer.undef(argName);
  }

  @Override
  public void analyzeSuspend(String analyzeSuspend) {
    lexer.analyzeSuspend(analyzeSuspend);
  }

  @Override
  public void analyzeResume() {
    lexer.analyzeResume();
  }
  // ***********************************
  // End of IPreprocessor implementation
  // ***********************************


}
