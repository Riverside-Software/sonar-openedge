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
package org.prorefactor.treeparser.symbols;

import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/**
 * A Symbol defined with DEFINE EVENT
 */
public class Event extends Symbol {

  public Event(String name, TreeParserSymbolScope scope) {
    super(name, scope);
  }

  /**
   * For this subclass of Symbol, fullName() returns the same value as getName()
   */
  @Override
  public String fullName() {
    return getName();
  }

  /**
   * Returns NodeTypes.EVENT.
   */
  @Override
  public int getProgressType() {
    return ProParserTokenTypes.EVENT;
  }

}
