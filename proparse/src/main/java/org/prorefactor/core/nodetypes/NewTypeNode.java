/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;

/**
 * Expression node: <code>NEW typeName(parameters)</code>
 */
public class NewTypeNode extends AbstractMethodCallNode {
  private Pair<ITypeInfo, IMethodElement> method = null;
  private DataType returnDataType = DataType.NOT_COMPUTED;
  private boolean computed = false;

  public NewTypeNode(ProToken t, JPNode parent, int num, boolean hasChildren, String methodName) {
    super(t, parent, num, hasChildren, methodName);
  }

  private void compute() {
    ProgramRootNode root = getTopLevelParent();
    if (root == null)
      return;

    var typeNameNode = (TypeNameNode) getFirstChild().getNextSibling();
    var typeInfo = root.getEnvironment().getTypeInfo(typeNameNode.getQualName());
    method = typeInfo == null ? null : getObjectConstructor(getTopLevelParent().getTypeInfoProvider(),
        findDirectChild(ABLNodeType.PARAMETER_LIST), typeInfo);
    returnDataType = new DataType(typeNameNode.getQualName());
  }

  @Override
  public synchronized DataType getDataType() {
    if (!computed) {
      compute();
      computed = true;
    }
    return returnDataType;
  }

  public synchronized ITypeInfo getTypeInfo() {
    if (!computed) {
      compute();
      computed = true;
    }
    return method == null ? null : method.getO1();
  }

  public synchronized IMethodElement getMethodElement() {
    if (!computed) {
      compute();
      computed = true;
    }
    return method == null ? null : method.getO2();
  }

}
