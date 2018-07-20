/*******************************************************************************
 * Copyright (c) 2018 Riverside Software
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.Set;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints out the structure of a JPNode AST as plain text.
 */
public class TreeNodeLister {
  private static final Logger LOG = LoggerFactory.getLogger(TreeNodeLister.class);

  private final JPNode topNode;
  private final Writer ofile;
  private final Set<ABLNodeType> ignored;

  public TreeNodeLister(JPNode topNode, Writer writer, ABLNodeType ignoredKw, ABLNodeType... ignoredKws) {
    this.topNode = topNode;
    this.ofile = writer;
    this.ignored = EnumSet.of(ignoredKw, ignoredKws);
  }

  /**
   * Print node content to PrintWriter
   */
  public void print() {
    try {
      printSub(topNode, 0);
    } catch (IOException uncaught) {
      LOG.error("Unable to write output", uncaught);
    }
  }

  /**
   * Print node and children
   * 
   * @param node Node to be printed
   * @param firstElem First child of parent element ?
   * @return False if node is skipped
   */
  private boolean printSub(JPNode node, int tabs) throws IOException {
    if (ignored.contains(node.getNodeType()))
      return false;
    printAttributes(node, tabs);
    if (!node.getDirectChildren().isEmpty()) {
      for (JPNode child : node.getDirectChildren()) {
        printSub(child, tabs + 1);
      }
    }
    return true;
  }

  private void printAttributes(JPNode node, int tabs) throws IOException {
    ofile.write(String.format("%3s %s", tabs, java.nio.CharBuffer.allocate(tabs).toString().replace('\0', ' ')));
    ofile.write(node.getNodeType() + (node.isStateHead() ? "^ " : " ") + (node.isStateHead() && node.getState2() != 0 ? node.getState2() : ""));
    if (node.attrGet(IConstants.OPERATOR) == IConstants.TRUE)
      ofile.write("°");
    if (node.attrGet(IConstants.STORETYPE) > 0)
      ofile.write("StoreType " + node.attrGet(IConstants.STORETYPE) + " ");
    if ((node.getNodeType() == ABLNodeType.ID) || (node.getNodeType() == ABLNodeType.TYPE_NAME)) {
      ofile.write("[");
      ofile.write(node.getText().replace('\'', ' ').replace('"', ' '));
      ofile.write("] ");
    }
    if (!"".equals(node.attrGetS(IConstants.QUALIFIED_CLASS_INT)))
      ofile.write(" Qualified name: '" + node.attrGetS(IConstants.QUALIFIED_CLASS_INT) + "'");
    ofile.write(String.format("@F%d:%d:%d", node.getFileIndex(), node.getLine(), node.getColumn()));
    ofile.write("\n");
  }

}
