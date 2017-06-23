package eu.rssw.pct;

public enum ParameterType {
  VARIABLE(2),
  TABLE(3),
  BUFFER(4),
  QUERY(5),
  DATASET(6),
  DATA_SOURCE(7),
  FORM(8),
  BROWSE(9),
  BUFFER_TEMP_TABLE(103),
  UNKNOWN(-1);

  private final int num;

  private ParameterType(int num) {
    this.num = num;
  }

  public int getNum() {
    return this.num;
  }

  public String getName() {
    return this.name().replace('_', '-');
  }

  public static ParameterType getParameterType(int type) {
    for (ParameterType t : ParameterType.values()) {
      if (t.num == type) {
        return t;
      }
    }
    return UNKNOWN;

  }
}
