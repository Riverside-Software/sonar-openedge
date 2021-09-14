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

public class TwoArgumentsExpression extends JPNode implements IExpression {
  public TwoArgumentsExpression(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Override
  public boolean hasProparseDirective(String directive) {
    return getFirstChild().hasProparseDirective(directive);
  }

  @Override
  public DataType getDataType() {
    DataType left = ((IExpression) getDirectChildren().get(0)).getDataType();
    DataType right = ((IExpression) getDirectChildren().get(1)).getDataType();

    switch (getNodeType()) {
      case PLUS:
      case MINUS:
        return handlePlus(left, right);
      case STAR:
      case MULTIPLY:
        return handleMult(left, right);
      case SLASH:
      case DIVIDE:
        return handleDiv(left, right);
      case MODULO:
        return DataType.INTEGER;
      case EQUAL:
      case EQ:
      case GTORLT:
      case NE:
      case RIGHTANGLE:
      case GTHAN:
      case GTOREQUAL:
      case GE:
      case LEFTANGLE:
      case LTHAN:
      case LTOREQUAL:
      case LE:
      case MATCHES:
      case BEGINS:
      case CONTAINS:
      case XOR:
      case AND:
      case OR:
        return DataType.LOGICAL;
      default:
        return DataType.NOT_COMPUTED;
    }
  }

  @Override
  public boolean isExpression() {
    return true;
  }

  @Override
  public JPNode asJPNode() {
    return this;
  }

  private DataType handlePlus(DataType left, DataType right) {
    if ((left == DataType.LONGCHAR) || (right == DataType.LONGCHAR))
      return DataType.LONGCHAR;
    if ((left == DataType.CHARACTER) || (right == DataType.CHARACTER))
      return DataType.CHARACTER;
    else if ((DataType.isDateLike(left) || DataType.isDateLike(right)) && (DataType.isNumeric(left) || DataType.isNumeric(right)))
      return DataType.isDateLike(left) ? left : right;
    else if (DataType.isNumeric(left) && DataType.isNumeric(right)) {
      if ((left == DataType.DECIMAL) || (right == DataType.DECIMAL))
        return DataType.DECIMAL;
      else if ((left == DataType.INT64) || (right == DataType.INT64) || (left == DataType.LONG)
          || (right == DataType.LONG))
        return DataType.INT64;
      else
        return DataType.INTEGER;
    }
    else
      return DataType.NOT_COMPUTED;
  }

  private DataType handleMult(DataType left, DataType right) {
    if ((left == DataType.DECIMAL) || (right == DataType.DECIMAL))
      return DataType.DECIMAL;
    else if ((left == DataType.INT64) || (right == DataType.INT64) || (left == DataType.LONG)
        || (right == DataType.LONG))
      return DataType.INT64;
    else if ((left == DataType.INTEGER) || (right == DataType.INTEGER))
      return DataType.INTEGER;
    else
      return DataType.NOT_COMPUTED;
  }

  private DataType handleDiv(DataType left, DataType right) {
    return DataType.DECIMAL;
  }
}
