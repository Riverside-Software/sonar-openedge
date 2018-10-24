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

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.symbols.ITableBuffer;

public interface ITreeParserRootSymbolScope extends ITreeParserSymbolScope {

  void addTableDefinitionIfNew(ITable table);
  RefactorSession getRefactorSession();
  ITableBuffer getLocalTableBuffer(ITable table);
  
  /**
   * Lookup an unqualified temp/work table field name. Does not test for uniqueness. That job is left to the compiler.
   * (In fact, anywhere this is run, the compiler would check that the field name is also unique against schema tables.)
   * Returns null if nothing found.
   */
  IField lookupUnqualifiedField(String name);
  /**
   * @return True is parse unit is a CLASS or INTERFACE
   */
  boolean isClass() ;
}
