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
package org.prorefactor.proparse;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.prorefactor.core.ProToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProparseErrorListener extends BaseErrorListener {
  private static final Logger LOG = LoggerFactory.getLogger(ProparseErrorListener.class);

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
      String msg, RecognitionException e) {
    ProToken tok = (ProToken) offendingSymbol;
    if (tok.getFileIndex() != 0) {
      LOG.error("Syntax error -- {} -- {}:{}:{} -- {}", recognizer.getInputStream().getSourceName(),
          tok.getFileName(), line, charPositionInLine, msg);
    } else {
      LOG.error("Syntax error -- {}:{}:{} -- {}", tok.getFileName(), line, charPositionInLine, msg);
    }
  }
}
