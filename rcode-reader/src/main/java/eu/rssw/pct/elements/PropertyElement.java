/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class PropertyElement extends AbstractAccessibleElement {
  private static final int PUBLIC_GETTER = 1;
  private static final int PROTECTED_GETTER = 2;
  private static final int PRIVATE_GETTER = 4;
  private static final int PUBLIC_SETTER = 8;
  private static final int PROTECTED_SETTER = 16;
  private static final int PRIVATE_SETTER = 32;
  private static final int HAS_GETTER = 256;
  private static final int HAS_SETTER = 512;
  private static final int PROPERTY_AS_VARIABLE = 1024;
  private static final int PROPERTY_IS_INDEXED = 8192;
  private static final int PROPERTY_IS_DEFAULT = 16384;

  private final int flags;
  private final VariableElement variable;
  private final MethodElement getter;
  private final MethodElement setter;

  public PropertyElement(String name, Set<AccessType> accessType, int flags, VariableElement var, MethodElement getter, MethodElement setter) {
    super(name, accessType);
    this.flags = flags;
    this.variable = var;
    this.getter = getter;
    this.setter = setter;
  }

  public static PropertyElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    VariableElement variable = null;
    int currPos = currentPos + 8;
    if ((flags & PROPERTY_AS_VARIABLE) != 0) {
      variable = VariableElement.fromDebugSegment("", accessType, segment, currPos, textAreaOffset, order);
      currPos += variable.size();
    }

    MethodElement getter = null;
    if ((flags & HAS_GETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_GETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_GETTER) != 0)
        atp.add(AccessType.PROTECTED);
      getter = MethodElement.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
      currPos += getter.size();
    }
    MethodElement setter = null;
    if ((flags & HAS_SETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_SETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_SETTER) != 0)
        atp.add(AccessType.PROTECTED);
      setter = MethodElement.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
    }
    return new PropertyElement(name2, accessType, flags, variable, getter, setter);
  }

  @Override
  public int size() {
    int size = 8;
    if (this.propertyAsVariable()) {
      size += this.variable.size();
    }
    if (this.hasGetter()) {
      size += this.getter.size();
    }
    if (this.hasSetter()) {
      size += this.setter.size();
    }
    return size;
  }

  public VariableElement getVariable() {
    return this.variable;
  }

  public MethodElement getGetter() {
    return this.getter;
  }

  public MethodElement getSetter() {
    return this.setter;
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

}
