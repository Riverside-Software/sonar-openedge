/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
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
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.PrimitiveDataType;

/**
 * Expression node: <code>&lt;expr&gt;:attributeName</code>. Can also be a reference to an enum value, a reference to
 * local class variable, a reference to a static property, ...
 */
public class AttributeReferenceNode extends ExpressionNode {
  private String attributeName = "";
  private boolean computed = false;
  private Pair<ITypeInfo, IPropertyElement> property = null;
  private Pair<ITypeInfo, IVariableElement> variable = null;
  private DataType returnDataType = DataType.NOT_COMPUTED;

  public AttributeReferenceNode(ProToken t, JPNode parent, int num, boolean hasChildren, String attributeName) {
    super(t, parent, num, hasChildren);
    this.attributeName = Strings.nullToEmpty(attributeName);
  }

  public String getAttributeName() {
    return attributeName;
  }

  private void handleSystemHandleNode(SystemHandleNode node, ProgramRootNode root) {
    if (node.getFirstChild().getNodeType() == ABLNodeType.THISOBJECT) {
      var typeInfo = root.getTypeInfoProvider().apply(root.getClassName());
      property = typeInfo == null ? null : typeInfo.lookupProperty(root.getTypeInfoProvider(), attributeName);
      var v1 = (typeInfo != null) && (property == null) ? typeInfo.lookupVariable(attributeName) : null;
      variable = v1 == null ? null : Pair.of(typeInfo, v1);
    } else if (node.getFirstChild().getNodeType() == ABLNodeType.SUPER) {
      var typeInfo = root.getTypeInfoProvider().apply(root.getClassName());
      typeInfo = typeInfo == null ? null : root.getTypeInfoProvider().apply(typeInfo.getParentTypeName());
      property = typeInfo == null ? null : typeInfo.lookupProperty(root.getTypeInfoProvider(), attributeName);
      var v1 = (typeInfo != null) && (property == null) ? typeInfo.lookupVariable(attributeName) : null;
      variable = v1 == null ? null : Pair.of(typeInfo, v1);
    } else {
      returnDataType = node.getAttributeDataType(attributeName.toUpperCase());
    }
  }

  private void handleFieldRefNode(FieldRefNode node, ProgramRootNode root) {
    if (node.isStaticReference()) {
      property = node.getStaticReference().lookupProperty(root.getTypeInfoProvider(), attributeName);
      var v1 = (property == null) ? node.getStaticReference().lookupVariable(attributeName) : null;
      variable = v1 == null ? null : Pair.of(node.getStaticReference(), v1);
    } else if (node.getSymbol() instanceof Event) {
      // Events only have Publish / Subscribe, no properties
    } else if (node.isIExpression()) {
      handleExpression(node.asIExpression(), root);
    }
  }

  private void handleExpression(IExpression expr, ProgramRootNode root) {
    var dataType = expr.getDataType();
    if (dataType.getPrimitive() == PrimitiveDataType.CLASS) {
      var typeInfo = root.getTypeInfoProvider().apply(dataType.getClassName());
      property = typeInfo == null ? null : typeInfo.lookupProperty(root.getTypeInfoProvider(), attributeName);
      var v1 = (typeInfo != null) && (property == null) ? typeInfo.lookupVariable(attributeName) : null;
      variable = v1 == null ? null : Pair.of(typeInfo, v1);
    } else if (dataType.getPrimitive() == PrimitiveDataType.HANDLE) {
      returnDataType = getStandardAttributeDataType(attributeName.toUpperCase());
    }
  }

  private void compute() {
    var root = getTopLevelParent();
    if (root == null)
      return;

    var firstChild = getFirstChild();
    if (firstChild instanceof SystemHandleNode shn) {
      handleSystemHandleNode(shn, root);
    } else if (firstChild instanceof FieldRefNode frn) {
      handleFieldRefNode(frn, root);
    } else if (firstChild.isIExpression()) {
      handleExpression(firstChild.asIExpression(), root);
    }

    if (property != null)
      returnDataType = property.getO2().getVariable().getDataType();
    else if (variable != null)
      returnDataType = variable.getO2().getDataType();
  }

  public synchronized ITypeInfo getTypeInfo() {
    if (!computed) {
      compute();
      computed = true;
    }
    if ((property == null) && (variable == null))
      return null;
    else if (property != null)
      return property.getO1();
    else
      return variable.getO1();
  }

  @Override
  public synchronized DataType getDataType() {
    if (!computed) {
      compute();
      computed = true;
    }
    return returnDataType;
  }

  public synchronized IPropertyElement getPropertyElement() {
    if (!computed) {
      compute();
      computed = true;
    }
    return property == null ? null : property.getO2();
  }

  public synchronized IVariableElement getVariableElement() {
    if (!computed) {
      compute();
      computed = true;
    }
    return variable == null ? null : variable.getO2();
  }

  public synchronized boolean isVariable() {
    if (!computed) {
      compute();
      computed = true;
    }
    return variable != null;
  }

  public synchronized boolean isProperty() {
    if (!computed) {
      compute();
      computed = true;
    }
    return property != null;
  }
}
