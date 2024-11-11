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

import java.util.List;

import javax.annotation.Nullable;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.Block;

public interface IStatement {
  /**
   * @return True only if node is a non-synthetic statement. Different from isIStatement().
   */
  boolean isStatement();

  /**
   * @return Secondary node type, i.e. VARIABLE in DEFINE VARIABLE statement. Can be null
   */
  @Nullable
  ABLNodeType getNodeType2();

  /**
   * @return Statement immediately before this one in the code flow. Null if first statement of block.
   */
  @Nullable
  IStatement getPreviousStatement();

  /**
   * @return Statement immediately following this one in the code flow
   */
  @Nullable
  IStatement getNextStatement();

  /**
   * @return Block this statement belongs to. Null when called on ProgramRootNode
   */
  @Nullable
  IStatementBlock getParentStatement();

  /**
   * @return Text of annotations attached to the current statement
   */
  List<String> getAnnotations();

  /**
   * @return Annotation statements attached to the current statement
   */
  List<AnnotationStatementNode> getAnnotationStatements();

  Block getEnclosingBlock();

  void setInBlock(Block inBlock);

  void setPreviousStatement(IStatement statement);

  void setNextStatement(IStatement statement);

  void setParentStatement(IStatementBlock statement);

  void addAnnotation(AnnotationStatementNode annotation);

  JPNode asJPNode();
}
