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

import org.prorefactor.core.JPNode;

public class ExecutionGraph {
  private final List<JPNode> vertices = new ArrayList<>();
  private final List<List<Integer>> edges = new ArrayList<>();

  public ExecutionGraph() {
    // No-op
  }

  public void addVertex(JPNode vertex) {
    if (!vertices.contains(vertex)) {
      vertices.add(vertex);
      edges.add(new ArrayList<>());
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
  }

  public List<JPNode> getVertices() {
    return vertices;
  }

  public List<List<Integer>> getEdges() {
    return edges;
  }
}
