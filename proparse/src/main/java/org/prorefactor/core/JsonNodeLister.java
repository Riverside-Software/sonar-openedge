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

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints out the structure of a JPNode AST as JSON.
 */
public class JsonNodeLister {
  private static final Logger LOG = LoggerFactory.getLogger(JsonNodeLister.class);

  private final JPNode topNode;
  private final Writer ofile;
  private final Set<Integer> ignored = new HashSet<>();

  public JsonNodeLister(JPNode topNode, Writer writer, Integer[] ignoredKeywords) {
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
    try {
      printSub(topNode, true);
    } catch (IOException uncaught) {
      LOG.error("Unable to write output");
    }
  }

  /**
   * Print node and children
   * 
   * @param node Node to be printed
   * @param firstElem First child of parent element ?
   * @return False if node is skipped
   */
  private boolean printSub(JPNode node, boolean firstElem) throws IOException {
    if (ignored.contains(node.getType()))
      return false;
    if (!firstElem) {
      ofile.write(',');
    }
    ofile.write('{');
    printAttributes(node);
    if (!node.getDirectChildren().isEmpty()) {
      boolean firstChild = true;
      ofile.write(", \"children\": [");
      for (JPNode child : node.getDirectChildren()) {
        // Next element won't be first child anymore if this element is printed
        firstChild &= !printSub(child, firstChild);
      }
      ofile.write(']');
    }
    ofile.write('}');
    
    return true;
  }

  private void printAttributes(JPNode node) throws IOException {
    ofile.write("\"name\": \"" + node.getNodeType());
    if (node.getNodeType() == ABLNodeType.ID) {
      ofile.write(" [");
      ofile.write(node.getText().replace('\'', ' ').replace('"', ' '));
      ofile.write("]");
    }
    ofile.write("\", \"head\": " + (node.isStateHead() ? "true" : "false") + ", \"line\": " + node.getLine() + ", \"column\": " + node.getColumn() + ", \"file\": " + node.getFileIndex());
  }

}
