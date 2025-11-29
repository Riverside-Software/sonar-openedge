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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.SystemHandles;
import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ISystemHandle;

public class ISystemHandleTest {
  private static final Function<OpenEdgeVersion, Function<String, ISystemHandle>> VERSION_SYS_HANDLE_PROVIDER = version -> {
    return name -> SystemHandles.getSysHandles(version).stream() //
      .filter(it -> it.getName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  public void test01() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("COMPILER");
      assertNotNull(syshdl);
      var val1 = syshdl.getMethod("GET-COLUMN( )");
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
      var val1 = syshdl.getMethod("GET-COLUMN( )");
      assertNotNull(val1);
      var list = val1.getParameters();
      assertEquals(list.length, 2);
      var list2 = syshdl.getAttributes();
      assertEquals(list2.size(), 4);
      var opt1 = list2.stream().findFirst();
      assertTrue(opt1.isPresent());
      assertEquals(opt1.get().getName(), "HANDLE");
      assertTrue(opt1.get().isReadOnly());
      assertFalse(opt1.get().isWriteOnly());

      var sub = list2.stream().filter(it -> it.getName().equals("NUM-ENTRIES")).toList();
      assertEquals(sub.size(), 1);
      assertFalse(sub.get(0).isReadOnly());
      assertFalse(sub.get(0).isWriteOnly());

    }
  }

  @Test
  public void testNotFound() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var syshdl = VERSION_SYS_HANDLE_PROVIDER.apply(version).apply("SELF");
      assertNull(syshdl);
    }
  }

}
