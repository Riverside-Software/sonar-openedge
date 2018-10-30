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
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IEventElement;
import eu.rssw.pct.elements.IParameter;

public class EventElement extends AbstractAccessibleElement implements IEventElement {
  private final int flags;
  private final int returnType;
  private final String returnTypeName;
  private final String delegateName;
  private final IParameter[] parameters;

  public EventElement(String name, Set<AccessType> accessType, int flags, int returnType, String returnTypeName,
      String delegateName, IParameter[] parameters) {
    super(name, accessType);
    this.flags = flags;
    this.returnType = returnType;
    this.returnTypeName = returnTypeName;
    this.delegateName = delegateName;
    this.parameters = parameters;
  }

  public static IEventElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int returnType = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int parameterCount = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String returnTypeName = typeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);

    int delegateNameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    String delegateName = delegateNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + delegateNameOffset);

    int currPos = currentPos + 24;
    IParameter[] parameters = new IParameter[parameterCount];
    for (int zz = 0; zz < parameterCount; zz++) {
      IParameter param = MethodParameter.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += param.size();
      parameters[zz] = param;
    }

    return new EventElement(name2, accessType, flags, returnType, returnTypeName, delegateName, parameters);
  }

  @Override
  public DataType getReturnType() {
    return DataType.getDataType(returnType);
  }

  @Override
  public String getReturnTypeName() {
    return returnTypeName;
  }

  @Override
  public String getDelegateName() {
    return delegateName;
  }

  @Override
  public IParameter[] getParameters() {
    return this.parameters;
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public int size() {
    int size = 24;
    for (IParameter p : parameters) {
      size += ((MethodParameter) p).size();
    }

    return size;
  }

}
