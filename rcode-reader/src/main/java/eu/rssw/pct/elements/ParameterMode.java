/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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
