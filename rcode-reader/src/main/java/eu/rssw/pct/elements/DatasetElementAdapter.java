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

public class DatasetElementAdapter extends TypeAdapter<IDatasetElement> {
  private final DataRelationElementAdapter relationAdapter = new DataRelationElementAdapter();

  @Override
  public void write(JsonWriter out, IDatasetElement value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }
    out.beginObject();
    out.name("name").value(value.getName());
    out.name("bufferNames");
    out.beginArray();
    for (String bufName : value.getBufferNames()) {
      out.value(bufName);
    }
    out.endArray();
    out.name("dataRelations");
    out.beginArray();
    for (IDataRelationElement rel : value.getDataRelations()) {
      relationAdapter.write(out, rel);
    }
    out.endArray();
    out.endObject();
  }

  @Override
  public IDatasetElement read(JsonReader in) throws IOException {
    throw new UnsupportedOperationException();
  }
}
