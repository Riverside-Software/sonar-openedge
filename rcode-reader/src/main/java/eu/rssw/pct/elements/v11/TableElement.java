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
import eu.rssw.pct.elements.IIndexElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.IVariableElement;

public class TableElement extends AbstractAccessibleElement implements ITableElement {
  private final int flags;
  private final int prvte;
  private final IVariableElement[] fields;
  private final IIndexElement[] indexes;
  private final String beforeTableName;

  public TableElement(String name, Set<AccessType> accessType, int flags, int prvte, IVariableElement[] fields,
      IIndexElement[] indexes, String beforeTableName) {
    super(name, accessType);
    this.fields = fields;
    this.indexes = indexes;
    this.beforeTableName = beforeTableName;
    this.flags = flags;
    this.prvte = prvte;
  }

  public static ITableElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int fieldCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int indexCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int prvte = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);
    int beforeNameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(
        ByteOrder.LITTLE_ENDIAN).getInt();
    String beforeTableName = beforeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + beforeNameOffset);

    IVariableElement[] fields = new VariableElement[fieldCount];
    int currPos = currentPos + 24;
    for (int zz = 0; zz < fieldCount; zz++) {
      IVariableElement var = VariableElement.fromDebugSegment("", null, segment, currPos, textAreaOffset, order);
      currPos += var.size();
      fields[zz] = var;
    }

    IIndexElement[] indexes = new IndexElement[indexCount];
    for (int zz = 0; zz < indexCount; zz++) {
      IIndexElement idx = IndexElement.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += idx.size();
      indexes[zz] = idx;
    }

    return new TableElement(name2, accessType, flags, prvte, fields, indexes, beforeTableName);
  }

  public int getFlags() {
    return flags;
  }

  public int getPrvte() {
    return prvte;
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
  public int size() {
    int size = 24;
    for (IVariableElement e : fields) {
      size += e.size();
    }
    for (IIndexElement e : indexes) {
      size += e.size();
    }
    return size;
  }

  @Override
  public String toString() {
    return String.format("%s Table %s - BeforeTable %s", accessType.toString(), name, beforeTableName);
  }
}
