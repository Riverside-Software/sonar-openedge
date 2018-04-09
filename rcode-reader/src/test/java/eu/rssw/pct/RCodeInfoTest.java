/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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

import java.io.FileInputStream;
import java.io.IOException;

import org.testng.annotations.Test;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.MethodElement;
import eu.rssw.pct.elements.PropertyElement;

public class RCodeInfoTest {

  @Test
  public void testEnum() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/MyEnum.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testInterface() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/IMyTest.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testClass() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/BackupDataCallback.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testClass2() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/propList.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      TypeInfo info = rci.getTypeInfo();
      assertNotNull(info);
      assertNotNull(info.getProperties());
      assertEquals(info.getProperties().size(), 6);

      PropertyElement prop1 = info.getProperty("prop1");
      assertNotNull(prop1);
      assertTrue(prop1.isPublic());

      PropertyElement prop2 = info.getProperty("prop2");
      assertNotNull(prop2);
      assertTrue(prop2.isPrivate());

      PropertyElement prop3 = info.getProperty("prop3");
      assertNotNull(prop3);
      assertTrue(prop3.isPublic());

      PropertyElement prop4 = info.getProperty("prop4");
      assertNotNull(prop4);
      assertTrue(prop4.isProtected());

      PropertyElement prop5 = info.getProperty("prop5");
      assertNotNull(prop5);
      assertTrue(prop5.isProtected());
      assertTrue(prop5.isAbstract());

      PropertyElement prop6 = info.getProperty("prop6");
      assertNotNull(prop6);
      assertTrue(prop6.isPublic());
      assertTrue(prop6.isStatic());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testClass3() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/ttClass.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/compile.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure2() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/AbstractTTCollection.r")) {
      RCodeInfo rci = new RCodeInfo(input, System.out);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure3() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/FileTypeRegistry.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure4() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/_dmpincr.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV12() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/WebRequest.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getVersion(), -1210);
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

}
