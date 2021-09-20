/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
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

import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;

import eu.rssw.pct.elements.DataType;

/**
 * Expression node: <code>constant</code> (such as <code>TRUE</code>)
 */
public class ConstantNode extends ExpressionNode {

  public ConstantNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Override
  public DataType getDataType() {
    switch (getFirstChild().getNodeType()) {
      case TRUE:
      case FALSE:
      case YES:
      case NO:
        return DataType.LOGICAL;
      case UNKNOWNVALUE:
        return DataType.UNKNOWN;
      case QSTRING:
        return DataType.CHARACTER;
      case LEXDATE:
        return DataType.DATE;
      case NULL:
        return DataType.UNKNOWN;
      case NUMBER:
        if (getFirstChild().getText().contains("."))
          return DataType.DECIMAL;
        else
          return DataType.INTEGER;
      default:
        // All remaining constants are interpreted as INTEGER
        return DataType.INTEGER;
    }
  }

}
