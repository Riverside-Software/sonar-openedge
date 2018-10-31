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
package eu.rssw.pct.elements.v12;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.Set;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IEnumDescriptor;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.v11.PropertyElementV11;

public class PropertyElementV12 extends PropertyElementV11 {
  private final IEnumDescriptor enumDesc;

  public PropertyElementV12(String name, Set<AccessType> accessType, int flags, IVariableElement var, IMethodElement getter, IMethodElement setter, IEnumDescriptor enumDesc) {
    super(name, accessType, flags, var, getter, setter);
    this.enumDesc = enumDesc;
  }

  public static IPropertyElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    IVariableElement variable = null;
    int currPos = currentPos + 16;
    if ((flags & PROPERTY_AS_VARIABLE) != 0) {
      variable = VariableElementV12.fromDebugSegment("", accessType, segment, currPos, textAreaOffset, order);
      currPos += variable.getSizeInRCode();
    }

    IMethodElement getter = null;
    if ((flags & HAS_GETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_GETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_GETTER) != 0)
        atp.add(AccessType.PROTECTED);
      getter = MethodElementV12.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
      currPos += getter.getSizeInRCode();
    }
    IMethodElement setter = null;
    if ((flags & HAS_SETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_SETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_SETTER) != 0)
        atp.add(AccessType.PROTECTED);
      setter = MethodElementV12.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
      currPos += setter.getSizeInRCode();
    }
    IEnumDescriptor enumDesc = null;
    if ((flags & PROPERTY_IS_ENUM) != 0) {
      enumDesc = EnumDescriptorV12.fromDebugSegment("", segment, currentPos, textAreaOffset, order);
    }
    return new PropertyElementV12(name2, accessType, flags, variable, getter, setter, enumDesc);
  }

  @Override
  public int getSizeInRCode() {
    int size = 16;
    if (this.propertyAsVariable()) {
      size += this.getVariable().getSizeInRCode();
    }
    if (this.hasGetter()) {
      size += this.getGetter().getSizeInRCode();
    }
    if (this.hasSetter()) {
      size += this.getSetter().getSizeInRCode();
    }
    if (enumDesc != null) {
      size += enumDesc.getSizeInRCode();
    }
    return size;
  }

}
