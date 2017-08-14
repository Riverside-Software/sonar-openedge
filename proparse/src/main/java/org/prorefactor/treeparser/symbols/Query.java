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

import org.prorefactor.core.NodeTypes;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/** A Symbol defined with DEFINE QUERY. */
public class Query extends Symbol {

  public Query(String name, TreeParserSymbolScope scope) {
    super(name, scope);
  }

  @Override
  public Symbol copyBare(TreeParserSymbolScope scope) {
    return new Query(getName(), scope);
  }

  /**
   * For this subclass of Symbol, fullName() returns the same value as getName()
   */
  @Override
  public String fullName() {
    return getName();
  }

  /**
   * @return NodeTypes.QUERY
   */
  @Override
  public int getProgressType() {
    return NodeTypes.QUERY;
  }

}
