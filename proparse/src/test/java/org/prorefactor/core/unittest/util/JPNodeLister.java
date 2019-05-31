/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.core.unittest.util;

import java.io.PrintWriter;
import java.util.Arrays;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;

/**
 * Prints out the structure of a JPNode AST. Prints nodes one per line, using indentation to show the tree structure.
 */
public class JPNodeLister {
  private final static int INDENT_BY = 4;

  private final JPNode topNode;
  private final PrintWriter ofile;

  public JPNodeLister(JPNode topNode, PrintWriter writer) {
    this.topNode = topNode;
    this.ofile = writer;
  }

  /**
   * Print node content to PrintWriter with default settings
   */
  public void print() {
    print(' ');
  }

  /**
   * Print node content to PrintWriter with specified spacer char
   */
  public void print(char spacer) {
    print(spacer, false, false, false, false);
  }

  /**
   * Print node content to PrintWriter
   */
  public void print(char spacer, boolean showLine, boolean showCol, boolean showFileName, boolean showStore) {
    print_sub(topNode, 0, spacer, showLine, showCol, showFileName, showStore);
  }

  protected String getExtraInfo(JPNode node, char spacer) {
    return "";
  }

  private void print_sub(JPNode node, int level, char spacer, boolean showLine, boolean showCol, boolean showFileName,
      boolean showStore) {
    printline(node, level, spacer, showLine, showCol, showFileName, showStore);
    for (JPNode child : node.getDirectChildren()) {
      print_sub(child, level + 1, spacer, showLine, showCol, showFileName, showStore);
    }
  }

  private void printline(JPNode node, int level, char spacer, boolean showLine, boolean showCol, boolean showFileName,
      boolean showStore) {
    // Indent node
    char[] indentArray = new char[level * INDENT_BY];
    Arrays.fill(indentArray, ' ');
    ofile.write(indentArray);

    // Node type
    ofile.append(node.getNodeType().name()).append(spacer);
    // Node text
    ofile.append(node.getText()).append(spacer);
    if (showLine)
      ofile.append(Integer.toString(node.getLine())).append(spacer);
    if (showCol)
      ofile.append(Integer.toString(node.getColumn())).append(spacer);
    if (showFileName)
      ofile.append(Integer.toString(node.getFileIndex())).append(spacer);
    if (showStore) {
      String storetype = node.attrGetS(IConstants.STORETYPE);
      if (storetype.length() != 0)
        ofile.append(storetype).append(spacer);
    }
    ofile.append(getExtraInfo(node, spacer));
    ofile.println();
  }

}
