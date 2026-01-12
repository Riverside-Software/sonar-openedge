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

public interface IFunctionDocumentation extends IElementDocumentation {

  IParameterDocumentation[] getParameters();

  DataType getReturnType();

  default boolean hasParameters(String name) {
    for (var param : getParameters()) {
      if (param.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  default IParameterDocumentation getParameter(String param) {
    var tmp = param;
    for (var elem : getParameters()) {
      if (tmp.equalsIgnoreCase(elem.getName())) {
        return elem;
      }
    }
    return null;
  }

  default String getIDESignature() {
    return getIDESignature(false);
  }

  default String getIDESignature(boolean chronological) {
    StringBuilder retVal = new StringBuilder(getName());
    if (getParameters().length > 0)
      retVal.append('(');
    boolean first = true;
    for (IParameterDocumentation p : getParameters()) {
      retVal.append(p.isOptional() ? " [" : "");
      if (first) {
        first = false;
      } else {
        retVal.append(", ");
      }
      retVal.append(p.getDataType());
      retVal.append(" ");
      retVal.append(p.getName());
      retVal.append(p.isOptional() ? "]" : "");
    }
    if (getParameters().length > 0)
      retVal.append(')');
    return retVal.toString();
  }

}