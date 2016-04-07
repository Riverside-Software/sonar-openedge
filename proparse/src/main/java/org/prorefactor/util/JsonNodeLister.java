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
package org.prorefactor.util;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;

/**
 * Prints out the structure of a JPNode AST as JSON.
 */
public class JsonNodeLister {
  private final JPNode topNode;
  private final PrintWriter ofile;
  private final Set<Integer> ignored = new HashSet<>();

  public JsonNodeLister(JPNode topNode, PrintWriter writer, Integer[] ignoredKeywords) {
    this.topNode = topNode;
    this.ofile = writer;
    for (Integer i : ignoredKeywords) {
      ignored.add(i);
    }
  }

  /**
   * Print node content to PrintWriter
   */
  public void print() {
    printSub(topNode, true);
  }

  /**
   * Print node and children
   * 
   * @param node Node to be printed
   * @param firstElem First child of parent element ?
   * @return False if node is skipped
   */
  private boolean printSub(JPNode node, boolean firstElem) {
    if (ignored.contains(node.getType()))
      return false;
//    if ((node.getType() == NodeTypes.LEFTPAREN) || (node.getType() == NodeTypes.RIGHTPAREN) || (node.getType() == NodeTypes.COMMA) || (node.getType() == NodeTypes.PERIOD) || (node.getType() == NodeTypes.LEXCOLON) || (node.getType() == NodeTypes.OBJCOLON) || (node.getType() == NodeTypes.THEN) || (node.getType() == NodeTypes.END))
//      return false;
    if (!firstElem) {
      ofile.write(',');
    }
    ofile.write('{');
    printAttributes(node);
    if (!node.getDirectChildren().isEmpty()) {
      boolean firstChild = true;
      ofile.write(", \"children\": [");
      ofile.println();
      for (JPNode child : node.getDirectChildren()) {
        // Next element won't be first child anymore if this element is printed
        firstChild &= !printSub(child, firstChild);
      }
      ofile.write(']');
    }
    ofile.write('}');
    ofile.println();
    
    return true;
  }

  private void printAttributes(JPNode node) {
    ofile.write("\"name\": \"" + NodeTypes.getTypeName(node.getType()));
    if (node.getType() == NodeTypes.ID) {
      ofile.write(" [");
      ofile.write(node.getText().replace('\'', ' ').replace('"', ' '));
      ofile.write("]");
    }
    ofile.write("\", \"head\": " + (node.isStateHead() ? "true" : "false") + ", \"line\": " + node.getLine() + ", \"column\": " + node.getColumn() + ", \"file\": " + node.getFileIndex());
  }

}
