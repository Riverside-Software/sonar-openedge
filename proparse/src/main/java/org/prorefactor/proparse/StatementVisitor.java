/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
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
package org.prorefactor.proparse;

import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;

/**
 * Visit all statements in a block, and execute a callback for each statement. Can be used with ParseUnit.getTopNode()
 */
public abstract class StatementVisitor {

  /**
   * Visitor's entrypoint
   */
  public final void walkStatementBlock(IStatementBlock block) {
    if (!preVisitStatementBlock(block))
      return;
    if (block.asJPNode().isIStatement())
      visitStatement(block.asJPNode().asIStatement());

    var currNode = block.getFirstStatement();
    while (currNode != null) {
      if (currNode instanceof IfStatementNode ifStmtNode) {
        walkIfStatementNode(ifStmtNode);
      } else if (currNode.asJPNode().isIStatementBlock()) {
        walkStatementBlock(currNode.asJPNode().asIStatementBlock());
      } else {
        visitStatement(currNode);
      }
      currNode = currNode.getNextStatement();
    }
    postVisitStatementBlock(block);
  }

  private final void walkIfStatementNode(IfStatementNode ifStmtNode) {
    visitStatement(ifStmtNode);
    // Then block or node
    walkIfThenOrElse(ifStmtNode.getThenBlockOrNode());
    // Else block or node
    var elseNode = ifStmtNode.getElseBlockOrNode();
    if (elseNode != null) {
      walkIfThenOrElse(elseNode);
    }
  }

  private final void walkIfThenOrElse(IStatement node) {
    if (node instanceof IfStatementNode ifStmtNode2) {
      walkIfStatementNode(ifStmtNode2);
    } else if (node.asJPNode().isIStatementBlock()) {
      walkStatementBlock(node.asJPNode().asIStatementBlock());
    } else {
      visitStatement(node);
    }
  }

  // Return false to skip this statement block
  boolean preVisitStatementBlock(IStatementBlock block) {
    return true;
  }

  void postVisitStatementBlock(IStatementBlock block) {
    // Nothing
  }

  abstract void visitStatement(IStatement stmt);
}
