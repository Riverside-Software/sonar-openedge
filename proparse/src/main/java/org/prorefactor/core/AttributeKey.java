package org.prorefactor.core;

public enum AttributeKey {
  STORETYPE(IConstants.STORETYPE),
  OPERATOR(IConstants.OPERATOR),
  STATE2(IConstants.STATE2),
  STATEHEAD(IConstants.STATEHEAD),
  PROPARSEDIRECTIVE(IConstants.PROPARSEDIRECTIVE),
  NODE_TYPE_KEYWORD(IConstants.NODE_TYPE_KEYWORD),
  ABBREVIATED(IConstants.ABBREVIATED),
  FULLTEXT(IConstants.FULLTEXT),
  INLINE_VAR_DEF(IConstants.INLINE_VAR_DEF),
  QUALIFIED_CLASS(IConstants.QUALIFIED_CLASS_INT);

  private int key;

  private AttributeKey(int key) {
    this.key = key;
  }

  public int getKey() {
    return key;
  }

  public String getName() {
    return name().toLowerCase().replace('_', '-');
  }
}