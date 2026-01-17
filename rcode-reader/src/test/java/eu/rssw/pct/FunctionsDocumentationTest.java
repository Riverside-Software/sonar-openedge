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
import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.FunctionsDocumentation;
import eu.rssw.pct.elements.IFunctionDocumentation;
import eu.rssw.pct.elements.ITypeInfo;

public class FunctionsDocumentationTest {
  private static final Function<OpenEdgeVersion, Function<String, ITypeInfo>> VERSION_TYPE_INFO_PROVIDER = version -> {
    return name -> BuiltinClasses.getBuiltinClasses(version).stream() //
      .filter(it -> it.getTypeName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

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
        assertNotNull(functionDocumentation.getVariants());
        assertNotNull(functionDocumentation.getIDESignature(VERSION_TYPE_INFO_PROVIDER.apply(OpenEdgeVersion.V128)));
        assertNotNull(functionDocumentation.getVariants()[0].getParameters());
        for (var variant : functionDocumentation.getVariants()) {
          for (var param : variant.getParameters()) {
            assertNotNull(param.getName());
            assertNotNull(param.getDescription());
            assertNotEquals(param.getDataType(), DataType.UNKNOWN,
                version + " " + functionDocumentation.getName() + " -- " + param.getName() + " -- " + "datatype");
            assertTrue(functionDocumentation.hasParameters(param.getName()));
          }
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
    var provider = VERSION_TYPE_INFO_PROVIDER.apply(OpenEdgeVersion.V128);
    var lookupFunc1 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V117).apply("LOOKUP");
    assertNotNull(lookupFunc1);
    assertEquals(lookupFunc1.getIDESignature(provider), "LOOKUP(CHAR expression, CHAR list [, CHAR character])");

    var lookupFunc2 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("LOOKUP");
    assertNotNull(lookupFunc2);
    assertEquals(lookupFunc2.getIDESignature(provider), "LOOKUP(CHAR expression, CHAR list [, CHAR delimiter])");

    var substringFunc = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("SUBSTRING");
    assertNotNull(substringFunc);
    assertEquals(substringFunc.getIDESignature(provider),
        "SUBSTRING(CHAR source, INT position [, INT length [, CHAR type]])");

    var lineCounterFunc = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("LINE-COUNTER");
    assertNotNull(lineCounterFunc);
    assertEquals(lineCounterFunc.getIDESignature(provider), "LINE-COUNTER([CHAR stream [, HDL handle]])");

    var connectedFunc = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("CONNECTED");
    assertNotNull(connectedFunc);
    assertEquals(connectedFunc.getIDESignatures(provider).length, 2);
    assertEquals(connectedFunc.getIDESignatures(provider)[0], "CONNECTED(CHAR logical-name)");
    assertEquals(connectedFunc.getIDESignatures(provider)[1], "CONNECTED(CHAR alias)");

    var validObjectFunc = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("VALID-OBJECT");
    assertNotNull(validObjectFunc);
    assertEquals(validObjectFunc.getIDESignatures(provider).length, 2);
    assertEquals(validObjectFunc.getIDESignatures(provider)[0], "VALID-OBJECT(HDL handle)");
    assertEquals(validObjectFunc.getIDESignatures(provider)[1], "VALID-OBJECT(Progress.Lang.Object object-reference)");
    assertEquals(validObjectFunc.getIDESignatures(new DataType[] {DataType.HANDLE}, provider)[0],
        "VALID-OBJECT(HDL handle)");
    assertEquals(validObjectFunc.getIDESignatures(new DataType[] {new DataType("Progress.Lang.AppError")}, provider)[0],
        "VALID-OBJECT(Progress.Lang.Object object-reference)");
  }

  @Test
  private void testFunctionDocumentationsPerVersion() {
    var functionDocumentation1 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V117).apply("LENGTH");
    assertNotNull(functionDocumentation1);
    var list = functionDocumentation1.getVariants()[0].getParameters();
    assertEquals(list.length, 2);
    var val1 = functionDocumentation1.getParameter("string");
    assertNotNull(val1);
    var functionDocumentation12 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V122).apply("LENGTH");
    assertNotNull(functionDocumentation12);
    var list12 = functionDocumentation12.getVariants()[0].getParameters();
    assertEquals(list12.length, 2);
    var val12 = functionDocumentation1.getParameter("string");
    assertNotNull(val12);
    var functionDocumentation13 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V128).apply("LENGTH");
    assertNotNull(functionDocumentation13);
    var list13 = functionDocumentation13.getVariants()[0].getParameters();
    assertEquals(list13.length, 2);
    var val13 = functionDocumentation13.getParameter("exp");
    assertNotNull(val13);
    var functionDocumentation14 = VERSION_FUNCTION_DOCUMENTATION_PROVIDER.apply(OpenEdgeVersion.V130).apply("LENGTH");
    assertNotNull(functionDocumentation14);
    var list14 = functionDocumentation14.getVariants()[0].getParameters();
    assertEquals(list14.length, 2);
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
    var list3 = functionDocumentation3.getVariants()[0].getParameters();
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
    assertNotNull(ascDocumentation.getVariants()[0].getParameters());
    var prm1 = ascDocumentation.getParameter("expression");
    assertNotNull(prm1);
    assertEquals(prm1.getDataType(), DataType.CHARACTER);
    assertFalse(prm1.isOptional());
    var prm2 = ascDocumentation.getParameter("target-codepage");
    assertNotNull(prm2);
    assertEquals(prm2.getDataType(), DataType.CHARACTER);
    assertTrue(prm2.isOptional());
    var prm3 = ascDocumentation.getParameter("source-codepage");
    assertNotNull(prm3);
    assertEquals(prm3.getDataType(), DataType.CHARACTER);
    assertTrue(prm3.isOptional());
    var prm4 = ascDocumentation.getParameter("unknown");
    assertNull(prm4);
  }
}
