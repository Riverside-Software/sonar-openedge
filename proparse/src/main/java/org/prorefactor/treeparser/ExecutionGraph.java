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
package org.prorefactor.treeparser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;

public class ExecutionGraph {
  private static final Predicate<IStatement> IS_BLOCK = it -> (it.getNodeType() == ABLNodeType.FUNCTION)
      || (it.getNodeType() == ABLNodeType.PROCEDURE) || (it.getNodeType() == ABLNodeType.METHOD)
      || (it.getNodeType() == ABLNodeType.ON);

  private final List<JPNode> vertices = new ArrayList<>();
  private final List<List<Integer>> edges = new ArrayList<>();
  private final List<List<Integer>> revEdges = new ArrayList<>();

  public ExecutionGraph() {
    // No-op
  }

  public static ExecutionGraph of(JPNode rootNode) {
    var g = new ExecutionGraph();
    if (rootNode.isIStatementBlock()) {
      g.addVerticesAndEdges(rootNode.asIStatementBlock(), null);
    }
    return g;
  }

  public void addVertex(JPNode vertex) {
    if (!vertices.contains(vertex)) {
      vertices.add(vertex);
      edges.add(new ArrayList<>());
      revEdges.add(new ArrayList<>());
    }
  }

  public void addEdge(JPNode from, JPNode to) {
    int fromIndex = vertices.indexOf(from);
    int toIndex = vertices.indexOf(to);

    if ((fromIndex == -1) || (toIndex == -1))
      return;

    List<Integer> list = edges.get(fromIndex);
    if (!list.contains(toIndex))
      list.add(toIndex);
    var list2 = revEdges.get(toIndex);
    if (!list2.contains(fromIndex))
      list2.add(fromIndex);

  }

  public List<JPNode> getVertices() {
    return vertices;
  }

  public List<List<Integer>> getEdges() {
    return edges;
  }

  public List<List<Integer>> getReverseEdges() {
    return revEdges;
  }

  public List<Integer> getEdges(JPNode node) {
    var idx = vertices.indexOf(node);
    return idx == -1 ? List.of() : edges.get(idx); 
  }

  public List<Integer> getReverseEdges(JPNode node) {
    var idx = vertices.indexOf(node);
    return idx == -1 ? List.of() : revEdges.get(idx); 
  }

  private IStatement getNextNonRoutineStatement(IStatement stmt) {
    var currStmt = stmt.getNextStatement();
    while ((currStmt != null) && IS_BLOCK.test(currStmt)) {
      currStmt = currStmt.getNextStatement();
    }
    return currStmt;
  }

  private void addVerticesAndEdges(IStatementBlock block, IStatement exitStmt) {
    var currStmt = block.getFirstStatement();
    var prevStmt = block.asJPNode();

    addVertex(block.asJPNode());

    while (currStmt != null) {
      if (IS_BLOCK.test(currStmt)) {
        currStmt = currStmt.getNextStatement();
        continue;
      }

      if (currStmt instanceof IfStatementNode ifStmt) {
        var tmp = getNextNonRoutineStatement(currStmt);
        addVertices(ifStmt, tmp == null ? exitStmt : tmp);
      } else if (currStmt instanceof IStatementBlock stmtBlock) {
        var tmp = getNextNonRoutineStatement(currStmt);
        addVerticesAndEdges(stmtBlock, tmp == null ? exitStmt : tmp);
      } else {
        addVertex(currStmt.asJPNode());
      }
      addEdge(prevStmt, currStmt.asJPNode());

      prevStmt = currStmt.asJPNode();
      currStmt = currStmt.getNextStatement();
    }
    if ((exitStmt != null) && (prevStmt != null)) {
      addVertex(exitStmt.asJPNode());
      addEdge(prevStmt, exitStmt.asJPNode());
    }
  }

  private void addVertices(IfStatementNode ifNode, IStatement exitStmt) {
    addVertex(ifNode.asJPNode());

    if (ifNode.getThenBlockOrNode() instanceof IfStatementNode ifNode2)
      addVertices(ifNode2, exitStmt);
    else if (ifNode.getThenBlockOrNode() instanceof IStatementBlock stmtBlock) {
      addVerticesAndEdges(stmtBlock, exitStmt);
    } else {
      addVertex(ifNode.getThenBlockOrNode().asJPNode());
      if (exitStmt != null) {
        addVertex(exitStmt.asJPNode());
        addEdge(ifNode.getThenBlockOrNode().asJPNode(), exitStmt.asJPNode());
      }
    }
    addEdge(ifNode.asJPNode(), ifNode.getThenBlockOrNode().asJPNode());

    if (ifNode.getElseBlockOrNode() != null) {
      if (ifNode.getElseBlockOrNode() instanceof IfStatementNode ifNode2)
        addVertices(ifNode2, exitStmt);
      else if (ifNode.getElseBlockOrNode() instanceof IStatementBlock stmtBlock)
        addVerticesAndEdges(stmtBlock, exitStmt);
      else {
        addVertex(ifNode.getElseBlockOrNode().asJPNode());
        if (exitStmt != null) {
          addVertex(exitStmt.asJPNode());
          addEdge(ifNode.getElseBlockOrNode().asJPNode(), exitStmt.asJPNode());
        }
      }

      addEdge(ifNode.asJPNode(), ifNode.getElseBlockOrNode().asJPNode());
    }

    if (exitStmt != null) {
      addVertex(exitStmt.asJPNode());
      addEdge(ifNode.asJPNode(), exitStmt.asJPNode());
    }
  }

  /**
   * Generate Mermaid input (https://mermaid.js.org)
   */
  public String toMermaidString() {
    var sb = new StringBuilder("flowchart TD\n");
    // Write all nodes first
    for (int zz = 0; zz < vertices.size(); zz++) {
      sb.append("Node").append(zz) //
        .append('[') //
        .append(vertices.get(zz).getNodeType().toString()) //
        .append(' ').append(vertices.get(zz).getLine()).append(':').append(vertices.get(zz).getColumn()) //
        .append(']') //
        .append('\n');
    }
    // Then the paths between nodes
    for (int zz = 0; zz < edges.size(); zz++) {
      for (var targetNode : edges.get(zz)) {
        sb.append("Node").append(zz) //
          .append(" --> Node").append(targetNode) //
          .append('\n');
      }
    }

    return sb.toString();
  }

}
