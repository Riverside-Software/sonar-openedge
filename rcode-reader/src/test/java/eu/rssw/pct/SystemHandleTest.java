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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.SystemHandles;
import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ISystemHandle;

public class SystemHandleTest {
  private static final Function<OpenEdgeVersion, Function<String, ISystemHandle>> VERSION_SYS_HANDLE_PROVIDER = version -> {
    return name -> SystemHandles.getSystemHandles(version).stream() //
      .filter(it -> it.getName().equalsIgnoreCase(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  private void testSignatures() {
    // Assert all signatures can be fetched, no unknown datatypes, documentation is available
    for (var version : OpenEdgeVersion.values()) {
      for (var systemHandle : SystemHandles.getSystemHandles(version)) {
        assertNotNull(systemHandle.getDescription());
        if (!"com-self".equalsIgnoreCase(systemHandle.getName()) && !"focus".equalsIgnoreCase(systemHandle.getName())
            && !"default-window".equalsIgnoreCase(systemHandle.getName())
            && !"current-window".equalsIgnoreCase(systemHandle.getName())
            && !"active-window".equalsIgnoreCase(systemHandle.getName())) {
          assertTrue(systemHandle.hasAttribute("type"), systemHandle.toString());
          assertFalse(systemHandle.hasAttribute("unknown"));
        }
        for (var method : systemHandle.getMethods()) {
          assertNotNull(method.getSignature());
          assertNotEquals(method.getReturnType(), DataType.UNKNOWN,
              version + " " + systemHandle.getName() + " -- " + method.getName() + " return datatype");
          assertNotNull(systemHandle.getMethodDocumentation(method.getName()));
          for (var prm : method.getParameters()) {
            assertFalse(prm.getName().isBlank());
            assertNotEquals(prm.getDataType(), DataType.UNKNOWN,
                version + " " + systemHandle.getName() + " -- " + method.getName() + " -- " + prm.getName());
          }
          assertTrue(systemHandle.hasMethod(method.getName()));
        }
        assertFalse(systemHandle.hasMethod("unknown"));
        assertNull(systemHandle.getMethod("unknown"));
        for (var attr : systemHandle.getAttributes()) {
          assertNotNull(attr.getName());
          assertNotNull(attr.getDescription());
          /*
           * assertNotEquals(attr.getDataType(), DataType.UNKNOWN, version + " " + systemHandle.getName() + " -- " +
           * attr.getName());
           */
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
      var list = val1.getParameters();
      assertEquals(list.length, 1);
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
}
