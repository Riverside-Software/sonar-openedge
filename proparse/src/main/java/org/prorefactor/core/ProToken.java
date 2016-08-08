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

import java.io.IOException;

import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.xfer.DataXferStream;
import org.prorefactor.xfer.Xferable;

public class ProToken extends CommonHiddenStreamToken implements Xferable {

  private int fileIndex;
  private int macroSourceNum;
  private IntegerIndex<String> filenameList;
  private int endFile;
  private int endLine;
  private int endColumn;

  public ProToken() {
    // Only to be used for persistence/serialization
  }

  public ProToken(IntegerIndex<String> filenameList, int type, String s) {
    super(type, s);
    this.filenameList = filenameList;
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

  public ProToken(ProToken orig) {
    super(orig.getType(), orig.getText());
    this.filenameList = orig.filenameList;
    this.fileIndex = orig.fileIndex;
    this.macroSourceNum = orig.macroSourceNum;
    this.line = orig.line;
    this.col = orig.col;
    this.endLine = orig.endLine;
    this.endColumn = orig.endColumn;
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

  public void setFileIndex(int fileIndex) {
    this.fileIndex = fileIndex;
  }

  /** A reference to the collection of filenames from the parse. */
  public void setFilenameList(IntegerIndex<String> filenameList) {
    this.filenameList = filenameList;
  }

  public void setMacroSourceNum(int macroSourceNum) {
    this.macroSourceNum = macroSourceNum;
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

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    out.writeInt(getType());
    out.writeInt(getMacroSourceNum());
    out.writeInt(getFileIndex());
    out.writeInt(getLine());
    out.writeInt(getColumn());
    out.writeRef(getText());
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    out.schemaInt("type");
    out.schemaInt("macroSourceNum");
    out.schemaInt("fileIndex");
    out.schemaInt("line");
    out.schemaInt("column");
    out.schemaRef("text");
  }

}
