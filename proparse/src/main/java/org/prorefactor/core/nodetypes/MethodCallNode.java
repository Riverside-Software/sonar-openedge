/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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
import org.prorefactor.core.Pair;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.symbols.Event;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.PrimitiveDataType;

/**
 * Expression node: <code>&lt;expr&gt;:methodName(parameters)</code>
 */
public class MethodCallNode extends ExpressionNode {
  private final String methodName;
  private boolean computed = false;
  private Pair<ITypeInfo, IMethodElement> method = null;
  private DataType returnDataType = DataType.NOT_COMPUTED;

  public MethodCallNode(ProToken t, JPNode parent, int num, boolean hasChildren, String methodName) {
    super(t, parent, num, hasChildren);
    this.methodName = Strings.nullToEmpty(methodName);
  }

  public String getMethodName() {
    return methodName;
  }

  private void handleSystemHandleNode(SystemHandleNode node, ProgramRootNode root) {
    if (node.getFirstChild().getNodeType() == ABLNodeType.THISOBJECT) {
      ITypeInfo typeInfo = root.getEnvironment().getTypeInfo(root.getClassName());
      method = typeInfo == null ? null : getObjectMethod(root.getTypeInfoProvider(),
          this.findDirectChild(ABLNodeType.METHOD_PARAM_LIST), typeInfo, methodName);
      returnDataType = method == null ? DataType.NOT_COMPUTED : method.getO2().getReturnType();
    } else if (node.getFirstChild().getNodeType() == ABLNodeType.SUPER) {
      ITypeInfo info = root.getEnvironment().getTypeInfo(root.getClassName());
      info = info == null ? null : root.getEnvironment().getTypeInfo(info.getParentTypeName());
      method = info == null ? null : getObjectMethod(root.getTypeInfoProvider(),
          this.findDirectChild(ABLNodeType.METHOD_PARAM_LIST), info, methodName);
      returnDataType = method == null ? DataType.NOT_COMPUTED : method.getO2().getReturnType();
    } else {
      returnDataType = node.getMethodDataType(methodName.toUpperCase());
    }
  }

  private void handleFieldRefNode(FieldRefNode node, ProgramRootNode root) {
    if (node.isStaticReference()) {
      method = getObjectMethod(root.getTypeInfoProvider(), findDirectChild(ABLNodeType.METHOD_PARAM_LIST), node.getStaticReference(),
          methodName);
      returnDataType = method == null ? DataType.NOT_COMPUTED : method.getO2().getReturnType();
    } else if ((node.getSymbol() instanceof Event)
        && ("publish".equalsIgnoreCase(methodName) || "subscribe".equalsIgnoreCase(methodName))) {
      // Events only have Publish / Subscribe
      returnDataType = DataType.VOID;
    } else if (node.isIExpression()) {
      handleExpression(node.asIExpression(), root);
    }
  }

  private void handleExpression(IExpression expr, ProgramRootNode root) {
    DataType dataType = expr.getDataType();
    if (dataType.getPrimitive() == PrimitiveDataType.CLASS) {
      ITypeInfo typeInfo = root.getEnvironment().getTypeInfo(dataType.getClassName());
      method = typeInfo == null ? null : getObjectMethod(getTopLevelParent().getTypeInfoProvider(),
          findDirectChild(ABLNodeType.METHOD_PARAM_LIST), typeInfo, methodName);
      returnDataType = method == null ? DataType.NOT_COMPUTED : method.getO2().getReturnType();
    } else if (dataType.getPrimitive() == PrimitiveDataType.HANDLE) {
      returnDataType = getStandardMethodDataType(methodName.toUpperCase());
    }
  }

  private void handleNonExpression(JPNode firstChild, ProgramRootNode root) {
    // Within constructor: super(...) or this-object(...)
    JPNode nextChild = firstChild.getNextSibling();
    if (((firstChild.getNodeType() == ABLNodeType.SUPER) || (firstChild.getNodeType() == ABLNodeType.THISOBJECT))
        && (nextChild.getNodeType() == ABLNodeType.LEFTPAREN)) {
      if (root.getTypeInfo() == null) {
        returnDataType = new DataType(root.getRootScope().getClassName());
      } else {
        returnDataType = new DataType(root.getTypeInfo().getTypeName());
      }
    }
  }

  private void compute() {
    // Default
    ProgramRootNode root = getTopLevelParent();
    if (root == null)
      return;

    JPNode firstChild = getFirstChild();
    if (firstChild instanceof SystemHandleNode) {
      handleSystemHandleNode((SystemHandleNode) firstChild, root);
    } else if (firstChild instanceof FieldRefNode) {
      handleFieldRefNode((FieldRefNode) firstChild, root);
    } else if (firstChild.isIExpression()) {
      handleExpression(firstChild.asIExpression(), root);
    } else {
      handleNonExpression(firstChild, root);
    }
  }

  public synchronized ITypeInfo getTypeInfo() {
    if (!computed) {
      compute();
      computed = true;
    }
    return method == null ? null : method.getO1();
  }

  @Override
  public synchronized DataType getDataType() {
    if (!computed) {
      compute();
      computed = true;
    }
    return returnDataType;
  }

  public synchronized IMethodElement getMethodElement() {
    if (!computed) {
      compute();
      computed = true;
    }
    return method == null ? null : method.getO2();
  }

}
