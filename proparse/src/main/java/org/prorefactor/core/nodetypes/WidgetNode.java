/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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

public class WidgetNode extends JPNode implements IExpression {

  public WidgetNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Override
  public boolean isExpression() {
    return true;
  }

  @Override
  public JPNode asJPNode() {
    return this;
  }

  @Override
  public DataType getDataType() {
    return DataType.HANDLE;
  }

  public DataType getMethodDataType(String id) {
    return DataType.NOT_COMPUTED;
  }

}
