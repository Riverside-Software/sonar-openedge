/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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
import eu.rssw.pct.elements.IEnumDescriptor;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.IVariableElement;

public class PropertyElement extends AbstractAccessibleElement implements IPropertyElement {
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

  private final IVariableElement varElement;
  private final Long enumValue;

  public PropertyElement(String name, boolean isStatic) {
    this(name, isStatic, DataType.VOID);
  }

  public PropertyElement(String name, boolean isStatic, DataType dataType) {
    this(name, isStatic, dataType, null);
  }

  public PropertyElement(String name, boolean isStatic, DataType dataType, Long enumValue) {
    super(name, isStatic ? EnumSet.of(AccessType.STATIC, AccessType.PUBLIC) : EnumSet.of(AccessType.PUBLIC));
    varElement = new VariableElement(name, dataType);
    this.enumValue = enumValue;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IVariableElement getVariable() {
    return varElement;
  }

  @Override
  public IMethodElement getGetter() {
    return null;
  }

  @Override
  public IMethodElement getSetter() {
    return null;
  }

  @Override
  public IEnumDescriptor getEnumDescriptor() {
    if (enumValue == null)
      return null;
    else
      return new IEnumDescriptor() {
        @Override
        public int getSizeInRCode() {
          return 0;
        }
        @Override
        public String getName() {
          return null;
        }
        @Override
        public long getValue() {
          return enumValue;
        }
      };
  }

}
