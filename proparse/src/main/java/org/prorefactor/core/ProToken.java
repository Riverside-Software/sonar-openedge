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

import org.prorefactor.proparse.IntegerIndex;

public class ProToken extends CommonHiddenStreamToken {
  private final IntegerIndex<String> filenameList;
  private final int fileIndex;
  private final int macroSourceNum;
  private final int endFile;
  private final int endLine;
  private final int endColumn;

  public ProToken(IntegerIndex<String> filenameList, int type, String txt) {
    this(filenameList, type, txt, 0, 0, 0, 0, 0, 0, 0);
  }

  public ProToken(IntegerIndex<String> filenameList, int type, String txt, int file, int line, int col, int endFile,
      int endLine, int endCol, int macroSourceNum) {
    super(type, txt);
    this.filenameList = filenameList;
    this.fileIndex = file;
    this.macroSourceNum = macroSourceNum;
    this.line = line;
    this.col = col;
    this.endFile = endFile;
    this.endLine = endLine;
    this.endColumn = endCol;
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

  public void setHiddenAfter(ProToken t) { // NOSONAR
    // In order to change visibility
    super.setHiddenAfter(t);
  }

  public void setHiddenBefore(ProToken t) { // NOSONAR
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

}
