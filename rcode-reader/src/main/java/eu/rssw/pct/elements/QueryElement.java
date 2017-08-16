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
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class QueryElement extends AbstractAccessibleElement {
  private final String[] bufferNames;
  private final int prvte;
  private final int flags;

  public QueryElement(String name, Set<AccessType> accessType, String[] buffers, int flags, int prvte) {
    super(name, accessType);
    this.bufferNames = buffers;
    this.flags = flags;
    this.prvte = prvte;
  }

  public static QueryElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int prvte = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    // int cacheSize = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    return new QueryElement(name2, accessType, bufferNames, flags, prvte);
  }

  public int getPrvte() {
    return prvte;
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public int size() {
    return (24 + 4 * bufferNames.length) + 7 & -8;
  }

  public String[] getBufferNames() {
    return bufferNames;
  }

}
