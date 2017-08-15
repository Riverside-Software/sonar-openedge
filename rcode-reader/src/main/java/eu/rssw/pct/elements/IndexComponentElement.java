/*
 * RCode library - OpenEdge plugin for SonarQube
 * Copyright (C) 2017 Riverside Software
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IndexComponentElement extends AbstractElement {
  private final int ascending;
  private final int flags;
  private final int position;

  public IndexComponentElement(int position, int flags, int ascending) {
    this.position = position;
    this.flags = flags;
    this.ascending = ascending;
  }

  protected static IndexComponentElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int ascending = segment[currentPos];
    int flags = segment[currentPos + 1];
    int position = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();

    return new IndexComponentElement(position, flags, ascending);
  }

  @Override
  public int size() {
    return 8;
  }

  public int getFlags() {
    return flags;
  }

  public int getFieldPosition() {
    return this.position;
  }

  public boolean getAscending() {
    return this.ascending == 105;
  }

  public boolean getDescending() {
    return this.ascending == 106;
  }

}
