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

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;

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
