package eu.rssw.listing;

import java.util.HashMap;
import java.util.Map;

public enum BlockType {
  PROCEDURE,
  FUNCTION,
  DO,
  REPEAT,
  FOR,
  METHOD,
  TRIGGER,
  CATCH,
  FINALLY,
  EDITING,
  CONSTRUCTOR,
  CLASS,
  INTERFACE,
  DESTRUCTOR,
  ENUM;

  private static final Map<String, BlockType> map = new HashMap<>();

  static {
    for (BlockType type : BlockType.values()) {
      map.put(type.toString(), type);
    }
  }

  public static BlockType getBlockType(String str) {
    return map.get(str.toUpperCase());
  }
}