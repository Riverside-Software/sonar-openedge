/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
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
