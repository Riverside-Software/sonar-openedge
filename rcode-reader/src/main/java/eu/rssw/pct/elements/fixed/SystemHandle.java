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

import java.util.ArrayList;
import java.util.Collection;

import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ISystemHandle;
import eu.rssw.pct.elements.IAttributeElement;

public class SystemHandle implements ISystemHandle {

  protected String sysName;

  private final Collection<IMethodElement> methods = new ArrayList<>();
  private final Collection<IAttributeElement> attributes = new ArrayList<>();

  public SystemHandle() {
    // No-op
  }

  public SystemHandle(String sysName) {
    this.sysName = sysName;
  }

  public void addMethod(IMethodElement element) {
    methods.add(element);
  }

  public void addAttribute(IAttributeElement element) {
    attributes.add(element);
  }

  @Override
  public boolean hasMethod(String name) {
    for (IMethodElement mthd : methods) {
      if (mthd.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  @Override
  public boolean hasAttribute(String name) {
    return false;
  }

  @Override
  public Collection<IMethodElement> getMethods() {
    return methods;
  }

  @Override
  public Collection<IAttributeElement> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return sysName;
  }

  @Override
  public String toString() {
    return String.format("SystemHandle %s", sysName);
  }

}
