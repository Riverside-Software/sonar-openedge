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
package org.prorefactor.proparse;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * The bottom InputSource object for an IncludeFile is the input for the include file itself.
 * 
 * Each upper (potentially stacked) InputSource object is for an argument reference: - include file argument reference
 * or reference to scoped or global preprocessor definition - the input stream pointer is a pointer to a stringstream
 * object
 * 
 * We keep a pointer to the input stream "A" in the InputSource object so that if "A" spawns a new input stream "B", we
 * can return to "A" when we are done with "B".
 */
class InputSource {

  int fileIndex = 0;
  int sourceNum;
  boolean isMacroExpansion = false;
  boolean isPrimaryInput = false;
  int nextCol = 1;
  int nextLine = 1;

  private BufferedReader theInput;

  InputSource(int sourceNum, BufferedReader theStream) {
    this.sourceNum = sourceNum;
    theInput = theStream;
  }

  InputSource(int sourceNum, BufferedReader theStream, boolean isPrimary) {
    this.sourceNum = sourceNum;
    isPrimaryInput = isPrimary;
    theInput = theStream;
  }

  int get() throws IOException {
    // We use nextLine and nextCol - that way '\n' can have a column
    // number at the end of the line it's on, rather than at column
    // 0 of the following line.
    // If this is a macro expansion, then we don't increment column
    // or line number. Those just stay put at the file position where the
    // macro '{' was referenced.
    // (Doesn't apply to filenames)
    int currChar = theInput.read();
    if (!isMacroExpansion) {
      if (currChar == '\n') {
        nextLine++;
        nextCol = 1;
      } else
        nextCol++;
    }
    return currChar;
  }

  void setInputFilePos(int fileIndex, int line, int col) {
    this.fileIndex = fileIndex;
    this.nextLine = line;
    this.nextCol = col;
  }

}
