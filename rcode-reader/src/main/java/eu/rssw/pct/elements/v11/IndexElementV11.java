/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2019 Riverside Software
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
import java.util.Arrays;

import com.google.common.base.Joiner;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.elements.AbstractElement;
import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.IIndexElement;

public class IndexElementV11 extends AbstractElement implements IIndexElement {
  private static final int UNIQUE_INDEX = 2;
  private static final int WORD_INDEX = 8;
  private static final int DEFAULT_INDEX = 16;

  private final IIndexComponentElement[] indexComponents;
  private final int primary;
  private final int flags;

  public IndexElementV11(String name, int primary, int flags, IIndexComponentElement[] indexComponents) {
    super(name);
    this.primary = primary;
    this.flags = flags;
    this.indexComponents = indexComponents;
  }

  protected static IIndexElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int primary = segment[currentPos];
    int flags = segment[currentPos + 1];

    int componentCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int nameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int currPos = currentPos + 16;
    IIndexComponentElement[] indexComponents = new IndexComponentElementV11[componentCount];
    for (int zz = 0; zz < componentCount; zz++) {
      IIndexComponentElement component = IndexComponentElementV11.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += component.getSizeInRCode();
      indexComponents[zz] = component;
    }

    return new IndexElementV11(name, primary, flags, indexComponents);
  }

  @Override
  public IIndexComponentElement[] getIndexComponents() {
    return this.indexComponents;
  }

  @Override
  public boolean isPrimary() {
    return primary == 1;
  }

  @Override
  public boolean isUnique() {
    return (flags & UNIQUE_INDEX) != 0;
  }

  @Override
  public boolean isWordIndex() {
    return (flags & WORD_INDEX) != 0;
  }

  @Override
  public boolean isDefaultIndex() {
    return (flags & DEFAULT_INDEX) != 0;
  }

  @Override
  public int getSizeInRCode() {
    int size = 16;
    for (IIndexComponentElement elem : indexComponents) {
      size += elem.getSizeInRCode();
    }
    return size;
  }

  @Override
  public String toString() {
    return String.format("Index %s %s %s - %d field(s)", getName(), isPrimary() ? "PRIMARY" : "",
        isUnique() ? "UNIQUE" : "", indexComponents.length);
  }

  @Override
  public int hashCode() {
    return (getName() + "/" + flags + "/" + Joiner.on('/').join(indexComponents)).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IIndexElement) {
      IIndexElement obj2 = (IIndexElement) obj;
      return getName().equals(obj2.getName()) && Arrays.deepEquals(indexComponents, obj2.getIndexComponents());
    }
    return false;

  }
}
