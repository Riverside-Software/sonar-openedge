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
package eu.rssw.pct.elements;

import java.util.Collection;

public interface IClassDocumentation {
  String getName();

  String getType();

  String getDescription();

  Collection<IMethodDocumentation> getMethods();

  Collection<IElementDocumentation> getProperties();

  default boolean hasMethod(String name) {
    for (var mthd : getMethods()) {
      if (mthd.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  default boolean hasProperty(String name) {
    for (var prop : getProperties()) {
      if (prop.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  /**
   * Return method with exactly the same name
   */
  default IMethodDocumentation getMethod(String method) {
    for (var elem : getMethods()) {
      if (method.equalsIgnoreCase(elem.getName())) {
        return elem;
      }
    }
    return null;
  }

  /**
   * Return property with exactly the same name
   */
  default IElementDocumentation getProperty(String property) {
    for (var elem : getProperties()) {
      if (property.equalsIgnoreCase(elem.getName())) {
        return elem;
      }
    }
    return null;
  }

}
