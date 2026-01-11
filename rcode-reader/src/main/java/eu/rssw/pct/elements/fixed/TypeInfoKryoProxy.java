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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.gson.annotations.JsonAdapter;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.TypeInfoAdapter;

@JsonAdapter(TypeInfoAdapter.class)
public class TypeInfoKryoProxy extends TypeInfoProxy {
  private final Kryo kryo;
  private final Path path;

  public TypeInfoKryoProxy(String typeName, Path path, Kryo kryo) {
    super(typeName);
    this.path = path;
    this.kryo = kryo;
  }

  @Override
  synchronized void checkTypeInfo() {
    if (typeInfo != null)
      return;
    try {
      typeInfo = (ITypeInfo) kryo.readClassAndObject(new Input(Files.readAllBytes(path)));
    } catch (IOException caught) {
      typeInfo = new TypeInfo(typeName, false, false, BuiltinClasses.PLO_CLASSNAME, "");
    } catch (Throwable caught) {
      
      typeInfo = new TypeInfo(typeName, false, false, BuiltinClasses.PLO_CLASSNAME, "");
    }
  }

}
