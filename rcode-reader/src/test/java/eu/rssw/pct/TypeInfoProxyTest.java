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
import static org.testng.Assert.assertTrue;

import java.nio.file.Paths;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.fixed.TypeInfoPLProxy;
import eu.rssw.pct.elements.fixed.TypeInfoRCodeProxy;

public class TypeInfoProxyTest {

  @Test
  public void testPLProxy01() {
    TypeInfoPLProxy info = new TypeInfoPLProxy("OpenEdge.ABLUnit.Reflection.ClassAnnotationInfo",
        Paths.get("src/test/resources/ablunit.pl"), "OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r");
    assertFalse(info.isInitialized());
    assertEquals(info.getTypeName(), "OpenEdge.ABLUnit.Reflection.ClassAnnotationInfo");
    assertFalse(info.isInitialized());
    assertTrue(info.getMethods().size() > 0);
    assertTrue(info.isInitialized());
  }

  @Test
  public void testPLProxy02() {
    TypeInfoPLProxy info = new TypeInfoPLProxy("OpenEdge.ABLUnit.Reflection.ClassAnnotationInfo",
        Paths.get("src/test/resources/invalid.pl"), "OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r");
    assertFalse(info.isInitialized());
    assertEquals(info.getTypeName(), "OpenEdge.ABLUnit.Reflection.ClassAnnotationInfo");
    assertFalse(info.isInitialized());
    // Invalid PL file, error silently swallowed, fall back to default TypeInfo
    assertEquals(info.getMethods().size(), 0);
    assertTrue(info.isInitialized());
  }

  @Test
  public void testPLProxy03() {
    TypeInfoPLProxy info = new TypeInfoPLProxy("OpenEdge.ABLUnit.Reflection.ClassAnnotationInfo",
        Paths.get("src/test/resources/ablunit.pl"), "OpenEdge/ABLUnit/Reflection/Invalid.r");
    assertFalse(info.isInitialized());
    assertEquals(info.getTypeName(), "OpenEdge.ABLUnit.Reflection.ClassAnnotationInfo");
    assertFalse(info.isInitialized());
    // Valid PL but invalid entry name, error silently swallowed, fall back to default TypeInfo
    assertEquals(info.getMethods().size(), 0);
    assertTrue(info.isInitialized());
  }

  @Test
  public void testRCodeProxy01() {
    TypeInfoRCodeProxy info = new TypeInfoRCodeProxy("rssw.pct.BackupDataCallback",
        Paths.get("src/test/resources/rcode/BackupDataCallback.r"));
    assertFalse(info.isInitialized());
    assertEquals(info.getTypeName(), "rssw.pct.BackupDataCallback");
    assertFalse(info.isInitialized());
    assertTrue(info.getMethods().size() > 0);
    assertTrue(info.isInitialized());
  }

  @Test
  public void testRCodeProxy02() {
    TypeInfoRCodeProxy info = new TypeInfoRCodeProxy("rssw.pct.BackupDataCallback",
        Paths.get("src/test/resources/rcode/Invalid.r"));
    assertFalse(info.isInitialized());
    assertEquals(info.getTypeName(), "rssw.pct.BackupDataCallback");
    assertFalse(info.isInitialized());
    // Invalid rcode, error silently swallowed, fall back to default TypeInfo
    assertEquals(info.getMethods().size(), 0);
    assertTrue(info.isInitialized());
  }

}
