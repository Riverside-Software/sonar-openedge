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

public class CharPos {
  protected final int ch;
  protected final int file;
  protected final int line;
  protected final int col;
  protected final int sourceNum;

  public CharPos(int ch, int file, int line, int col, int sourceNum) {
    this.ch = ch;
    this.file = file;
    this.line = line;
    this.col = col;
    this.sourceNum = sourceNum;
  }

  public CharPos(CharPos other) {
    this.ch = other.ch;
    this.file = other.file;
    this.line = other.line;
    this.col = other.col;
    this.sourceNum = other.sourceNum;
  }

  public int getFile() {
    return file;
  }

  public int getLine() {
    return line;
  }

  public int getCol() {
    return col;
  }

  public int getSourceNum() {
    return sourceNum;
  }
}
