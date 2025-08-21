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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.Pair;
import org.prorefactor.core.nodetypes.BuiltinFunctionNode;
import org.prorefactor.core.nodetypes.IExpression;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.LocalMethodCallNode;
import org.prorefactor.core.nodetypes.MethodCallNode;
import org.prorefactor.core.nodetypes.SingleArgumentExpression;
import org.prorefactor.core.nodetypes.TwoArgumentsExpression;
import org.prorefactor.core.nodetypes.UserFunctionCallNode;

import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;

public class CognitiveComplexityListener extends StatementListener {
  private static final EnumSet<ABLNodeType> EXTERNAL_BLOCK = EnumSet.of(ABLNodeType.PROCEDURE, ABLNodeType.PROGRAM_ROOT,
      ABLNodeType.FUNCTION, ABLNodeType.METHOD);
  private static final EnumSet<ABLNodeType> NESTING_INCREMENT = EnumSet.of(ABLNodeType.ELSE, ABLNodeType.CASE,
      ABLNodeType.FOR, ABLNodeType.REPEAT, ABLNodeType.DO, ABLNodeType.ON);

  private final List<Pair<JPNode, Integer>> items = new ArrayList<>();
  private final IStatementBlock routineBlock;
  private final ITypeInfo typeInfo;
  private final IMethodElement methdElem;

  private int nesting = 0;

  public CognitiveComplexityListener(IStatementBlock block) {
    this(block, null, null);
  }

  public CognitiveComplexityListener(IStatementBlock block, ITypeInfo typeInfo, IMethodElement elem) {
    this.routineBlock = block;
    this.typeInfo = typeInfo;
    this.methdElem = elem;
  }

  public int getComplexity() {
    // Each item increases complexity by 1 + nesting level
    return items.stream().mapToInt(it -> it.getO2() + 1).sum();
  }

  public int getMainFileComplexity() {
    // Each item in main file increases complexity by 1 + nesting level
    return items.stream() //
      .filter(it -> it.getO1().getFileIndex() == 0) //
      .mapToInt(it -> it.getO2() + 1) //
      .sum();
  }

  public List<Pair<JPNode, Integer>> getItems() {
    return items;
  }

  @Override
  public boolean enterStatementBlock(IStatementBlock block) {
    return (block == this.routineBlock) || !EXTERNAL_BLOCK.contains(block.asJPNode().getNodeType());
  }

  @Override
  public void exitStatementBlock(IStatementBlock block) {
    var nodeType = block.asJPNode().getNodeType();
    var parentNodeType = block.asJPNode().getParent() == null ? null : block.asJPNode().getParent().getNodeType();
    var thenOrElseDo = (parentNodeType != null) && (parentNodeType == ABLNodeType.THEN)
        || (parentNodeType == ABLNodeType.ELSE);

    if ((block != this.routineBlock) && NESTING_INCREMENT.contains(nodeType)) {
      if ((nodeType != ABLNodeType.DO) || !isSimpleDo(block.asJPNode()) || thenOrElseDo)
        nesting--;
    }
  }

  @Override
  public void enterThen(IStatement stmt) {
    // In ELSE IF ?
    if (stmt.asJPNode().getParent().getParent() != stmt.getParentStatement().asJPNode()) {
      items.add(Pair.of(stmt.asJPNode().getParent().getParent().getParent(), 0));
    } else {
      items.add(Pair.of(stmt.asJPNode().getParent(), nesting));
    }
  }

  @Override
  public void enterElse(IStatement stmt) {
    if (stmt.getNodeType() == ABLNodeType.IF)
      return;
    items.add(Pair.of(stmt.asJPNode().getParent(), 0));
  }

  @Override
  public void enterStatement(IStatement stmt) {
    var nodeType = stmt.getNodeType();

    // Check all expressions
    for (var expr : stmt.asJPNode().queryExpressionsCurrentStatement()) {
      visitExpression(expr);
    }

    // Automatically add complexity
    if ((nodeType == ABLNodeType.CASE) || (nodeType == ABLNodeType.CATCH) || (nodeType == ABLNodeType.REPEAT)) {
      items.add(Pair.of(stmt.asJPNode(), nesting));
    }

    // Add complexity if node is a real DO block
    var simpleDo = false;
    var thenOrElseDo = false;
    if (nodeType == ABLNodeType.DO) {
      simpleDo = isSimpleDo(stmt.asJPNode());
      var parentNodeType = stmt.asJPNode().getParent().getNodeType();
      thenOrElseDo = (parentNodeType == ABLNodeType.THEN) || (parentNodeType == ABLNodeType.ELSE);
      if (!thenOrElseDo)
        items.add(Pair.of(stmt.asJPNode(), nesting));
    }

    // Implementation has to match exitStatementBlock()
    if ((stmt instanceof IStatementBlock block) && (block != this.routineBlock)
        && NESTING_INCREMENT.contains(nodeType)) {
      if ((nodeType != ABLNodeType.DO) || !simpleDo || thenOrElseDo)
        nesting++;
    }
  }

  void visitExpression(IExpression exprNode) {
    if (exprNode.asJPNode().getParent().getNodeType() == ABLNodeType.WHERE
        && (exprNode.asJPNode().getParent().getParent().getNodeType() == ABLNodeType.RECORD_SEARCH)) {
      items.add(Pair.of(exprNode.asJPNode().getParent(), 0));
    }
    if (exprNode instanceof TwoArgumentsExpression expr2) {
      var nodeType = exprNode.asJPNode().getNodeType();
      if ((nodeType == ABLNodeType.XOR) || (nodeType == ABLNodeType.AND) || (nodeType == ABLNodeType.OR)) {
        items.add(Pair.of(exprNode.asJPNode(), 0));
      }
      visitExpression(expr2.getLeftExpression());
      visitExpression(expr2.getRightExpression());
    } else if (exprNode instanceof SingleArgumentExpression expr2) {
      visitExpression(expr2.getExpression());
    } else if (exprNode instanceof BuiltinFunctionNode expr2) {
      if (expr2.asJPNode().getFirstChild().getNodeType() == ABLNodeType.IF) {
        items.add(Pair.of(expr2.asJPNode().getFirstChild(), nesting));
      } else {
        visitChildren(exprNode);
      }
    } else if (exprNode instanceof UserFunctionCallNode) {
      visitChildren(exprNode);
    } else if (exprNode instanceof LocalMethodCallNode expr2) {
      checkMethodCall(exprNode.asJPNode().firstNaturalChild(), expr2.getTypeInfo(), expr2.getMethodElement());
    } else if (exprNode instanceof MethodCallNode expr2) {
      checkMethodCall(exprNode.asJPNode().firstNaturalChild(), expr2.getTypeInfo(), expr2.getMethodElement());
    }
  }

  private void checkMethodCall(JPNode node, ITypeInfo exprTypeInfo, IMethodElement exprElem) {
    if ((exprTypeInfo != null) && (exprElem != null) && exprTypeInfo.equals(typeInfo) && exprElem.equals(methdElem)) {
      items.add(Pair.of(node, 0));
    }
  }

  private void visitChildren(IExpression exprNode) {
    for (JPNode ch : exprNode.asJPNode().getDirectChildren()) {
      for (IExpression expr : ch.queryExpressions()) {
        visitExpression(expr);
      }
    }
  }

  private boolean isSimpleDo(JPNode node) {
    // node is DO node, check if only child is WIH FRAME
    if (node.getDirectChildren().isEmpty())
      return true;
    var simpleDo = true;
    for (var ch : node.getDirectChildren()) {
      if (ch.getNodeType() == ABLNodeType.LEXCOLON)
        return simpleDo;
      simpleDo = simpleDo && (ch.getNodeType() == ABLNodeType.WITH);
    }
    return false;
  }
}
