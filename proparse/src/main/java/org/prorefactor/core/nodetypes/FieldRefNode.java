/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2019 Riverside Software
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;

public class FieldRefNode extends JPNode {
  public FieldRefNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
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
    return findDirectChild(ABLNodeType.ID.getType());
  }

  @Nullable
  public BufferScope getBufferScope() {
    return (BufferScope) getLink(IConstants.BUFFERSCOPE);
  }

  public void setBufferScope(@Nonnull BufferScope bufferScope) {
    setLink(IConstants.BUFFERSCOPE, bufferScope);
  }

}
