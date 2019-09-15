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
import java.util.Set;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.v11.VariableElementV11;

public class VariableElementV12 extends VariableElementV11 {

  public VariableElementV12(String name, Set<AccessType> accessType, int dataType, int extent, int flags,
      String typeName) {
    super(name, accessType, dataType, extent, flags, typeName);
  }

  public static IVariableElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment,
      int currentPos, int textAreaOffset, ByteOrder order) {
    int dataType = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 18, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 20, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    String typeName = typeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);

    return new VariableElementV12(name2, accessType, dataType, extent, flags, typeName);
  }

}
