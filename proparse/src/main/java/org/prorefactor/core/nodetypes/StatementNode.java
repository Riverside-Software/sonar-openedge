/********************************************************************************
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
package org.prorefactor.core.nodetypes;

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.Block;

public class StatementNode extends JPNode implements IStatement {
  private IStatement previousStatement;
  private IStatement nextStatement;
  private IStatementBlock parentStatement;
  private List<String> annotations;
  private ABLNodeType state2;
  private Block inBlock;

  public StatementNode(ProToken t, JPNode parent, int num, boolean hasChildren, ABLNodeType state2) {
    super(t, parent, num, hasChildren);
    this.state2 = state2;
  }

  @Override
  public IStatement getPreviousStatement() {
    return previousStatement;
  }

  @Override
  public IStatement getNextStatement() {
    return nextStatement;
  }

  @Override
  public IStatementBlock getParentStatement() {
    return parentStatement;
  }

  @Override
  public ABLNodeType getNodeType2() {
    return state2;
  }

  @Override
  public int getState2() {
    return state2 != null ? state2.getType() : 0;
  }

  @Override
  public boolean isStatement() {
    return true;
  }

  @Override
  public boolean isIStatement() {
    return true;
  }

  @Override
  public JPNode asJPNode() {
    return this;
  }

  @Override
  public IStatement asIStatement() {
    return this;
  }

  @Override
  public void setPreviousStatement(IStatement statement) {
    this.previousStatement = statement;
  }

  @Override
  public void setNextStatement(IStatement statement) {
    this.nextStatement = statement;
  }

  @Override
  public void setParentStatement(IStatementBlock statement) {
    this.parentStatement = statement;
  }

  @Override
  public List<String> getAnnotations() {
    return annotations;
  }

  @Override
  public void addAnnotation(String annotation) {
    if (annotations == null)
      annotations = new ArrayList<>();
    annotations.add(annotation);
  }

  @Override
  public boolean hasAnnotation(String str) {
    if (currNodeHasAnnotation(str))
      return true;
    else if (parentStatement != null)
      return parentStatement.asJPNode().hasAnnotation(str);
    else
      return false;
  }

  private boolean currNodeHasAnnotation(String str) {
    if (annotations == null)
      return false;
    return annotations.stream().anyMatch(it -> it.equals(str) || it.startsWith(str + '('));
  }

  @Override
  public Block getEnclosingBlock() {
    return inBlock;
  }

  @Override
  public void setInBlock(Block inBlock) {
    this.inBlock = inBlock;
  }

}
