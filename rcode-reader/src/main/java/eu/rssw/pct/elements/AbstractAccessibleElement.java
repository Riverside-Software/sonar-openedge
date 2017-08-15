/*
 * RCode library - OpenEdge plugin for SonarQube
 * Copyright (C) 2017 Riverside Software
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

import java.util.Set;

import eu.rssw.pct.AccessType;

public abstract class AbstractAccessibleElement extends AbstractElement {
  protected Set<AccessType> accessType;

  public AbstractAccessibleElement(String name) {
    this(name, null);
  }

  public AbstractAccessibleElement(String name, Set<AccessType> accessType) {
    super(name);
    this.accessType = accessType;
  }

  public boolean isProtected() {
    return (accessType != null) && accessType.contains(AccessType.PROTECTED);
  }

  public boolean isPublic() {
    return (accessType != null) && accessType.contains(AccessType.PUBLIC);
  }

  public boolean isPrivate() {
    return (accessType != null) && accessType.contains(AccessType.PRIVATE);
  }

  public boolean isAbstract() {
    return (accessType != null) && accessType.contains(AccessType.ABSTRACT);
  }

  public boolean isStatic() {
    return (accessType != null) && accessType.contains(AccessType.STATIC);
  }
}
