/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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
package eu.rssw.pct.elements.fixed;

import java.util.EnumSet;

import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IIndexComponentElement;

public class IndexComponentElement extends AbstractAccessibleElement implements IIndexComponentElement {
  private final int position;
  private final boolean ascending;

  public IndexComponentElement(String name, int position, boolean ascending) {
    super(name, EnumSet.of(AccessType.PUBLIC));
    this.position = position;
    this.ascending = ascending;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isStatic() {
    return false;
  }

  @Override
  public boolean isAscending() {
    return ascending;
  }

  @Override
  public int getFieldPosition() {
    return position;
  }

}
