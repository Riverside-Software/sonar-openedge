/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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
package org.prorefactor.macrolevel;

/**
 * Position of macro in a file 
 */
public class MacroPosition {
  private final int fileNum;
  private final int line;
  private final int column;
  
  public MacroPosition(int fileNum, int line, int column) {
    this.fileNum = fileNum;
    this.line = line;
    this.column = column;
  }
  
  public int getFileNum() {
    return fileNum;
  }
  
  public int getLine() {
    return line;
  }
  
  public int getColumn() {
    return column;
  }
}
