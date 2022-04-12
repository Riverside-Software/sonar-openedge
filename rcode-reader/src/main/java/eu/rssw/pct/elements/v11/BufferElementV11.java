/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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
import eu.rssw.pct.elements.IBufferElement;

public class BufferElementV11 extends AbstractAccessibleElement implements IBufferElement {
  private static final int TEMP_TABLE = 4;

  private final String tableName;
  private final String databaseName;
  private final int flags;

  public BufferElementV11(String name, Set<AccessType> accessType, String tableName, String dbName, int flags) {
    super(name, accessType);
    this.tableName = tableName;
    this.databaseName = dbName;
    this.flags = flags;
  }

  public static IBufferElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int tableNameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    String tableName = tableNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + tableNameOffset);

    int databaseNameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    String databaseName = databaseNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + databaseNameOffset);

    int flags = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort() & 0xffff;

    return new BufferElementV11(name2, accessType, tableName, databaseName, flags);
  }

  @Override
  public String getTableName() {
    return tableName;
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public boolean isTempTableBuffer() {
    return (flags & TEMP_TABLE) != 0;
  }

  @Override
  public int getSizeInRCode() {
    return 24;
  }

  @Override
  public String toString() {
    return String.format("Buffer %s for %s.%s", getName(), databaseName, tableName);
  }

  @Override
  public int hashCode() {
    return (getName() + "/" + databaseName + "/" + tableName).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IBufferElement) {
      IBufferElement obj2 = (IBufferElement) obj;
      return (getName().equals(obj2.getName()) && databaseName.equals(obj2.getDatabaseName())
          && tableName.equals(obj2.getTableName()));
    }
    return false;
  }

}
