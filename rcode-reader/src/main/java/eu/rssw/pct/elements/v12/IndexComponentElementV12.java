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
package eu.rssw.pct.elements.v12;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.v11.IndexComponentElementV11;

public class IndexComponentElementV12 extends IndexComponentElementV11 {

  public IndexComponentElementV12(int position, int flags, boolean ascending) {
    super(position, flags, ascending);
  }

  protected static IIndexComponentElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int ascending = segment[currentPos + 6];
    int flags = segment[currentPos + 7];
    int position = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();

    return new IndexComponentElementV12(position, flags, ascending == 105);
  }

}
