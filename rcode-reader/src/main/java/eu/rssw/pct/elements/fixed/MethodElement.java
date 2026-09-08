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
import java.util.Set;

import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IParameter;

public class MethodElement extends AbstractAccessibleElement implements IMethodElement {
  private final DataType returnDataType;
  private final IParameter[] parameters;
  private final int extent;

  public MethodElement(String name, boolean isStatic, DataType returnDataType, IParameter... params) {
    this(name, isStatic, returnDataType, 0, params);
  }

  public MethodElement(String name, boolean isStatic, DataType returnDataType, int extent, IParameter... params) {
    this(name, isStatic, false, returnDataType, extent, params);
  }

  public MethodElement(String name, boolean isStatic, boolean isAbstract, DataType returnDataType, int extent, IParameter... params) {
    super(name, getAccessType(isStatic, isAbstract));
    this.returnDataType = returnDataType;
    this.parameters = params;
    this.extent = extent;
  }

  private static Set<AccessType> getAccessType(boolean isStatic, boolean isAbstract) {
    var accessType = EnumSet.of(AccessType.PUBLIC);
    if (isStatic)
      accessType.add(AccessType.STATIC);
    if (isAbstract)
      accessType.add(AccessType.ABSTRACT);

    return accessType;
  }

  @Override
  public DataType getReturnType() {
    return returnDataType;
  }

  @Override
  public IParameter[] getParameters() {
    return parameters;
  }

  @Override
  public int getExtent() {
    return extent;
  }

  @Override
  public boolean isDestructor() {
    return false;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isFinal() {
    return false;
  }

  @Override
  public boolean isProcedure() {
    return false;
  }

  @Override
  public boolean isFunction() {
    return false;
  }

  @Override
  public boolean isConstructor() {
    return false;
  }

  @Override
  public boolean isOverloaded() {
    return false;
  }
}
