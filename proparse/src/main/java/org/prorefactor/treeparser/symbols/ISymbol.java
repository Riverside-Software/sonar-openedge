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

import javax.annotation.Nullable;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.ITreeParserSymbolScope;

public interface ISymbol {

  /**
   * Get the "full" name for this symbol. This is expected to be overridden in subclasses. For example, we might expect
   * "database.buffer.field" to be the return for a field buffer.
   */
  String fullName();

  int getAllRefsCount();

  int getNumReads();

  int getNumWrites();

  int getNumReferenced();

  /**
   * If symbol is defined <code>AS something</code>, then return the <code>AS</code> node. Otherwise, return null.
   */
  @Nullable
  Object getAsNode(); // TEMP-ANTLR4 Return JPNode object

  /**
   * If symbol is defined by a <code>DEFINE</code> syntax, then return the <code>DEFINE</code> node. Otherwise, return
   * null.
   */
  @Nullable
  Object getDefineNode(); // TEMP-ANTLR4 Return JPNode object

  /**
   * If symbol is defined with syntax other than a direct <code>DEFINE</code>, then return the <code>ID</code> node.
   * Otherwise, return null.
   */
  @Nullable
  Object getIndirectDefineIdNode(); // TEMP-ANTLR4 Return JPNode object

  /**
   * If symbol is defined <code>LIKE something</code>, then return the <code>LIKE</code> node. Otherwise, return null.
   */
  @Nullable
  Object getLikeNode(); // TEMP-ANTLR4 Return JPNode object

  String getName();

  /**
   * From TokenTypes: VARIABLE, FRAME, MENU, MENUITEM, etc. A TableBuffer object always returns BUFFER, regardless of
   * whether the object is a named buffer or a default buffer. A FieldBuffer object always returns FIELD.
   */
  ABLNodeType getProgressType();

  ITreeParserSymbolScope getScope();

  /**
   * Take note of a symbol reference (read, write, reference by name)
   */
  void noteReference(ContextQualifier contextQualifier);

  /**
   * @return True if this variable is a procedure/function/method parameter
   */
  boolean isParameter();

}