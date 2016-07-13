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
package org.prorefactor.refactor.settings;

public class ProparseSettings implements IProparseSettings {
  private final boolean capKeyword, indentTab, multiParse, proparseDirectives;
  private final int indentSpaces;
  private final String keywordall, rCodeDir;
  private boolean projectBinaries = false;

  public ProparseSettings() {
    this(true, false, false, true, 2, "", "");
  }

  public ProparseSettings(boolean capKeyword, boolean indentTab, boolean multiParse, boolean proparseDirectives,
      int indentSpaces, String keywordAll, String rCodeDir) {
    this.capKeyword = capKeyword;
    this.indentTab = indentTab;
    this.multiParse = multiParse;
    this.proparseDirectives = proparseDirectives;
    this.indentSpaces = indentSpaces;
    this.keywordall = keywordAll;
    this.rCodeDir = rCodeDir;
  }

  @Override
  public boolean getCapKeyword() {
    return capKeyword;
  }

  @Override
  public boolean getIndentTab() {
    return indentTab;
  }

  @Override
  public int getIndentSpaces() {
    return indentSpaces;
  }

  @Override
  public String getKeywordAll() {
    return keywordall;
  }

  @Override
  public String getRCodeDir() {
    return rCodeDir;
  }

  @Override
  public boolean isMultiParse() {
    return multiParse;
  }

  @Override
  public boolean getProparseDirectives() {
    return proparseDirectives;
  }

  @Override
  public void enableProjectBinaries() {
    projectBinaries = true;
  }

  @Override
  public void disableProjectBinaries() {
    projectBinaries = false;
  }

  @Override
  public boolean getProjectBinaries() {
    return projectBinaries;
  }

}
