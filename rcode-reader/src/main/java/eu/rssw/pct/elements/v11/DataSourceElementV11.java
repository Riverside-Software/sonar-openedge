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

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IDataSourceElement;

public class DataSourceElementV11 extends AbstractAccessibleElement implements IDataSourceElement {
  private final String queryName;
  private final String keyComponentNames;
  private final String[] bufferNames;

  public DataSourceElementV11(String name, Set<AccessType> accessType, String queryName, String keyComponentNames,
      String[] bufferNames) {
    super(name, accessType);
    this.queryName = queryName;
    this.keyComponentNames = keyComponentNames;
    this.bufferNames = bufferNames;
  }

  public static IDataSourceElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment,
      int currentPos, int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int queryNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String queryName = queryNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + queryNameOffset);

    int keyComponentNamesOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    String keyComponentNames = keyComponentNamesOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + keyComponentNamesOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    return new DataSourceElementV11(name2, accessType, queryName, keyComponentNames, bufferNames);
  }

  @Override
  public String getQueryName() {
    return queryName;
  }

  @Override
  public String getKeyComponents() {
    return keyComponentNames;
  }

  @Override
  public String[] getBufferNames() {
    return bufferNames;
  }

  @Override
  public int getSizeInRCode() {
    int size = 24 + (this.bufferNames.length * 4);
    return size + 7 & -8;
  }

  @Override
  public String toString() {
    return String.format("Datasource %s for %d buffer(s)", queryName, bufferNames.length);
  }

  @Override
  public int hashCode() {
    return (queryName + "-" + keyComponentNames + "-" + String.join("/", bufferNames)).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IDataSourceElement) {
      IDataSourceElement obj2 = (IDataSourceElement) obj;
      return queryName.equals(obj2.getQueryName()) && keyComponentNames.equals(obj2.getKeyComponents())
          && Arrays.deepEquals(bufferNames, obj2.getBufferNames());
    }
    return false;
  }

}
