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

import com.google.common.base.Strings;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.PrimitiveDataType;

public class AttributeReferenceNode extends JPNode implements IExpression {
  private String attributeName = "";

  public AttributeReferenceNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    this(t, parent, num, hasChildren, "");
  }

  public AttributeReferenceNode(ProToken t, JPNode parent, int num, boolean hasChildren, String attributeName) {
    super(t, parent, num, hasChildren);
    this.attributeName = Strings.nullToEmpty(attributeName);
  }

  public String getAttributeName() {
    return attributeName;
  }

  @Override
  public DataType getDataType() {
    if (getFirstChild() instanceof SystemHandleNode) {
      SystemHandleNode shn = (SystemHandleNode) getFirstChild();
      return shn.getAttributeDataType(attributeName.toUpperCase());
    }

    ProgramRootNode root = getTopLevelParent();
    if (root == null)
      return DataType.UNKNOWN;

    // Left-Handle expression has to be a class
    IExpression expr = (IExpression) getFirstChild();
    if (expr.getDataType().getDataType() == PrimitiveDataType.CLASS) {
      ITypeInfo info = root.getParserSupport().getProparseSession().getTypeInfo(expr.getDataType().getClassName());
      if (info != null) {
        for (IPropertyElement m : info.getProperties()) {
          if (m.getName().equalsIgnoreCase(attributeName))
            return m.getVariable().getDataType();
        }
        for (IVariableElement e: info.getVariables()) {
          if (e.getName().equalsIgnoreCase(attributeName))
            return e.getDataType();
        }
      }
    } else if (expr.getDataType().getDataType() == PrimitiveDataType.HANDLE) {
      // On va tenter quoi ??
      return ExpressionNode.getStandardAttributeDataType(attributeName.toUpperCase());
    }

    return DataType.UNKNOWN;
  }

  @Override
  public boolean isExpression() {
    return true;
  }

}
