/*******************************************************************************
 * Copyright (c) 2017 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Just pass tokens along...
 */
public class NoOpPostLexer implements TokenSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(NoOpPostLexer.class);

  private final Lexer lexer;
  private ProToken currToken;

  public NoOpPostLexer(Lexer lexer) {
    this.lexer = lexer;
  }

  @Override
  public ProToken nextToken() {
    LOGGER.trace("Entering nextToken()");
    currToken = lexer.nextToken();
    return currToken;
  }



  @Override
  public int getLine() {
    return currToken.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return currToken.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return currToken.getInputStream();
  }

  @Override
  public String getSourceName() {
    return IntStream.UNKNOWN_SOURCE_NAME;
  }

  @Override
  public void setTokenFactory(TokenFactory<?> factory) {
    throw new UnsupportedOperationException("Unable to override ProTokenFactory");
  }

  @Override
  public TokenFactory<?> getTokenFactory() {
    return null;
  }

}
