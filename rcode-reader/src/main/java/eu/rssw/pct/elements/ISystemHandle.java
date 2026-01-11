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
package eu.rssw.pct.elements;

import java.util.Collection;

public interface ISystemHandle {
  String getName();
  String getDescription();

  Collection<IMethodElement> getMethods();
  Collection<IAttributeElement> getAttributes();

  default boolean hasMethod(String name) {
    for (var mthd : getMethods()) {
      if (mthd.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  default boolean hasAttribute(String name) {
    for (var attr : getAttributes()) {
      if (attr.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  default IMethodElement getMethod(String method) {
    var tmp = method; 
    for (var elem : getMethods()) {
      if (tmp.equalsIgnoreCase(elem.getName())) {
        return elem;
      }
    }
    return null;
  }

  String getMethodDocumentation(String method);

  default IAttributeElement getAttribute(String attribute) {
    for (var elem : getAttributes()) {
      if (attribute.equalsIgnoreCase(elem.getName())) {
        return elem;
      }
    }

    return null;
  }

}