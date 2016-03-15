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

public interface IProparseSettings {

  public void enableParserListing();
  public void disableParserListing();
  public void enableProjectBinaries();
  public void disableProjectBinaries();

  public boolean getCapKeyword();
  public boolean getIndentTab();
  public boolean isMultiParse();
  public boolean getProparseDirectives();
  public boolean getParserListing();
  public boolean getProjectBinaries();
  public int getIndentSpaces();
  public String getKeywordAll();
  public String getRCodeDir();
}
