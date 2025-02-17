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
package eu.rssw.pct.elements.v11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.elements.AbstractElement;
import eu.rssw.pct.elements.IIndexComponentElement;

public class IndexComponentElementV11 extends AbstractElement implements IIndexComponentElement {
  private final int flags;
  private final int position;
  private final boolean ascending;

  public IndexComponentElementV11(int position, int flags, boolean ascending) {
    this.position = position;
    this.flags = flags;
    this.ascending = ascending;
  }

  protected static IIndexComponentElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int ascending = segment[currentPos];
    int flags = segment[currentPos + 1];
    int position = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();

    return new IndexComponentElementV11(position, flags, ascending == 106);
  }

  public int getFlags() {
    return flags;
  }

  public int getFieldPosition() {
    return this.position;
  }

  public boolean isAscending() {
    return this.ascending;
  }

  @Override
  public int getSizeInRCode() {
    return 8;
  }

  @Override
  public String toString() {
    return String.format("Field #%d", position);
  }

  @Override
  public int hashCode() {
    return position * 7 + (ascending ? 3 : 1);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IIndexComponentElement) {
      IIndexComponentElement obj2 = (IIndexComponentElement) obj;
      return (position == obj2.getFieldPosition()) && (ascending == obj2.isAscending());
    }
    return false;
  }
}
