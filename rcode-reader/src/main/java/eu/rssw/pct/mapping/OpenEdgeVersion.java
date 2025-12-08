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
package eu.rssw.pct.mapping;

import javax.annotation.Nonnull;

public enum OpenEdgeVersion {
  V117,
  V122,
  V128,
  V130;

  public String getSystemHandlesPath() {
    switch (this) {
      case V117:
        return "doc/11.7/syshdl.json";
      case V122:
        return "doc/12.2/syshdl.json";
      case V128:
        return "doc/12.8/syshdl.json";
      case V130:
        return "doc/13.0/syshdl.json";
    }
    return "";
  }

  public String getClassStructurePath() {
    switch (this) {
      case V117:
        return "doc/11.7/classes.json";
      case V122:
        return "doc/12.2/classes.json";
      case V128:
        return "doc/12.8/classes.json";
      case V130:
        return "doc/13.0/classes.json";
    }
    return "";
  }
  
  public String getClassDocumentationPath() {
    switch (this) {
      case V117:
        return "doc/11.7/classDoc.json";
      case V122:
        return "doc/12.2/classDoc.json";
      case V128:
        return "doc/12.8/classDoc.json";
      case V130:
        return "doc/13.0/classDoc.json";
    }
    return "";
  }
  
  public String getFunctionsDocumentationPath() {
    switch (this) {
      case V117:
        return "doc/11.7/functionsDoc.json";
      case V122:
        return "doc/12.2/functionsDoc.json";
      case V128:
        return "doc/12.8/functionsDoc.json";
      case V130:
        return "doc/13.0/functionsDoc.json";
    }
    return "";
  }

  @Nonnull
  public static OpenEdgeVersion getVersion(String version) {
    if ((version == null) || version.isBlank())
      return V128;
    if (version.startsWith("11."))
      return V117;
    if (version.startsWith("13."))
      return V130;
    var pos = version.indexOf('.');
    if ((pos == -1) || (pos == version.length() - 1))
      return V128;
    var minor = -1;
    try {
      minor = Integer.parseInt(version.substring(pos + 1));
    } catch (NumberFormatException uncaught) {
      // Uncaught
    }
    if (minor <= 2)
      return V122;
    else
      return V128;
  }
}