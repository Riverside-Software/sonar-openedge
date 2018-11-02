/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.proparse.antlr4;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;

class JPNodeQuery implements ICallback<List<JPNode>> {
  private final List<JPNode> result = new ArrayList<>();
  private final Set<ABLNodeType> findTypes;
  private final boolean stateHeadOnly;
  private final boolean mainFileOnly;
  private final JPNode currStatement;

  /**
   * @deprecated Since 2.1.3, use {@link JPNodeQuery#JPNodeQuery(ABLNodeType, ABLNodeType...)}
   */
  @Deprecated
  public JPNodeQuery(Integer... types) {
    this(false, false, null, types);
  }

  /**
   * @deprecated Since 2.1.3, use {@link JPNodeQuery#JPNodeQuery(boolean, ABLNodeType, ABLNodeType...)}
   */
  @Deprecated
  public JPNodeQuery(boolean stateHeadOnly, Integer... types) {
    this(stateHeadOnly, false, null, types);
  }

  /**
   * @deprecated Since 2.1.3, use {@link JPNodeQuery#JPNodeQuery(boolean, boolean, ABLNodeType, ABLNodeType...)}
   */
  @Deprecated
  public JPNodeQuery(boolean stateHeadOnly, boolean mainFileOnly, JPNode currentStatement, Integer... types) {
    this.stateHeadOnly = stateHeadOnly;
    this.mainFileOnly = mainFileOnly;
    if ((currentStatement != null) && (currentStatement.getStatement() != null)) {
      this.currStatement = currentStatement.getStatement();
    } else {
      this.currStatement = null;
    }
    this.findTypes = new HashSet<>();
    for (Integer i : types) {
      findTypes.add(ABLNodeType.getNodeType(i));
    }
  }

  public JPNodeQuery(ABLNodeType type, ABLNodeType... types) {
    this(false, false, null, type, types);
  }

  public JPNodeQuery(boolean stateHeadOnly, ABLNodeType type, ABLNodeType... types) {
    this(stateHeadOnly, false, null, type , types);
  }

  public JPNodeQuery(boolean stateHeadOnly, boolean mainFileOnly, JPNode currentStatement, ABLNodeType type,  ABLNodeType... types) {
    this.stateHeadOnly = stateHeadOnly;
    this.mainFileOnly = mainFileOnly;
    this.currStatement = currentStatement;
    this.findTypes = EnumSet.of(type, types);
  }

  @Override
  public List<JPNode> getResult() {
    return result;
  }

  @Override
  public boolean visitNode(JPNode node) {
    if ((currStatement != null) && (node.getStatement() != currStatement))
      return false;

    if (mainFileOnly && (node.getFileIndex() > 0))
      return true;

    if (stateHeadOnly && !node.isStateHead())
      return true;

    if (findTypes.isEmpty() || findTypes.contains(node.getNodeType())) {
      result.add(node);
    }

    return true;
  }

}
