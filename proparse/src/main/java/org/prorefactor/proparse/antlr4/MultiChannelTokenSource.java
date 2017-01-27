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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.WritableToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Last layer of TokenSource, used to dispatch preprocessor statements, comments and whitespaces to the right channel 
 */
public class MultiChannelTokenSource implements TokenSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(MultiChannelTokenSource.class);

  // All preprocessor statements (&MESSAGE, &ANALYZE-SUSPEND and RESUME, &GLOBAL/SCOPED DEFINE and &UNDEFINE) go to this channel
  public static final int PREPROCESSOR_CHANNEL = 2;
  // All &_PROPARSE statements go to this channel
  public static final int PROPARSE_CHANNEL = 3;
  
  private final TokenSource source;
  private Token currentToken;

  public MultiChannelTokenSource(TokenSource input) {
    this.source = input;
  }

  @Override
  public Token nextToken() {
    LOGGER.trace("Entering nextToken()");
    currentToken = source.nextToken();
    switch (currentToken.getType()) {
      case PreprocessorParser.COMMENT:
      case PreprocessorParser.WS:
        ((WritableToken) currentToken).setChannel(Token.HIDDEN_CHANNEL);
        break;
      case PreprocessorParser.AMPMESSAGE:
      case PreprocessorParser.AMPANALYZESUSPEND:
      case PreprocessorParser.AMPANALYZERESUME:
      case PreprocessorParser.AMPGLOBALDEFINE:
      case PreprocessorParser.AMPSCOPEDDEFINE:
      case PreprocessorParser.AMPUNDEFINE:
        ((WritableToken) currentToken).setChannel(PREPROCESSOR_CHANNEL);
        break;
      case PreprocessorParser.PROPARSEDIRECTIVE:
        ((WritableToken) currentToken).setChannel(PROPARSE_CHANNEL);
        break;
      default:
    }

    return currentToken;
  }

  @Override
  public int getLine() {
    return currentToken.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return currentToken.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return currentToken.getInputStream();
  }

  @Override
  public String getSourceName() {
    return IntStream.UNKNOWN_SOURCE_NAME;
  }

  @Override
  public void setTokenFactory(TokenFactory<?> factory) {
    throw new UnsupportedOperationException("Unable to change TokenFactory object");
  }

  @Override
  public TokenFactory<?> getTokenFactory() {
    return source.getTokenFactory();
  }

}
