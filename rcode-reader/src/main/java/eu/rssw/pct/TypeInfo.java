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
package eu.rssw.pct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import eu.rssw.pct.elements.BufferElement;
import eu.rssw.pct.elements.EventElement;
import eu.rssw.pct.elements.MethodElement;
import eu.rssw.pct.elements.PropertyElement;
import eu.rssw.pct.elements.TableElement;
import eu.rssw.pct.elements.VariableElement;

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

  private Collection<MethodElement> methods = new ArrayList<>();
  private Collection<PropertyElement> properties = new ArrayList<>();
  private Collection<EventElement> events = new ArrayList<>();
  private Collection<VariableElement> variables = new ArrayList<>();
  private Collection<TableElement> tables = new ArrayList<>();
  private Collection<BufferElement> buffers = new ArrayList<>();

  public BufferElement getBufferFor(String name) {
    for (BufferElement tbl : buffers) {
      if (tbl.getName().equalsIgnoreCase(name)) {
        return tbl;
      }
    }
    return null;
  }

  public boolean hasTempTable(String inName) {
    for (TableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
  }

  public TableElement getTempTable(String inName) {
    for (TableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return tbl;
      }
    }
    return null;
  }

  public boolean hasProperty(String name) {
    for (PropertyElement prop : properties) {
      if (prop.getName().equalsIgnoreCase(name) && (prop.isPublic() || prop.isProtected()))
        return true;
    }
    return false;
  }

  protected PropertyElement getProperty(String name) {
    // Only for testing
    for (PropertyElement prop : properties) {
      if (prop.getName().equalsIgnoreCase(name))
        return prop;
    }
    return null;
  }

  public boolean hasBuffer(String inName) {
    // TODO Can it be abbreviated ??
    for (BufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
  }

  public BufferElement getBuffer(String inName) {
    for (BufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return buf;
      }
    }
    return null;
  }

  public Collection<MethodElement> getMethods() {
    return methods;
  }

  public Collection<PropertyElement> getProperties() {
    return properties;
  }

  public Collection<EventElement> getEvents() {
    return events;
  }

  public Collection<VariableElement> getVariables() {
    return variables;
  }

  public Collection<TableElement> getTables() {
    return tables;
  }

  public Collection<BufferElement> getBuffers() {
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
