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
import eu.rssw.pct.elements.IIndexElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.IVariableElement;

public class TableElement extends AbstractAccessibleElement implements ITableElement {
  private final IVariableElement[] fields;
  private final IIndexElement[] indexes;
  private final String beforeTableName;
  private final boolean isStatic;

  public TableElement(String name, String beforeTableName, IIndexElement[] indexes, IVariableElement... field) {
    this(name, beforeTableName, false, indexes, field);
  }

  public TableElement(String name, String beforeTableName, boolean isStatic, IIndexElement[] indexes,
      IVariableElement... field) {
    super(name, EnumSet.of(AccessType.PUBLIC));
    this.isStatic = isStatic;
    this.fields = field;
    this.indexes = indexes;
    this.beforeTableName = beforeTableName;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getBeforeTableName() {
    return beforeTableName;
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
  public boolean isNoUndo() {
    return false;
  }

  @Override
  public boolean isSerializable() {
    return false;
  }

  @Override
  public boolean isNonSerializable() {
    return false;
  }

  @Override
  public boolean isStatic() {
    return isStatic;
  }

}
