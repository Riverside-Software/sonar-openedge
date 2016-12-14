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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.WritableToken;

public class ProToken implements WritableToken {
  private int type;
  private String text;
  private int channel;
  private int line;
  private int charPositionInLine;
  // -1 means token was conjured up since it doesn't have a valid index.
  private int tokenIndex = -1;
  private TokenSource source;

  private int macroSourceNum;
  private int fileIndex;

  public ProToken(int type, String text) {
    this(type, text, 0, -1);
  }

  public ProToken(int type, String text, int line, int charPositionInLine) {
    this(type, text, line, charPositionInLine, 0);
  }
  
  public ProToken(int type, String text, int line, int charPositionInLine, int fileIndex) {
    this.type = type;
    this.text = text;
    this.line = line;
    this.charPositionInLine = charPositionInLine;
    this.fileIndex = fileIndex;
  }

  public ProToken(ProToken orig) {
    this(orig.getType(), orig.getText(), orig.getLine(), orig.getCharPositionInLine());
    this.channel = orig.getChannel();
    this.macroSourceNum = orig.getMacroSourceNum();
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

  @Override
  public int getType() {
    return type;
  }

  @Override
  public void setType(int ttype) {
    this.type = ttype;
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public void setText(String text) {
    this.text = text;
  }

  @Override
  public int getChannel() {
    return channel;
  }

  @Override
  public void setChannel(int channel) {
    this.channel = channel;
  }

  @Override
  public int getLine() {
    return line;
  }

  @Override
  public void setLine(int line) {
    this.line = line;
  }

  @Override
  public int getCharPositionInLine() {
    return charPositionInLine;
  }

  @Override
  public void setCharPositionInLine(int pos) {
    this.charPositionInLine = pos;
  }

  @Override
  public int getTokenIndex() {
    return tokenIndex;
  }

  @Override
  public void setTokenIndex(int index) {
    this.tokenIndex = index;
  }

  @Override
  public int getStartIndex() {
    // -1 means not implemented
    return -1;
  }

  @Override
  public int getStopIndex() {
    // -1 means not implemented
    return -1;
  }

  @Override
  public TokenSource getTokenSource() {
    return source;
  }

  public void setTokenSource(TokenSource source) {
    this.source = source;
  }

  @Override
  public CharStream getInputStream() {
    return null;
  }

  @Override
  public String toString() {
    String channelStr = "";
    if ( channel>0 ) {
      channelStr=",channel="+channel;
    }
    String txt = getText();
    if ( txt!=null ) {
      txt = txt.replace("\n","\\n");
      txt = txt.replace("\r","\\r");
      txt = txt.replace("\t","\\t");
    }
    else {
      txt = "<no text>";
    }
    return "[@"+fileIndex+":"+getTokenIndex()+","+"='"+txt+"',<"+type+">"+channelStr+","+line+":"+getCharPositionInLine()+"]";
  }
}
