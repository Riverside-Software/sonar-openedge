package org.prorefactor.treeparser;

import antlr.SemanticException;

public class TreeParserException extends SemanticException {

  private static final long serialVersionUID = 3643827078682041743L;

  public TreeParserException(String msg) {
    super(msg);
  }

  public TreeParserException(String s, String fileName, int line, int column) {
    super(s, fileName, line, column);
  }
}
