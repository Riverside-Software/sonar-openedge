/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
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

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractElement;
import eu.rssw.pct.elements.IDataRelationElement;

public class DataRelationElementV11 extends AbstractElement implements IDataRelationElement {
  private final String parentBufferName;
  private final String childBufferName;
  private final String fieldPairs;
  private final int flags;

  public DataRelationElementV11(String name, String parentBuffer, String childBuffer, String fieldPairs, int flags) {
    super(name);
    this.parentBufferName = parentBuffer;
    this.childBufferName = childBuffer;
    this.fieldPairs = fieldPairs;
    this.flags = flags;
  }

  public static DataRelationElementV11 fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort() & 0xffff;

    int parentBufferNameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    String parentBufferName = parentBufferNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + parentBufferNameOffset);

    int childBufferNameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String childBufferName = childBufferNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + childBufferNameOffset);

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int fieldPairsOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    String fieldPairs = fieldPairsOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + fieldPairsOffset);

    return new DataRelationElementV11(name, parentBufferName, childBufferName, fieldPairs, flags);
  }

  @Override
  public String getParentBufferName() {
    return parentBufferName;
  }

  @Override
  public String getChildBufferName() {
    return childBufferName;
  }

  @Override
  public String getFieldPairs() {
    return fieldPairs;
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public int getSizeInRCode() {
    return 24;
  }

  @Override
  public String toString() {
    return String.format("Data relation from %s to %s", parentBufferName, childBufferName);
  }

  @Override
  public int hashCode() {
    return (parentBufferName + "/" + childBufferName + "/" + fieldPairs).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IDataRelationElement) {
      IDataRelationElement obj2 = (IDataRelationElement) obj;
      return (parentBufferName.equals(obj2.getParentBufferName()) && childBufferName.equals(obj2.getChildBufferName())
          && fieldPairs.equals(obj2.getFieldPairs()));
    }
    return false;
  }
}
