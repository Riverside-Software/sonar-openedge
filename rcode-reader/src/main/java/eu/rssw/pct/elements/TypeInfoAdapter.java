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

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TypeInfoAdapter extends TypeAdapter<ITypeInfo> {
  private final MethodElementAdapter methodAdapter = new MethodElementAdapter();
  private final PropertyElementAdapter propertyAdapter = new PropertyElementAdapter();
  private final EventElementAdapter eventAdapter = new EventElementAdapter();
  private final VariableElementAdapter variableAdapter = new VariableElementAdapter();
  private final TableElementAdapter tableAdapter = new TableElementAdapter();
  private final BufferElementAdapter bufferAdapter = new BufferElementAdapter();
  private final DatasetElementAdapter datasetAdapter = new DatasetElementAdapter();

  @Override
  public void write(JsonWriter out, ITypeInfo value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.beginObject();
    out.name("typeName").value(value.getTypeName());
    out.name("parentTypeName").value(value.getParentTypeName());
    out.name("assemblyName").value(value.getAssemblyName());

    // Flags
    out.name("isFinal").value(value.isFinal());
    out.name("isInterface").value(value.isInterface());
    out.name("hasStatics").value(value.hasStatics());
    out.name("isBuiltIn").value(value.isBuiltIn());
    out.name("isHybrid").value(value.isHybrid());
    out.name("hasDotNetBase").value(value.hasDotNetBase());
    out.name("isAbstract").value(value.isAbstract());
    out.name("isSerializable").value(value.isSerializable());
    out.name("isUseWidgetPool").value(value.isUseWidgetPool());

    // Interfaces
    out.name("interfaces");
    out.beginArray();
    for (String iface : value.getInterfaces()) {
      out.value(iface);
    }
    out.endArray();

    // Methods
    out.name("methods");
    out.beginArray();
    for (IMethodElement method : value.getMethods()) {
      methodAdapter.write(out, method);
    }
    out.endArray();

    // Properties
    out.name("properties");
    out.beginArray();
    for (IPropertyElement prop : value.getProperties()) {
      propertyAdapter.write(out, prop);
    }
    out.endArray();

    // Events
    out.name("events");
    out.beginArray();
    for (IEventElement event : value.getEvents()) {
      eventAdapter.write(out, event);
    }
    out.endArray();

    // Variables
    out.name("variables");
    out.beginArray();
    for (IVariableElement v : value.getVariables()) {
      variableAdapter.write(out, v);
    }
    out.endArray();

    // Tables
    out.name("tables");
    out.beginArray();
    for (ITableElement table : value.getTables()) {
      tableAdapter.write(out, table);
    }
    out.endArray();

    // Buffers
    out.name("buffers");
    out.beginArray();
    for (IBufferElement buffer : value.getBuffers()) {
      bufferAdapter.write(out, buffer);
    }
    out.endArray();

    // Datasets
    out.name("datasets");
    out.beginArray();
    for (IDatasetElement dataset : value.getDatasets()) {
      datasetAdapter.write(out, dataset);
    }
    out.endArray();

    out.endObject();
  }

  @Override
  public ITypeInfo read(JsonReader in) throws IOException {
    throw new UnsupportedOperationException();
  }
}
