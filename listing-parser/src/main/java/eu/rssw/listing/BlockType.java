/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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