/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
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
