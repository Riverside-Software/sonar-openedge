/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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

import java.util.EnumSet;
import java.util.Set;

public abstract class AbstractAccessibleElement extends AbstractElement implements IAccessibleElement {
  private Set<AccessType> accessType;

  public AbstractAccessibleElement(String name, Set<AccessType> accessType) {
    super(name);
    this.accessType = accessType == null ? EnumSet.noneOf(AccessType.class) : accessType;
  }

  public boolean isProtected() {
    return accessType.contains(AccessType.PROTECTED);
  }

  public boolean isPublic() {
    return accessType.contains(AccessType.PUBLIC);
  }

  public boolean isPrivate() {
    return accessType.contains(AccessType.PRIVATE);
  }

  public boolean isAbstract() {
    return accessType.contains(AccessType.ABSTRACT);
  }

  public boolean isStatic() {
    return accessType.contains(AccessType.STATIC);
  }
}
