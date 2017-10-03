/*******************************************************************************
 * Copyright (c) 2016 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class JPNodeQuery implements ICallback<List<JPNode>> {
  private final List<JPNode> result = new ArrayList<>();
  private final Set<ABLNodeType> findTypes;
  private final boolean stateHeadOnly;
  private final boolean mainFileOnly;

  @Deprecated
  public JPNodeQuery(Integer... types) {
    this(false, false, types);
  }

  @Deprecated
  public JPNodeQuery(boolean stateHeadOnly, Integer... types) {
    this(stateHeadOnly, false, types);
  }

  @Deprecated
  public JPNodeQuery(boolean stateHeadOnly, boolean mainFileOnly, Integer... types) {
    this.stateHeadOnly = stateHeadOnly;
    this.mainFileOnly = mainFileOnly;
    this.findTypes = new HashSet<>();
    for (Integer i : types) {
      findTypes.add(ABLNodeType.getNodeType(i));
    }
  }

  public JPNodeQuery(ABLNodeType type, ABLNodeType... types) {
    this(false, false, type, types);
  }

  public JPNodeQuery(boolean stateHeadOnly, ABLNodeType type, ABLNodeType... types) {
    this(stateHeadOnly, false, type , types);
  }

  public JPNodeQuery(boolean stateHeadOnly, boolean mainFileOnly, ABLNodeType type,  ABLNodeType... types) {
    this.stateHeadOnly = stateHeadOnly;
    this.mainFileOnly = mainFileOnly;
    this.findTypes = EnumSet.of(type, types);
  }

  @Override
  public List<JPNode> getResult() {
    return result;
  }

  @Override
  public boolean visitNode(JPNode node) {
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
