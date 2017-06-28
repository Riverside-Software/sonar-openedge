package eu.rssw.pct.elements;

public enum ElementKind {
  UNKNOWN(0),
  METHOD(1),
  VARIABLE(2),
  TABLE(3),
  BUFFER(4),
  QUERY(5),
  DATASET(6),
  DATASOURCE(7),
  PROPERTY(8),
  EVENT(9);
  
  private final int num;

  private ElementKind(int num) {
    this.num = num;
  }

  public int getNum() {
    return this.num;
  }

  public static ElementKind getKind(int type) {
    for (ElementKind t : ElementKind.values()) {
      if (t.num == type) {
        return t;
      }
    }
    return UNKNOWN;

  }
}
