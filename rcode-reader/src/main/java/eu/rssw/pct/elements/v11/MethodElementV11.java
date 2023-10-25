/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
import java.util.stream.Collectors;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IParameter;

public class MethodElementV11 extends AbstractAccessibleElement implements IMethodElement {
  protected static final int METHOD_DESCRIPTOR_SIZE = 24;
  protected static final int FINAL_METHOD = 1;
  protected static final int PROTECTED_METHOD = 2;
  protected static final int PUBLIC_METHOD = 4;
  protected static final int PRIVATE_METHOD = 8;
  protected static final int PROCEDURE_METHOD = 16;
  protected static final int FUNCTION_METHOD = 32;
  protected static final int CONSTRUCTOR_METHOD = 64;
  protected static final int DESTRUCTOR_METHOD = 128;
  protected static final int OVERLOADED_METHOD = 256;
  protected static final int STATIC_METHOD = 512;

  private final int flags;
  private final DataType returnType;
  private final int extent;
  private final IParameter[] parameters;

  public MethodElementV11(String name, Set<AccessType> accessType, int flags, DataType returnType, int extent,
      IParameter[] parameters) {
    super(name, accessType);
    this.flags = flags;
    this.returnType = returnType;
    this.extent = extent;
    this.parameters = parameters;
  }

  public static IMethodElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort() & 0xffff;
    int returnType = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int paramCount = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String typeName = returnType != 42 ? null
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);
    DataType returnTypeObj = typeName == null ? DataType.get(returnType) : new DataType(typeName);

    int currPos = currentPos + 24;
    IParameter[] parameters = new IParameter[paramCount];
    for (int zz = 0; zz < paramCount; zz++) {
      IParameter param = MethodParameterV11.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += param.getSizeInRCode();
      parameters[zz] = param;
    }

    return new MethodElementV11(name2, accessType, flags, returnTypeObj, extent, parameters);
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public DataType getReturnType() {
    return returnType;
  }

  @Override
  public IParameter[] getParameters() {
    return this.parameters;
  }

  @Override
  public boolean isStatic() {
    return (flags & STATIC_METHOD) != 0;
  }

  @Override
  public boolean isProcedure() {
    return (flags & PROCEDURE_METHOD) != 0;
  }

  @Override
  public boolean isFinal() {
    return (flags & FINAL_METHOD) != 0;
  }

  @Override
  public boolean isFunction() {
    return (flags & FUNCTION_METHOD) != 0;
  }

  @Override
  public boolean isConstructor() {
    return (flags & CONSTRUCTOR_METHOD) != 0;
  }

  @Override
  public boolean isDestructor() {
    return (flags & DESTRUCTOR_METHOD) != 0;
  }

  @Override
  public boolean isOverloaded() {
    return (flags & OVERLOADED_METHOD) != 0;
  }

  @Override
  public int getExtent() {
    if (this.extent == 32769) {
      return -1;
    }
    return this.extent;
  }

  @Override
  public int getSizeInRCode() {
    int size = 24;
    for (IParameter p : parameters) {
      size += p.getSizeInRCode();
    }
    return size;
  }

  @Override
  public String toString() {
    return String.format("Method %s(%d arguments) returns %s", getName(), parameters.length, getReturnType());
  }

  @Override
  public int hashCode() {
    String str = Arrays.stream(parameters).map(IParameter::toString).collect(Collectors.joining("/"));
    return (getName() + "/" + getReturnType() + "/" + getExtent() + "/" + str).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IMethodElement) {
      IMethodElement obj2 = (IMethodElement) obj;
      return getName().equals(obj2.getName()) && getReturnType().equals(obj2.getReturnType())
          && (extent == obj2.getExtent()) && Arrays.deepEquals(parameters, obj2.getParameters());
    }
    return false;
  }
}
