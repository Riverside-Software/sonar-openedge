package org.prorefactor.core;

public enum AttributeValue {
  FALSE(IConstants.FALSE),
  TRUE(IConstants.TRUE),
  ST_VARIABLE(IConstants.ST_VAR),
  ST_DBTABLE(IConstants.ST_DBTABLE),
  ST_TTABLE(IConstants.ST_TTABLE),
  ST_WTABLE(IConstants.ST_WTABLE);

  int key;

  private AttributeValue(int key) {
    this.key = key;
  }

  public int getKey() {
    return key;
  }

  public String getName() {
    return name().toLowerCase().replace('_', '-');
  }
}