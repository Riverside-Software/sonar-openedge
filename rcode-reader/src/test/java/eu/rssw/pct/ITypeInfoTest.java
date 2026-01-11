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
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.TypeInfo;

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
      assertNotNull(info.getMethod(provider, "GetProcessTemplateNames", new DataType[] {}, new ParameterMode[] {}));
      assertNull(info.getMethod(provider, "GetProcessTemplateNames", new DataType[] {DataType.INT64},
          new ParameterMode[] {ParameterMode.INPUT}));
      assertNotNull(info.getMethod(provider, "GetDataSlotTemplates", new DataType[] {DataType.CHARACTER},
          new ParameterMode[] {ParameterMode.INPUT}));
      assertNotNull(info.getMethod(provider, "StartProcess",
          new DataType[] {DataType.CHARACTER, new DataType("Progress.BPM.DataSlotTemplate")},
          new ParameterMode[] {ParameterMode.INPUT, ParameterMode.INPUT}));
    }
  }

  @Test
  public void test02() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      var info = provider.apply("Progress.Json.ObjectModel.JsonArray");
      assertNotNull(info);
      assertNull(
          info.getMethod(provider, "Add", new DataType[] {}, new ParameterMode[] {}));
      assertNotNull(info.getMethod(provider, "Add",
          new DataType[] {DataType.CHARACTER}, new ParameterMode[] {ParameterMode.INPUT}));
      assertNotNull(info.getMethod(provider, "Add", new DataType[] {DataType.LONGCHAR},
          new ParameterMode[] {ParameterMode.INPUT}));
      assertNotNull(info.getMethod(provider, "Add",
          new DataType[] {new DataType("Progress.Json.ObjectModel.JsonArray")},
          new ParameterMode[] {ParameterMode.INPUT}));
      assertNull(info.getMethod(provider, "Add",
          new DataType[] {new DataType("Progress.Lang.Object")}, new ParameterMode[] {ParameterMode.INPUT}));

      var val1 = info.getMethod(provider, "GetDatetime",
          new DataType[] {DataType.INTEGER, DataType.INTEGER},
          new ParameterMode[] {ParameterMode.INPUT, ParameterMode.INPUT});
      assertNotNull(val1);
      assertEquals(val1.getO1().getTypeName(), "Progress.Json.ObjectModel.JsonArray");
      assertEquals(val1.getO2().getReturnType(), DataType.DATETIME);

      var methd01 = info.getMethod(provider, "GetCharacter",
          new DataType[] {DataType.INTEGER}, new ParameterMode[] {ParameterMode.INPUT});
      assertNotNull(methd01);
      assertEquals(methd01.getO2().getExtent(), 0);

      var methd02 = info.getMethod(provider, "GetCharacter",
          new DataType[] {DataType.INTEGER, DataType.INTEGER},
          new ParameterMode[] {ParameterMode.INPUT, ParameterMode.INPUT});
      assertNotNull(methd02);
      assertEquals(methd02.getO2().getExtent(), -1);

      var info2 = provider.apply("Progress.Json.ObjectModel.JsonObject");
      var methd03 = info2.getMethod(provider, "Write",
          new DataType[] {DataType.CHARACTER, DataType.LOGICAL},
          new ParameterMode[] {ParameterMode.INPUT, ParameterMode.INPUT});
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
      assertNotNull(info01.getMethod(provider, "ToString", new DataType[] {},
          new ParameterMode[] {}));
      assertNotNull(info02.getMethod(provider, "ToString", new DataType[] {},
          new ParameterMode[] {}));
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
          new DataType[] {DataType.INTEGER}, new ParameterMode[] {ParameterMode.INPUT});
      assertNotNull(val1);
      assertEquals(val1.getO1().getTypeName(), "rssw.ParentClass");
      assertEquals(val1.getO2().getReturnType(), DataType.VOID);
      // Expected is method from child class
      Pair<ITypeInfo, IMethodElement> val2 = typeInfo02.getMethod(map::get, "method1", new DataType[] {DataType.INT64},
          new ParameterMode[] {ParameterMode.INPUT});
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

      var val1 = typeInfo.getExactMatchMethod(map::get, "method01", new DataType[] {DataType.CHARACTER},
          new ParameterMode[] {ParameterMode.INPUT});
      assertNull(val1);

      var val2 = typeInfo.getExactMatchMethod(map::get, "method01", new DataType[] {DataType.CHARACTER},
          new ParameterMode[] {ParameterMode.INPUT_OUTPUT});
      assertNotNull(val2);
      assertEquals(val2.getO2().getParameters()[0].getMode(), ParameterMode.INPUT_OUTPUT);

      var val3 = typeInfo.getExactMatchMethod(map::get, "method01", new DataType[] {DataType.CHARACTER},
          new ParameterMode[] {ParameterMode.OUTPUT});
      assertNotNull(val3);
      assertEquals(val3.getO2().getParameters()[0].getMode(), ParameterMode.OUTPUT);

      assertNotEquals(val2.getO2(), val3.getO2());

      var val4 = typeInfo.getMethod(map::get, "method01", new DataType[] {DataType.CHARACTER},
          new ParameterMode[] {ParameterMode.INPUT});
      assertNotNull(val4);
      // CompatibleMatch will return first method added in ITypeInfo, regardless of ParameterMode
      // Not completely accurate, but good enough for now
      assertEquals(val4.getO2(), val2.getO2());

      var val5 = typeInfo.getMethod(map::get, "method01", new DataType[] {DataType.CHARACTER},
          new ParameterMode[] {ParameterMode.INPUT_OUTPUT});
      assertEquals(val5.getO2(), val2.getO2());
      assertEquals(val5.getO2().getParameters()[0].getMode(), ParameterMode.INPUT_OUTPUT);

      var val6 = typeInfo.getMethod(map::get, "method01", new DataType[] {DataType.CHARACTER},
          new ParameterMode[] {ParameterMode.OUTPUT});
      assertEquals(val6.getO2(), val3.getO2());
      assertEquals(val6.getO2().getParameters()[0].getMode(), ParameterMode.OUTPUT);
    }
  }

  @Test
  public void testUnknownDataType() {
    for (var version : OpenEdgeVersion.values()) {
      var provider = VERSION_TYPE_INFO_PROVIDER.apply(version);
      var info = provider.apply("Progress.Json.ObjectModel.JsonArray");
      assertNotNull(info);
      assertNull(info.getMethod(provider, "Add", new DataType[] {DataType.UNKNOWN},
          new ParameterMode[] {ParameterMode.INPUT}));
      assertNotNull(info.getMethod(provider, "Read", new DataType[] {DataType.UNKNOWN},
          new ParameterMode[] {ParameterMode.INPUT}));
    }
  }

  @Test
  public void testCompatibleMatch() {
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

      var val6 = typeInfo2.getMethod(map::get, "method01", new DataType[] {DataType.INTEGER},
          new ParameterMode[] {ParameterMode.INPUT});
      assertNotNull(val6);
    }
  }
}
