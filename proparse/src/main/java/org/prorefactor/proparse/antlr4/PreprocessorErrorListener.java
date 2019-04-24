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

import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.prorefactor.core.ProToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreprocessorErrorListener extends BaseErrorListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(PostLexer.class);

  private final ProgressLexer lexer;
  private final List<ProToken> tokens;

  public PreprocessorErrorListener(ProgressLexer lexer, List<ProToken> tokens) {
    this.lexer = lexer;
    this.tokens = tokens;
  }

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException e) {
    LOGGER.error("Unexpected symbol '{}' in preprocessor expression '{}' at position {}",
        ((Token) offendingSymbol).getText(), getExpressionAsString(), charPositionInLine);
    if (tokens.isEmpty())
      LOGGER.error("Exception found while analyzing '{}'", lexer.getFilename(0));
    else if (tokens.get(0).getFileIndex() == 0)
      LOGGER.error("Exception found while analyzing '{}' at line {}", lexer.getFilename(0), tokens.get(0).getLine());
    else
      LOGGER.error("Exception found in file '{}' at line {} while analyzing '{}'",
          lexer.getFilename(tokens.get(0).getFileIndex()), tokens.get(0).getLine(), lexer.getFilename(0));
  }

  private String getExpressionAsString() {
    StringBuilder sb = new StringBuilder();
    for (ProToken tok : tokens) {
      sb.append(tok.getText()).append(' ');
    }

    return sb.toString();
  }
}
