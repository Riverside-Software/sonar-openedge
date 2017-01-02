/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import com.google.common.io.Files;

/**
 * The bottom InputSource object for an IncludeFile is the input for the include file itself.
 * 
 * Each upper (potentially stacked) InputSource object is for an argument reference:
 * <ul>
 * <li>include file argument reference or reference to scoped or global preprocessor definition
 * <li>the input stream pointer is a pointer to a string stream object
 * </ul>
 * We keep a pointer to the input stream "A" in the InputSource object so that if "A" spawns a new input stream "B", we
 * can return to "A" when we are done with "B".
 */
class InputSource {
  private final boolean primaryInput;
  private final int sourceNum;
  private final String fileContent;

  private boolean isMacroExpansion;
  int fileIndex = 0;
  private int nextCol = 1;
  private int nextLine = 1;

  private int currPos;

  public InputSource(int sourceNum, String s) {
    this.sourceNum = sourceNum;
    this.primaryInput = false;
    this.fileContent = s;
  }

  public InputSource(int sourceNum, File file, Charset charset) throws IOException {
    this(sourceNum, file, charset, false);
  }

  public InputSource(int sourceNum, File file, Charset charset, boolean isPrimary) throws IOException {
    this.sourceNum = sourceNum;
    this.primaryInput = isPrimary;
    this.fileContent = Files.toString(file, charset);
  }

  public int get() {
    // We use nextLine and nextCol - that way '\n' can have a column number at the end of the line it's on, rather than
    // at column 0 of the following line.
    // If this is a macro expansion, then we don't increment column or line number. Those just stay put at the file
    // position where the macro '{' was referenced.
    if (currPos >= fileContent.length()) {
      return -1;
    }

    int currChar = fileContent.charAt(currPos++);
    if (!isMacroExpansion) {
      if (currChar == '\n') {
        nextLine++;
        nextCol = 1;
      } else {
        nextCol++;
      }
    }
    return currChar;
  }

  public int getSourceNum() {
    return sourceNum;
  }

  public int getNextCol() {
    return nextCol;
  }

  public int getNextLine() {
    return nextLine;
  }

  public boolean isPrimaryInput() {
    return primaryInput;
  }

  public void enableMacroExpansion() {
    this.isMacroExpansion = true;
  }

  public void setNextCol(int nextCol) {
    this.nextCol = nextCol;
  }

  public void setNextLine(int nextLine) {
    this.nextLine = nextLine;
  }
}
