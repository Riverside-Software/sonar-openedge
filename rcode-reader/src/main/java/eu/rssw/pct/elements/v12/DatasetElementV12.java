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
package eu.rssw.pct.elements.v12;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IDataRelationElement;
import eu.rssw.pct.elements.IDatasetElement;
import eu.rssw.pct.elements.v11.DatasetElementV11;

public class DatasetElementV12 extends DatasetElementV11 {

  public DatasetElementV12(String name, Set<AccessType> accessType, String[] bufferNames,
      IDataRelationElement[] relations) {
    super(name, accessType, bufferNames, relations);
  }

  public static IDatasetElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(order).getShort();
    int relationshipCount = ByteBuffer.wrap(segment, currentPos + 16, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    // Round to next byte
    int currPos = currentPos + 24 + (bufferCount * 4 + 7 & -8);
    IDataRelationElement[] relations = new DataRelationElementV12[relationshipCount];
    for (int zz = 0; zz < relationshipCount; zz++) {
      IDataRelationElement param = DataRelationElementV12.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += param.getSizeInRCode();
      relations[zz] = param;
    }

    return new DatasetElementV12(name2, accessType, bufferNames, relations);
  }

}
