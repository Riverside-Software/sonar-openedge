package org.prorefactor.macrolevel;

public class MacroPosition {
  private final int fileNum;
  private final int line;
  private final int column;
  
  public MacroPosition(int fileNum, int line, int column) {
    this.fileNum = fileNum;
    this.line = line;
    this.column = column;
  }
  
  public int getFileNum() {
    return fileNum;
  }
  
  public int getLine() {
    return line;
  }
  
  public int getColumn() {
    return column;
  }
}
