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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.ClassDocumentationUtil;
import eu.rssw.pct.elements.IClassDocumentation;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class ClassDocumentationTest {
  private static final Function<OpenEdgeVersion, Function<String, IClassDocumentation>> VERSION_CLASS_DOCUMENTATION_PROVIDER = version -> {
    return name -> ClassDocumentationUtil.getClassesDocumentation(version).stream() //
      .filter(it -> it.getName().equals(name)) //
      .findFirst() //
      .orElse(null);
  };

  @Test
  public void test01() {
    for (var version : OpenEdgeVersion.values()) {
      var classDoc = VERSION_CLASS_DOCUMENTATION_PROVIDER.apply(version).apply("Progress.Data.BindingSource");
      assertNotNull(classDoc);
      assertEquals(classDoc.getType(), "class");
      assertNotNull(classDoc.getDescription());
      assertEquals(classDoc.getMethods().size(), 6);

      if (version.equals(OpenEdgeVersion.V117) || version.equals(OpenEdgeVersion.V122)) {
        assertEquals(classDoc.getProperties().size(), 21);
      } else {
        assertEquals(classDoc.getProperties().size(), 23);
      }
      assertTrue(classDoc.hasProperty("AllowEdit"));
      assertFalse(classDoc.hasProperty("UnknownProperty"));
      assertTrue(classDoc.hasMethod("Dispose"));
      assertFalse(classDoc.hasProperty("UnknownMethod"));

      var methd1 = classDoc.getMethod("Refresh");
      assertNotNull(methd1);
      var prms = methd1.getParameters();
      assertEquals(prms.length, 1);
      assertEquals(prms[0].getName(), "record-index");
      assertNotNull(prms[0].getDescription());

      var prop1 = classDoc.getProperty("AllowEdit");
      assertNotNull(prop1);
      assertNotNull(prop1.getDescription());
    }
  }

  @Test
  private void testDescriptions() {
    // Assert all documentations can be fetched
    for (var version : OpenEdgeVersion.values()) {
      for (var clzDoc : ClassDocumentationUtil.getClassesDocumentation(version)) {
        assertNotNull(clzDoc.getDescription());
        for (var method : clzDoc.getMethods()) {
          assertNotNull(method.getDescription());
          if (method.getParameters().length > 0) {
            for (var param : method.getParameters()) {
              assertNotNull(param.getDescription());
            }
          }
        }
      }
    }
  }

}
