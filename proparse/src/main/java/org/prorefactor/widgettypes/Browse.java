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
package org.prorefactor.widgettypes;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.treeparser.FieldContainer;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.SymbolScope;

public class Browse extends FieldContainer implements IFieldLevelWidget {

  public Browse(String name, SymbolScope scope) {
    super(name, scope);
  }

  @Override
  public Symbol copyBare(SymbolScope scope) {
    return new Browse(getName(), scope);
  }

  /**
   * @return NodeTypes.BROWSE
   */
  @Override
  public int getProgressType() {
    return NodeTypes.BROWSE;
  }

}
