/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
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
package org.prorefactor.treeparser.symbols;

import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/** A Symbol defined with DEFINE DATA-SOURCE. */
public class Datasource extends Symbol {

  public Datasource(String name, TreeParserSymbolScope scope) {
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
   * Returns NodeTypes.DATASOURCE
   */
  @Override
  public int getProgressType() {
    return Proparse.DATASOURCE;
  }

}
