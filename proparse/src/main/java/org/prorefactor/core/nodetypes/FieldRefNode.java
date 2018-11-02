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
package org.prorefactor.core.nodetypes;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.Symbol;

public class FieldRefNode extends JPNode {
  public FieldRefNode(ProToken t) {
    super(t);
  }

  public BufferScope getBufferScope() {
    BufferScope bufferScope = (BufferScope) getLink(IConstants.BUFFERSCOPE);
    assert bufferScope != null;
    return bufferScope;
  }

  public String getClassName() {
    return ((Primative) getSymbol()).getClassName();
  }

  /**
   * Returns null if symbol is null or is a graphical component
   */
  public DataType getDataType() {
    if (getSymbol() == null) {
      // Just in order to avoid NPE
      return null;
    }
    if (!(getSymbol() instanceof Primative)) {
      return null;
    }
    return ((Primative) getSymbol()).getDataType();
  }

  /**
   * We very often need to reference the ID node for a Field_ref node. The Field_ref node is a synthetic node - it
   * doesn't have any text. If we want the field/variable name, or the file/line/column, then we probably want to get
   * those from the ID node.
   */
  @Override
  public JPNode getIdNode() {
    JPNode idNode = findDirectChild(ABLNodeType.ID.getType());
    assert idNode != null;
    return idNode;
  }

  /**
   * Get the Symbol for a Field_ref node.
   * 
   * @return Always returns one of two Symbol types: Variable or FieldBuffer.
   */
  @Override
  public Symbol getSymbol() {
    // Can't assert symbol != null, because we aren't currently resolving
    // references to METHODs (like in eventVar:Subscribe(MethodName).
    return (Symbol) getLink(IConstants.SYMBOL);
  }

  public void setBufferScope(BufferScope bufferScope) {
    assert bufferScope != null;
    setLink(IConstants.BUFFERSCOPE, bufferScope);
  }

  public void setSymbol(ISymbol symbol) {
    assert symbol != null;
    setLink(IConstants.SYMBOL, symbol);
  }

}
