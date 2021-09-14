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

public class NamedMemberNode extends JPNode implements IExpression {
  private String namedMember = "";

  public NamedMemberNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    this(t, parent, num, hasChildren, "");
  }

  public NamedMemberNode(ProToken t, JPNode parent, int num, boolean hasChildren, String namedMember) {
    super(t, parent, num, hasChildren);
    this.namedMember = Strings.nullToEmpty(namedMember);
  }

  public String getNamedMember() {
    return namedMember;
  }

  @Override
  public boolean isExpression() {
    return true;
  }

  @Override
  public JPNode asJPNode() {
    return this;
  }

}
