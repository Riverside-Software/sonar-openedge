/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package eu.rssw.pct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.rssw.pct.elements.IBufferElement;
import eu.rssw.pct.elements.IEventElement;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.IVariableElement;

public class TypeInfo {
  private static final int IS_FINAL = 1;
  private static final int IS_INTERFACE = 2;
  private static final int USE_WIDGET_POOL = 4;
  private static final int IS_DOTNET = 8;
  private static final int HAS_STATICS = 64;
  private static final int IS_BUILTIN = 128;
  private static final int IS_HYBRID = 2048;
  private static final int HAS_DOTNETBASE = 4096;
  private static final int IS_ABSTRACT = 32768;
  private static final int IS_SERIALIZABLE = 65536;
  
  protected String typeName;
  protected String parentTypeName;
  protected String assemblyName;
  protected int flags;
  private List<String> interfaces = new ArrayList<>();

  private Collection<IMethodElement> methods = new ArrayList<>();
  private Collection<IPropertyElement> properties = new ArrayList<>();
  private Collection<IEventElement> events = new ArrayList<>();
  private Collection<IVariableElement> variables = new ArrayList<>();
  private Collection<ITableElement> tables = new ArrayList<>();
  private Collection<IBufferElement> buffers = new ArrayList<>();

  public IBufferElement getBufferFor(String name) {
    for (IBufferElement tbl : buffers) {
      if (tbl.getName().equalsIgnoreCase(name)) {
        return tbl;
      }
    }
    return null;
  }

  public boolean hasTempTable(String inName) {
    for (ITableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasMethod(String name) {
    for (IMethodElement mthd : methods) {
      if (mthd.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  public ITableElement getTempTable(String inName) {
    for (ITableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return tbl;
      }
    }
    return null;
  }

  public boolean hasProperty(String name) {
    for (IPropertyElement prop : properties) {
      if (prop.getName().equalsIgnoreCase(name) && (prop.isPublic() || prop.isProtected()))
        return true;
    }
    return false;
  }

  protected IPropertyElement getProperty(String name) {
    // Only for testing
    for (IPropertyElement prop : properties) {
      if (prop.getName().equalsIgnoreCase(name))
        return prop;
    }
    return null;
  }

  public boolean hasBuffer(String inName) {
    // TODO Can it be abbreviated ??
    for (IBufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
  }

  public IBufferElement getBuffer(String inName) {
    for (IBufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return buf;
      }
    }
    return null;
  }

  public Collection<IMethodElement> getMethods() {
    return methods;
  }

  public Collection<IPropertyElement> getProperties() {
    return properties;
  }

  public Collection<IEventElement> getEvents() {
    return events;
  }

  public Collection<IVariableElement> getVariables() {
    return variables;
  }

  public Collection<ITableElement> getTables() {
    return tables;
  }

  public Collection<IBufferElement> getBuffers() {
    return buffers;
  }

  public String getTypeName() {
    return typeName;
  }

  public String getParentTypeName() {
    return parentTypeName;
  }

  public String getAssemblyName() {
    return assemblyName;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  @Override
  public String toString() {
    return String.format("Type info %s - Parent %s", typeName, parentTypeName);
  }

  public boolean isFinal() {
    return (flags & IS_FINAL) != 0;
  }

  public boolean isInterface() {
    return (flags & IS_INTERFACE) != 0;
  }

  public boolean hasStatics() {
    return (flags & HAS_STATICS) != 0;
  }

  public boolean isBuiltIn() {
    return (flags & IS_BUILTIN) != 0;
  }

  public boolean isHybrid() {
    return (flags & IS_HYBRID) != 0;
  }

  public boolean hasDotNetBase() {
    return (flags & HAS_DOTNETBASE) != 0;
  }

  public boolean isAbstract() {
    return (flags & IS_ABSTRACT) != 0;
  }

  public boolean isSerializable() {
    return (flags & IS_SERIALIZABLE) != 0;
  }

  public boolean isUseWidgetPool() {
    return (flags & USE_WIDGET_POOL) != 0;
  }

  protected boolean isDotNet() {
    return (flags & IS_DOTNET) != 0;
  }

}
