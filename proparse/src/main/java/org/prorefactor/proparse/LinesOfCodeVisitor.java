/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
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
package org.prorefactor.proparse;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.misc.IntervalSet;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;

public class LinesOfCodeVisitor extends StatementVisitor {
  private Set<String> annotations = new HashSet<>();
  private Map<Integer, IntervalSet> counts = new HashMap<>();

  public LinesOfCodeVisitor() {
    this("");
  }

  public LinesOfCodeVisitor(String annotations) {
    Collections.addAll(this.annotations, annotations.split(","));
  }

  public Map<Integer, IntervalSet> getCounts() {
    return counts;
  }

  public void visitStatement(IStatement stmt) {
    if ((stmt.asJPNode().getNodeType() == ABLNodeType.DEFINE) || (stmt.asJPNode().getNodeType() == ABLNodeType.END))
      return;
    visitNode(stmt.asJPNode());
  }

  private void visitNode(JPNode node) {
    if (node.isNatural()) {
      counts.computeIfAbsent(node.getFileIndex(), it -> new IntervalSet()) //
        .add(node.getLine(), node.getEndLine());
    }
    for (JPNode child : node.getDirectChildren()) {
      if (!child.isIStatement())
        visitNode(child);
    }
  }

  @Override
  boolean preVisitStatementBlock(IStatementBlock block) {
    if (block.asJPNode().isIStatement()) {
      for (String ann : annotations) {
        if (block.asJPNode().hasAnnotation(ann))
          return false;
      }
    }
    return true;
  }

}
