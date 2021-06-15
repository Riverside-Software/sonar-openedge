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
  protected static final int METHOD_DESCRIPTOR_SIZE = 24;
  protected static final int FINAL_METHOD = 1;
  protected static final int PROTECTED_METHOD = 2;
  protected static final int PUBLIC_METHOD = 4;
  protected static final int PRIVATE_METHOD = 8;
  protected static final int PROCEDURE_METHOD = 16;
  protected static final int FUNCTION_METHOD = 32;
  protected static final int CONSTRUCTOR_METHOD = 64;
  protected static final int DESTRUCTOR_METHOD = 128;
  protected static final int OVERLOADED_METHOD = 256;
  protected static final int STATIC_METHOD = 512;

  private final int returnType = 0;
  private final String returnTypeName = null;
  private final int extent = 1;

  public MethodElement(String name, boolean isStatic) {
    super(name, isStatic ? EnumSet.of(AccessType.STATIC): EnumSet.noneOf(AccessType.class));
  }

  public String getReturnTypeName() {
    return returnTypeName;
  }

  public DataType getReturnType() {
    return DataType.getDataType(returnType);
  }

  public IParameter[] getParameters() {
    return new IParameter[] { };
  }

  public int getExtent() {
    if (this.extent == 32769) {
      return -1;
    }
    return this.extent;
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
