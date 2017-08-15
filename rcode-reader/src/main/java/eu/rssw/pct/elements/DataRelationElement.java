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

import eu.rssw.pct.RCodeInfo;

public class DataRelationElement extends AbstractElement {
  private final String parentBufferName;
  private final String childBufferName;
  private final String fieldPairs;
  private final int flags;

  public DataRelationElement(String name, String parentBuffer, String childBuffer, String fieldPairs, int flags) {
    super(name);
    this.parentBufferName = parentBuffer;
    this.childBufferName = childBuffer;
    this.fieldPairs = fieldPairs;
    this.flags = flags;
  }

  public static DataRelationElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    // int pairCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();

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

    return new DataRelationElement(name, parentBufferName, childBufferName, fieldPairs, flags);
  }

  public String getParentBufferName() {
    return parentBufferName;
  }

  public String getChildBufferName() {
    return childBufferName;
  }

  public String getFieldPairs() {
    return fieldPairs;
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public int size() {
    return 24;
  }
}
