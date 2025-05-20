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
 * Visit all statements in a block, and execute a callback for each statement. Can be used with walkStatementBlock(ParseUnit.getTopNode())
 */
public abstract class StatementListener {

  /**
   * Listener's entrypoint
   */
  public final void walkStatementBlock(IStatementBlock block) {
    if (!enterStatementBlock(block))
      return;
    if (block.asJPNode().isIStatement())
      enterStatement(block.asJPNode().asIStatement());

    var currNode = block.getFirstStatement();
    while (currNode != null) {
      handleStatement(currNode);
      currNode = currNode.getNextStatement();
    }

    if (block.asJPNode().isIStatement())
      exitStatement(block.asJPNode().asIStatement());
    exitStatementBlock(block);
  }

  private final void handleStatement(IStatement stmt) {
    if (stmt instanceof IfStatementNode ifStmtNode) {
      walkIfStatementNode(ifStmtNode);
    } else if (stmt.asJPNode().isIStatementBlock()) {
      walkStatementBlock(stmt.asJPNode().asIStatementBlock());
    } else {
      enterStatement(stmt);
      exitStatement(stmt);
    }
  }

  private final void walkIfStatementNode(IfStatementNode ifStmtNode) {
    enterStatement(ifStmtNode);

    // Then block or node
    enterThen(ifStmtNode.getThenBlockOrNode());
    handleStatement(ifStmtNode.getThenBlockOrNode());
    exitThen(ifStmtNode.getThenBlockOrNode());

    // Else block or node
    var elseNode = ifStmtNode.getElseBlockOrNode();
    if (elseNode != null) {
      enterElse(elseNode);
      handleStatement(elseNode);
      exitElse(elseNode);
    }

    exitStatement(ifStmtNode);
  }

  public void enterThen(IStatement stmt) {
    // Nothing
  }

  public void exitThen(IStatement stmt) {
    // Nothing
  }

  public void enterElse(IStatement stmt) {
    // Nothing
  }

  public void exitElse(IStatement stmt) {
    // Nothing
  }

  /**
   * Return false to skip this statement block
   * @param block Statement block
   */
  public boolean enterStatementBlock(IStatementBlock block) {
    return true;
  }

  public void exitStatementBlock(IStatementBlock block) {
    // Nothing
  }

  public void enterStatement(IStatement stmt) {
    // Nothing
  }

  public void exitStatement(IStatement stmt) {
    // Nothing
  }
}
