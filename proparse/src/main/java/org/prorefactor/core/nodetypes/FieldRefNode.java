/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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
package org.prorefactor.core.nodetypes;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.Primitive;

import eu.rssw.pct.elements.DataType;

/**
 * Expression node: <code>ID</code> where ID is a variable, field name, ...
 */
public class FieldRefNode extends ExpressionNode {
  private ContextQualifier qualifier;
  private int storeType;
  private boolean unqualifiedField;
  private boolean inlineVar;
  private boolean abbrev;

  public FieldRefNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  public void setContextQualifier(ContextQualifier qualifier) {
    this.qualifier = qualifier;
  }

  public ContextQualifier getQualifier() {
    return qualifier;
  }

  public void setUnqualifiedField(boolean unqualifiedField) {
    this.unqualifiedField = unqualifiedField;
  }

  public boolean isUnqualifiedField() {
    return unqualifiedField;
  }

  public void setInlineVar(boolean inlineVar) {
    this.inlineVar = inlineVar;
  }

  public boolean isInlineVar() {
    return inlineVar;
  }

  public int getStoreType() {
    return storeType;
  }

  public void setStoreType(int storeType) {
    this.storeType = storeType;
  }

  public void setAbbrev(boolean abbrev) {
    this.abbrev = abbrev;
  }

  @Override
  public boolean isAbbreviated() {
    return abbrev;
  }

  /**
   * Returns null if symbol is null or is a graphical component
   */
  @Override
  public DataType getDataType() {
    if (getSymbol() instanceof Primitive) {
      DataType dataType = ((Primitive) getSymbol()).getDataType();
      return dataType == null ? DataType.NOT_COMPUTED : dataType;
    }
    return DataType.NOT_COMPUTED;
  }

  /**
   * We very often need to reference the ID node for a Field_ref node. The Field_ref node is a synthetic node - it
   * doesn't have any text. If we want the field/variable name, or the file/line/column, then we probably want to get
   * those from the ID node.
   */
  @Override
  public JPNode getIdNode() {
    return findDirectChild(ABLNodeType.ID.getType());
  }

}
