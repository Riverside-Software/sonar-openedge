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
package org.prorefactor.core.unittest.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;

public class AttributedWriter {

  BufferedWriter writer = null;

  private String getAttributes(JPNode node) {
    StringBuffer nodeComments = new StringBuffer("");
    getAttributesForSymbol(node, nodeComments);
    getAttributesForBlock(node, nodeComments);
    if (nodeComments.length() > 0) {
      nodeComments.insert(0, " /*");
      nodeComments.append(" */ ");
      if (node.getType() == NodeTypes.Program_root)
        nodeComments.append(System.getProperty("line.separator"));
    }
    return nodeComments.toString();
  } // getAttributes

  private void getAttributesForBlock(JPNode node, StringBuffer nodeComments) {
    Block block = (Block) node.getLink(IConstants.BLOCK);
    if (block == null)
      return;
    TableBuffer[] buffers = block.getBlockBuffers();
    if (buffers.length == 0)
      return;
    // Collect the names in a sorted set, so we can write them in
    // a consistent (sorted) order. Important for running automated
    // unit/regression tests.
    TreeSet<String> names = new TreeSet<String>();
    for (TableBuffer buffSymbol : buffers) {
      StringBuffer name = new StringBuffer();
      if (buffSymbol.getTable().getStoretype() == IConstants.ST_DBTABLE) {
        name.append(buffSymbol.getTable().getDatabase().getName());
        name.append(".");
      }
      name.append(buffSymbol.getName());
      names.add(name.toString());
    }
    nodeComments.append(" buffers=");
    int i = 0;
    for (String name : names) {
      if (i++ > 0)
        nodeComments.append(",");
      nodeComments.append(name);
    }
  }

  private void getAttributesForSymbol(JPNode node, StringBuffer nodeComments) {
    Symbol symbol = (Symbol) node.getLink(IConstants.SYMBOL);
    if (symbol == null)
      return;
    nodeComments.append(" ");
    nodeComments.append(symbol.getScope().depth());
    nodeComments.append(":");
    nodeComments.append(symbol.fullName());
    if (node.attrGet(IConstants.ABBREVIATED) > 0)
      nodeComments.append(" abbrev");
    if (node.attrGet(IConstants.UNQUALIFIED_FIELD) > 0)
      nodeComments.append(" unqualfield");
  }

  private void walker(JPNode node, boolean showSiblings) throws IOException {
    if (node == null)
      return;
    if (node.attrGet(IConstants.OPERATOR) == IConstants.TRUE) {
      walker(node.firstChild(), false);
      writeNode(node);
      walker(node.firstChild().nextSibling(), true);
    } else {
      writeNode(node);
      walker(node.firstChild(), true);
    }
    if (showSiblings)
      walker(node.nextSibling(), true);
  }

  /**
   * Parse and write a source file, with comments detailing some of the node attributes added by TreeParser01.
   * 
   * @param inName Name of the compile unit's source file.
   * @param outName Name of the file to write out to.
   */
  public void write(String inName, File outName, RefactorSession session) throws Exception {
    try {
      ParseUnit pu = new ParseUnit(new File(inName), session);
      pu.treeParser01();
      writer = new BufferedWriter(new FileWriter(outName));
      walker(pu.getTopNode(), true);
    } finally {
      if (writer != null)
        writer.close();
    }
  }

  private void writeNode(JPNode node) throws IOException {
    ProToken t = node.getHiddenFirst();
    while (t != null) {
      writer.write(t.getText());
      t = t.getNext();
    }
    writer.write(getAttributes(node));
    writer.write(node.getText());
  }

}
