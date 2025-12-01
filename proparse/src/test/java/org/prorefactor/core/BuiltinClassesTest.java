package org.prorefactor.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class BuiltinClassesTest {
  private static final Function<OpenEdgeVersion, Function<String, ITypeInfo>> VERSION_TYPE_INFO_PROVIDER = version -> {
    return name -> BuiltinClasses.getBuiltinClasses(version).stream() //
      .filter(it -> it.getTypeName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  private void testSignatures() {
    // Assert all signatures can be fetched
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      for (ITypeInfo typeInfo : BuiltinClasses.getBuiltinClasses(version)) {
        for (IMethodElement method : typeInfo.getMethods()) {
          assertNotNull(method.getSignature());
          assertNotEquals(method.getReturnType(), DataType.UNKNOWN, "Method " + method.getSignature());
        }
      }
    }
  }

  @Test
  private void testClassPerVersion() {
    var provider117 = VERSION_TYPE_INFO_PROVIDER.apply(OpenEdgeVersion.V117);
    var provider122 = VERSION_TYPE_INFO_PROVIDER.apply(OpenEdgeVersion.V122);
    var provider128 = VERSION_TYPE_INFO_PROVIDER.apply(OpenEdgeVersion.V128);

    var info1 = provider117.apply("Progress.IO.MemoryInputStream");
    assertNull(info1);
    var info12 = provider122.apply("Progress.IO.MemoryInputStream");
    assertNotNull(info12);

    var info2 = provider117.apply("Progress.Archive.ArchiveInfo");
    assertNull(info2);
    var info21 = provider122.apply("Progress.Archive.ArchiveInfo");
    assertNull(info21);
    var info22 = provider128.apply("Progress.Archive.ArchiveInfo");
    assertNotNull(info22);

    var info31 = provider122.apply("Progress.Util.DateTimeHelper");
    assertNull(info31);
    var info32 = provider128.apply("Progress.Util.DateTimeHelper");
    assertNotNull(info32);

    var info41 = provider117.apply("Progress.ApplicationServer.AgentManager");
    assertNotNull(info41);
    assertNull(info41.getMethod(provider117, "PushProfilerData",
        new DataType[] {DataType.CHARACTER, DataType.INTEGER, DataType.LONGCHAR},
        new ParameterMode[] {ParameterMode.INPUT, ParameterMode.INPUT, ParameterMode.INPUT}));
    var info42 = provider122.apply("Progress.ApplicationServer.AgentManager");
    assertNotNull(info42);
    assertNotNull(info42.getMethod(provider122, "PushProfilerData",
        new DataType[] {DataType.CHARACTER, DataType.INTEGER, DataType.LONGCHAR},
        new ParameterMode[] {ParameterMode.INPUT, ParameterMode.INPUT, ParameterMode.INPUT}));

    var info51 = provider117.apply("Progress.Reflect.Property");
    assertNotNull(info51);
    var list = info51.getAllProperties(provider117);
    assertEquals(list.size(), 17);
    var sub1 = list.stream().filter(
        it -> "Progress.Reflect.Property".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub1.size(), 7);
    assertEquals(sub1.get(6).getO2().getName(), "CanWrite");
    var info52 = provider128.apply("Progress.Reflect.Property");
    assertNotNull(info52);
    var list2 = info52.getAllProperties(provider128);
    assertEquals(list2.size(), 18);
    var sub2 = list2.stream().filter(
        it -> "Progress.Reflect.Property".equalsIgnoreCase(it.getO1().getTypeName())).toList();
    assertEquals(sub2.size(), 8);
    assertEquals(sub2.get(7).getO2().getName(), "IsFinal");
  }

}
