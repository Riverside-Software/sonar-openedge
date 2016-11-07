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

import org.prorefactor.core.JPNode;

public interface ISymbol {

  /**
   * Get the "full" name for this symbol. This is expected to be overridden in subclasses. For example, we might expect
   * "database.buffer.field" to be the return for a field buffer.
   */
  String fullName();

  int getAllRefsCount();

  int getNumReads();

  int getNumWrites();

  /**
   * If this was defined AS something, then we have an AS node
   */
  JPNode getAsNode();

  /**
   * If this symbol was defined directly by a DEFINE syntax, then this returns the DEFINE node, otherwise null.
   */
  JPNode getDefineNode();

  /**
   * If this symbol was defined with syntax other than a direct DEFINE, then this returns the ID node, otherwise null.
   */
  JPNode getIndirectDefineIdNode();

  /**
   * If this was defined LIKE something, then we have a LIKE node
   */
  JPNode getLikeNode();

  String getName();

  /**
   * From TokenTypes: VARIABLE, FRAME, MENU, MENUITEM, etc. A TableBuffer object always returns BUFFER, regardless of
   * whether the object is a named buffer or a default buffer. A FieldBuffer object always returns FIELD.
   */
  int getProgressType();

  SymbolScope getScope();

  /**
   * Is the symbol newly defined here and visible to other compile units? This includes PROTECTED members visible to
   * subclasses.
   */
  boolean isExported();

  /**
   * Defined as SHARED?
   */
  boolean isImported();

  /**
   * Take note of a symbol reference (read, write, reference by name)
   */
  void noteReference(ContextQualifier contextQualifier);

  /**
   * @see #getAsNode()
   */
  void setAsNode(JPNode asNode);

  /**
   * We store the DEFINE|FUNCTION|METHOD|PROCEDURE node if available and sensible. If defined in a syntax where there is
   * no DEFINE node briefly preceeding the ID node, then we store the ID node.
   */
  void setDefOrIdNode(JPNode node);

  /**
   * @see #getLikeNode()
   */
  void setLikeNode(JPNode likeNode);

  void setName(String name);

}