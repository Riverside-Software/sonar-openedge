/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
import java.util.List;
import java.util.function.Function;


public interface ITypeInfo {
  String getTypeName();
  String getParentTypeName();
  String getAssemblyName();
  List<String> getInterfaces();

  boolean isFinal();
  boolean isInterface();
  boolean hasStatics();
  boolean isBuiltIn();
  boolean isHybrid();
  boolean hasDotNetBase();
  boolean isAbstract();
  boolean isSerializable();
  boolean isUseWidgetPool();

  Collection<IMethodElement> getMethods();
  Collection<IPropertyElement> getProperties();
  Collection<IEventElement> getEvents();
  Collection<IVariableElement> getVariables();
  Collection<ITableElement> getTables();
  Collection<IBufferElement> getBuffers();
  Collection<IDatasetElement> getDatasets();

  IBufferElement getBuffer(String inName);
  IBufferElement getBufferFor(String name);
  IPropertyElement getProperty(String name);
  ITableElement getTempTable(String inName);
  IDatasetElement getDataset(String dataset);

  boolean hasTempTable(String inName);
  boolean hasMethod(String name);
  boolean hasProperty(String name);
  boolean hasBuffer(String inName);

  /**
   * Returns simple name of this class (without package name)
   */
  default String getSimpleName() {
    String str = getTypeName();
    int lastDot = str.lastIndexOf('.');
    return lastDot == -1 ? str : str.substring(lastDot + 1);
  }

  /**
   * Returns package name of this class. Returns empty string if class has no package.
   */
  default String getPackageName() {
    String str = getTypeName();
    int lastDot = str.lastIndexOf('.');
    return lastDot == -1 ? "" : str.substring(0 ,lastDot );
  }

  default boolean isAssignableFrom(String clsName, Function<String, ITypeInfo> provider) {
    ITypeInfo info = provider.apply(clsName);
    if (info == null)
      return false;
    if (info.getTypeName().equals(getTypeName()))
      return true;
    for (String str : info.getInterfaces()) {
      if (str.equals(getTypeName()))
          return true;
    }
    return isAssignableFrom(info.getParentTypeName(), provider);
  }

  default IMethodElement getExactMatch(Function<String, ITypeInfo> provider, String method, DataType... parameters) {
    for (IMethodElement elem : getMethods()) {
      if (method.equalsIgnoreCase(elem.getName()) && (elem.getParameters().length == parameters.length)) {
        boolean match = true;
        for (int zz = 0; zz < elem.getParameters().length; zz++) {
          match &= elem.getParameters()[zz].getDataType().equals(parameters[zz]);
        }
        if (match)
          return elem;
      }
    }
    ITypeInfo parent = provider.apply(getParentTypeName());
    if (parent != null)
      return parent.getExactMatch(provider, method, parameters);

    return null;
  }

  default IMethodElement getCompatibleMatch(Function<String, ITypeInfo> provider, String method, DataType... parameters) {
    for (IMethodElement elem : getMethods()) {
      if (method.equalsIgnoreCase(elem.getName()) && (elem.getParameters().length == parameters.length)) {
        boolean match = true;
        for (int zz = 0; zz < elem.getParameters().length; zz++) {
          match &= elem.getParameters()[zz].getDataType().isCompatible(parameters[zz], provider);
        }
        if (match)
          return elem;
      }
    }
    ITypeInfo parent = provider.apply(getParentTypeName());
    if (parent != null)
      return parent.getCompatibleMatch(provider, method, parameters);

    return null;
  }

  default IMethodElement getMethod(Function<String, ITypeInfo> provider, String method, DataType... parameters) {
    IMethodElement exactMatch = getExactMatch(provider, method, parameters);
    if (exactMatch != null)
      return exactMatch;
    return getCompatibleMatch(provider, method, parameters);
  }
}