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
package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
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
  public void recover(Parser recognizer, RecognitionException e) {
    if (allowRecover) {
      super.recover(recognizer, e);
    } else {
      throw new ParseCancellationException(e);
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
    // Just convert into ProToken type
    Token tok = super.getMissingSymbol(recognizer);
    LOGGER.debug("Injecting missing token {} at line {} - column {}", ABLNodeType.getNodeType(tok.getType()),
        tok.getLine(), tok.getCharPositionInLine());
    return new ProToken.Builder(ABLNodeType.getNodeType(tok.getType()), tok.getText()).setLine(
        tok.getLine()).setCharPositionInLine(tok.getCharPositionInLine()).build();
  }
}
