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
package eu.rssw.pct.elements.v11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IVariableElement;

public class VariableElementV11 extends AbstractAccessibleElement implements IVariableElement {
  private static final int READ_ONLY = 1;
  private static final int WRITE_ONLY = 2;
  private static final int BASE_IS_DOTNET = 4;
  private static final int NO_UNDO = 8;

  private final int dataType;
  private final int extent;
  private final int flags;
  private final String typeName;

  public VariableElementV11(String name, Set<AccessType> accessType, int dataType, int extent, int flags,
      String typeName) {
    super(name, accessType);
    this.dataType = dataType;
    this.extent = extent;
    this.flags = flags;
    this.typeName = typeName;
  }

  public static IVariableElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment,
      int currentPos, int textAreaOffset, ByteOrder order) {
    int dataType = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String typeName = typeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);

    return new VariableElementV11(name2, accessType, dataType, extent, flags, typeName);
  }

  @Override
  public int getExtent() {
    return this.extent;
  }

  @Override
  public DataType getDataType() {
    return DataType.getDataType(dataType);
  }

  public String getTypeName() {
    return typeName;
  }

  public boolean isReadOnly() {
    return (flags & READ_ONLY) != 0;
  }

  public boolean isWriteOnly() {
    return (flags & WRITE_ONLY) != 0;
  }

  public boolean isNoUndo() {
    return (flags & NO_UNDO) != 0;
  }

  public boolean baseIsDotNet() {
    return (flags & BASE_IS_DOTNET) != 0;
  }

  @Override
  public int getSizeInRCode() {
    return 24;
  }

  public String toString() {
    return String.format("Variable %s [%d] - %s", getName(), extent, getDataType().toString());
  }

  @Override
  public int hashCode() {
    return (getName() + "/" + getDataType() + "/" + getExtent()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IVariableElement) {
      IVariableElement obj2 = (IVariableElement) obj;
      return getName().equals(obj2.getName()) && getDataType().equals(obj2.getDataType())
          && (extent == obj2.getExtent());
    }
    return false;
  }
}
