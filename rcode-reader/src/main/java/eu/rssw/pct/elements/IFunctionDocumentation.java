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

import java.util.ArrayList;
import java.util.Collection;

public interface IFunctionDocumentation extends IElementDocumentation {

  IFunctionParameterList[] getVariants();

  DataType getReturnType();

  default boolean hasParameters(String name) {
    for (var variant : getVariants()) {
      for (var param : variant.getParameters()) {
        if (param.getName().equalsIgnoreCase(name))
          return true;
      }
    }
    return false;
  }

  default IFunctionParameter getParameter(String param) {
    var tmp = param;
    for (var variant : getVariants()) {
      for (var elem : variant.getParameters()) {
        if (tmp.equalsIgnoreCase(elem.getName())) {
          return elem;
        }
      }
    }
    return null;
  }

  default String getIDESignature() {
    return getIDESignatures(null)[0];
  }

  default String[] getIDESignatures(DataType[] datatypes) {
    var variants = getVariants(datatypes);

    var retVal = new String[variants.length];
    var offset = 0;
    for (var variant : variants) {
      retVal[offset++] = getSignature(variant);
    }

    return retVal;
  }

  private String getSignature(IFunctionParameterList variant) {
    StringBuilder sigtmp = new StringBuilder(getName());

    if (variant.getParameters().length > 0)
      sigtmp.append('(');
    boolean first = true;
    int openBrackets = 0;
    for (var p : variant.getParameters()) {
      if (p.isOptional()) {
        sigtmp.append(first ? "[" : " [");
        openBrackets++;
      }
      if (first) {
        first = false;
      } else {
        sigtmp.append(", ");
      }
      sigtmp.append(p.getDataType());
      sigtmp.append(" ");
      sigtmp.append(p.getName());
    }
    // Close all opened brackets
    while (openBrackets-- > 0) {
      sigtmp.append("]");
    }
    if (variant.getParameters().length > 0)
      sigtmp.append(')');

    return sigtmp.toString();
  }

  private IFunctionParameterList[] getVariants(DataType[] datatypes) {
    var variants = getVariants();
    if (variants.length == 0)
      return variants;
    if (datatypes == null)
      return variants;

    Collection<IFunctionParameterList> coll = new ArrayList<>();
    for (var variant : variants) {
      var xx = 0;
      var addVariant = true;
      var prm = variant.getParameters();
      for (var datatype : datatypes) {
        if (prm.length <= xx || xx >= datatypes.length || !prm[xx].getDataType().equals(datatype)) {
          addVariant = false;
        }
        xx++;
      }
      if (addVariant)
        coll.add(variant);
    }
    if (coll.isEmpty())
      coll.add(variants[0]);

    return coll.toArray(new IFunctionParameterList[0]);
  }

}