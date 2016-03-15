package org.prorefactor.treeparser;

public class TreeParserException extends Exception {
  private static final long serialVersionUID = 4003548635999211002L;

  public TreeParserException(String msg) {
    super(msg);
  }
  
  public TreeParserException(Throwable t) {
    super(t);
  }
  
  public TreeParserException(String msg, Throwable t) {
    super(msg, t);
  }
}
