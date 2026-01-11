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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.rssw.pct.elements.IBufferElement;
import eu.rssw.pct.elements.IDatasetElement;
import eu.rssw.pct.elements.IEventElement;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.IVariableElement;
import eu.rssw.pct.elements.TypeInfoAdapter;

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(TypeInfoAdapter.class)
public class TypeInfo implements ITypeInfo {
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
  private final List<String> interfaces = new ArrayList<>();

  private final Collection<IMethodElement> methods = new ArrayList<>();
  private final Collection<IPropertyElement> properties = new ArrayList<>();
  private final Collection<IVariableElement> variables = new ArrayList<>();
  private final Collection<IEventElement> events = new ArrayList<>();
  private final Collection<ITableElement> tables = new ArrayList<>();
  private final Collection<IBufferElement> buffers = new ArrayList<>();

  public TypeInfo() {
    // No-op
  }

  public TypeInfo(String typeName, boolean iface, boolean abstrct, String parentTypeName, String assemblyName,
      String... interfaces) {
    this.typeName = typeName;
    this.parentTypeName = parentTypeName;
    this.assemblyName = assemblyName;
    this.interfaces.addAll(Arrays.asList(interfaces));
    this.flags = (iface ? IS_INTERFACE : 0) | (abstrct ? IS_ABSTRACT : 0);
  }

  public void addEvent(IEventElement element) {
    events.add(element);
    if (element.isStatic())
      this.flags = this.flags | HAS_STATICS;
  }

  public void addMethod(IMethodElement element) {
    methods.add(element);
    if (element.isStatic())
      this.flags = this.flags | HAS_STATICS;
  }

  public void addProperty(IPropertyElement element) {
    properties.add(element);
    if (element.isStatic())
      this.flags = this.flags | HAS_STATICS;
  }

  public void addVariable(IVariableElement element) {
    variables.add(element);
    if (element.isStatic())
      this.flags = this.flags | HAS_STATICS;
  }

  public void addTable(ITableElement element) {
    tables.add(element);
    if (element.isStatic())
      this.flags = this.flags | HAS_STATICS;
    var buf = new BufferElement(element.getName());
    buffers.add(buf);
  }

  @Override
  public IBufferElement getBufferFor(String name) {
    for (IBufferElement tbl : buffers) {
      if (tbl.getName().equalsIgnoreCase(name)) {
        return tbl;
      }
    }
    return null;
  }

  @Override
  public boolean hasTempTable(String inName) {
    for (ITableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
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
  public ITableElement getTempTable(String inName) {
    for (ITableElement tbl : tables) {
      if (tbl.getName().equalsIgnoreCase(inName)) {
        return tbl;
      }
    }
    return null;
  }

  @Override
  public boolean hasProperty(String name) {
    return false;
  }

  @Override
  public boolean hasBuffer(String inName) {
    for (IBufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public IBufferElement getBuffer(String inName) {
    for (IBufferElement buf : buffers) {
      if (buf.getName().equalsIgnoreCase(inName)) {
        return buf;
      }
    }
    return null;
  }

  @Override
  public Collection<IMethodElement> getMethods() {
    return methods;
  }

  @Override
  public Collection<IPropertyElement> getProperties() {
    return properties;
  }

  @Override
  public Collection<IEventElement> getEvents() {
    return Collections.emptyList();
  }

  @Override
  public Collection<IVariableElement> getVariables() {
    return variables;
  }

  @Override
  public Collection<ITableElement> getTables() {
    return tables;
  }

  @Override
  public Collection<IBufferElement> getBuffers() {
    return buffers;
  }

  @Override
  public Collection<IDatasetElement> getDatasets() {
    return Collections.emptyList();
  }

  @Override
  public IDatasetElement getDataset(String dataset) {
    return null;
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public String getParentTypeName() {
    return parentTypeName;
  }

  @Override
  public String getAssemblyName() {
    return assemblyName;
  }

  @Override
  public List<String> getInterfaces() {
    return interfaces;
  }

  @Override
  public String toString() {
    return String.format("TypeInfo %s", typeName);
  }

  @Override
  public boolean isFinal() {
    return (flags & IS_FINAL) != 0;
  }

  @Override
  public boolean isInterface() {
    return (flags & IS_INTERFACE) != 0;
  }

  @Override
  public boolean hasStatics() {
    return (flags & HAS_STATICS) != 0;
  }

  @Override
  public boolean isBuiltIn() {
    return (flags & IS_BUILTIN) != 0;
  }

  @Override
  public boolean isHybrid() {
    return (flags & IS_HYBRID) != 0;
  }

  @Override
  public boolean hasDotNetBase() {
    return (flags & HAS_DOTNETBASE) != 0;
  }

  @Override
  public boolean isAbstract() {
    return (flags & IS_ABSTRACT) != 0;
  }

  @Override
  public boolean isSerializable() {
    return (flags & IS_SERIALIZABLE) != 0;
  }

  @Override
  public boolean isUseWidgetPool() {
    return (flags & USE_WIDGET_POOL) != 0;
  }

  protected boolean isDotNet() {
    return (flags & IS_DOTNET) != 0;
  }

}
