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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import com.google.gson.annotations.JsonAdapter;

import eu.rssw.pct.PLReader.InvalidLibraryException;
import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.TypeInfoAdapter;

@JsonAdapter(TypeInfoAdapter.class)
public class TypeInfoRCodeProxy extends TypeInfoProxy {
  private final Path rcode;

  public TypeInfoRCodeProxy(String typeName, Path rcode) {
    super(typeName);
    this.rcode = rcode;
  }

  @Override
  synchronized void checkTypeInfo() {
    if (typeInfo != null)
      return;
    try (var input = new FileInputStream(rcode.toFile())) {
      RCodeInfo info = new RCodeInfo(input);
      if (info.isClass())
        typeInfo = info.getTypeInfo();
      else
        typeInfo = new TypeInfo(typeName, false, false, BuiltinClasses.PLO_CLASSNAME, "");
    } catch (InvalidLibraryException | InvalidRCodeException | IOException caught) {
      typeInfo = new TypeInfo(typeName, false, false, BuiltinClasses.PLO_CLASSNAME, "");
    }
  }

}
