/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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
package org.prorefactor.core.nodetypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.Block;

/**
 * Specialized type of JPNode for those token types: DO, FOR, REPEAT, FUNCTION, PROCEDURE, CONSTRUCTOR, DESTRUCTOR,
 * METHOD, CATCH, ON
 */
public class StatementBlockNode extends StatementNode implements IStatementBlock {
  private final String label;
  private Block block;
  private IStatement firstStatement;

  public StatementBlockNode(ProToken t, JPNode parent, int num, boolean hasChildren, ABLNodeType state2) {
    this(t, parent, num, hasChildren, state2, null);
  }

  public StatementBlockNode(ProToken t, JPNode parent, int num, boolean hasChildren, ABLNodeType state2, String label) {
    super(t, parent, num, hasChildren, state2);
    this.label = label;
  }

  public String getBlockLabel() {
    return label;
  }

  @Override
  public IStatement getFirstStatement() {
    return firstStatement;
  }

  @Override
  public void setFirstStatement(IStatement firstStatement) {
    this.firstStatement = firstStatement;
  }

  @Nullable
  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public void setBlock(@Nonnull Block block) {
    this.block = block;
  }

  @Override
  public boolean hasBlock() {
    return block != null;
  }

  @Override
  public boolean isIStatementBlock() {
    return true;
  }

  @Override
  public IStatementBlock asIStatementBlock() {
    return this;
  }

}
