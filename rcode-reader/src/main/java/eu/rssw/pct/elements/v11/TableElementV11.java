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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IIndexElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.IVariableElement;

public class TableElementV11 extends AbstractAccessibleElement implements ITableElement {
  private final int flags;
  private final IVariableElement[] fields;
  private final IIndexElement[] indexes;
  private final String beforeTableName;

  public TableElementV11(String name, Set<AccessType> accessType, int flags, IVariableElement[] fields,
      IIndexElement[] indexes, String beforeTableName) {
    super(name, accessType);
    this.fields = fields;
    this.indexes = indexes;
    this.beforeTableName = beforeTableName;
    this.flags = flags;
  }

  public static ITableElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int fieldCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int indexCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort() & 0xffff;

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);
    int beforeNameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    String beforeTableName = beforeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + beforeNameOffset);

    IVariableElement[] fields = new VariableElementV11[fieldCount];
    int currPos = currentPos + 24;
    for (int zz = 0; zz < fieldCount; zz++) {
      IVariableElement elem = VariableElementV11.fromDebugSegment("", null, segment, currPos, textAreaOffset, order);
      currPos += elem.getSizeInRCode();
      fields[zz] = elem;
    }

    IIndexElement[] indexes = new IndexElementV11[indexCount];
    for (int zz = 0; zz < indexCount; zz++) {
      IIndexElement idx = IndexElementV11.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += idx.getSizeInRCode();
      indexes[zz] = idx;
    }

    return new TableElementV11(name2, accessType, flags, fields, indexes, beforeTableName);
  }

  public int getFlags() {
    return flags;
  }

  @Override
  public boolean isNoUndo() {
    return (flags & 2) != 0;
  }

  @Override
  public boolean isSerializable() {
    return (flags & 4) != 0;
  }

  @Override
  public boolean isNonSerializable() {
    return (flags & 8) != 0;
  }

  @Override
  public IVariableElement[] getFields() {
    return fields;
  }

  @Override
  public IIndexElement[] getIndexes() {
    return indexes;
  }

  @Override
  public String getBeforeTableName() {
    return beforeTableName;
  }

  @Override
  public int getSizeInRCode() {
    int size = 24;
    for (IVariableElement e : fields) {
      size += e.getSizeInRCode();
    }
    for (IIndexElement e : indexes) {
      size += e.getSizeInRCode();
    }
    return size;
  }

  @Override
  public String toString() {
    return String.format("Table %s - BeforeTable %s", getName(), beforeTableName);
  }

  @Override
  public int hashCode() {
    String str1 = Arrays.stream(fields).map(IVariableElement::toString).collect(Collectors.joining("-"));
    String str2 = Arrays.stream(indexes).map(IIndexElement::toString).collect(Collectors.joining("-"));
    return (getName() + "/" + str1 + "/" + str2).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ITableElement) {
      ITableElement obj2 = (ITableElement) obj;
      return getName().equals(obj2.getName()) && beforeTableName.equals(obj2.getBeforeTableName())
          && Arrays.deepEquals(fields, obj2.getFields()) && Arrays.deepEquals(indexes, obj2.getIndexes());
    }
    return false;
  }

}
