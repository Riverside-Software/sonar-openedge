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

import java.nio.ByteOrder;

import eu.rssw.pct.elements.AbstractElement;
import eu.rssw.pct.elements.IEnumDescriptor;

public class EnumDescriptorV12 extends AbstractElement implements IEnumDescriptor {

  public EnumDescriptorV12(String name) {
    super(name);
  }

  public static IEnumDescriptor fromDebugSegment(String name, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    return new EnumDescriptorV12(name);
  }

  @Override
  public int getSizeInRCode() {
    return 16;
  }
}
