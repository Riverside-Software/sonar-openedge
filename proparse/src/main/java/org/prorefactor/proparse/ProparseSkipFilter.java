/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;

/**
 * Skip sections of code enclosed in <code>{&amp;_proparse_ skip-section}</code> and
 * <code>{&amp;_proparse_ skip-section-end}</code>
 */
public class ProparseSkipFilter implements TokenSource {
  private final TokenSource source;

  private ProToken currToken;
  private ProToken endToken = null;

  public ProparseSkipFilter(TokenSource source) {
    this.source = source;
  }

  @Override
  public Token nextToken() {
    if (endToken != null) {
      Token t = endToken;
      endToken = null;
      return t;
    }

    currToken = (ProToken) source.nextToken();
    if (currToken.getNodeType() == ABLNodeType.PROPARSEDIRECTIVE) {
      handleDirective();
    }
    return currToken;
  }

  private void handleDirective() {
    if ("skip-section".equalsIgnoreCase(currToken.getText())) {
      ProToken tok = (ProToken) source.nextToken();
      while (true) {
        if (tok.getNodeType() == ABLNodeType.EOF_ANTLR4)
          return;
        else if ((tok.getNodeType() == ABLNodeType.PROPARSEDIRECTIVE)
            && "skip-section-end".equalsIgnoreCase(tok.getText())) {
          endToken = tok;
          return;
        } else {
          tok = (ProToken) source.nextToken();
        }
      }
    }
  }

  @Override
  public int getLine() {
    return source.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return source.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return source.getInputStream();
  }

  @Override
  public String getSourceName() {
    return source.getSourceName();
  }

  @Override
  public void setTokenFactory(TokenFactory<?> factory) {
    throw new UnsupportedOperationException("Unable to change TokenFactory object");
  }

  @Override
  public TokenFactory<?> getTokenFactory() {
    return source.getTokenFactory();
  }

}
