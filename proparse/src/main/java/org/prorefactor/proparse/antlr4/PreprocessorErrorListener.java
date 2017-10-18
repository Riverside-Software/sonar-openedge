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

import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
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
    LOGGER.error("Found symbol '{}' in preprocessor expression '{}' at position {}", offendingSymbol,
        getExpressionAsString(), charPositionInLine);
    if (tokens.isEmpty())
      LOGGER.error("Expression found while analyzing '{}'", lexer.getFilename(0));
    else if (tokens.get(0).getFileIndex() == 0)
      LOGGER.error("Expression found while analyzing '{}' at line {}", lexer.getFilename(0), tokens.get(0).getLine());
    else
      LOGGER.error("Expression found in file '{}' at line {} while analyzing '{}'",
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
