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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class JPNodeQuery implements ICallback<List<JPNode>> {
  private final List<JPNode> result = new ArrayList<>();
  private final Set<Integer> findTypes;
  private final boolean stateHeadOnly;

  public JPNodeQuery(Integer... types) {
    this(false, types);
  }

  public JPNodeQuery(boolean stateHeadOnly, Integer... types) {
    this.stateHeadOnly = stateHeadOnly;
    findTypes = new HashSet<>();
    for (Integer i : types) {
      findTypes.add(i);
    }
  }

  @Override
  public List<JPNode> getResult() {
    return result;
  }

  @Override
  public boolean visitNode(JPNode node) {
    if (stateHeadOnly && !node.isStateHead())
      return true;

    if (findTypes.isEmpty() || findTypes.contains(node.getType())) {
      result.add(node);
    }

    return true;
  }

}
