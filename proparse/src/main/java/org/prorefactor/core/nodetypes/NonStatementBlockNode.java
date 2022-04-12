/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
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

import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.Block;

/**
 * Specialized type of JPNode for those token types: PROPERTY_GETTER, PROPERTY_SETTER, THEN, ELSE
 */
public class NonStatementBlockNode extends JPNode implements IStatementBlock {
  private Block block;
  private IStatement firstStatement;
  private IStatementBlock parentStatement;

  public NonStatementBlockNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Override
  public void setFirstStatement(IStatement firstStatement) {
    this.firstStatement = firstStatement;
  }

  @Override
  public IStatement getFirstStatement() {
    return firstStatement;
  }

  @Override
  public IStatementBlock getParentStatement() {
    return parentStatement;
  }

  @Override
  public void setParentStatement(IStatementBlock statement) {
    this.parentStatement = statement;
  }

  @Override
  public boolean hasBlock() {
    return block != null;
  }

  @Override
  public Block getBlock() {
    return block;
  }

  @Override
  public void setBlock(Block block) {
    this.block = block;
  }

  @Override
  public JPNode asJPNode() {
    return this;
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
