package eu.rssw.pct;

public enum ParameterMode {
  INPUT(6028),
  OUTPUT(6049),
  INPUT_OUTPUT(6110),
  BUFFER(1070),
  RETURN(-1);

  private final int num;

  private ParameterMode(int num) {
    this.num = num;
  }

  public int getRCodeConstant() {
    return this.num;
  }

  public String getName() {
    return name().replace('_', '-');
  }

  public static ParameterMode getParameterMode(int mode) {
    for (ParameterMode m : ParameterMode.values()) {
      if (m.num == mode) {
        return m;
      }
    }
    return INPUT;
  }
}
