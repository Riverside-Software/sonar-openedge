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
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Pair;

public class ProToken extends CommonToken {
  private static final long serialVersionUID = 8235907735284214484L;

  private int fileIndex;
  private int macroSourceNum;

  public ProToken(int type, String text) {
    super(type, text);
    this.fileIndex = -1;
  }

  public ProToken(Pair<TokenSource, CharStream> source, int type, int channel, int start, int stop) {
    super(source, type, channel, start, stop);
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
}
