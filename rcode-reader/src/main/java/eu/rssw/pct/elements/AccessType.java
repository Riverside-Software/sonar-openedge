/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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

import java.util.EnumSet;
import java.util.Set;

public enum AccessType {
  PUBLIC,
  PRIVATE,
  PROTECTED,
  PACKAGE_PRIVATE,
  PACKAGE_PROTECTED,
  STATIC,
  ABSTRACT,
  FINAL,
  CONSTRUCTOR;

  public static Set<AccessType> getTypeFromString(int val) {
    Set<AccessType> set = EnumSet.noneOf(AccessType.class);
    if ((val & 1) != 0)
      set.add(PUBLIC);
    if ((val & 2) != 0)
      set.add(PROTECTED);
    if ((val & 4) != 0)
      set.add(PRIVATE);
    if ((val & 512) != 0)
      set.add(PACKAGE_PRIVATE);
    if ((val & 1024) != 0)
      set.add(PACKAGE_PROTECTED);
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
