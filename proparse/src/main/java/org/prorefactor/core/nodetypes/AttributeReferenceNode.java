/********************************************************************************
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

import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.PrimitiveDataType;

/**
 * Expression node: <code>&lt;expr&gt;:attributeName</code>. Can also be a reference to an enum value.
 */
public class AttributeReferenceNode extends ExpressionNode {
  private String attributeName = "";

  public AttributeReferenceNode(ProToken t, JPNode parent, int num, boolean hasChildren, String attributeName) {
    super(t, parent, num, hasChildren);
    this.attributeName = Strings.nullToEmpty(attributeName);
  }

  public String getAttributeName() {
    return attributeName;
  }

  @Override
  public DataType getDataType() {
    ProgramRootNode root = getTopLevelParent();
    if (root == null)
      return DataType.NOT_COMPUTED;

    if (getFirstChild() instanceof SystemHandleNode) {
      SystemHandleNode shn = (SystemHandleNode) getFirstChild();
      return shn.getAttributeDataType(attributeName.toUpperCase());
    } else if ((getFirstChild() instanceof FieldRefNode) && ((FieldRefNode) getFirstChild()).isStaticReference()) {
      ITypeInfo info = ((FieldRefNode) getFirstChild()).getStaticReference();
      return ExpressionNode.getObjectAttributeDataType(getTopLevelParent().getEnvironment(), info, attributeName,
          false);
    }

    // Left-Handle expression has to be a class
    IExpression expr = getFirstChild().asIExpression();
    PrimitiveDataType pdt = expr.getDataType().getPrimitive();
    if (pdt == PrimitiveDataType.CLASS) {
      ITypeInfo info = root.getEnvironment().getTypeInfo(expr.getDataType().getClassName());
      if (info != null) {
        for (IPropertyElement m : info.getProperties()) {
          if (m.getName().equalsIgnoreCase(attributeName))
            return m.getVariable().getDataType();
        }
        for (IVariableElement e : info.getVariables()) {
          if (e.getName().equalsIgnoreCase(attributeName))
            return e.getDataType();
        }
      }
    } else if (pdt == PrimitiveDataType.HANDLE) {
      return ExpressionNode.getStandardAttributeDataType(attributeName.toUpperCase());
    }

    return DataType.NOT_COMPUTED;
  }

}
