/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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

import eu.rssw.pct.elements.AccessType;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.v11.MethodElementV11;
import eu.rssw.pct.elements.v11.MethodParameterV11;
import eu.rssw.pct.elements.v11.TypeInfoV11;

public final class ProgressClasses {
  private static final IParameter[] EMPTY_PARAMETERS = new IParameter[] {};
  private static final String PROGRESS_LANG_OBJECT = "Progress.Lang.Object";

  private ProgressClasses() {
    // No-op
  }

  public static final Collection<ITypeInfo> getProgressClasses() {
    Collection<ITypeInfo> coll = new ArrayList<>();
    coll.add(getProgressLangObject());

    return coll;
  }

  private static final ITypeInfo getProgressLangObject() {
    ITypeInfo info = new TypeInfoV11(PROGRESS_LANG_OBJECT, "", "", 0);
    info.getMethods().add(new MethodElementV11("Clone", EnumSet.of(AccessType.PUBLIC), 0, DataType.CLASS.getNum(),
        PROGRESS_LANG_OBJECT, 0, EMPTY_PARAMETERS));
    info.getMethods().add(
        new MethodElementV11("Equals", EnumSet.of(AccessType.PUBLIC), 0, DataType.LOGICAL.getNum(), "", 0,
            new IParameter[] {
                new MethodParameterV11(0, "otherObj", 2, MethodParameterV11.PARAMETER_INPUT, 0, DataType.CLASS.getNum(),
                    PROGRESS_LANG_OBJECT, 0)}));
    info.getMethods().add(new MethodElementV11("GetClass", EnumSet.of(AccessType.PUBLIC), 0, DataType.CLASS.getNum(),
        "Progress.Lang.Class", 0, EMPTY_PARAMETERS));
    info.getMethods().add(new MethodElementV11("ToString", EnumSet.of(AccessType.PUBLIC), 0,
        DataType.CHARACTER.getNum(), "", 0, EMPTY_PARAMETERS));

    return info;
  }
}
