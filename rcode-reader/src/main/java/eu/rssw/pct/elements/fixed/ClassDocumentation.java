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

import java.util.ArrayList;
import java.util.Collection;

import eu.rssw.pct.elements.IClassDocumentation;
import eu.rssw.pct.elements.IMethodDocumentation;
import eu.rssw.pct.elements.IElementDocumentation;

public class ClassDocumentation implements IClassDocumentation {
  private final String docName;
  private final String type;
  private final String description;

  private final Collection<IMethodDocumentation> methods = new ArrayList<>();
  private final Collection<IElementDocumentation> properties = new ArrayList<>();

  public ClassDocumentation(String docName, String type, String description) {
    this.docName = docName;
    this.type = type;
    this.description = description;
  }

  public void addMethod(IMethodDocumentation element) {
    methods.add(element);
  }

  public void addProperty(PropertyDocumentation element) {
    properties.add(element);
  }

  @Override
  public Collection<IMethodDocumentation> getMethods() {
    return methods;
  }

  @Override
  public Collection<IElementDocumentation> getProperties() {
    return properties;
  }

  @Override
  public String getName() {
    return docName;
  }

  @Override
  public String toString() {
    return String.format("Class documentation of %s", docName);
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getDescription() {
    return description;
  }

}
