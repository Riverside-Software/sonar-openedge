/********************************************************************************
 * Copyright (c) 2015-2023 Riverside Software
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
import org.prorefactor.treeparser.symbols.Event;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.PrimitiveDataType;

/**
 * Expression node: <code>&lt;expr&gt;:methodName(parameters)</code>
 */
public class MethodCallNode extends ExpressionNode {
  private String methodName = "";

  public MethodCallNode(ProToken t, JPNode parent, int num, boolean hasChildren, String methodName) {
    super(t, parent, num, hasChildren);
    this.methodName = Strings.nullToEmpty(methodName);
  }

  public String getMethodName() {
    return methodName;
  }

  @Override
  public DataType getDataType() {
    ProgramRootNode root = getTopLevelParent();
    if (root == null)
      return DataType.NOT_COMPUTED;

    if (getFirstChild() instanceof SystemHandleNode) {
      SystemHandleNode shn = (SystemHandleNode) getFirstChild();
      return shn.getMethodDataType(methodName.toUpperCase());
    } else if (getFirstChild() instanceof FieldRefNode) {
      if (((FieldRefNode) getFirstChild()).isStaticReference()) {
        ITypeInfo info = ((FieldRefNode) getFirstChild()).getStaticReference();
        return getObjectMethodDataType(root.getTypeInfoProvider(), findDirectChild(ABLNodeType.METHOD_PARAM_LIST), info,
            methodName);
      } else if ((getFirstChild().getSymbol() instanceof Event)
          && ("publish".equalsIgnoreCase(methodName) || "subscribe".equalsIgnoreCase(methodName))) {
        // Events only have Publish / Subscribe
        return DataType.VOID;
      }
    }

    // Left-Handle expression has to be a class
    IExpression expr = getFirstChild().asIExpression();
    if (expr != null) {
      DataType dataType = expr.getDataType();
      if (dataType.getPrimitive() == PrimitiveDataType.CLASS) {
        ITypeInfo info = root.getEnvironment().getTypeInfo(dataType.getClassName());
        return getObjectMethodDataType(getTopLevelParent().getTypeInfoProvider(),
            findDirectChild(ABLNodeType.METHOD_PARAM_LIST), info, methodName);
      } else if (dataType.getPrimitive() == PrimitiveDataType.HANDLE) {
        return getStandardMethodDataType(methodName.toUpperCase());
      }
    } else {
      // Within constructor: super(...) or this-object(...)
      JPNode firstChild = getFirstChild();
      JPNode nextChild = firstChild.getNextSibling();
      if (((firstChild.getNodeType() == ABLNodeType.SUPER) || (firstChild.getNodeType() == ABLNodeType.THISOBJECT))
          && (nextChild.getNodeType() == ABLNodeType.LEFTPAREN)) {
        return new DataType(root.getTypeInfo().getTypeName());
      }
    }
    return DataType.NOT_COMPUTED;
  }

}
