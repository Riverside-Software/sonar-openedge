/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
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
    var provider130 = VERSION_TYPE_INFO_PROVIDER.apply(OpenEdgeVersion.V130);

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

    var info31 = provider117.apply("Progress.Data.DummyRow");
    assertNull(info31);
    var info32 = provider122.apply("Progress.Data.DummyRow");
    assertNotNull(info32);
    var info33 = provider128.apply("Progress.Data.DummyRow");
    assertNotNull(info33);

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

    var info61 = provider117.apply("Progress.Collections.ICollection");
    assertNull(info61);
    var info62 = provider122.apply("Progress.Collections.ICollection");
    assertNull(info62);
    var info63 = provider128.apply("Progress.Collections.ICollection");
    assertNotNull(info63);
    var info64 = provider130.apply("Progress.Collections.ICollection");
    assertNotNull(info64);
  }

  private static Consumer<ITypeInfo> classConsumer(BufferedWriter writer) {
    return info -> {
      try {
        writer.write(info.getTypeName());
        writer.newLine();
        for (var str : info.getInterfaces()) {
          writer.write("  Interface: " + str);
          writer.newLine();
        }
        for (var p : info.getProperties().stream().sorted((a, b) -> a.getName().compareTo(b.getName())).toList()) {
          writer.write(p.isStatic() ? "  Static " : "  ");
          writer.write("Property ");
          writer.write(p.getName());
          writer.write(" " + p.getVariable().getDataType() + " " + p.getVariable().getExtent());
          writer.newLine();
        }
        for (var m : info.getMethods().stream().sorted((a, b) -> a.getName().compareTo(b.getName())).toList()) {
          writer.write(m.isStatic() ? "  Static " : "  ");
          writer.write("Method ");
          writer.write(m.getReturnType().toString() + " ");
          writer.write(m.getIDESignature());
          writer.newLine();
        }
      } catch (IOException uncaught) {
        System.out.println(uncaught.getMessage());
      }
    };
  }

  @Test
  public void test0() throws IOException {
    Files.createDirectories(Path.of("target/dump"));
    try (var output = Files.newBufferedWriter(Path.of("target/dump/V117.txt"))) {
      BuiltinClasses.getBuiltinClasses(OpenEdgeVersion.V117).stream().sorted((a, b) -> a.getTypeName().compareTo(b.getTypeName())).forEach(classConsumer(output));
    }
    try (var output = Files.newBufferedWriter(Path.of("target/dump/V122.txt"))) {
      BuiltinClasses.getBuiltinClasses(OpenEdgeVersion.V122).stream().sorted((a, b) -> a.getTypeName().compareTo(b.getTypeName())).forEach(classConsumer(output));
    }
    try (var output = Files.newBufferedWriter(Path.of("target/dump/V128.txt"))) {
      BuiltinClasses.getBuiltinClasses(OpenEdgeVersion.V128).stream().sorted((a, b) -> a.getTypeName().compareTo(b.getTypeName())).forEach(classConsumer(output));
    }
    try (var output = Files.newBufferedWriter(Path.of("target/dump/V130.txt"))) {
      BuiltinClasses.getBuiltinClasses(OpenEdgeVersion.V130).stream().sorted((a, b) -> a.getTypeName().compareTo(b.getTypeName())).forEach(classConsumer(output));
    }
    assertTrue(Files.exists(Path.of("target/dump/V117.txt")));
    assertTrue(Files.exists(Path.of("target/dump/V122.txt")));
    assertTrue(Files.exists(Path.of("target/dump/V128.txt")));
    assertTrue(Files.exists(Path.of("target/dump/V130.txt")));
  }

}
