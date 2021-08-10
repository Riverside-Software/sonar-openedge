/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
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
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IParameter;

public class MethodElement extends AbstractAccessibleElement implements IMethodElement {

  public MethodElement(String name, boolean isStatic) {
    super(name, isStatic ? EnumSet.of(AccessType.STATIC) : EnumSet.noneOf(AccessType.class));
  }

  @Override
  public DataType getReturnType() {
    return DataType.VOID;
  }

  @Override
  public IParameter[] getParameters() {
    return new IParameter[] {};
  }

  @Override
  public int getExtent() {
    return 1;
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
