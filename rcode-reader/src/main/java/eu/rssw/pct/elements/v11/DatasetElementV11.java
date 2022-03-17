/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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
import eu.rssw.pct.elements.IDataRelationElement;
import eu.rssw.pct.elements.IDatasetElement;

public class DatasetElementV11 extends AbstractAccessibleElement implements IDatasetElement {
  private final String[] bufferNames;
  private final IDataRelationElement[] relations;

  public DatasetElementV11(String name, Set<AccessType> accessType, String[] bufferNames,
      IDataRelationElement[] relations) {
    super(name, accessType);
    this.bufferNames = bufferNames;
    this.relations = relations;
  }

  public static IDatasetElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int relationshipCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    // Round to next byte
    int currPos = currentPos + 24 + (bufferCount * 4 + 7 & -8);
    IDataRelationElement[] relations = new DataRelationElementV11[relationshipCount];
    for (int zz = 0; zz < relationshipCount; zz++) {
      IDataRelationElement param = DataRelationElementV11.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += param.getSizeInRCode();
      relations[zz] = param;
    }

    return new DatasetElementV11(name2, accessType, bufferNames, relations);
  }

  public IDataRelationElement[] getDataRelations() {
    return this.relations;
  }

  public String[] getBufferNames() {
    return bufferNames;
  }

  @Override
  public int getSizeInRCode() {
    int size = 24 + (bufferNames.length * 4 + 7 & -8);
    for (IDataRelationElement elem : relations) {
      size += elem.getSizeInRCode();
    }
    return size;
  }

  @Override
  public String toString() {
    return String.format("Dataset %s for %d buffer(s) and %d relations", getName(), bufferNames.length, relations.length);
  }

  @Override
  public int hashCode() {
    String str1 = Joiner.on('/').join(bufferNames);
    String str2 = Joiner.on('/').join(relations);
    return (str1 + "-" + str2).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IDatasetElement) {
      IDatasetElement obj2 = (IDatasetElement) obj;
      return (Arrays.deepEquals(bufferNames, obj2.getBufferNames())
          && Arrays.deepEquals(relations, obj2.getDataRelations()));
    }
    return false;
  }
}
