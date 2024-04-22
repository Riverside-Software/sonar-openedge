/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2024 Riverside Software
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

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
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
  private final Set<ABLNodeType> ignored;

  public JsonNodeLister(JPNode topNode, Writer writer, ABLNodeType ignoredKw, ABLNodeType... ignoredKws) {
    this.topNode = topNode;
    this.ofile = writer;
    this.ignored = EnumSet.of(ignoredKw, ignoredKws);
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
    if (ignored.contains(node.getNodeType()))
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
