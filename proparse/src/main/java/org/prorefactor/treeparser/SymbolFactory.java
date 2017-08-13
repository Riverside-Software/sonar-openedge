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
package org.prorefactor.treeparser;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.Datasource;
import org.prorefactor.treeparser.symbols.Query;
import org.prorefactor.treeparser.symbols.Stream;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.widgets.Browse;
import org.prorefactor.treeparser.symbols.widgets.Button;
import org.prorefactor.treeparser.symbols.widgets.Frame;
import org.prorefactor.treeparser.symbols.widgets.Image;
import org.prorefactor.treeparser.symbols.widgets.Menu;
import org.prorefactor.treeparser.symbols.widgets.MenuItem;
import org.prorefactor.treeparser.symbols.widgets.Rectangle;
import org.prorefactor.treeparser.symbols.widgets.Submenu;

/** Create a Symbol of the appropriate subclass. */
public final class SymbolFactory {

  private SymbolFactory() {
    // Shouldn't be instantiated
  }

  public static Symbol create(int symbolType, String name, SymbolScope scope) {
    switch (symbolType) {
      case NodeTypes.DATASET:
        return new Dataset(name, scope);
      case NodeTypes.DATASOURCE:
        return new Datasource(name, scope);
      case NodeTypes.QUERY:
        return new Query(name, scope);
      case NodeTypes.STREAM:
        return new Stream(name, scope);
      // Widgets
      case NodeTypes.BROWSE:
        return new Browse(name, scope);
      case NodeTypes.BUTTON:
        return new Button(name, scope);
      case NodeTypes.FRAME:
        return new Frame(name, scope);
      case NodeTypes.IMAGE:
        return new Image(name, scope);
      case NodeTypes.MENU:
        return new Menu(name, scope);
      case NodeTypes.MENUITEM:
        return new MenuItem(name, scope);
      case NodeTypes.RECTANGLE:
        return new Rectangle(name, scope);
      case NodeTypes.SUBMENU:
        return new Submenu(name, scope);
      default:
        assert false : "Unexpected values for SymbolFactory" + " " + symbolType + " " + name;
        return null;
    }
  }

}
