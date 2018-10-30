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
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.IVariableElement;

public class PropertyElement extends eu.rssw.pct.elements.v11.PropertyElement {
  // TODO Implement enum support

  public PropertyElement(String name, Set<AccessType> accessType, int flags, IVariableElement var, IMethodElement getter, IMethodElement setter) {
    super(name, accessType, flags, var, getter, setter);
  }

  public static IPropertyElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int flags = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    IVariableElement variable = null;
    int currPos = currentPos + 8;
    if ((flags & PROPERTY_AS_VARIABLE) != 0) {
      variable = VariableElement.fromDebugSegment("", accessType, segment, currPos, textAreaOffset, order);
      currPos += variable.getSizeInRCode();
    }

    IMethodElement getter = null;
    if ((flags & HAS_GETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_GETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_GETTER) != 0)
        atp.add(AccessType.PROTECTED);
      getter = MethodElement.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
      currPos += getter.getSizeInRCode();
    }
    IMethodElement setter = null;
    if ((flags & HAS_SETTER) != 0) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if ((flags & PUBLIC_SETTER) != 0)
        atp.add(AccessType.PUBLIC);
      if ((flags & PROTECTED_SETTER) != 0)
        atp.add(AccessType.PROTECTED);
      setter = MethodElement.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
    }
    // TODO Implement enum support
    return new PropertyElement(name2, accessType, flags, variable, getter, setter);
  }

  @Override
  public int getSizeInRCode() {
    // TODO Implement enum support
    return super.getSizeInRCode();
  }

}
