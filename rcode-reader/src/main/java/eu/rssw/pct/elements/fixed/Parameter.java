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
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.ParameterType;
import eu.rssw.pct.elements.PrimitiveDataType;

public class Parameter extends AbstractAccessibleElement implements IParameter {

  private final int num;
  private final int extent;
  private final ParameterMode mode;
  private final DataType dataType;
  private final ParameterType paramType;

  public Parameter(int num, String name, int extent, ParameterMode mode, DataType dataType) {
    this(num, name, extent, mode, dataType, ParameterType.VARIABLE);
  }

  public Parameter(int num, String name, int extent, ParameterMode mode, DataType dataType, ParameterType paramType) {
    super(name, EnumSet.noneOf(AccessType.class));
    this.num = num;
    this.extent = extent;
    this.mode = mode;
    this.dataType = dataType;
    this.paramType = paramType;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getExtent() {
    return extent;
  }

  @Override
  public int getNum() {
    return num;
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public ParameterMode getMode() {
    return mode;
  }

  @Override
  public ParameterType getParameterType() {
    return paramType;
  }

  @Override
  public boolean isClassDataType() {
    return dataType.getPrimitive() == PrimitiveDataType.CLASS;
  }

}
