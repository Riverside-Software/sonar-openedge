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

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.SystemHandles;
import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ISystemHandle;
import eu.rssw.pct.elements.ITypeInfo;

public class SystemHandleTest {
  private static final Function<OpenEdgeVersion, Function<String, ISystemHandle>> VERSION_SYS_HANDLE_PROVIDER = version -> {
    return name -> SystemHandles.getSystemHandles(version).stream() //
      .filter(it -> it.getName().equalsIgnoreCase(name)) //
      .findFirst() //
      .orElse(null);
  };
  private static final Function<OpenEdgeVersion, Function<String, ITypeInfo>> VERSION_TYPE_INFO_PROVIDER = version -> {
    return name -> BuiltinClasses.getBuiltinClasses(version).stream() //
      .filter(it -> it.getTypeName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  public void testSignatures() {
    // Assert all signatures can be fetched, no unknown datatypes, documentation is available
    for (var version : OpenEdgeVersion.values()) {
      for (var systemHandle : SystemHandles.getSystemHandles(version)) {
        assertNotNull(systemHandle.getDescription());
        if (!"com-self".equalsIgnoreCase(systemHandle.getName()) && !"focus".equalsIgnoreCase(systemHandle.getName())
            && !"default-window".equalsIgnoreCase(systemHandle.getName())
            && !"current-window".equalsIgnoreCase(systemHandle.getName())
            && !"active-window".equalsIgnoreCase(systemHandle.getName())
            && !"shadow-window".equalsIgnoreCase(systemHandle.getName())) {
          assertTrue(systemHandle.hasAttribute("type"), systemHandle.toString());
          assertFalse(systemHandle.hasAttribute("unknown"));
        }
        for (var method : systemHandle.getMethods()) {
          assertNotNull(method.getVariants());
          assertNotNull(method.getIDESignature(VERSION_TYPE_INFO_PROVIDER.apply(version), true));
          assertNotNull(method.getVariants()[0].getParameters());
          assertTrue(method.getVariants().length > 0);
          for (var variant : method.getVariants()) {
            for (var prm : variant.getParameters()) {
              assertFalse(prm.getName().isBlank());
              assertNotEquals(prm.getDataType(), DataType.UNKNOWN,
                  version + " " + systemHandle.getName() + " -- " + method.getName() + " -- " + prm.getName());
            }
          }

          assertNotEquals(method.getReturnType(), DataType.UNKNOWN,
              version + " " + systemHandle.getName() + " -- " + method.getName() + " return datatype");
          assertNotNull(systemHandle.getMethodDocumentation(method.getName()));

          assertTrue(systemHandle.hasMethod(method.getName()));
        }
        assertFalse(systemHandle.hasMethod("unknown"));
        assertNull(systemHandle.getMethod("unknown"));
        for (var attr : systemHandle.getAttributes()) {
          assertNotNull(attr.getName());
          assertNotNull(attr.getDescription());
          assertNotEquals(attr.getDataType(), DataType.UNKNOWN,
              version + " " + systemHandle.getName() + " -- " + attr.getName());
          assertFalse(attr.isReadOnly() && attr.isWriteOnly(),
              version + " " + systemHandle.getName() + " -- " + attr.getName());
        }
      }
    }
  }

  @Test
  public void test01() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("COMPILER");
      assertNotNull(syshdl);
      var val1 = syshdl.getMethod("GET-COLUMN");
      assertNotNull(val1);
      assertEquals(val1.getReturnType(), DataType.INTEGER);

    }
  }

  @Test
  public void test02() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("TARGET-PROCEDURE");
      assertNotNull(syshdl);
      var list = syshdl.getAttributes();
      assertEquals(list.size(), 23);
      var list2 = syshdl.getMethods();
      assertEquals(list2.size(), version == OpenEdgeVersion.V117 ? 4 : 5);
    }
  }

  @Test
  public void test03() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("FONT-TABLE");
      assertNotNull(syshdl);
      var val1 = syshdl.getMethod("get-text-height-chars");
      assertNotNull(val1);
      assertNotNull(val1.getVariants());
      assertEquals(val1.getVariants().length, 1);
      for (var variant : val1.getVariants()) {
        assertEquals(variant.getParameters().length, 1);
        for (var prm : variant.getParameters()) {
          assertFalse(prm.getName().isBlank());
          assertNotEquals(prm.getDataType(), DataType.UNKNOWN,
              version + " " + syshdl.getName() + " -- " + val1.getName() + " -- " + prm.getName());
        }
      }

      var list2 = syshdl.getAttributes();
      assertEquals(list2.size(), 4);
      var opt1 = list2.stream().findFirst();
      assertTrue(opt1.isPresent());
      assertEquals(opt1.get().getName(), "HANDLE");
      assertTrue(opt1.get().isReadOnly());
      assertFalse(opt1.get().isWriteOnly());

      var attr = syshdl.getAttribute("num-entries");
      assertNotNull(attr);
      assertFalse(attr.isReadOnly());
      assertFalse(attr.isWriteOnly());
    }
  }

  @Test
  public void test04() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("RCODE-INFO");
      assertNotNull(syshdl);
      var list = syshdl.getAttributes();
      assertEquals(list.size(), version == OpenEdgeVersion.V122 ? 16 : 15);
      assertEquals(syshdl.getMethods().size(), 0);
    }
  }

  @Test
  public void test05() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("TEMP-TABLE");
      assertNotNull(syshdl);
      var list = syshdl.getAttributes();
      assertEquals(list.size(), 29);
      assertEquals(syshdl.getMethods().size(), 18);
      var val1 = syshdl.getMethod("READ-JSON");
      assertNotNull(val1);
      assertNotNull(val1.getVariants());
      assertEquals(val1.getVariants().length, 6);
      for (var variant : val1.getVariants()) {
        assertEquals(variant.getParameters().length, 3);
      }
    }
  }

  @Test
  public void test06() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("SESSION");
      assertNotNull(syshdl);
      assertEquals(syshdl.getMethods().size(), 7);
      var val1 = syshdl.getMethod("ADD-SUPER-PROCEDURE");
      assertNotNull(val1);
      assertNotNull(val1.getVariants());
      assertEquals(val1.getVariants().length, 1);
      for (var variant : val1.getVariants()) {
        assertEquals(variant.getParameters().length, 2);
      }
    }
  }

  @Test
  public void testNotFound() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("SELF");
      assertNull(syshdl);
    }
  }

  @Test
  private void testSysHdlPerVersion() {
    var syshdl1 = VERSION_SYS_HANDLE_PROVIDER.apply(OpenEdgeVersion.V117).apply("PROFILER");
    assertNull(syshdl1);
    var syshdl12 = VERSION_SYS_HANDLE_PROVIDER.apply(OpenEdgeVersion.V122).apply("PROFILER");
    assertNotNull(syshdl12);
    var syshdl13 = VERSION_SYS_HANDLE_PROVIDER.apply(OpenEdgeVersion.V128).apply("PROFILER");
    assertNotNull(syshdl13);
    var syshdl14 = VERSION_SYS_HANDLE_PROVIDER.apply(OpenEdgeVersion.V130).apply("PROFILER");
    assertNotNull(syshdl14);

    var syshdl2 = VERSION_SYS_HANDLE_PROVIDER.apply(OpenEdgeVersion.V117).apply("SECURITY-POLICY");
    assertNotNull(syshdl2);
    var list = syshdl2.getAttributes();
    assertEquals(list.size(), 12);
    var sub = list.stream().filter(it -> "ALLOW-PREV-DESERIALIZATION".equalsIgnoreCase(it.getName())).toList();
    assertEquals(sub.size(), 0);

    var syshdl22 = VERSION_SYS_HANDLE_PROVIDER.apply(OpenEdgeVersion.V122).apply("SECURITY-POLICY");
    assertNotNull(syshdl22);
    var list2 = syshdl22.getAttributes();
    assertEquals(list2.size(), 13);
    var sub2 = list2.stream().filter(it -> "ALLOW-PREV-DESERIALIZATION".equalsIgnoreCase(it.getName())).toList();
    assertEquals(sub2.size(), 1);
  }

  @Test
  public void testBrowse() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("BROWSE");
      assertNotNull(syshdl);
      assertNotNull(syshdl.getMethod("ADD-FIRST"));
    }
  }

  @Test
  public void testParamOption01() {
    for (var version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("Query");
      assertNotNull(syshdl);
      var val1 = syshdl.getMethod("GET-CURRENT");
      assertNotNull(val1);
      assertEquals(val1.getVariants().length, 2);
      assertEquals(val1.getVariants()[1].getParameters().length, 0);
      var val2 = syshdl.getMethod("GET-FIRST");
      assertNotNull(val2);
      assertEquals(val2.getVariants().length, 2);
      assertEquals(val2.getVariants()[1].getParameters().length, 0);
      var val3 = syshdl.getMethod("GET-LAST");
      assertNotNull(val3);
      assertEquals(val3.getVariants().length, 2);
      assertEquals(val3.getVariants()[1].getParameters().length, 0);
      var val4 = syshdl.getMethod("GET-NEXT");
      assertNotNull(val4);
      assertEquals(val4.getVariants().length, 2);
      assertEquals(val4.getVariants()[1].getParameters().length, 0);
      var val5 = syshdl.getMethod("GET-PREV");
      assertNotNull(val5);
      assertEquals(val5.getVariants().length, 2);
      assertEquals(val5.getVariants()[1].getParameters().length, 0);
    }
  }

  @Test
  public void testParamOption02() {
    for (var version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("Browse");
      assertNotNull(syshdl);
      var val1 = syshdl.getMethod("INSERT-ROW");
      assertNotNull(val1);
      assertEquals(val1.getVariants().length, 2);
      assertEquals(val1.getVariants()[1].getParameters().length, 0);
      var val2 = syshdl.getMethod("SET-REPOSITIONED-ROW");
      assertNotNull(val2);
      assertEquals(val2.getVariants().length, 1);
      assertEquals(val2.getVariants()[0].getParameters().length, 2);
      assertFalse(val2.getVariants()[0].getParameters()[0].isOptional());
      assertTrue(val2.getVariants()[0].getParameters()[1].isOptional());
    }
  }

  @Test
  public void testNotEmptyVariant() {
    for (var version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("Buffer");
      assertNotNull(syshdl);
      var val1 = syshdl.getMethod("FIND-BY-ROWID");
      assertNotNull(val1);
      assertEquals(val1.getVariants().length, 1);
      assertEquals(val1.getVariants()[0].getParameters().length, 2);
      assertFalse(val1.getVariants()[0].getParameters()[0].isOptional());
      assertTrue(val1.getVariants()[0].getParameters()[1].isOptional());
    }
  }

  @Test
  public void testParamOrder() {
    for (var version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("Buffer");
      assertNotNull(syshdl);
      var val1 = syshdl.getMethod("WRITE-JSON");
      assertNotNull(val1);
      assertEquals(val1.getVariants().length, 7);
      assertEquals(val1.getVariants()[0].getParameters().length, 7);
      assertEquals(val1.getVariants()[0].getParameters()[0].getName(), "target-type");
      assertEquals(val1.getVariants()[0].getParameters()[1].getName(), "file");
      assertEquals(val1.getVariants()[0].getParameters()[2].getName(), "formatted");
      assertEquals(val1.getVariants()[0].getParameters()[3].getName(), "encoding");
      assertEquals(val1.getVariants()[0].getParameters()[4].getName(), "omit-initial-values");
      assertEquals(val1.getVariants()[0].getParameters()[5].getName(), "omit-outer-object");
      assertEquals(val1.getVariants()[0].getParameters()[6].getName(), "write-before-image");
    }
  }

}
