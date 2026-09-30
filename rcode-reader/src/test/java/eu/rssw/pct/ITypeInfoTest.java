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
package eu.rssw.pct;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.function.Function;

import org.prorefactor.core.Pair;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.ITypeInfo.ParameterDescriptor;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.TypeInfo;
import eu.rssw.pct.elements.fixed.VariableElement;

public class ITypeInfoTest {
  private static final Function<OpenEdgeVersion, Function<String, ITypeInfo>> VERSION_TYPE_INFO_PROVIDER = version -> {
    return name -> BuiltinClasses.getBuiltinClasses(version).stream() //
      .filter(it -> it.getTypeName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  public void test01() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      var info = provider.apply("Progress.BPM.UserSession");
      assertNotNull(info);
      assertNotNull(info.getMethod(provider, "GetProcessTemplateNames", new ParameterDescriptor[] {}));
      assertNull(info.getMethod(provider, "GetProcessTemplateNames",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.INT64, 0, ParameterMode.INPUT)}));
      assertNotNull(info.getMethod(provider, "GetDataSlotTemplates",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT)}));
      assertNotNull(info.getMethod(provider, "StartProcess",
          new ParameterDescriptor[] {
              new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT),
              new ParameterDescriptor(new DataType("Progress.BPM.DataSlotTemplate"), 2, ParameterMode.INPUT)}));
    }
  }

  @Test
  public void test02() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      var info = provider.apply("Progress.Json.ObjectModel.JsonArray");
      assertNotNull(info);
      assertNull(info.getMethod(provider, "Add", new ParameterDescriptor[] {}));
      assertNotNull(info.getMethod(provider, "Add",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT)}));
      assertNotNull(info.getMethod(provider, "Add",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.LONGCHAR, 0, ParameterMode.INPUT)}));
      assertNotNull(info.getMethod(provider, "Add", new ParameterDescriptor[] {
          new ParameterDescriptor(new DataType("Progress.Json.ObjectModel.JsonArray"), 0, ParameterMode.INPUT)}));
      assertNull(info.getMethod(provider, "Add", new ParameterDescriptor[] {
          new ParameterDescriptor(new DataType("Progress.Lang.Object"), 0, ParameterMode.INPUT)}));

      var val1 = info.getMethod(provider, "GetDatetime",
          new ParameterDescriptor[] {
              new ParameterDescriptor(DataType.INTEGER, 0, ParameterMode.INPUT),
              new ParameterDescriptor(DataType.INTEGER, 0, ParameterMode.INPUT)});
      assertNotNull(val1);
      assertEquals(val1.getO1().getTypeName(), "Progress.Json.ObjectModel.JsonArray");
      assertEquals(val1.getO2().getReturnType(), DataType.DATETIME);

      var methd01 = info.getMethod(provider, "GetCharacter",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.INTEGER, 0, ParameterMode.INPUT)});
      assertNotNull(methd01);
      assertEquals(methd01.getO2().getExtent(), 0);

      var methd02 = info.getMethod(provider, "GetCharacter",
          new ParameterDescriptor[] {
              new ParameterDescriptor(DataType.INTEGER, 0, ParameterMode.INPUT),
              new ParameterDescriptor(DataType.INTEGER, 0, ParameterMode.INPUT)});
      assertNotNull(methd02);
      assertEquals(methd02.getO2().getExtent(), -1);

      var info2 = provider.apply("Progress.Json.ObjectModel.JsonObject");
      var methd03 = info2.getMethod(provider, "Write",
          new ParameterDescriptor[] {
              new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT),
              new ParameterDescriptor(DataType.LOGICAL, 0, ParameterMode.INPUT)});
      assertNotNull(methd03);
      assertEquals(methd03.getO2().getParameters()[0].getMode(), ParameterMode.INPUT_OUTPUT);
      assertEquals(methd03.getO2().getParameters()[0].getDataType(), DataType.LONGCHAR);
    }
  }

  @Test
  public void test03() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      ITypeInfo info01 = provider.apply("Progress.Lang.Object");
      ITypeInfo info02 = provider.apply("Progress.Json.ObjectModel.JsonArray");
      assertNotNull(info01);
      assertNotNull(info02);
      assertNotNull(info01.getMethod(provider, "ToString", new ParameterDescriptor[] {}));
      assertNotNull(info02.getMethod(provider, "ToString", new ParameterDescriptor[] {}));
    }
  }

  @Test
  public void test04() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      TypeInfo typeInfo01 = new TypeInfo("rssw.ParentClass", false, false, "Progress.Lang.Object", "");
      typeInfo01.addMethod(new MethodElement("method1", false, DataType.VOID,
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
      TypeInfo typeInfo02 = new TypeInfo("rssw.ChildClass", false, false, "rssw.ParentClass", "");
      typeInfo02.addMethod(new MethodElement("method1", false, DataType.INTEGER,
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
      map.put(typeInfo01.getTypeName(), typeInfo01);
      map.put(typeInfo02.getTypeName(), typeInfo02);

      // Expected is method from parent class
      Pair<ITypeInfo, IMethodElement> val1 = typeInfo02.getMethod(map::get, "method1",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.INTEGER, 0, ParameterMode.INPUT)});
      assertNotNull(val1);
      assertEquals(val1.getO1().getTypeName(), "rssw.ParentClass");
      assertEquals(val1.getO2().getReturnType(), DataType.VOID);
      // Expected is method from child class
      Pair<ITypeInfo, IMethodElement> val2 = typeInfo02.getMethod(map::get, "method1",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.INT64, 0, ParameterMode.INPUT)});
      assertNotNull(val2);
      assertEquals(val2.getO1().getTypeName(), "rssw.ChildClass");
      assertEquals(val2.getO2().getReturnType(), DataType.INTEGER);
    }
  }

  @Test
  public void testStatics() {
    var typeInfo01 = new TypeInfo("rssw.ParentClass", false, false, "Progress.Lang.Object", "");
    typeInfo01.addMethod(new MethodElement("method1", false, DataType.VOID,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INTEGER)));
    assertFalse(typeInfo01.hasStatics());

    var typeInfo02 = new TypeInfo("rssw.ChildClass", false, false, "Progress.Lang.Object", "");
    typeInfo02.addMethod(new MethodElement("method1", true, DataType.INTEGER,
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
    assertTrue(typeInfo02.hasStatics());
  }

  @Test
  public void test05() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      var info = provider.apply("Progress.IO.FileInputStream");
      assertNotNull(info);

      var list = info.getAllProperties(provider);
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

      var list2 = info.getAllMethods(provider);
      var sub4 = list2.stream().filter(
          it -> "Progress.Lang.Object".equalsIgnoreCase(it.getO1().getTypeName())).toList();
      assertEquals(sub4.size(), 4);
      var sub5 = list2.stream().filter(
          it -> "Progress.IO.FileInputStream".equalsIgnoreCase(it.getO1().getTypeName())).toList();
      assertEquals(sub5.size(), 0);
      var sub6 = list2.stream().filter(
          it -> "Progress.IO.InputStream".equalsIgnoreCase(it.getO1().getTypeName())).toList();
      assertEquals(sub6.size(), 7);

      var list3 = info.getAllConstructors(provider);
      assertEquals(list3.size(), 1);
      var c1 = list3.get(0);
      assertEquals(c1.getO1().getTypeName(), "Progress.IO.FileInputStream");
      assertTrue(c1.getO2().isConstructor());
      assertEquals(c1.getO2().getParameters().length, 1);
    }
  }

  @Test
  public void test06() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      var info = provider.apply("Progress.Lang.AppError");
      assertNotNull(info);

      var list = info.getAllProperties(provider);
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
      var sub4 = list.stream().filter(it -> "Progress.Lang.Error".equalsIgnoreCase(it.getO1().getTypeName())).toList();
      assertEquals(sub4.size(), 0);

      var list2 = info.getAllMethods(provider);
      var sub5 = list2.stream().filter(
          it -> "Progress.Lang.Object".equalsIgnoreCase(it.getO1().getTypeName())).toList();
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
      var sub8 = list2.stream().filter(it -> "Progress.Lang.Error".equalsIgnoreCase(it.getO1().getTypeName())).toList();
      assertEquals(sub8.size(), 0);

      var list3 = info.getAllConstructors(provider);
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

  @Test
  public void test07() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      var typeInfo = new TypeInfo("TestClass", false, false, "Progress.Lang.Object", "");
      typeInfo.addMethod(new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT_OUTPUT, DataType.CHARACTER)));
      typeInfo.addMethod(new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.OUTPUT, DataType.CHARACTER)));
      map.put(typeInfo.getTypeName(), typeInfo);

      var val1 = typeInfo.getExactMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT)});
      assertNull(val1);

      var val2 = typeInfo.getExactMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT_OUTPUT)});
      assertNotNull(val2);
      assertEquals(val2.getO2().getParameters()[0].getMode(), ParameterMode.INPUT_OUTPUT);

      var val3 = typeInfo.getExactMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.OUTPUT)});
      assertNotNull(val3);
      assertEquals(val3.getO2().getParameters()[0].getMode(), ParameterMode.OUTPUT);

      assertNotEquals(val2.getO2(), val3.getO2());

      var val4 = typeInfo.getMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT)});
      assertNotNull(val4);
      // CompatibleMatch will return first method added in ITypeInfo, regardless of ParameterMode
      // Not completely accurate, but good enough for now
      assertEquals(val4.getO2(), val2.getO2());

      var val5 = typeInfo.getMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT_OUTPUT)});
      assertEquals(val5.getO2(), val2.getO2());
      assertEquals(val5.getO2().getParameters()[0].getMode(), ParameterMode.INPUT_OUTPUT);

      var val6 = typeInfo.getMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.OUTPUT)});
      assertEquals(val6.getO2(), val3.getO2());
      assertEquals(val6.getO2().getParameters()[0].getMode(), ParameterMode.OUTPUT);
    }
  }

  @Test
  public void test08() {
    HashMap<String, ITypeInfo> map = new HashMap<>();

    var typeInfo = new TypeInfo("pkg.ParentTestClass", false, false, "Progress.Lang.Object", "");
    typeInfo.addVariable(new VariableElement("var1", DataType.INT64));
    var typeInfo2 = new TypeInfo("pkg.ChildTestClass", false, false, "pkg.ParentTestClass", "");
    typeInfo2.addVariable(new VariableElement("var2", DataType.CHARACTER));
    map.put(typeInfo.getTypeName(), typeInfo);
    map.put(typeInfo2.getTypeName(), typeInfo2);

    assertEquals(typeInfo.getAllVariables(map::get).size(), 1);
    var var0 = typeInfo.getAllVariables(map::get).get(0);
    assertEquals(var0.getO1(), typeInfo);

    assertEquals(typeInfo2.getAllVariables(map::get).size(), 2);
    var var1 = typeInfo2.getAllVariables(map::get).get(0);
    assertEquals(var1.getO1(), typeInfo);
    var var2 = typeInfo2.getAllVariables(map::get).get(1);
    assertEquals(var2.getO1(), typeInfo2);
  }

  @Test
  public void testUnknownDataType() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      var info = provider.apply("Progress.Json.ObjectModel.JsonArray");
      assertNotNull(info);
      assertNull(info.getMethod(provider, "Add",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.UNKNOWN, 0, ParameterMode.INPUT)}));
      assertNotNull(info.getMethod(provider, "Read",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.UNKNOWN, 0, ParameterMode.INPUT)}));
    }
  }

  @Test
  public void testCompatibleMatch01() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      var typeInfo2 = new TypeInfo("TestClass2", false, false, "Progress.Lang.Object", "");
      typeInfo2.addMethod(new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.DECIMAL)));
      typeInfo2.addMethod(new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.INT64)));
      map.put(typeInfo2.getTypeName(), typeInfo2);

      var val6 = typeInfo2.getMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.INTEGER, 0, ParameterMode.INPUT)});
      assertNotNull(val6);
    }
  }

  @Test
  public void testCompatibleMatch02() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      var typeInfo2 = new TypeInfo("TestClass2", false, false, "Progress.Lang.Object", "");
      // void method01(prm1: DSET_HDL)
      typeInfo2.addMethod(new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.DATASET_HANDLE)));
      // void method02(prm1: HDL)
      typeInfo2.addMethod(new MethodElement("method02", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.HANDLE)));
      // void method03(prm1: TBL_HDL)
      typeInfo2.addMethod(new MethodElement("method03", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.TABLE_HANDLE)));
      // void method04(prm1: WIDG_HDL)
      typeInfo2.addMethod(new MethodElement("method04", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.COMPONENT_HANDLE)));
      map.put(typeInfo2.getTypeName(), typeInfo2);

      var val1 = typeInfo2.getMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.DATASET_HANDLE, 0, ParameterMode.INPUT)});
      assertNotNull(val1);
      var val2 = typeInfo2.getMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.HANDLE, 0, ParameterMode.INPUT)});
      assertNull(val2);

      var val3 = typeInfo2.getMethod(map::get, "method02",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.DATASET_HANDLE, 0, ParameterMode.INPUT)});
      assertNotNull(val3);
      var val4 = typeInfo2.getMethod(map::get, "method02",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.HANDLE, 0, ParameterMode.INPUT)});
      assertNotNull(val4);
      var val5 = typeInfo2.getMethod(map::get, "method02",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.TABLE_HANDLE, 0, ParameterMode.INPUT)});
      assertNotNull(val5);
      var val6 = typeInfo2.getMethod(map::get, "method02",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.COMPONENT_HANDLE, 0, ParameterMode.INPUT)});
      assertNotNull(val6);

      var val7 = typeInfo2.getMethod(map::get, "method03",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.HANDLE, 0, ParameterMode.INPUT)});
      assertNull(val7);
      var val8 = typeInfo2.getMethod(map::get, "method03",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.TABLE_HANDLE, 0, ParameterMode.INPUT)});
      assertNotNull(val8);

      var val9 = typeInfo2.getMethod(map::get, "method04",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.HANDLE, 0, ParameterMode.INPUT)});
      assertNull(val9);
      var val10 = typeInfo2.getMethod(map::get, "method04",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.COMPONENT_HANDLE, 0, ParameterMode.INPUT)});
      assertNotNull(val10);
    }
  }

  @Test
  public void testCompatibleMatch03() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      var typeInfo = new TypeInfo("TestClass", false, false, "Progress.Lang.Object", "");
      var m1 = new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER));
      var m2 = new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.CHARACTER));
      typeInfo.addMethod(m1);
      typeInfo.addMethod(m2);
      map.put(typeInfo.getTypeName(), typeInfo);

      // No array
      var val1 = typeInfo.getExactMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT)});
      assertThat(val1).isNotNull() //
        .returns(typeInfo, Pair::getO1) //
        .returns(m1, Pair::getO2);
      var val2 = typeInfo.getCompatibleMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT)});
      assertThat(val2).isNotNull() //
        .returns(typeInfo, Pair::getO1) //
        .returns(m1, Pair::getO2);

      // Array
      var val3 = typeInfo.getExactMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 10, ParameterMode.INPUT)});
      assertThat(val3).isNotNull() //
        .returns(typeInfo, Pair::getO1) //
        .returns(m2, Pair::getO2);
      var val4 = typeInfo.getCompatibleMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 10, ParameterMode.INPUT)});
      assertThat(val4).isNotNull() //
        .returns(typeInfo, Pair::getO1) //
        .returns(m2, Pair::getO2);
    }
  }

  @Test
  public void testCompatibleMatch04() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      var typeInfo = new TypeInfo("TestClass", false, false, "Progress.Lang.Object", "");
      var m1 = new MethodElement("method01", false, DataType.VOID, //
          new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER));
      var m2 = new MethodElement("method02", false, DataType.VOID, //
          new Parameter(1, "prm1", -1, ParameterMode.INPUT, DataType.CHARACTER));
      typeInfo.addMethod(m1);
      typeInfo.addMethod(m2);
      map.put(typeInfo.getTypeName(), typeInfo);

      var val1 = typeInfo.getCompatibleMatchMethod(map::get, "method01",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 1, ParameterMode.INPUT)});
      assertThat(val1).isNull();

      var val2 = typeInfo.getCompatibleMatchMethod(map::get, "method02",
          new ParameterDescriptor[] {new ParameterDescriptor(DataType.CHARACTER, 0, ParameterMode.INPUT)});
      assertThat(val2).isNull();
    }
  }

  @Test
  public void testCamelCase() {
    assertEquals(new TypeInfo("com.progress.HelloWorld", false, false, "", "").toUpperCaseAcronym(), "HW");
    assertEquals(new TypeInfo("com.progress.helloworld", false, false, "", "").toUpperCaseAcronym(), "");
    assertEquals(new TypeInfo("com.progress.helloWorld", false, false, "", "").toUpperCaseAcronym(), "W");
    assertEquals(new TypeInfo("HelloWorld", false, false, "", "").toUpperCaseAcronym(), "HW");
    assertEquals(new TypeInfo("com.progress.Hello-World_%IDislikeSymbols", false, false, "", "").toUpperCaseAcronym(),
        "HWIDS");
  }

  /**
   * Test most specific match with class hierarchy.
   * When multiple methods accept compatible class types, the most specific one should be selected.
   */
  @Test
  public void testMostSpecificClassMatch() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      // Create a class hierarchy: GrandChild -> Child -> Parent -> Progress.Lang.Object
      var parentClass = new TypeInfo("rssw.Parent", false, false, "Progress.Lang.Object", "");
      var childClass = new TypeInfo("rssw.Child", false, false, "rssw.Parent", "");
      var grandChildClass = new TypeInfo("rssw.GrandChild", false, false, "rssw.Child", "");
      map.put(parentClass.getTypeName(), parentClass);
      map.put(childClass.getTypeName(), childClass);
      map.put(grandChildClass.getTypeName(), grandChildClass);

      // Create a class with overloaded methods accepting different levels of the hierarchy
      var typeInfo = new TypeInfo("rssw.TestClass", false, false, "Progress.Lang.Object", "");
      var methodParent = new MethodElement("process", false, DataType.INTEGER,
          new Parameter(1, "obj", 0, ParameterMode.INPUT, new DataType("rssw.Parent")));
      var methodChild = new MethodElement("process", false, DataType.CHARACTER,
          new Parameter(1, "obj", 0, ParameterMode.INPUT, new DataType("rssw.Child")));
      var methodGrandChild = new MethodElement("process", false, DataType.LOGICAL,
          new Parameter(1, "obj", 0, ParameterMode.INPUT, new DataType("rssw.GrandChild")));
      typeInfo.addMethod(methodParent);
      typeInfo.addMethod(methodChild);
      typeInfo.addMethod(methodGrandChild);
      map.put(typeInfo.getTypeName(), typeInfo);

      // When passing GrandChild, should find GrandChild method (exact match)
      var result1 = typeInfo.getMethod(map::get, "process",
          new ParameterDescriptor[] {new ParameterDescriptor(new DataType("rssw.GrandChild"), 0, ParameterMode.INPUT)});
      assertNotNull(result1);
      assertEquals(result1.getO2().getReturnType(), DataType.LOGICAL);

      // When passing Child, should find Child method (exact match)
      var result2 = typeInfo.getMethod(map::get, "process",
          new ParameterDescriptor[] {new ParameterDescriptor(new DataType("rssw.Child"), 0, ParameterMode.INPUT)});
      assertNotNull(result2);
      assertEquals(result2.getO2().getReturnType(), DataType.CHARACTER);

      // When passing Parent, should find Parent method (exact match)
      var result3 = typeInfo.getMethod(map::get, "process",
          new ParameterDescriptor[] {new ParameterDescriptor(new DataType("rssw.Parent"), 0, ParameterMode.INPUT)});
      assertNotNull(result3);
      assertEquals(result3.getO2().getReturnType(), DataType.INTEGER);
    }
  }

  /**
   * Test that when passing a subclass, the most specific compatible method is selected.
   */
  @Test
  public void testMostSpecificCompatibleClassMatch() {
    HashMap<String, ITypeInfo> map = new HashMap<>();
    for (var version : OpenEdgeVersion.values()) {
      map.clear();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      // Create class hierarchy
      var parentClass = new TypeInfo("rssw.Parent2", false, false, "Progress.Lang.Object", "");
      var childClass = new TypeInfo("rssw.Child2", false, false, "rssw.Parent2", "");
      var grandChildClass = new TypeInfo("rssw.GrandChild2", false, false, "rssw.Child2", "");
      map.put(parentClass.getTypeName(), parentClass);
      map.put(childClass.getTypeName(), childClass);
      map.put(grandChildClass.getTypeName(), grandChildClass);

      // Create class with methods for Parent and Child only (not GrandChild)
      var typeInfo = new TypeInfo("rssw.TestClass2", false, false, "Progress.Lang.Object", "");
      var methodParent = new MethodElement("process", false, DataType.INTEGER,
          new Parameter(1, "obj", 0, ParameterMode.INPUT, new DataType("rssw.Parent2")));
      var methodChild = new MethodElement("process", false, DataType.CHARACTER,
          new Parameter(1, "obj", 0, ParameterMode.INPUT, new DataType("rssw.Child2")));
      typeInfo.addMethod(methodParent);
      typeInfo.addMethod(methodChild);
      map.put(typeInfo.getTypeName(), typeInfo);

      // When passing GrandChild, should find Child method (most specific compatible)
      var result = typeInfo.getMethod(map::get, "process",
          new ParameterDescriptor[] {new ParameterDescriptor(new DataType("rssw.GrandChild2"), 0, ParameterMode.INPUT)});
      assertNotNull(result);
      assertEquals(result.getO2().getReturnType(), DataType.CHARACTER);
    }
  }

  /**
   * Test most specific match with Progress.Lang.Object hierarchy.
   */
  @Test
  public void testMostSpecificJsonHierarchy() {
    for (var version : OpenEdgeVersion.values()) {
      HashMap<String, ITypeInfo> map = new HashMap<>();
      BuiltinClasses.getBuiltinClasses(version).forEach(it -> map.put(it.getTypeName(), it));

      // Create class with methods accepting PLO and JsonObject
      var typeInfo = new TypeInfo("rssw.JsonProcessor", false, false, "Progress.Lang.Object", "");
      var methodPLO = new MethodElement("handle", false, DataType.INTEGER,
          new Parameter(1, "obj", 0, ParameterMode.INPUT, new DataType("Progress.Lang.Object")));
      var methodJson = new MethodElement("handle", false, DataType.CHARACTER,
          new Parameter(1, "obj", 0, ParameterMode.INPUT, new DataType("Progress.Json.ObjectModel.JsonObject")));
      typeInfo.addMethod(methodPLO);
      typeInfo.addMethod(methodJson);
      map.put(typeInfo.getTypeName(), typeInfo);

      // When passing JsonObject, should find JsonObject method (most specific)
      var result1 = typeInfo.getMethod(map::get, "handle",
          new ParameterDescriptor[] {new ParameterDescriptor(new DataType("Progress.Json.ObjectModel.JsonObject"), 0, ParameterMode.INPUT)});
      assertNotNull(result1);
      assertEquals(result1.getO2().getReturnType(), DataType.CHARACTER);

      // When passing PLO, should find PLO method (exact match)
      var result2 = typeInfo.getMethod(map::get, "handle",
          new ParameterDescriptor[] {new ParameterDescriptor(new DataType("Progress.Lang.Object"), 0, ParameterMode.INPUT)});
      assertNotNull(result2);
      assertEquals(result2.getO2().getReturnType(), DataType.INTEGER);

      // When passing JsonArray (sibling of JsonObject, both extend JsonConstruct), should find PLO method
      var result3 = typeInfo.getMethod(map::get, "handle",
          new ParameterDescriptor[] {new ParameterDescriptor(new DataType("Progress.Json.ObjectModel.JsonArray"), 0, ParameterMode.INPUT)});
      assertNotNull(result3);
      assertEquals(result3.getO2().getReturnType(), DataType.INTEGER);
    }
  }

}
