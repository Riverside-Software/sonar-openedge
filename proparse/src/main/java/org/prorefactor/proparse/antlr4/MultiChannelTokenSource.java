/********************************************************************************
 * Copyright (c) 2015-2019 Riverside Software
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
package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ProToken;
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
        ((ProToken) currentToken).setChannel(Token.HIDDEN_CHANNEL);
        break;
      case PreprocessorParser.AMPMESSAGE:
      case PreprocessorParser.AMPANALYZESUSPEND:
      case PreprocessorParser.AMPANALYZERESUME:
      case PreprocessorParser.AMPGLOBALDEFINE:
      case PreprocessorParser.AMPSCOPEDDEFINE:
      case PreprocessorParser.AMPUNDEFINE:
      case PreprocessorParser.INCLUDEDIRECTIVE:
        ((ProToken) currentToken).setChannel(PREPROCESSOR_CHANNEL);
        break;
      case PreprocessorParser.PROPARSEDIRECTIVE:
        ((ProToken) currentToken).setChannel(PROPARSE_CHANNEL);
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
