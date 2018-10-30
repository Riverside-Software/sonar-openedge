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
package eu.rssw.pct.elements.v11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IDataRelationElement;
import eu.rssw.pct.elements.IDatasetElement;

public class DatasetElement extends AbstractAccessibleElement implements IDatasetElement {
  private final int prvte;
  private final String[] bufferNames;
  private final IDataRelationElement[] relations;

  public DatasetElement(String name, Set<AccessType> accessType, int prvte, String[] bufferNames,
      IDataRelationElement[] relations) {
    super(name, accessType);
    this.prvte = prvte;
    this.bufferNames = bufferNames;
    this.relations = relations;
  }

  public static IDatasetElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int relationshipCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int prvte = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    // int flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    int currPos = currentPos + 4 * bufferCount;
    IDataRelationElement[] relations = new DataRelationElement[relationshipCount];
    for (int zz = 0; zz < relationshipCount; zz++) {
      IDataRelationElement param = DataRelationElement.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += param.size();
      relations[zz] = param;
    }

    return new DatasetElement(name2, accessType, prvte, bufferNames, relations);
  }

  @Override
  public String toString() {
    return String.format("Dataset %s for %d buffer(s) and %d relations", name, bufferNames.length, relations.length);
  }

  @Override
  public int size() {
    int size = 24 + (bufferNames.length * 4);
    for (IDataRelationElement elem : relations) {
      size += elem.size();
    }
    return size + 7 & -8;
  }

  public IDataRelationElement[] getDataRelations() {
    return this.relations;
  }

  public String[] getBufferNames() {
    return bufferNames;
  }

}
