package eu.rssw.antlr.database.objects;

import java.util.HashMap;
import java.util.Map;

public enum TriggerType {
  CREATE, DELETE, FIND, WRITE, REPLICATION_CREATE, REPLICATION_WRITE, REPLICATION_DELETE, ASSIGN;

  private static final Map<String, TriggerType> map = new HashMap<>();

  static {
    for (TriggerType type : TriggerType.values()) {
      map.put(type.toString(), type);
    }
  }

  public static TriggerType getTriggerType(String str) {
    return map.get(str.replace('-', '_').replace("\"", "").toUpperCase());
  }

}
