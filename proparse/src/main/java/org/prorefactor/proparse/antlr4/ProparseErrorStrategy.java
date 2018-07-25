/*******************************************************************************
 * Copyright (c) 2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.prorefactor.core.ABLNodeType;

/**
 * As tokens are manually generated in Proparse, method {@link DefaultErrorStrategy#getMissingSymbol} fails with
 * NullPointerException because {@link Token#getTokenSource} returns null.
 * 
 * We just use the same implementation with a different token creation type
 */
public class ProparseErrorStrategy extends DefaultErrorStrategy {

  @Override
  protected Token getMissingSymbol(Parser recognizer) {
    Token currentSymbol = recognizer.getCurrentToken();
    IntervalSet expecting = getExpectedTokens(recognizer);
    int expectedTokenType = Token.INVALID_TYPE;
    if (!expecting.isNil()) {
      expectedTokenType = expecting.getMinElement(); // get any element
    }
    String tokenText;
    if (expectedTokenType == Token.EOF)
      tokenText = "<missing EOF>";
    else
      tokenText = "<missing " + recognizer.getVocabulary().getDisplayName(expectedTokenType) + ">";
    Token current = currentSymbol;
    Token lookback = recognizer.getInputStream().LT(-1);
    if (current.getType() == Token.EOF && lookback != null) {
      current = lookback;
    }

    ProToken tok = new ProToken(ABLNodeType.getNodeType(expectedTokenType), tokenText);
    tok.setLine(current.getLine());
    tok.setCharPositionInLine(current.getCharPositionInLine());
    return tok;
  }
}
