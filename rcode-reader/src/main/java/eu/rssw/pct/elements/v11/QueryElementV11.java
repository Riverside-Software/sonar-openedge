/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
import java.util.Arrays;
import java.util.Set;

import com.google.common.base.Joiner;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IQueryElement;

public class QueryElementV11 extends AbstractAccessibleElement implements IQueryElement {
  private final String[] bufferNames;
  private final int prvte;
  private final int flags;

  public QueryElementV11(String name, Set<AccessType> accessType, String[] buffers, int flags, int prvte) {
    super(name, accessType);
    this.bufferNames = buffers;
    this.flags = flags;
    this.prvte = prvte;
  }

  public static IQueryElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int prvte = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort() & 0xffff;

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    return new QueryElementV11(name2, accessType, bufferNames, flags, prvte);
  }

  @Override
  public String[] getBufferNames() {
    return bufferNames;
  }

  public int getPrvte() {
    return prvte;
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public int getSizeInRCode() {
    return (24 + 4 * bufferNames.length) + 7 & -8;
  }

  @Override
  public String toString() {
    return String.format("Query %s for %d buffer(s)", getName(), bufferNames.length); 
  }

  @Override
  public int hashCode() {
    return (getName() + "/" + Joiner.on('-').join(bufferNames)).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IQueryElement) {
      IQueryElement obj2 = (IQueryElement) obj;
      return getName().equals(obj2.getName()) && Arrays.deepEquals(bufferNames, obj2.getBufferNames());
    }
    return false;
  }
}
