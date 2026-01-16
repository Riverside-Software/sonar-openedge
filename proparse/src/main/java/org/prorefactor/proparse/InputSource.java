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
package org.prorefactor.proparse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(InputSource.class);

  // TODO Almost sure this field is useless
  private final int sourceNum;

  private final String fileContent;
  private final int fileIndex;
  private final boolean macroExpansion;

  private int nextCol = 0;
  private int nextLine = 1;
  private int currPos;
  private String currAnalyzeSuspend = null;

  public InputSource(int sourceNum, String src, int fileIndex) {
    this.sourceNum = sourceNum;
    this.fileIndex = fileIndex;
    this.macroExpansion = false;
    this.fileContent = src;
  }

  public InputSource(int sourceNum, String str, int fileIndex, int line, int col) {
    LOGGER.trace("New InputSource object for macro element '{}'", str);
    this.sourceNum = sourceNum;
    this.fileContent = str;
    this.fileIndex = fileIndex;
    this.macroExpansion = true;
    this.nextLine = line;
    this.nextCol = col;
  }

  public InputSource(int sourceNum, File file, Charset charset, int fileIndex, boolean skipXCode) throws IOException {
    LOGGER.trace("New InputSource object for file '{}'", file.getName());
    this.sourceNum = sourceNum;
    this.fileIndex = fileIndex;
    this.macroExpansion = false;
    try (var input = Files.newInputStream(file.toPath())) {
      var src = input.readAllBytes();
      if (isXCoded(src)) {
        if (skipXCode)
          this.fileContent = " ";
        else
          throw new XCodedFileException(file.getName());
      } else {
        this.fileContent = new String(src, charset);
      }
    }
  }

  public InputSource(int sourceNum, String fileName, byte[] src, Charset charset, int fileIndex, boolean skipXCode) throws IOException {
    LOGGER.trace("New InputSource object for include stream '{}'", fileName);
    this.sourceNum = sourceNum;
    this.fileIndex = fileIndex;
    this.macroExpansion = false;
    if (isXCoded(src)) {
      if (skipXCode)
        this.fileContent = " ";
      else
        throw new XCodedFileException(fileName);
    } else {
      this.fileContent = new String(src, charset);
    }
  }

  public int get() {
    // Skip first character if it's a BOM
    if ((currPos == 0) && !fileContent.isEmpty() && fileContent.charAt(0) == 0xFEFF)
      currPos++;

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
        nextCol = 0;
      } else {
        nextCol++;
      }
    }
    return currChar;
  }

  public int getFileIndex() {
    return fileIndex;
  }

  public boolean isMacroExpansion() {
    return macroExpansion;
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

  @CheckForNull
  public String getAnalyzeSuspend() {
    return currAnalyzeSuspend;
  }

  public void setAnalyzeSuspend(@Nonnull String str) {
    this.currAnalyzeSuspend = str;
  }

  public void setNextCol(int nextCol) {
    this.nextCol = nextCol;
  }

  public void setNextLine(int nextLine) {
    this.nextLine = nextLine;
  }

  public String getContent() {
    return fileContent;
  }

  private static boolean isXCoded(byte[] src) {
    return (src.length > 0) && ((src[0] == 0x11) || (src[0] == 0x13));
  }

}
