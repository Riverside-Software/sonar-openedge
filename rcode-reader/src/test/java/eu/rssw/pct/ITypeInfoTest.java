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
package eu.rssw.pct;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

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
  private static final Function<String, ITypeInfo> TYPE_INFO_PROVIDER = name -> BuiltinClasses.getBuiltinClasses().stream() //
    .filter(it -> it.getTypeName().equals(name)) //
    .findFirst() //
    .orElse(null);

  @Test
  public void test01() {
    var info = TYPE_INFO_PROVIDER.apply("Progress.BPM.UserSession");
    assertNotNull(info);
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "GetProcessTemplateNames"));
    assertNull(info.getMethod(TYPE_INFO_PROVIDER, "GetProcessTemplateNames", DataType.INT64));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "GetDataSlotTemplates", DataType.CHARACTER));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "StartProcess", DataType.CHARACTER,
        new DataType("Progress.BPM.DataSlotTemplate")));
  }

  @Test
  public void test02() {
    var info = TYPE_INFO_PROVIDER.apply("Progress.Json.ObjectModel.JsonArray");
    assertNotNull(info);
    assertNull(info.getMethod(TYPE_INFO_PROVIDER, "Add"));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", DataType.CHARACTER));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", DataType.LONGCHAR));
    assertNotNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", new DataType("Progress.Json.ObjectModel.JsonArray")));
    assertNull(info.getMethod(TYPE_INFO_PROVIDER, "Add", new DataType("Progress.Lang.Object")));

    var val1 = info.getMethod(TYPE_INFO_PROVIDER, "GetDatetime", DataType.INTEGER, DataType.INTEGER);
    assertNotNull(val1);
    assertEquals(val1.getO1().getTypeName(), "Progress.Json.ObjectModel.JsonArray");
    assertEquals(val1.getO2().getReturnType(), DataType.DATETIME);

    var methd01 = info.getMethod(TYPE_INFO_PROVIDER, "GetCharacter", DataType.INTEGER);
    assertNotNull(methd01);
    assertEquals(methd01.getO2().getExtent(), 0);

    var methd02 = info.getMethod(TYPE_INFO_PROVIDER, "GetCharacter", DataType.INTEGER, DataType.INTEGER);
    assertNotNull(methd02);
    assertEquals(methd02.getO2().getExtent(), -1);
  }

  @Test
  public void test03() {
    ITypeInfo info01 = TYPE_INFO_PROVIDER.apply("Progress.Lang.Object");
    ITypeInfo info02 = TYPE_INFO_PROVIDER.apply("Progress.Json.ObjectModel.JsonArray");
    assertNotNull(info01);
    assertNotNull(info02);
    assertNotNull(info01.getMethod(TYPE_INFO_PROVIDER, "ToString"));
    assertNotNull(info02.getMethod(TYPE_INFO_PROVIDER, "ToString"));
  }

  @Test
  public void test04() {
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

  @Test
  public void test05() {
    var info = TYPE_INFO_PROVIDER.apply("Progress.IO.FileInputStream");
    assertNotNull(info);

    var list = info.getAllProperties(TYPE_INFO_PROVIDER);
    assertEquals(list.size(), 4);
    var sub1 = list.stream().filter(
        it -> "Progress.IO.FileInputStream".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub1.size(), 1);
    assertEquals(sub1.get(0).getO2().getName(), "FileName");
    var sub2 = list.stream().filter(
        it -> "Progress.IO.InputStream".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub2.size(), 1);
    assertEquals(sub2.get(0).getO2().getName(), "Closed");
    var sub3 = list.stream().filter(it -> "Progress.Lang.Object".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub3.size(), 2);
    assertEquals(sub3.get(0).getO2().getName(), "Next-Sibling");
    assertEquals(sub3.get(1).getO2().getName(), "Prev-Sibling");

    var list2 = info.getAllMethods(TYPE_INFO_PROVIDER);
    var sub4 = list2.stream().filter(it -> "Progress.Lang.Object".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub4.size(), 4);
    var sub5 = list2.stream().filter(
        it -> "Progress.IO.FileInputStream".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub5.size(), 0);
    var sub6 = list2.stream().filter(
        it -> "Progress.IO.InputStream".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub6.size(), 7);

    var list3 = info.getAllConstructors(TYPE_INFO_PROVIDER);
    assertEquals(list3.size(), 1);
    var c1 = list3.get(0);
    assertEquals(c1.getO1().getTypeName(), "Progress.IO.FileInputStream");
    assertTrue(c1.getO2().isConstructor());
    assertEquals(c1.getO2().getParameters().length, 1);
  }

  @Test
  public void test06() {
    var info = TYPE_INFO_PROVIDER.apply("Progress.Lang.AppError");
    assertNotNull(info);

    var list = info.getAllProperties(TYPE_INFO_PROVIDER);
    assertEquals(list.size(), 6);
    var sub1 = list.stream().filter(
        it -> "Progress.Lang.AppError".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub1.size(), 1);
    assertEquals(sub1.get(0).getO2().getName(), "ReturnValue");
    var sub2 = list.stream().filter(
        it -> "Progress.Lang.ProError".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub2.size(), 3);
    assertEquals(sub2.get(0).getO2().getName(), "NumMessages");
    assertEquals(sub2.get(1).getO2().getName(), "CallStack");
    assertEquals(sub2.get(2).getO2().getName(), "Severity");
    var sub3 = list.stream().filter(it -> "Progress.Lang.Object".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub3.size(), 2);
    assertEquals(sub3.get(0).getO2().getName(), "Next-Sibling");
    assertEquals(sub3.get(1).getO2().getName(), "Prev-Sibling");
    var sub4 = list.stream().filter(
        it -> "Progress.Lang.Error".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub4.size(), 0);

    var list2 = info.getAllMethods(TYPE_INFO_PROVIDER);
    var sub5 = list2.stream().filter(it -> "Progress.Lang.Object".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub5.size(), 4);
    var sub6 = list2.stream().filter(
        it -> "Progress.Lang.AppError".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub6.size(), 2);
    assertEquals(sub6.get(0).getO2().getName(), "AddMessage");
    assertEquals(sub6.get(1).getO2().getName(), "RemoveMessage");
    var sub7 = list2.stream().filter(
        it -> "Progress.Lang.ProError".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub7.size(), 2);
    assertEquals(sub7.get(0).getO2().getName(), "GetMessage");
    assertEquals(sub7.get(1).getO2().getName(), "GetMessageNum");
    var sub8 = list2.stream().filter(
        it -> "Progress.Lang.Error".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub8.size(), 0);

    var list3 = info.getAllConstructors(TYPE_INFO_PROVIDER);
    assertEquals(list3.size(), 3);
    var c1 = list3.get(0);
    assertEquals(c1.getO1().getTypeName(), "Progress.Lang.AppError");
    assertTrue(c1.getO2().isConstructor());
    assertEquals(c1.getO2().getParameters().length, 0);
    var c2 = list3.get(1);
    assertEquals(c2.getO1().getTypeName(), "Progress.Lang.AppError");
    assertTrue(c2.getO2().isConstructor());
    assertEquals(c2.getO2().getParameters().length, 2);
    var c3 = list3.get(2);
    assertEquals(c3.getO1().getTypeName(), "Progress.Lang.AppError");
    assertTrue(c3.getO2().isConstructor());
    assertEquals(c3.getO2().getParameters().length, 1);

  }
}
