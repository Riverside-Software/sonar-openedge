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
package eu.rssw.pct;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.HashMap;
import java.util.function.Function;

import org.prorefactor.core.Pair;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.TypeInfo;

public class ITypeInfoTest {
  private static final Function<String, ITypeInfo> TYPE_INFO_PROVIDER = name -> //
  BuiltinClasses.getBuiltinClasses().stream() //
    .filter(it -> it.getTypeName().equals(name)) //
    .findFirst() //
    .orElse(null);

  @Test
  public void test1() {
    ITypeInfo info = TYPE_INFO_PROVIDER.apply("Progress.BPM.UserSession");
    assertNotNull(info);
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "GetProcessTemplateNames"));
    assertNull(info.getMethod(TYPE_INFO_PROVIDER, "GetProcessTemplateNames", DataType.INT64));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "GetDataSlotTemplates", DataType.CHARACTER));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "StartProcess", DataType.CHARACTER,
        new DataType("Progress.BPM.DataSlotTemplate")));
  }

  @Test
  public void test2() {
    ITypeInfo info = TYPE_INFO_PROVIDER.apply("Progress.Json.ObjectModel.JsonArray");
    assertNotNull(info);
    assertNull(info.getMethod(TYPE_INFO_PROVIDER, "Add"));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", DataType.CHARACTER));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", DataType.LONGCHAR));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", new DataType("Progress.Json.ObjectModel.JsonArray")));
    assertNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", new DataType("Progress.Lang.Object")));

    Pair<ITypeInfo, IMethodElement> val1 = info.getMethod(TYPE_INFO_PROVIDER, "GetDatetime", DataType.INTEGER, DataType.INTEGER);
    assertNotNull(val1);
    assertEquals(val1.getO1().getTypeName(), "Progress.Json.ObjectModel.JsonArray");
    assertEquals(val1.getO2().getReturnType(), DataType.DATETIME);
  }

  @Test
  public void test3() {
    ITypeInfo info01 = TYPE_INFO_PROVIDER.apply("Progress.Lang.Object");
    ITypeInfo info02 = TYPE_INFO_PROVIDER.apply("Progress.Json.ObjectModel.JsonArray");
    assertNotNull(info01);
    assertNotNull(info02);
    assertNotNull(info01.getMethod(TYPE_INFO_PROVIDER, "ToString"));
    assertNotNull(info02.getMethod(TYPE_INFO_PROVIDER, "ToString"));
  }

  @Test
  public void test4() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    BuiltinClasses.getBuiltinClasses().forEach(it -> map.put(it.getTypeName(), it));

    TypeInfo typeInfo01 = new TypeInfo("rssw.ParentClass", false, false, "Progress.Lang.Object", "");
    typeInfo01.addMethod(new MethodElement("method1", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    TypeInfo typeInfo02 = new TypeInfo("rssw.ChildClass", false, false, "rssw.ParentClass", "");
    typeInfo02.addMethod(new MethodElement("method1", false, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
    map.put(typeInfo01.getTypeName(), typeInfo01);
    map.put(typeInfo02.getTypeName(), typeInfo02);

    // Expected is method from parent class
    Pair<ITypeInfo, IMethodElement> val1 = typeInfo02.getMethod(map::get, "method1", DataType.INTEGER);
    assertNotNull(val1);
    assertEquals(val1.getO1().getTypeName(), "rssw.ParentClass");
    assertEquals(val1.getO2().getReturnType(), DataType.VOID);
    // Expected is method from child class
    Pair<ITypeInfo, IMethodElement> val2 = typeInfo02.getMethod(map::get, "method1", DataType.INT64);
    assertNotNull(val2);
    assertEquals(val2.getO1().getTypeName(), "rssw.ChildClass");
    assertEquals(val2.getO2().getReturnType(), DataType.INTEGER);
  }
}
