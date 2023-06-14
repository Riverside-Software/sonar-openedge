package org.prorefactor.core;

public class JPErrorNode extends JPNode {

  protected JPErrorNode(ProToken token, JPNode parent, int num, boolean hasChildren) {
    super(token, parent, num, hasChildren);
  }

  @Override
  public boolean isErrorNode() {
    return true;
  }

}
