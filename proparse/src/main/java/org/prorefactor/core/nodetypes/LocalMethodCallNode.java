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

import org.prorefactor.core.JPNode;
import org.prorefactor.core.Pair;
import org.prorefactor.core.ProToken;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;

/**
 * Expression node: <code>methodName(parameters)</code> (only in classes)
 */
public class LocalMethodCallNode extends ExpressionNode {
  private final String methodName;
  private boolean computed = false;
  private Pair<ITypeInfo, IMethodElement> method = null;

  public LocalMethodCallNode(ProToken t, JPNode parent, int num, boolean hasChildren, String methodName) {
    super(t, parent, num, hasChildren);
    this.methodName = Strings.nullToEmpty(methodName);
  }

  public String getMethodName() {
    return methodName;
  }

  private void compute() {
    ProgramRootNode root = getTopLevelParent();
    if (root != null) {
      ITypeInfo typeInfo = root.getTypeInfo();
      if (root.isClass() && (typeInfo == null))
        typeInfo = root.getTypeInfoProvider().apply(BuiltinClasses.PLO_CLASSNAME);
      method = typeInfo == null ? null : getObjectMethod(root.getTypeInfoProvider(), this, typeInfo, methodName);
    }
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

  @Override
  public DataType getDataType() {
    IMethodElement tmp = getMethodElement();
    return tmp == null ? DataType.NOT_COMPUTED : tmp.getReturnType();
  }

}
