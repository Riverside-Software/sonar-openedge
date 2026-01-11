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
package eu.rssw.pct.elements.fixed;

import java.util.EnumSet;

import eu.rssw.pct.elements.AbstractAccessibleElement;
import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.IIndexComponentElement;
import eu.rssw.pct.elements.IIndexElement;

public class IndexElement extends AbstractAccessibleElement implements IIndexElement {

  private final IIndexComponentElement[] indexComponents;
  private final boolean primary;
  private final boolean unique;
  private final boolean wordIndex;

  public IndexElement(String name, boolean primary, boolean unique, boolean wordIndex,
      IIndexComponentElement... indexComponents) {
    super(name, EnumSet.of(AccessType.PUBLIC));
    this.primary = primary;
    this.unique = unique;
    this.wordIndex = wordIndex;
    this.indexComponents = indexComponents;
  }

  @Override
  public int getSizeInRCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public IIndexComponentElement[] getIndexComponents() {
    return indexComponents;
  }

  @Override
  public boolean isPrimary() {
    return primary;
  }

  @Override
  public boolean isUnique() {
    return unique;
  }

  @Override
  public boolean isWordIndex() {
    return wordIndex;
  }

  @Override
  public boolean isDefaultIndex() {
    // unused
    return false;
  }

}
