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
package org.prorefactor.treeparser.symbols;

import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.TreeParserSymbolScope;

public interface ISymbol {

  String getName();

  /**
   * Get the "full" name for this symbol. For example, we might expect "database.buffer.field" to be the return value
   * for a field buffer.
   */
  String fullName();

  int getAllRefsCount();

  int getNumReads();

  int getNumWrites();

  int getNumReferenced();

  void setDefinitionNode(JPNode node);

  /**
   * Returns tree object where symbol was defined. Can be a DEFINE keyword or directly the ID token. 
   */
  JPNode getDefineNode();

  /**
   * From TokenTypes: VARIABLE, FRAME, MENU, MENUITEM, etc. A TableBuffer object always returns BUFFER, regardless of
   * whether the object is a named buffer or a default buffer. A FieldBuffer object always returns FIELD.
   */
  int getProgressType();

  TreeParserSymbolScope getScope();

  /**
   * Take note of a symbol reference (read, write, reference by name)
   */
  void noteReference(ContextQualifier contextQualifier);

}