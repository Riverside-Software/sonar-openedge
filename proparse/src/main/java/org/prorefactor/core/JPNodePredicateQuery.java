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
package org.prorefactor.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class JPNodePredicateQuery implements ICallback<List<JPNode>> {
  public static final Predicate<JPNode> MAIN_FILE_ONLY = node -> node.getFileIndex() == 0;
  public static final Predicate<JPNode> STATEMENT_ONLY = JPNode::isStateHead;

  private final List<JPNode> result = new ArrayList<>();
  private final Predicate<JPNode> predicate;

  public JPNodePredicateQuery(Predicate<JPNode> pred1) {
    predicate = pred1;
  }

  public JPNodePredicateQuery(Predicate<JPNode> pred1, Predicate<JPNode> pred2) {
    predicate = pred1.and(pred2);
  }

  public JPNodePredicateQuery(Predicate<JPNode> pred1, Predicate<JPNode> pred2, Predicate<JPNode> pred3) {
    predicate = pred1.and(pred2).and(pred3);
  }

  @Override
  public List<JPNode> getResult() {
    return result;
  }

  @Override
  public boolean visitNode(JPNode node) {
    if (predicate.test(node))
      result.add(node);

    return true;
  }

}
