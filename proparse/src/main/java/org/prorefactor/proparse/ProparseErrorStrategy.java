/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
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

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * As tokens are manually generated in Proparse, method {@link DefaultErrorStrategy#getMissingSymbol} fails with
 * NullPointerException because {@link Token#getTokenSource} returns null.
 * 
 * We just use the same implementation with a different token creation type
 */
public class ProparseErrorStrategy extends DefaultErrorStrategy {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProparseErrorStrategy.class);

  private final boolean allowDeletion;
  private final boolean allowInsertion;
  private final boolean allowRecover;

  public ProparseErrorStrategy(boolean allowDeletion, boolean allowInsertion, boolean allowRecover) {
    super();
    this.allowDeletion = allowDeletion;
    this.allowInsertion = allowInsertion;
    this.allowRecover = allowRecover;
  }

  @Override
  public void recover(Parser recognizer, RecognitionException cause) {
    if (allowRecover) {
      super.recover(recognizer, cause);
    } else {
      String msg = "Syntax error";
      if (cause instanceof NoViableAltException)
        msg = getMsgForNoViableAlternative(recognizer, (NoViableAltException) cause);
      else if (cause instanceof InputMismatchException)
        msg = getMsgForInputMismatch(recognizer, (InputMismatchException) cause);
      throw new ParseCancellationException(msg, cause);
    }
  }

  @Override
  protected Token singleTokenDeletion(Parser recognizer) {
    if (allowDeletion) {
      return super.singleTokenDeletion(recognizer);
    } else {
      return null;
    }
  }

  @Override
  protected boolean singleTokenInsertion(Parser recognizer) {
    if (allowInsertion) {
      return super.singleTokenInsertion(recognizer);
    } else {
      return false;
    }
  }

  @Override
  protected Token getMissingSymbol(Parser recognizer) {
    // Rewrite superclass method as it throws NPE on ProToken.getTokenSource().getInputStream()
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
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Injecting missing token {} at line {} - column {}", ABLNodeType.getNodeType(expectedTokenType),
          current.getLine(), current.getCharPositionInLine());
    }

    return new ProToken.Builder(ABLNodeType.getNodeType(expectedTokenType), tokenText) //
      .setLine(current.getLine()) //
      .setCharPositionInLine(current.getCharPositionInLine()) //
      .build();
  }

  private String getMsgForInputMismatch(Parser recognizer, InputMismatchException e) {
    return "Mismatched input '" + getTokenErrorDisplay(e.getOffendingToken()) + "', expecting '"
        + e.getExpectedTokens().toString(recognizer.getVocabulary()) + "'";
  }
  
  private String getMsgForNoViableAlternative(Parser recognizer, NoViableAltException e) {
    TokenStream tokens = recognizer.getInputStream();
    String input;
    if (tokens != null) {
      if (e.getStartToken().getType() == Token.EOF)
        input = "<EOF>";
      else
        input = tokens.getText(e.getStartToken(), e.getOffendingToken());
    } else {
      input = "<unknown input>";
    }
    String msg = "No viable alternative at input " + escapeWSAndQuote(input);
    return msg;
  }

}
