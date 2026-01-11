/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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
package org.prorefactor.core;

import org.antlr.v4.runtime.WritableToken;

public class WritableProToken extends ProToken implements WritableToken {
  private Integer line = null;
  private Integer charPositionInLine = null;

  WritableProToken(ABLNodeType type, String text) {
    super(type, text);
  }

  @Override
  public int getLine() {
    return this.line == null ? super.getLine() : line;
  }

  @Override
  public int getCharPositionInLine() {
    return this.charPositionInLine == null ? super.getCharPositionInLine() : charPositionInLine;
  }

  @Override
  public void setLine(int line) {
    this.line = line;
  }

  @Override
  public void setCharPositionInLine(int charPositionInLine) {
    this.charPositionInLine = charPositionInLine;
  }

  @Override
  public void setText(String text) {
    throw new UnsupportedOperationException("Nope...");
  }

  @Override
  public void setType(int ttype) {
    throw new UnsupportedOperationException("Nope...");
  }

}
