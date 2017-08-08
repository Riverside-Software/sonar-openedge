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
package org.prorefactor.core;

import antlr.CommonHiddenStreamToken;

import java.util.Objects;

import org.prorefactor.proparse.IntegerIndex;

public class ProToken extends CommonHiddenStreamToken {
  private final IntegerIndex<String> filenameList;
  private final int fileIndex;
  private final int macroSourceNum;
  private final int endFile;
  private final int endLine;
  private final int endColumn;
  private final String analyzeSuspend;

  public ProToken(IntegerIndex<String> filenameList, int type, String txt) {
    this(filenameList, type, txt, 0, 0, 0, 0, 0, 0, 0, "");
  }

  public ProToken(IntegerIndex<String> filenameList, int type, String txt, int file, int line, int col, int endFile,
      int endLine, int endCol, int macroSourceNum, String analyzeSuspend) {
    super(type, txt);
    this.filenameList = filenameList;
    this.fileIndex = file;
    this.macroSourceNum = macroSourceNum;
    this.line = line;
    this.col = col;
    this.endFile = endFile;
    this.endLine = endLine;
    this.endColumn = endCol;
    this.analyzeSuspend = analyzeSuspend;
  }

  public int getFileIndex() {
    return fileIndex;
  }

  public int getMacroSourceNum() {
    return macroSourceNum;
  }

  /**
   * A reference to the collection of filenames from the parse
   */
  public IntegerIndex<String> getFilenameList() {
    return filenameList;
  }

  @Override
  public String getFilename() {
    if ((filenameList == null) || (fileIndex < 0) || (fileIndex > filenameList.size())) {
      return "";
    }
    String ret = filenameList.getValue(fileIndex);
    if (ret == null) {
      ret = "";
    }
    return ret;
  }

  /**
   * Convenience method for (ProToken) getHiddenAfter()
   */
  public ProToken getNext() {
    return (ProToken) getHiddenAfter();
  }

  /**
   * Convenience method for (ProToken) getHiddenBefore()
   */
  public ProToken getPrev() {
    return (ProToken) getHiddenBefore();
  }

  public void setHiddenAfter(ProToken t) {
    // In order to change visibility
    super.setHiddenAfter(t);
  }

  public void setHiddenBefore(ProToken t) {
    // In order to change visibility
    super.setHiddenBefore(t);
  }

  /**
   * @return Ending line of token. Not guaranteed to be identical to the start line
   */
  public int getEndLine() {
    return endLine;
  }

  /**
   * @return Ending column of token. Not guaranteed to be greater than start column, as some tokens may include the
   *         newline character
   */
  public int getEndColumn() {
    return endColumn;
  }

  /**
   * @return File number of end of token. Not guaranteed to be identical to file index, as a token can be spread over
   *         two different files, thanks to the magic of the preprocessor
   */
  public int getEndFileIndex() {
    return endFile;
  }

  /**
   * @return Comma-separated list of &ANALYZE-SUSPEND options. Never null.
   */
  public String getAnalyzeSuspend() {
    return analyzeSuspend;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProToken) {
      ProToken tok = (ProToken) obj;
      return ((tok.type == this.type) && (tok.text.equals(this.text)) && (tok.line == this.line)
          && (tok.col == this.col) && (tok.fileIndex == this.fileIndex) && (tok.endFile == this.endFile)
          && (tok.endLine == this.endLine) && (tok.endColumn == this.endColumn)
          && (tok.macroSourceNum == this.macroSourceNum));
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, text, line, col, fileIndex, endFile, endLine, endColumn, macroSourceNum);
  }

  @Override
  public String toString() {
    return "[\"" + getText().replace('\r', ' ').replace('\n', ' ') + "\",<" + type + ">,macro=" + macroSourceNum
        + ",file=" + fileIndex + ":" + endFile + ",line=" + line + ":" + endLine + ",col=" + col + ":" + endColumn
        + "]";
  }
}
