package eu.rssw.pct;

import java.util.EnumSet;
import java.util.Set;

public enum AccessType {
  PUBLIC,
  PRIVATE,
  PROTECTED,
  STATIC,
  ABSTRACT,
  FINAL,
  CONSTRUCTOR;

  public static Set<AccessType> getTypeFromString(int val) {
    Set<AccessType> set = EnumSet.noneOf(AccessType.class);
    switch (val & 0x07) {
      case 1:
        set.add(PUBLIC);
        break;
      case 2:
        set.add(PROTECTED);
        break;
      case 4:
        set.add(PRIVATE);
        break;
      default:
        break;
    }
    if ((val & 0x08) != 0)
      set.add(CONSTRUCTOR);
    if ((val & 0x10) != 0)
      set.add(FINAL);
    if ((val & 0x20) != 0)
      set.add(STATIC);
    if ((val & 0x40) != 0)
      set.add(ABSTRACT);

    return set;
  }
}
