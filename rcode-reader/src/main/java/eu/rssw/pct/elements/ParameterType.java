/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2019 Riverside Software
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
package eu.rssw.pct.elements;

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
