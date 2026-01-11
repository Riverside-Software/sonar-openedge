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

import com.google.gson.annotations.JsonAdapter;

@JsonAdapter(MethodElementAdapter.class)
public interface IMethodElement extends IAccessibleElement {
  DataType getReturnType();
  int getExtent();
  IParameter[] getParameters();

  boolean isProcedure();
  boolean isFunction();
  boolean isConstructor();
  boolean isDestructor();
  boolean isOverloaded();
  boolean isFinal();

  default String getSignatureWithoutModifiers() {
    var retVal = new StringBuilder(getName()).append('(');
    var first = true;
    for (var p : getParameters()) {
      if (first) {
        first = false;
      } else {
        retVal.append(',');
      }
      retVal.append(p.getSignature());
    }
    retVal.append(')');

    return retVal.toString();
  }

  default String getSignature() {
    var retVal = new StringBuilder(getSignatureWithoutModifiers());
    if (isAbstract())
      retVal.append('A');
    if (isStatic())
      retVal.append('S');
    if (isPublic())
      retVal.append('U');
    else if (isProtected())
      retVal.append('T');
    else if (isPackageProtected())
      retVal.append("PT");
    else if (isPrivate())
      retVal.append('V');
    else if (isPackagePrivate())
      retVal.append("PV");

    return retVal.toString();
  }

  default String getIDESignature() {
    return getIDESignature(false);
  }

  default String getIDESignature(boolean chronological) {
    StringBuilder retVal = new StringBuilder(getName()).append('(');
    boolean first = true;
    for (IParameter p : getParameters()) {
      if (first) {
        first = false;
      } else {
        retVal.append(", ");
      }
      retVal.append(p.getIDESignature(chronological));
    }
    retVal.append(')');
    return retVal.toString();
  }

  default String getIDEInsertElement(boolean upperCase) {
    StringBuilder retVal = new StringBuilder(getName()).append('(');
    int cnt = 1;
    for (IParameter p : getParameters()) {
      if (cnt > 1) {
        retVal.append(", ");
      }
      String mode = "";
      if (p.getMode() == ParameterMode.INPUT_OUTPUT)
        mode = "input-output ";
      else if ((p.getMode() == ParameterMode.OUTPUT) || (p.getMode() == ParameterMode.RETURN))
        mode = "output ";
      if (upperCase)
        mode = mode.toUpperCase();
      retVal.append(mode).append("${" + cnt++ + ":").append(p.getName()).append("}");
    }
    retVal.append(")$0");
    return retVal.toString();
  }

}
