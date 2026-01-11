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
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IVariableElement;

public class VariableElement extends AbstractAccessibleElement implements IVariableElement {
  private final DataType dataType;
  private final boolean isStatic;

  public VariableElement(String name, DataType dataType) {
    this(name, false, dataType);
  }

  public VariableElement(String name, boolean isStatic, DataType dataType) {
    super(name, EnumSet.of(AccessType.PUBLIC));
    this.isStatic = isStatic;
    this.dataType = dataType;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getExtent() {
    return 0;
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public boolean isWriteOnly() {
    return false;
  }

  @Override
  public boolean isNoUndo() {
    return false;
  }

  @Override
  public boolean baseIsDotNet() {
    return false;
  }

  @Override
  public boolean isStatic() {
    return isStatic;
  }
}
