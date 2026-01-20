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
package eu.rssw.pct.elements.fixed;

import java.util.EnumSet;

import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IBufferElement;

public class BufferElement extends AbstractAccessibleElement implements IBufferElement {
  private final String databaseName;
  private final String tableName;
  private final boolean isTempTableBuffer;

  public BufferElement(String tableName) {
    this("", tableName, true);
  }

  public BufferElement(String databaseName, String tableName, boolean isTempTableBuffer) {
    super(tableName, EnumSet.of(AccessType.PUBLIC));
    this.databaseName = databaseName;
    this.tableName = tableName;
    this.isTempTableBuffer = isTempTableBuffer;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public String getTableName() {
    return tableName;
  }

  @Override
  public boolean isTempTableBuffer() {
    return isTempTableBuffer;
  }

}
