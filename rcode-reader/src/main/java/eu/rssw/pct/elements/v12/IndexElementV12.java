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
package eu.rssw.pct.elements.v12;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.IIndexElement;
import eu.rssw.pct.elements.v11.IndexElementV11;

public class IndexElementV12 extends IndexElementV11 {

  public IndexElementV12(String name, int primary, int flags, IIndexComponentElement[] indexComponents) {
    super(name, primary, flags, indexComponents);
  }

  protected static IIndexElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int primary = segment[currentPos + 14];
    int flags = segment[currentPos + 15];

    int componentCount = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort();
    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int currPos = currentPos + 16;
    IIndexComponentElement[] indexComponents = new IndexComponentElementV12[componentCount];
    for (int zz = 0; zz < componentCount; zz++) {
      IIndexComponentElement component = IndexComponentElementV12.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += component.getSizeInRCode();
      indexComponents[zz] = component;
    }

    return new IndexElementV12(name, primary, flags, indexComponents);
  }

}
