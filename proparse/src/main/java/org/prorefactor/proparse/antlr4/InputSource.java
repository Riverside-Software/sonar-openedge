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

import com.google.common.io.ByteProcessor;
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
public class InputSource {
  // TODO Almost sure those two fields are useless
  private final boolean primaryInput;
  private final int sourceNum;

  private final String fileContent;
  private final int fileIndex;
  private final boolean macroExpansion;

  private int nextCol = 1;
  private int nextLine = 1;
  private int currPos;

  public InputSource(int sourceNum, String str, int fileIndex, int line, int col) {
    this.sourceNum = sourceNum;
    this.primaryInput = false;
    this.fileContent = str;
    this.fileIndex = fileIndex;
    this.macroExpansion = true;
    this.nextLine = line;
    this.nextCol = col;
  }

  public InputSource(int sourceNum, File file, Charset charset, int fileIndex) throws IOException {
    this(sourceNum, file, charset, fileIndex, false);
  }

  public InputSource(int sourceNum, File file, Charset charset, int fileIndex, boolean isPrimary) throws IOException {
    this.sourceNum = sourceNum;
    this.primaryInput = isPrimary;
    this.fileIndex = fileIndex;
    if (Files.readBytes(file, new XCodedFileByteProcessor())) {
      throw new XCodedFileException(file.getAbsolutePath());
    }
    this.fileContent = Files.toString(file, charset);
    this.macroExpansion = false;
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
    if (!macroExpansion) {
      if (currChar == '\n') {
        nextLine++;
        nextCol = 1;
      } else {
        nextCol++;
      }
    }
    return currChar;
  }

  public int getFileIndex() {
    return fileIndex;
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

  public void setNextCol(int nextCol) {
    this.nextCol = nextCol;
  }

  public void setNextLine(int nextLine) {
    this.nextLine = nextLine;
  }

  /**
   * XCode'd files start with byte 0x11 or 0x13
   */
  private class XCodedFileByteProcessor implements ByteProcessor<Boolean> {
    private boolean isXCoded = false;

    @Override
    public boolean processBytes(byte[] buf, int off, int len) throws IOException {
      isXCoded = (buf[0] == 0x11) || (buf[0] == 0x13);
      // No need to read more bytes
      return false;
    }

    @Override
    public Boolean getResult() {
      return isXCoded;
    }
  }
}
