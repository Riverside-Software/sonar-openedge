/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
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
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.ParameterType;
import eu.rssw.pct.elements.PrimitiveDataType;

public class MethodParameterV11 extends AbstractElement implements IParameter {
  private static final int PARAMETER_APPEND = 1;
  private static final int PARAMETER_HANDLE = 2;
  private static final int PARAMETER_BIND = 4;

  public static final int PARAMETER_INPUT = 6028;
  public static final int PARAMETER_INOUT = 6110;
  public static final int PARAMETER_OUTPUT = 6049;
  public static final int PARAMETER_BUFFER = 1070;

  private final int paramNum;
  private final int extent;
  private final int flags;
  private final int parameterType;
  private final int paramMode;
  private final DataType dataType;

  public MethodParameterV11(int num, String name, int type, int mode, int flags, DataType dataType, int extent) {
    super(name);
    this.paramNum = num;
    this.parameterType = type;
    this.paramMode = mode;
    this.dataType = dataType;
    this.flags = flags;
    this.extent = extent;
  }

  protected static IParameter fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int parameterType = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int paramMode = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    int dataType = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 10, Short.BYTES).order(order).getShort() & 0xffff;
    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    int nameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();

    String typeName = dataType != PrimitiveDataType.CLASS.getNum() ? null
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);
    DataType dataTypeObj = typeName == null ? DataType.get(dataType) : new DataType(typeName);

    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    return new MethodParameterV11(0, name, parameterType, paramMode, flags, dataTypeObj, extent);
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public int getNum() {
    return paramNum;
  }

  @Override
  public int getExtent() {
    return extent;
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public ParameterType getParameterType() {
    return ParameterType.getParameterType(this.parameterType);
  }

  @Override
  public ParameterMode getMode() {
    return ParameterMode.getParameterMode(paramMode);
  }

  @Override
  public boolean isClassDataType() {
    return dataType.getPrimitive() == PrimitiveDataType.CLASS;
  }

  public boolean isBind() {
    return (flags & PARAMETER_BIND) != 0;
  }

  public boolean isAppend() {
    return (flags & PARAMETER_APPEND) != 0;
  }

  public boolean isHandle() {
    return (flags & PARAMETER_HANDLE) != 0;
  }

  @Override
  public int getSizeInRCode() {
    return 24;
  }

  @Override
  public String toString() {
    return getMode() + " " + getParameterType() + " " + getName() + " AS " + getDataType();
  }

  @Override
  public int hashCode() {
    return (getMode() + "/" + getParameterType() + "/" + getName() + "/" + getDataType()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IParameter) {
      IParameter obj2 = (IParameter) obj;
      return getName().equals(obj2.getName()) && getMode().equals(obj2.getMode())
          && getParameterType().equals(obj2.getParameterType()) && dataType.equals(obj2.getDataType());
    }
    return false;
  }
}
