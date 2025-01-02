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

import java.util.Collection;
import java.util.List;

import eu.rssw.pct.elements.IBufferElement;
import eu.rssw.pct.elements.IDatasetElement;
import eu.rssw.pct.elements.IEventElement;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.IVariableElement;

/**
 * Proxy for a TypeInfo object. Used by SonarLint to delay file loading until required by the analyzer
 */
public abstract class TypeInfoProxy implements ITypeInfo {
  protected final String typeName;
  protected ITypeInfo typeInfo = null;

  protected TypeInfoProxy(String typeName) {
    this.typeName = typeName;
  }

  /**
   * typeInfo object is expected not to be null after execution of this method.
   */
  abstract void checkTypeInfo();

  public boolean isInitialized() {
    return typeInfo != null;
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  @Override
  public IBufferElement getBufferFor(String name) {
    checkTypeInfo();
    return typeInfo.getBufferFor(name);
  }

  @Override
  public boolean hasTempTable(String inName) {
    checkTypeInfo();
    return typeInfo.hasTempTable(inName);
  }

  @Override
  public boolean hasMethod(String name) {
    checkTypeInfo();
    return typeInfo.hasMethod(name);
  }

  @Override
  public ITableElement getTempTable(String inName) {
    checkTypeInfo();
    return typeInfo.getTempTable(inName);
  }

  @Override
  public boolean hasProperty(String name) {
    checkTypeInfo();
    return typeInfo.hasProperty(name);
  }

  @Override
  public IPropertyElement getProperty(String name) {
    checkTypeInfo();
    return typeInfo.getProperty(name);
  }

  @Override
  public boolean hasBuffer(String inName) {
    checkTypeInfo();
    return typeInfo.hasBuffer(inName);
  }

  @Override
  public IBufferElement getBuffer(String inName) {
    checkTypeInfo();
    return typeInfo.getBuffer(inName);
  }

  @Override
  public Collection<IMethodElement> getMethods() {
    checkTypeInfo();
    return typeInfo.getMethods();
  }

  @Override
  public Collection<IPropertyElement> getProperties() {
    checkTypeInfo();
    return typeInfo.getProperties();
  }

  @Override
  public Collection<IEventElement> getEvents() {
    checkTypeInfo();
    return typeInfo.getEvents();
  }

  @Override
  public Collection<IVariableElement> getVariables() {
    checkTypeInfo();
    return typeInfo.getVariables();
  }

  @Override
  public Collection<ITableElement> getTables() {
    checkTypeInfo();
    return typeInfo.getTables();
  }

  @Override
  public Collection<IBufferElement> getBuffers() {
    checkTypeInfo();
    return typeInfo.getBuffers();
  }

  @Override
  public Collection<IDatasetElement> getDatasets() {
    checkTypeInfo();
    return typeInfo.getDatasets();
  }

  @Override
  public IDatasetElement getDataset(String getDataset) {
    checkTypeInfo();
    return typeInfo.getDataset(getDataset);
  }

  @Override
  public String getParentTypeName() {
    checkTypeInfo();
    return typeInfo.getParentTypeName();
  }

  @Override
  public String getAssemblyName() {
    checkTypeInfo();
    return typeInfo.getAssemblyName();
  }

  @Override
  public List<String> getInterfaces() {
    checkTypeInfo();
    return typeInfo.getInterfaces();
  }

  @Override
  public String toString() {
    checkTypeInfo();
    return String.format("TypeInfoProxy for %s", typeInfo);
  }

  @Override
  public boolean isFinal() {
    checkTypeInfo();
    return typeInfo.isFinal();
  }

  @Override
  public boolean isInterface() {
    checkTypeInfo();
    return typeInfo.isInterface();
  }

  @Override
  public boolean hasStatics() {
    checkTypeInfo();
    return typeInfo.hasStatics();
  }

  @Override
  public boolean isBuiltIn() {
    checkTypeInfo();
    return typeInfo.isBuiltIn();
  }

  @Override
  public boolean isHybrid() {
    checkTypeInfo();
    return typeInfo.isHybrid();
  }

  @Override
  public boolean hasDotNetBase() {
    checkTypeInfo();
    return typeInfo.hasDotNetBase();
  }

  @Override
  public boolean isAbstract() {
    checkTypeInfo();
    return typeInfo.isAbstract();
  }

  @Override
  public boolean isSerializable() {
    checkTypeInfo();
    return typeInfo.isSerializable();
  }

  @Override
  public boolean isUseWidgetPool() {
    checkTypeInfo();
    return typeInfo.isUseWidgetPool();
  }

}
