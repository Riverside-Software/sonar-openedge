/*******************************************************************************
 * Copyright (c) 2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptiveErrorListener extends BaseErrorListener {
  private static final Logger LOG = LoggerFactory.getLogger(DescriptiveErrorListener.class);

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException e) {
    ProToken tok = (ProToken) offendingSymbol;
    String fileName = tok.getFileIndex() == 0 ? "main file"
        : ((Proparse) recognizer).getParserSupport().getFilename(tok.getFileIndex());
    LOG.error("Syntax error @ {}:{}:{} {}", fileName, line, charPositionInLine, msg);
  }
}
