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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IDataRelationElement;
import eu.rssw.pct.elements.IDatasetElement;

public class DatasetElement extends AbstractAccessibleElement implements IDatasetElement {
  private final String[] bufferNames;
  private final IDataRelationElement[] relations;

  public DatasetElement(String name, String[] bufferNames, IDataRelationElement[] relations) {
    super(name, EnumSet.of(AccessType.PUBLIC));
    this.bufferNames = bufferNames;
    this.relations = relations;
  }

  @Override
  public IDataRelationElement[] getDataRelations() {
    return this.relations;
  }

  @Override
  public String[] getBufferNames() {
    return bufferNames;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return String.format("Dataset %s for %d buffer(s) and %d relations", getName(), bufferNames.length, relations.length);
  }

  @Override
  public int hashCode() {
    String str1 = String.join("/", bufferNames);
    String str2 = Arrays.stream(relations).map(IDataRelationElement::toString).collect(Collectors.joining(","));
    return (str1 + "-" + str2).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IDatasetElement obj2) {
      return (Arrays.deepEquals(bufferNames, obj2.getBufferNames())
          && Arrays.deepEquals(relations, obj2.getDataRelations()));
    }
    return false;
  }
}
