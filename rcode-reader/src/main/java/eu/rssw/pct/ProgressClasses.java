/*
 * RCode library - OpenEdge plugin for SonarQube
 * Copyright (C) 2017 Riverside Software
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
package eu.rssw.pct;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

import eu.rssw.pct.elements.MethodElement;
import eu.rssw.pct.elements.MethodParameter;

public final class ProgressClasses {
  private static final IParameter[] EMPTY_PARAMETERS = new MethodParameter[] {};
  private static final String PROGRESS_LANG_OBJECT = "Progress.Lang.Object";

  private ProgressClasses() {
    // No-op
  }

  public static final Collection<TypeInfo> getProgressClasses() {
    Collection<TypeInfo> coll = new ArrayList<>();
    coll.add(getProgressLangObject());

    return coll;
  }

  private static final TypeInfo getProgressLangObject() {
    TypeInfo info = new TypeInfo();
    info.typeName = PROGRESS_LANG_OBJECT;
    info.getMethods().add(new MethodElement("Clone", EnumSet.of(AccessType.PUBLIC), 0, DataType.CLASS.getNum(),
        PROGRESS_LANG_OBJECT, 0, EMPTY_PARAMETERS));
    info.getMethods().add(
        new MethodElement("Equals", EnumSet.of(AccessType.PUBLIC), 0, DataType.LOGICAL.getNum(), "", 0,
            new MethodParameter[] {
                new MethodParameter(0, "otherObj", 2, MethodParameter.PARAMETER_INPUT, 0, DataType.CLASS.getNum(),
                    PROGRESS_LANG_OBJECT, 0)}));
    info.getMethods().add(new MethodElement("GetClass", EnumSet.of(AccessType.PUBLIC), 0, DataType.CLASS.getNum(),
        "Progress.Lang.Class", 0, EMPTY_PARAMETERS));
    info.getMethods().add(new MethodElement("ToString", EnumSet.of(AccessType.PUBLIC), 0, DataType.CHARACTER.getNum(),
        "", 0, EMPTY_PARAMETERS));

    return info;
  }
}
