/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.v11.MethodElementV11;

public class MethodElementV12 extends MethodElementV11 {

  public MethodElementV12(String name, Set<AccessType> accessType, int flags, int returnType, String returnTypeName,
      int extent, IParameter[] parameters) {
    super(name, accessType, flags, returnType, returnTypeName, extent, parameters);
  }

  public static IMethodElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int returnType = ByteBuffer.wrap(segment, currentPos + 16, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int paramCount = ByteBuffer.wrap(segment, currentPos + 18, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 22, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(
        ByteOrder.LITTLE_ENDIAN).getInt();
    String typeName = typeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);

    int currPos = currentPos + 56;
    IParameter[] parameters = new IParameter[paramCount];
    for (int zz = 0; zz < paramCount; zz++) {
      IParameter param = MethodParameterV12.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += param.getSizeInRCode();
      parameters[zz] = param;
    }
    
    return new MethodElementV12(name2, accessType, flags, returnType, typeName, extent, parameters);
  }

  @Override
  public int getSizeInRCode() {
    int size = 56;
    for (IParameter p : getParameters()) {
      size += p.getSizeInRCode();
    }
    return size;
  }

}
