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
import java.util.EnumSet;
import java.util.Set;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IEnumDescriptor;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.IVariableElement;

public class PropertyElementV11 extends AbstractAccessibleElement implements IPropertyElement {
  public static final int PUBLIC_GETTER = 1;
  public static final int PROTECTED_GETTER = 2;
  public static final int PRIVATE_GETTER = 4;
  public static final int PUBLIC_SETTER = 8;
  public static final int PROTECTED_SETTER = 16;
  public static final int PRIVATE_SETTER = 32;
  public static final int HAS_GETTER = 256;
  public static final int HAS_SETTER = 512;
  public static final int PROPERTY_AS_VARIABLE = 1024;
  public static final int PROPERTY_IS_INDEXED = 8192;
  public static final int PROPERTY_IS_DEFAULT = 16384;
  public static final int PROPERTY_IS_ENUM = 65536;
  public static final int PACKAGE_PRIVATE_GETTER = 0x20000;
  public static final int PACKAGE_PROTECTED_GETTER = 0x40000;
  public static final int PACKAGE_PRIVATE_SETTER = 0x80000;
  public static final int PACKAGE_PROTECTED_SETTER = 0x100000;

  private final int flags;
  private final IVariableElement variable;
  private final IMethodElement getter;
  private final IMethodElement setter;

  public PropertyElementV11(String name, Set<AccessType> accessType, int flags, IVariableElement varElement, IMethodElement getter, IMethodElement setter) {
    super(name, accessType);
    this.flags = flags;
    this.variable = varElement;
    this.getter = getter;
    this.setter = setter;
  }

  public static IPropertyElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort() & 0xffff;

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    IVariableElement variable = null;
    int currPos = currentPos + 8;
    if ((flags & PROPERTY_AS_VARIABLE) != 0) {
      variable = VariableElementV11.fromDebugSegment("", accessType, segment, currPos, textAreaOffset, order);
      currPos += variable.getSizeInRCode();
    }

    IMethodElement getter = null;
    if ((flags & HAS_GETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_GETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_GETTER) != 0)
        atp.add(AccessType.PROTECTED);
      getter = MethodElementV11.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
      currPos += getter.getSizeInRCode();
    }
    IMethodElement setter = null;
    if ((flags & HAS_SETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_SETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_SETTER) != 0)
        atp.add(AccessType.PROTECTED);
      setter = MethodElementV11.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
    }
    return new PropertyElementV11(name2, accessType, flags, variable, getter, setter);
  }

  public int getFlags() {
    return flags;
  }

  public IVariableElement getVariable() {
    return this.variable;
  }

  @Override
  public IMethodElement getGetter() {
    return this.getter;
  }

  @Override
  public IMethodElement getSetter() {
    return this.setter;
  }

  @Override
  public IEnumDescriptor getEnumDescriptor() {
    return null;
  }

  @Override
  public int getSizeInRCode() {
    int size = 8;
    if (this.propertyAsVariable()) {
      size += this.variable.getSizeInRCode();
    }
    if (this.hasGetter()) {
      size += this.getter.getSizeInRCode();
    }
    if (this.hasSetter()) {
      size += this.setter.getSizeInRCode();
    }
    return size;
  }

  public boolean propertyAsVariable() {
    return (flags & PROPERTY_AS_VARIABLE) != 0;
  }

  public boolean hasGetter() {
    return (flags & HAS_GETTER) != 0;
  }

  public boolean hasSetter() {
    return (flags & HAS_SETTER) != 0;
  }

  public boolean isGetterPublic() {
    return (flags & PUBLIC_GETTER) != 0;
  }

  public boolean isGetterProtected() {
    return (flags & PROTECTED_GETTER) != 0;
  }

  public boolean isGetterPrivate() {
    return (flags & PRIVATE_GETTER) != 0;
  }

  public boolean isSetterPublic() {
    return (flags & PUBLIC_SETTER) != 0;
  }

  public boolean isSetterProtected() {
    return (flags & PROTECTED_SETTER) != 0;
  }

  public boolean isSetterPrivate() {
    return (flags & PRIVATE_SETTER) != 0;
  }

  public boolean isIndexed() {
    return (flags & PROPERTY_IS_INDEXED) != 0;
  }

  public boolean isDefault() {
    return (flags & PROPERTY_IS_DEFAULT) != 0;
  }

  public boolean canRead() {
    return (isGetterPrivate() || isGetterProtected() || isGetterPublic());
  }

  public boolean canWrite() {
    return (isSetterPrivate() || isSetterProtected() || isSetterPublic());
  }

  @Override
  public String toString() {
    return String.format("Property %s AS %s", getName(), getVariable().getDataType());
  }

  @Override
  public int hashCode() {
    return (getName() + "/" + variable.toString()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IPropertyElement) {
      IPropertyElement obj2 = (IPropertyElement) obj;
      return getName().equals(obj2.getName()) && variable.equals(obj2.getVariable());
    }
    return false;
  }
}
