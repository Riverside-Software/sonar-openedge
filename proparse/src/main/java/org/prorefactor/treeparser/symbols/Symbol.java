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
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.ITreeParserSymbolScope;

/**
 * Base class for any type of symbol which needs to be kept track of when parsing a 4gl compile unit's AST.
 */
public abstract class Symbol implements ISymbol {
  private int allRefsCount = 0;
  private int numReads = 0;
  private int numWrites = 0;
  private int numRefd = 0;
  private JPNode asNode;
  private boolean parameter = false;

  // We store the DEFINE node if available and sensible. If defined in a syntax where there is no DEFINE node briefly
  // preceeding the ID node, then we store the ID node. If this is a schema symbol, then this member is null.
  private JPNode defNode;
  private JPNode likeNode;

  // What scope this symbol was defined in
  private ITreeParserSymbolScope scope;
  // Stores the full name, original (mixed) case as in definition
  private final String name;

  public Symbol(String name, ITreeParserSymbolScope scope) {
    this(name, scope, false);
  }

  public Symbol(String name, ITreeParserSymbolScope scope, boolean parameter) {
    this.name = name;
    this.scope = scope;
    this.parameter = parameter;
    scope.addSymbol(this);
  }

  @Override
  public void setAsNode(JPNode asNode) {
    this.asNode = asNode;
  }

  @Override
  public void setDefOrIdNode(JPNode node) {
    defNode = node;
  }

  @Override
  public void setLikeNode(JPNode likeNode) {
    this.likeNode = likeNode;
  }

  @Override
  public int getAllRefsCount() {
    return allRefsCount;
  }

  @Override
  public int getNumReads() {
    return numReads;
  }

  @Override
  public int getNumWrites() {
    return numWrites;
  }

  @Override
  public int getNumReferenced() {
    return numRefd;
  }

  @Override
  public JPNode getAsNode() {
    return asNode;
  }

  @Override
  public JPNode getDefineNode() {
    if ((defNode != null) && (defNode.getType() != ProParserTokenTypes.ID))
      return defNode;

    return null;
  }

  @Override
  public JPNode getIndirectDefineIdNode() {
    if ((defNode != null) && (defNode.getType() == ProParserTokenTypes.ID))
      return defNode;

    return null;
  }

  @Override
  public JPNode getLikeNode() {
    return likeNode;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ITreeParserSymbolScope getScope() {
    return scope;
  }

  @Override
  public void noteReference(ContextQualifier contextQualifier) {
    allRefsCount++;
    if (ContextQualifier.isRead(contextQualifier))
      numReads++;
    if (ContextQualifier.isWrite(contextQualifier))
      numWrites++;
    if (ContextQualifier.isReference(contextQualifier))
      numRefd++;
  }

  @Override
  public String toString() {
    return fullName();
  }

  public void setParameter(boolean parameter) {
    this.parameter = parameter;
  }

  /**
   * @return True if this variable is a procedure/function/method parameter
   */
  public boolean isParameter() {
    return parameter;
  }
}
