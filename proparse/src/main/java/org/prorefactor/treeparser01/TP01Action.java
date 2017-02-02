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
package org.prorefactor.treeparser01;

import org.prorefactor.treeparser.ParseUnit;

/**
 * Superclass of empty actions methods for ITreeParserAction. Subclasses can override and implement any of these
 * methods, which are all called directly by TreeParser01. TP01Support is the default implementation.
 */
public abstract class TP01Action implements ITreeParserAction {
  private ParseUnit parseUnit;

  @Override
  public ParseUnit getParseUnit() {
    return parseUnit;
  }

  @Override
  public void setParseUnit(ParseUnit parseUnit) {
    this.parseUnit = parseUnit;
  }
}
