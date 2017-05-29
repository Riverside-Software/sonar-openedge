/*******************************************************************************
 * Copyright (c) 2016-2017 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.WritableToken;

public class ProToken implements WritableToken {

  /**
   * This is the backing field for {@link #getType} and {@link #setType}.
   */
  protected int type;

  /**
   * This is the backing field for {@link #getLine} and {@link #setLine}.
   */
  protected int line;

  /**
   * This is the backing field for {@link #getCharPositionInLine} and {@link #setCharPositionInLine}.
   */
  protected int charPositionInLine = -1; // set to invalid position

  /**
   * This is the backing field for {@link #getChannel} and {@link #setChannel}.
   */
  protected int channel = DEFAULT_CHANNEL;

  /**
   * This is the backing field for {@link #getText} when the token text is explicitly set in the constructor or via
   * {@link #setText}.
   *
   * @see #getText()
   */
  protected String text;

  /**
   * This is the backing field for {@link #getTokenIndex} and {@link #setTokenIndex}.
   */
  protected int index = -1;

  /**
   * This is the backing field for {@link #getStartIndex} and {@link #setStartIndex}.
   */
  protected int start;

  /**
   * This is the backing field for {@link #getStopIndex} and {@link #setStopIndex}.
   */
  protected int stop;

  private int fileIndex;
  private int endFileIndex;
  private int endLine;
  private int endCharPositionInLine;
  private int macroSourceNum;

  private String analyzeSuspend = "";

  public ProToken(int type, String text) {
    this.type = type;
    this.channel = DEFAULT_CHANNEL;
    this.text = text;
    this.fileIndex = -1;
  }

  public ProToken(int type, int channel, int start, int stop, int line, int col) {
    this.type = type;
    this.channel = channel;
    this.start = start;
    this.stop = stop;
    this.line = line;
    this.charPositionInLine = col;
  }

  public int getEndFileIndex() {
    return endFileIndex;
  }

  public void setEndFileIndex(int endFileIndex) {
    this.endFileIndex = endFileIndex;
  }

  public int getEndLine() {
    return endLine;
  }

  public void setEndLine(int endLine) {
    this.endLine = endLine;
  }

  public int getEndCharPositionInLine() {
    return endCharPositionInLine;
  }

  public void setEndCharPositionInLine(int endCharPositionInLine) {
    this.endCharPositionInLine = endCharPositionInLine;
  }

  public int getMacroSourceNum() {
    return macroSourceNum;
  }

  public void setMacroSourceNum(int macroSourceNum) {
    this.macroSourceNum = macroSourceNum;
  }

  public int getFileIndex() {
    return fileIndex;
  }

  public void setFileIndex(int fileIndex) {
    this.fileIndex = fileIndex;
  }

  public void setAnalyzeSuspend(String analyzeSuspend) {
    this.analyzeSuspend = analyzeSuspend;
  }

  /**
   * @return Comma-separated list of &ANALYZE-SUSPEND options. Never null.
   */
  public String getAnalyzeSuspend() {
    return analyzeSuspend;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public int getType() {
    return type;
  }

  @Override
  public int getLine() {
    return line;
  }

  @Override
  public int getCharPositionInLine() {
    return charPositionInLine;
  }

  @Override
  public int getChannel() {
    return channel;
  }

  @Override
  public int getTokenIndex() {
    return index;
  }

  @Override
  public int getStartIndex() {
    return start;
  }

  @Override
  public int getStopIndex() {
    return stop;
  }

  @Override
  public TokenSource getTokenSource() {
    return null;
  }

  @Override
  public CharStream getInputStream() {
    return null;
  }

  @Override
  public void setText(String text) {
    this.text = text;
  }

  @Override
  public void setType(int ttype) {
    this.type = ttype;
  }

  @Override
  public void setLine(int line) {
    this.line = line;
  }

  @Override
  public void setCharPositionInLine(int pos) {
    this.charPositionInLine = pos;
  }

  @Override
  public void setChannel(int channel) {
    this.channel = channel;
  }

  @Override
  public void setTokenIndex(int index) {
    this.index = index;
  }
}
