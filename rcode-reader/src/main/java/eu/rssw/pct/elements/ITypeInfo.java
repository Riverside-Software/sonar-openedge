/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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

}