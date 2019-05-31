/********************************************************************************
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
package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ProToken;
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
