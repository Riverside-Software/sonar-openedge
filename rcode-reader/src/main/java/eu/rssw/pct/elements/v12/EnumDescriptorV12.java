/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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

import eu.rssw.pct.elements.AbstractElement;
import eu.rssw.pct.elements.IEnumDescriptor;

public class EnumDescriptorV12 extends AbstractElement implements IEnumDescriptor {
  private long value;
  private int dupIdx;

  public EnumDescriptorV12(String name, long value, int dupIdx) {
    super(name);
    this.value = value;
    this.dupIdx = dupIdx;
  }

  @Override
  public long getValue() {
    return value;
  }

  public int getDupIdx() {
    return dupIdx;
  }

  public static IEnumDescriptor fromDebugSegment(String name, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    long value = ByteBuffer.wrap(segment, currentPos, Long.BYTES).order(ByteOrder.BIG_ENDIAN).getLong();
    int dupIdx = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(ByteOrder.BIG_ENDIAN).getShort();

    return new EnumDescriptorV12(name, value, dupIdx);
  }

  @Override
  public int getSizeInRCode() {
    return 16;
  }
}
