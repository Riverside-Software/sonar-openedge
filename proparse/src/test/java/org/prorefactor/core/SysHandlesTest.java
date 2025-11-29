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
package org.prorefactor.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ISystemHandle;
import eu.rssw.pct.elements.SystemHandles;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class SysHandlesTest {
  private static final Function<OpenEdgeVersion, Function<String, ISystemHandle>> VERSION_SYS_HANDLE_PROVIDER = version -> {
    return name -> SystemHandles.getSysHandles(version).stream() //
      .filter(it -> it.getName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  private void testSignatures() {
    // Assert all signatures can be fetched
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      for (ISystemHandle systemHandle : SystemHandles.getSysHandles(version)) {
        for (IMethodElement method : systemHandle.getMethods()) {
          assertNotNull(method.getSignature());
        }
      }
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
