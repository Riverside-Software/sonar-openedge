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
import org.prorefactor.treeparser.symbols.Routine;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.DataType;

/**
 * Expression node: <code>functionName(parameters)</code> (only in procedures)
 */
public class UserFunctionCallNode extends ExpressionNode {
  private String functionName = "";

  public UserFunctionCallNode(ProToken t, JPNode parent, int num, boolean hasChildren, String functionName) {
    super(t, parent, num, hasChildren);
    this.functionName = Strings.nullToEmpty(functionName);
  }

  public String getFunctionName() {
    return functionName;
  }

  @Override
  public DataType getDataType() {
    ProgramRootNode root = getTopLevelParent();
    if (root == null)
      return DataType.NOT_COMPUTED;

    for (Routine r : root.getRootScope().getRoutines()) {
      if (r.getName().equalsIgnoreCase(functionName))
        return r.getReturnDatatypeNode();
    }

    return DataType.NOT_COMPUTED;
  }

}
