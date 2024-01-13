/********************************************************************************
 * Copyright (c) 2015-2023 Riverside Software
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
package org.prorefactor.core.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserSymbolScope;

public class StatementFlowWriter extends AbstractProparseTest {

  BufferedWriter writer = null;

  /**
   * Parse and write a source file, with comments detailing some of the node attributes added by TreeParser01.
   * 
   * @param inName  Name of the compile unit's source file.
   * @param outName Name of the file to write out to.
   * @throws IOException
   */
  public void write(String inName, File outName, RefactorSession session) throws IOException {
    try {
      ParseUnit pu = getParseUnit(new File(inName), session);
      pu.treeParser01();
      writer = new BufferedWriter(new FileWriter(outName));
      walker(pu.getTopNode().asIStatementBlock(), null, 0);
      writer.newLine();
    } finally {
      if (writer != null)
        writer.close();
    }
  }

  private void walker(IStatementBlock node, TreeParserSymbolScope scope, int level) throws IOException {
    writeNode(node.asJPNode(), scope, level);

    IStatement ch = node.getFirstStatement();
    while (ch != null) {
      if (ch instanceof IfStatementNode) {
        IfStatementNode ifNode = (IfStatementNode) ch;
        writeNode(ifNode.asJPNode(), scope, level + 2);
        writeNode(ifNode.getThenNode(), scope, level + 4);
        if (ifNode.getThenBlockOrNode() instanceof IStatementBlock)
          walker((IStatementBlock) ifNode.getThenBlockOrNode(), node.asJPNode().hasBlock() ? node.asJPNode().getBlock().getSymbolScope() : scope, level + 6);
        else
          writeNode(ifNode.getThenBlockOrNode().asJPNode(), scope, level + 6);
        if (ifNode.getElseNode() != null) {
          writeNode(ifNode.getElseNode(), scope, level + 4);
          if (ifNode.getElseBlockOrNode() instanceof IStatementBlock)
            walker((IStatementBlock) ifNode.getElseBlockOrNode(), node.asJPNode().hasBlock() ? node.asJPNode().getBlock().getSymbolScope() : scope, level + 6);
          else
            writeNode(ifNode.getElseBlockOrNode().asJPNode(), scope, level + 6);
        }
      } else if (ch.asJPNode().isIStatementBlock()) {
        walker(ch.asJPNode().asIStatementBlock(), node.asJPNode().hasBlock() ? node.asJPNode().getBlock().getSymbolScope() : scope, level + 2);
      } else {
        writeNode(ch.asJPNode(), scope, level + 2);
      }
      ch = ch.getNextStatement();
    }
  }

  private void writeNode(JPNode node, TreeParserSymbolScope scope, int level) throws IOException {
    writer.write(indent(level));
    writer.write(node.getNodeType().toString());
    writer.write(" ");
    writer.write(node.getPositionString());

    if (node.hasBlock()) {
      writer.write(" -- Block");
      if (node.hasBlock() && (node.getBlock().getSymbolScope() != scope))
        writer.write(" -- Scope");
    }
    writer.newLine();
  }

  private String indent(int level) {
    return java.nio.CharBuffer.allocate(level).toString().replace('\0', ' ');
  }

}
