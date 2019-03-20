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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.prorefactor.core.ProToken;
import org.prorefactor.proparse.ParserSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DescriptiveErrorListener extends BaseErrorListener {
  private static final Logger LOG = LoggerFactory.getLogger(DescriptiveErrorListener.class);

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException e) {
    ProToken tok = (ProToken) offendingSymbol;
    ParserSupport support = ((Proparse) recognizer).getParserSupport();
    LOG.error("Syntax error -- {} -- {}:{}:{} -- {}", support.getFilename(0), support.getFilename(tok.getFileIndex()),
        line, charPositionInLine, msg);
  }
}
