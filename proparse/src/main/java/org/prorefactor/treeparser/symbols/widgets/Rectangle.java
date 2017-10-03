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
package org.prorefactor.treeparser.symbols.widgets;

import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.Widget;

public class Rectangle extends Widget implements IFieldLevelWidget {

  public Rectangle(String name, TreeParserSymbolScope scope) {
    super(name, scope);
  }

  /**
   * @return NodeTypes.RECTANGLE
   */
  @Override
  public int getProgressType() {
    return ProParserTokenTypes.RECTANGLE;
  }

}
