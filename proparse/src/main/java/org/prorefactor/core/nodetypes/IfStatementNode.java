package org.prorefactor.core.nodetypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;

public class IfStatementNode extends StatementBlockNode {
  private JPNode thenNode;
  private JPNode elseNode;
  private IStatement thenBlockOrNode;
  private IStatement elseBlockOrNode;

  public IfStatementNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren, null);
  }

  @Override
  public void setFirstStatement(IStatement firstStatement) {
    throw new IllegalStateException("Can't attach first statement to IF node");
  }

  public void setThenNode(JPNode thenNode) {
    this.thenNode = thenNode;
  }

  public void setElseNode(JPNode elseNode) {
    this.elseNode = elseNode;
  }

  public void setThenBlockOrNode(IStatement thenBlockOrNode) {
    this.thenBlockOrNode = thenBlockOrNode;
  }

  public void setElseBlockOrNode(IStatement elseBlockOrNode) {
    this.elseBlockOrNode = elseBlockOrNode;
  }

  /**
   * Return THEN keyword
   */
  @Nonnull
  public JPNode getThenNode() {
    return thenNode;
  }

  /**
   * Return the THEN statement or statement block
   */
  @Nonnull
  public IStatement getThenBlockOrNode() {
    return thenBlockOrNode;
  }

  /**
   * Return ELSE keyword
   */
  @Nullable
  public JPNode getElseNode() {
    return elseNode;
  }

  /**
   * Return the THEN statement or statement block
   */
  @Nullable
  public IStatement getElseBlockOrNode() {
    return elseBlockOrNode;
  }
}
