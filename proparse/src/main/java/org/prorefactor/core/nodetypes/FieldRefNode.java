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
package org.prorefactor.core.nodetypes;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;

public class FieldRefNode extends JPNode {
  private static final long serialVersionUID = 7754879272592544238L;

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
   * @see FieldRefNode#getSymbol() as it can be null
   */
  public DataType getDataType() {
    if (getSymbol() == null) {
      // Just in order to avoid NPE
      return null;
    }
    return ((Primative) getSymbol()).getDataType();
  }

  /**
   * We very often need to reference the ID node for a Field_ref node. The Field_ref node is a synthetic node - it
   * doesn't have any text. If we want the field/variable name, or the file/line/column, then we probably want to get
   * those from the ID node.
   */
  public JPNode getIdNode() {
    JPNode idNode = findDirectChild(NodeTypes.ID);
    assert idNode != null;
    return idNode;
  }

  /**
   * Get the Symbol for a Field_ref node. TODO Currently returns null if the Field_ref is actually a reference to a
   * METHOD.
   * 
   * @return Always returns one of two Symbol types: Variable or FieldBuffer.
   */
  @Override
  public Symbol getSymbol() {
    Symbol symbol = (Symbol) getLink(IConstants.SYMBOL);
    // Can't assert symbol != null, because we aren't currently resolving
    // references to METHODs (like in eventVar:Subscribe(MethodName).
    return symbol;
  }

  public void setBufferScope(BufferScope bufferScope) {
    assert bufferScope != null;
    setLink(IConstants.BUFFERSCOPE, bufferScope);
  }

  public void setSymbol(FieldBuffer symbol) {
    assert symbol != null;
    setLink(IConstants.SYMBOL, symbol);
  }

  public void setSymbol(IFieldLevelWidget symbol) {
    assert symbol != null;
    setLink(IConstants.SYMBOL, symbol);
  }

  public void setSymbol(Variable symbol) {
    assert symbol != null;
    setLink(IConstants.SYMBOL, symbol);
  }

}
