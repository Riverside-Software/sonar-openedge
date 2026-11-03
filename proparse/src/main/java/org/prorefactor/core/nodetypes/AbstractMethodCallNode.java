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
import org.prorefactor.core.ProToken;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;

public abstract class AbstractMethodCallNode extends ExpressionNode {
  private final String methodName;

  AbstractMethodCallNode(ProToken t, JPNode parent, int num, boolean hasChildren, String methodName) {
    super(t, parent, num, hasChildren);
    this.methodName = Strings.nullToEmpty(methodName);
  }

  public String getMethodName() {
    return methodName;
  }

  public abstract ITypeInfo getTypeInfo();
  public abstract IMethodElement getMethodElement();
}
