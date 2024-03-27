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

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.PrimitiveDataType;
import eu.rssw.pct.elements.v11.MethodParameterV11;

public class MethodParameterV12 extends MethodParameterV11 {

  public MethodParameterV12(int num, String name, int type, int mode, int flags, DataType dataType, int extent) {
    super(num, name, type, mode, flags, dataType, extent);
  }

  protected static IParameter fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int parameterType = ByteBuffer.wrap(segment, currentPos + 10, Short.BYTES).order(order).getShort();
    int paramMode = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(order).getShort();
    int dataType = ByteBuffer.wrap(segment, currentPos + 16, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 18, Short.BYTES).order(order).getShort() & 0xffff;
    int typeNameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    int nameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();

    String typeName = dataType != PrimitiveDataType.CLASS.getNum() ? null
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);
    DataType dataTypeObj = typeName == null ? DataType.get(dataType) : new DataType(typeName);
    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    return new MethodParameterV12(0, name, parameterType, paramMode, flags, dataTypeObj, extent);
  }

}
