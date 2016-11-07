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
import org.prorefactor.core.NodeTypes;

/**
 * Base class for any type of symbol which needs to be kept track of when parsing a 4gl compile unit's AST.
 */
public abstract class Symbol implements ISymbol {
  private int allRefsCount = 0;
  private int numReads = 0;
  private int numWrites = 0;
  private JPNode asNode;

  /**
   * We store the DEFINE node if available and sensible. If defined in a syntax where there is no DEFINE node briefly
   * preceeding the ID node, then we store the ID node. If this is a schema symbol, then this member is null.
   */
  private JPNode defNode;

  private JPNode likeNode;

  /** What scope this symbol was defined in. */
  private SymbolScope scope;

  /** Stores the full name, original (mixed) case as in definition. */
  private String name;

  /** Only to be used for persistence/serialization. */
  protected Symbol() {
  }

  protected Symbol(int allRefsCount, JPNode asNode, JPNode defNode, JPNode likeNode, String name, int numReads,
      int numWrites, SymbolScope scope) {
    this.allRefsCount = allRefsCount;
    this.asNode = asNode;
    this.defNode = defNode;
    this.likeNode = likeNode;
    this.name = name;
    this.numReads = numReads;
    this.numWrites = numWrites;
    this.scope = scope;
  }

  Symbol(SymbolScope scope) {
    this.scope = scope;
    scope.addSymbol(this);
  }

  /**
   * Generate a bare-bones copy of this symbol. Requires the scope to attach it to as the argument.
   */
  public abstract Symbol copyBare(SymbolScope intoScope);

  @Override
  public abstract String fullName();

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
  public JPNode getAsNode() {
    return asNode;
  }

  @Override
  public JPNode getDefineNode() {
    if (defNode != null && defNode.getType() != NodeTypes.ID)
      return defNode;
    return null;
  }

  @Override
  public JPNode getIndirectDefineIdNode() {
    if (defNode != null && defNode.getType() == NodeTypes.ID)
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
  public abstract int getProgressType();

  @Override
  public SymbolScope getScope() {
    return scope;
  }

  @Override
  public boolean isExported() {
    // If the symbol belongs to a SymbolScopeSuper, then not only has it already
    // been determined that the symbol is exported, but also the rest of this
    // method would just not work because there is never any AST linked to any
    // of the symbols in a SymbolScopeSuper.
    if (this.scope instanceof SymbolScopeSuper)
      return true;
    SymbolScopeRoot unitScope = scope.getRootScope();
    // If this is not at the unit (root) scope, then it cannot be visible.
    if (scope != unitScope)
      return false;
    if (unitScope.getClassName() != null) {
      // For class members, only elements declared PUBLIC|PROTECTED are visible.
      // Unnamed buffers don't have a DEFINE node.
      if (defNode == null)
        return false;
      return (defNode.findDirectChild(NodeTypes.PUBLIC) != null)
          || (defNode.findDirectChild(NodeTypes.PROTECTED) != null);
    }
    // If there is no DEFINE node (inline var def), then it is not visible.
    if (defNode == null)
      return false;
    switch (defNode.getType()) {
      case NodeTypes.DEFINE:
        return defNode.firstChild().getType() == NodeTypes.NEW;
      case NodeTypes.FUNCTION:
      case NodeTypes.PROCEDURE:
        return defNode.findDirectChild(NodeTypes.PRIVATE) == null;
    }
    return false;
  }

  @Override
  public boolean isImported() {
    // If there is no DEFINE node (inline var def), then it is not SHARED.
    if (defNode == null || defNode.getType() != NodeTypes.DEFINE)
      return false;
    if (defNode.firstChild().getType() == NodeTypes.SHARED)
      return true;
    return false;
  }

  @Override
  public void noteReference(ContextQualifier contextQualifier) {
    allRefsCount++;
    if (ContextQualifier.isRead(contextQualifier))
      numReads++;
    if (ContextQualifier.isWrite(contextQualifier))
      numWrites++;
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
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return fullName();
  }

}
