/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.treeparser;

import org.prorefactor.proparse.ProParserTokenTypes;
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

  public static Symbol create(int symbolType, String name, TreeParserSymbolScope scope) {
    switch (symbolType) {
      case ProParserTokenTypes.DATASET:
        return new Dataset(name, scope);
      case ProParserTokenTypes.DATASOURCE:
        return new Datasource(name, scope);
      case ProParserTokenTypes.QUERY:
        return new Query(name, scope);
      case ProParserTokenTypes.STREAM:
        return new Stream(name, scope);
      // Widgets
      case ProParserTokenTypes.BROWSE:
        return new Browse(name, scope);
      case ProParserTokenTypes.BUTTON:
        return new Button(name, scope);
      case ProParserTokenTypes.FRAME:
        return new Frame(name, scope);
      case ProParserTokenTypes.IMAGE:
        return new Image(name, scope);
      case ProParserTokenTypes.MENU:
        return new Menu(name, scope);
      case ProParserTokenTypes.MENUITEM:
        return new MenuItem(name, scope);
      case ProParserTokenTypes.RECTANGLE:
        return new Rectangle(name, scope);
      case ProParserTokenTypes.SUBMENU:
        return new Submenu(name, scope);
      default:
        assert false : "Unexpected values for SymbolFactory" + " " + symbolType + " " + name;
        return null;
    }
  }

}
