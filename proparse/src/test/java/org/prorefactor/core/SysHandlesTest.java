package org.prorefactor.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ISystemHandle;
import eu.rssw.pct.elements.SysHandles;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class SysHandlesTest {
  private static final Function<OpenEdgeVersion, Function<String, ISystemHandle>> VERSION_SYS_HANDLE_PROVIDER = version -> {
    return name -> SysHandles.getSysHandles(version).stream() //
      .filter(it -> it.getName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  private void testSignatures() {
    // Assert all signatures can be fetched
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      for (ISystemHandle systemHandle : SysHandles.getSysHandles(version)) {
        for (IMethodElement method : systemHandle.getMethods()) {
          assertNotNull(method.getSignature());
        }
      }
    }
  }

  @Test
  private void testRightSysHdlPerVersion() {
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
    var list = syshdl2.getAllAttributes();
    assertEquals(list.size(), 12);
    var sub = list.stream().filter(it -> "ALLOW-PREV-DESERIALIZATION".equalsIgnoreCase(it.getName())).toList();
    assertEquals(sub.size(), 0);

    var syshdl22 = VERSION_SYS_HANDLE_PROVIDER.apply(OpenEdgeVersion.V122).apply("SECURITY-POLICY");
    assertNotNull(syshdl22);
    var list2 = syshdl22.getAllAttributes();
    assertEquals(list2.size(), 13);
    var sub2 = list2.stream().filter(it -> "ALLOW-PREV-DESERIALIZATION".equalsIgnoreCase(it.getName())).toList();
    assertEquals(sub2.size(), 1);
  }
}
