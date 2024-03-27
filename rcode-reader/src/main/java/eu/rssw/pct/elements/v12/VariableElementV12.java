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
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.PrimitiveDataType;
import eu.rssw.pct.elements.v11.VariableElementV11;

public class VariableElementV12 extends VariableElementV11 {

  public VariableElementV12(String name, Set<AccessType> accessType, DataType dataType, int extent, int flags) {
    super(name, accessType, dataType, extent, flags);
  }

  public static IVariableElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment,
      int currentPos, int textAreaOffset, ByteOrder order) {
    int dataType = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 18, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 20, Short.BYTES).order(order).getShort() & 0xffff;

    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    String typeName = dataType != PrimitiveDataType.CLASS.getNum() ? null
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);
    DataType dataTypeObj = typeName == null ? DataType.get(dataType) : new DataType(typeName);

    return new VariableElementV12(name2, accessType, dataTypeObj, extent, flags);
  }

}
