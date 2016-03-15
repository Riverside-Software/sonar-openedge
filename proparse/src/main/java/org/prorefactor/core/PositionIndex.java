/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides an index of JPNode objects, sorted by fileIndex, line, and column.
 */
public class PositionIndex {
  /**
   * Comparator for sorting by fileIndex/line/col.
   */
  private static final Comparator<JPNode> FILE_POSITION = new Comparator<JPNode>() {
    @Override
    public int compare(JPNode n1, JPNode n2) {
      int ret;
      ret = n1.getFileIndex() - n2.getFileIndex();
      if (ret != 0)
        return ret;
      ret = n1.getLine() - n2.getLine();
      if (ret != 0)
        return ret;
      return n1.getColumn() - n2.getColumn();
    }
  };

  private SortedSet<JPNode> nodeSet = new TreeSet<>(FILE_POSITION);

  public void addNode(JPNode node) {
    nodeSet.add(node);
  }

  /** Get the node at a position, or the next node immediately after the position. */
  public JPNode getNodeFrom(int file, int line, int col) {
    SortedSet<JPNode> tailSet = nodeSet.tailSet(new JPNode(file, line, col));
    if (tailSet.isEmpty())
      return null;
    return tailSet.first();
  }

}
