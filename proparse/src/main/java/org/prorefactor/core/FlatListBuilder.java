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
import java.util.List;

class FlatListBuilder implements ICallback<List<JPNode>> {
  private final List<JPNode> result = new ArrayList<>();
  
  @Override
  public List<JPNode> getResult() {
    return result;
  }

  @Override
  public boolean visitNode(JPNode node) {
    if (node.attrGet(IConstants.OPERATOR) == IConstants.TRUE) {
      // Consider that an operator only has 2 children
      visitNode(node.getFirstChild());
      result.add(node);
      visitNode(node.getFirstChild().getNextSibling());
      return false;
    } else {
      result.add(node);
    }
    return true;
  }

}
