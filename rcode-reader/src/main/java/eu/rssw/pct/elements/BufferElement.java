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
package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class BufferElement extends AbstractAccessibleElement {
  private static final int TEMP_TABLE = 4;

  private final String tableName;
  private final String databaseName;
  private final int flags;

  public BufferElement(String name, Set<AccessType> accessType, String tableName, String dbName, int flags) {
    super(name, accessType);
    this.tableName = tableName;
    this.databaseName = dbName;
    this.flags = flags;
  }

  public static BufferElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int tableNameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    String tableName = tableNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + tableNameOffset);

    int databaseNameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    String databaseName = databaseNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + databaseNameOffset);

    int flags = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(order).getShort();
    // int prvt = ByteBuffer.wrap(segment, currentPos + 16, Short.BYTES).order(order).getShort();

    return new BufferElement(name2, accessType, tableName, databaseName, flags);
  }

  @Override
  public String toString() {
    return String.format("Buffer %s for %s.%s", name, databaseName, tableName);
  }

  public String getTableName() {
    return tableName;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public int size() {
    return 24;
  }

  public boolean isTempTableBuffer() {
    return (flags & TEMP_TABLE) != 0;
  }

}
