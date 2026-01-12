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

import eu.rssw.pct.mapping.OpenEdgeVersion;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.FunctionsDocumentation;
import eu.rssw.pct.elements.IFunctionDocumentation;

public class FunctionsDocumentationTest {
  private static final Function<OpenEdgeVersion, Function<String, IFunctionDocumentation>> VERSION_FUNCTION_DOCUMENTATION_PROVIDER = version -> {
    return name -> FunctionsDocumentation.getFunctionsDocumentation(version).stream() //
      .filter(it -> it.getName().equalsIgnoreCase(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  private void testDocumentation() {
    // Assert no unknown datatypes, documentation is available
    for (var version : OpenEdgeVersion.values()) {
      for (var functionDocumentation : FunctionsDocumentation.getFunctionsDocumentation(version)) {
        assertNotNull(functionDocumentation.getDescription());
        assertNotNull(functionDocumentation.getName());
        assertNotEquals(functionDocumentation.getReturnType(), null,
            version + " " + functionDocumentation.getName() + " -- " + "returndatatype");
        assertNotNull(functionDocumentation.getIDESignature());
        assertNotNull(functionDocumentation.getParameters());
        for (var param : functionDocumentation.getParameters()) {
          assertNotNull(param.getName());
          assertNotNull(param.getDescription());
          assertNotEquals(param.getDataType(), DataType.UNKNOWN,
              version + " " + functionDocumentation.getName() + " -- " + param.getName() + " -- " + "datatype");
          assertTrue(functionDocumentation.hasParameters(param.getName()));
        }
        assertFalse(functionDocumentation.hasParameters("unknown"));
        assertNull(functionDocumentation.getParameter("unknown"));
      }
    }
  }

  @Test
  public void test01() {
    for (OpenEdgeVersion version : OpenEdgeVersion.values()) {
      var functionDocumentation = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(version).apply("LOOKUP");
      assertNotNull(functionDocumentation);
      var val1 = functionDocumentation.getParameter("list");
      assertNotNull(val1);
      assertEquals(val1.getDataType(), DataType.CHARACTER);
    }
  }

  @Test
  public void testSignatures() {
    var functionDocumentation1 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V117).apply("LOOKUP");
    assertNotNull(functionDocumentation1);
    assertEquals(functionDocumentation1.getIDESignature(),
        "LOOKUP(CHARACTER expression, CHARACTER list [, CHARACTER character])");

    var functionDocumentation2 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("LOOKUP");
    assertNotNull(functionDocumentation2);
    assertEquals(functionDocumentation2.getIDESignature(),
        "LOOKUP(CHARACTER expression, CHARACTER list [, CHARACTER delimiter])");

    var functionDocumentation3 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("SUBSTRING");
    assertNotNull(functionDocumentation3);
    assertEquals(functionDocumentation3.getIDESignature(),
        "SUBSTRING(CHARACTER source, INTEGER position, INTEGER length [, CHARACTER type])");
  }

  @Test
  private void testFunctionDocumentationsPerVersion() {
    var functionDocumentation1 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V117).apply("LENGTH");
    assertNotNull(functionDocumentation1);
    var list = functionDocumentation1.getParameters();
    assertEquals(list.length, 4);
    var val1 = functionDocumentation1.getParameter("string");
    assertNotNull(val1);
    var functionDocumentation12 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V122).apply("LENGTH");
    assertNotNull(functionDocumentation12);
    var list12 = functionDocumentation12.getParameters();
    assertEquals(list12.length, 4);
    var val12 = functionDocumentation1.getParameter("string");
    assertNotNull(val12);
    var functionDocumentation13 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("LENGTH");
    assertNotNull(functionDocumentation13);
    var list13 = functionDocumentation13.getParameters();
    assertEquals(list13.length, 4);
    var val13 = functionDocumentation13.getParameter("exp");
    assertNotNull(val13);
    var functionDocumentation14 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V130).apply("LENGTH");
    assertNotNull(functionDocumentation14);
    var list14 = functionDocumentation14.getParameters();
    assertEquals(list14.length, 4);
    var val14 = functionDocumentation14.getParameter("exp");
    assertNotNull(val14);

    var functionDocumentation2 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V117).apply("HASH-CODE");
    assertNull(functionDocumentation2);
    var functionDocumentation21 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V122).apply(
        "HASH-CODE");
    assertNull(functionDocumentation21);
    var functionDocumentation22 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply(
        "HASH-CODE");
    assertNotNull(functionDocumentation22);
    var functionDocumentation23 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V130).apply(
        "HASH-CODE");
    assertNotNull(functionDocumentation23);

    var functionDocumentation3 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V117).apply("CAN-DO");
    assertNotNull(functionDocumentation3);
    var list3 = functionDocumentation3.getParameters();
    assertEquals(list3.length, 2);
    var val3 = functionDocumentation3.getParameter("userid");
    assertNotNull(val3);
    assertTrue(val3.isOptional());
    var val31 = functionDocumentation3.getParameter("id-pattern-list");
    assertNotNull(val31);
    assertFalse(val31.isOptional());
  }

  @Test
  public void testOptionalParameter() {
    var ascDocumentation = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("ASC");
    assertNotNull(ascDocumentation);
    assertNotNull(ascDocumentation.getParameters());
    var prm1 = ascDocumentation.getParameter("expression");
    assertNotNull(prm1);
    assertEquals(prm1.getDataType(), DataType.CHARACTER);
    assertFalse(prm1.isOptional());
    var prm2 = ascDocumentation.getParameter("target-codepage");
    assertNotNull(prm2);
    assertEquals(prm2.getDataType(), DataType.CHARACTER);
    assertFalse(prm2.isOptional());
    var prm3 = ascDocumentation.getParameter("source-codepage");
    assertNotNull(prm3);
    assertEquals(prm3.getDataType(), DataType.CHARACTER);
    assertTrue(prm3.isOptional());
    var prm4 = ascDocumentation.getParameter("unknown");
    assertNull(prm4);
  }
}
