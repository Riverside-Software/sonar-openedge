/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
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
import java.nio.file.Path;

import eu.rssw.pct.FileEntry;
import eu.rssw.pct.PLReader;
import eu.rssw.pct.PLReader.InvalidLibraryException;
import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.BuiltinClasses;

public class TypeInfoPLProxy extends TypeInfoProxy {

  private final Path plPath;
  private final String entryName;

  public TypeInfoPLProxy(String typeName, Path plPath, String entryName) {
    super(typeName);
    this.entryName = entryName;
    this.plPath = plPath;
  }

  @Override
  synchronized void checkTypeInfo() {
    if (typeInfo != null)
      return;
    try {
      PLReader reader = new PLReader(plPath);
      FileEntry entry = reader.getEntry(entryName);
      if (entry == null)
        typeInfo = new TypeInfo(typeName, false, false, BuiltinClasses.PLO_CLASSNAME, "");
      else {
        RCodeInfo info = new RCodeInfo(reader.getInputStream(entry));
        if (info.isClass())
          typeInfo = info.getTypeInfo();
        else
          typeInfo = new TypeInfo(typeName, false, false, BuiltinClasses.PLO_CLASSNAME, "");
      }
    } catch (InvalidLibraryException | InvalidRCodeException | IOException caught) {
      typeInfo = new TypeInfo(typeName, false, false, BuiltinClasses.PLO_CLASSNAME, "");
    }
  }

}
