/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
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

import java.io.FileInputStream;
import java.io.IOException;

import org.testng.annotations.Test;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IDatasetElement;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.ParameterType;

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
      ITypeInfo info = rci.getTypeInfo();
      assertNotNull(info);
      assertNotNull(info.getProperties());
      assertEquals(info.getProperties().size(), 6);

      IPropertyElement prop1 = info.getProperty("prop1");
      assertNotNull(prop1);
      assertTrue(prop1.isPublic());

      IPropertyElement prop2 = info.getProperty("prop2");
      assertNotNull(prop2);
      assertTrue(prop2.isPrivate());

      IPropertyElement prop3 = info.getProperty("prop3");
      assertNotNull(prop3);
      assertTrue(prop3.isPublic());

      IPropertyElement prop4 = info.getProperty("prop4");
      assertNotNull(prop4);
      assertTrue(prop4.isProtected());

      IPropertyElement prop5 = info.getProperty("prop5");
      assertNotNull(prop5);
      assertTrue(prop5.isProtected());
      assertTrue(prop5.isAbstract());

      IPropertyElement prop6 = info.getProperty("prop6");
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
  public void testClassMinSize() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/ClassMinSize.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getTypeInfo().getProperties().size(), 2);
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
      RCodeInfo rci = new RCodeInfo(input);
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
  public void testV11() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/WebRequestV11.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getVersion(), 1100);

      assertNotNull(rci.getTypeInfo());
      assertNotNull(rci.getTypeInfo().getMethods());
      assertEquals(rci.getTypeInfo().getMethods().size(), 24);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isProtected()).count(), 0);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isPrivate()).count(), 6);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isConstructor()).count(), 1);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isPublic()).count(), 18);

      assertNotNull(rci.getTypeInfo().getTables());
      assertEquals(rci.getTypeInfo().getTables().size(), 0);
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV12() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/WebRequestV12.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getVersion(), -1215);

      assertNotNull(rci.getTypeInfo());
      assertNotNull(rci.getTypeInfo().getMethods());
      assertEquals(rci.getTypeInfo().getMethods().size(), 26);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isProtected()).count(), 0);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isPrivate()).count(), 6);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isConstructor()).count(), 1);
      assertEquals(rci.getTypeInfo().getMethods().stream().filter(m -> m.isPublic()).count(), 20);

      assertNotNull(rci.getTypeInfo().getTables());
      assertEquals(rci.getTypeInfo().getTables().size(), 0);
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV121() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/NMSTrace.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertNotNull(rci.getTypeInfo());
      assertEquals(rci.getTypeInfo().getProperties().size(), 5);
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testPackageProtected() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/PkgLevelAttr.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertNotNull(rci.getTypeInfo());
      assertEquals(rci.getTypeInfo().getProperties().size(), 3);
      IPropertyElement obj1 = rci.getTypeInfo().getProperty("obj0");
      assertNotNull(obj1);
      assertTrue(obj1.isPackageProtected());

      IPropertyElement obj2 = rci.getTypeInfo().getProperty("obj1");
      assertNotNull(obj2);
      assertTrue(obj2.isPackagePrivate());

      IPropertyElement obj3 = rci.getTypeInfo().getProperty("obj2");
      assertNotNull(obj3);
      assertTrue(obj3.isPublic());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testElements() throws IOException {
    try (FileInputStream input = new FileInputStream("src/test/resources/rcode/TestClassElements.r")) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertNotNull(rci.getTypeInfo());

      assertNotNull(rci.getTypeInfo().getTables());
      assertEquals(rci.getTypeInfo().getTables().size(), 2);
      ITableElement tt1 = rci.getTypeInfo().getTempTable("tt1");
      assertNotNull(tt1);
      assertEquals(tt1.getFields().length, 3); // Always an empty field at the end (ROWID ?)
      assertEquals(tt1.getIndexes().length, 1);
      ITableElement tt2 = rci.getTypeInfo().getTempTable("tt2");
      assertNotNull(tt2);
      assertEquals(tt2.getFields().length, 3);
      assertEquals(tt2.getIndexes().length, 1);
      assertNull(rci.getTypeInfo().getTempTable("tt3"));

      assertNotNull(rci.getTypeInfo().getDatasets());
      assertEquals(rci.getTypeInfo().getDatasets().size(), 1);
      IDatasetElement ds1 = rci.getTypeInfo().getDataset("ds1");
      assertNotNull(ds1);
      assertTrue(ds1.isProtected());
      assertEquals(ds1.getBufferNames().length, 2);
      assertEquals(ds1.getBufferNames()[0], "tt1");
      assertEquals(ds1.getBufferNames()[1], "tt2");
      assertNull(rci.getTypeInfo().getDataset("ds2"));

      IMethodElement testMethod = null;
      for (IMethodElement elem : rci.getTypeInfo().getMethods()) {
        if ("testMethod".equalsIgnoreCase(elem.getName()))
          testMethod = elem;
      }
      assertNotNull(testMethod);
      assertEquals(testMethod.getExtent(), -32767);
      assertEquals(testMethod.getParameters().length, 3);
      assertEquals(testMethod.getParameters()[0].getParameterType(), ParameterType.TABLE);
      assertEquals(testMethod.getParameters()[0].getMode(), ParameterMode.INPUT);
      assertEquals(testMethod.getParameters()[1].getParameterType(), ParameterType.DATASET);
      assertEquals(testMethod.getParameters()[1].getMode(), ParameterMode.OUTPUT);
      assertEquals(testMethod.getParameters()[2].getParameterType(), ParameterType.VARIABLE);
      assertEquals(testMethod.getParameters()[2].getMode(), ParameterMode.INPUT);
      assertEquals(testMethod.getParameters()[2].getABLDataType(), DataType.INTEGER);
      assertEquals(testMethod.getParameters()[2].getExtent(), 3);

      IMethodElement testMethod21 = null;
      IMethodElement testMethod22 = null;
      for (IMethodElement elem : rci.getTypeInfo().getMethods()) {
        if ("testMethod2".equalsIgnoreCase(elem.getName()) && elem.getExtent() != 0)
          testMethod21 = elem;
        else if ("testMethod2".equalsIgnoreCase(elem.getName()) && elem.getExtent() == 0)
          testMethod22 = elem;
      }
      assertNotNull(testMethod21);
      assertNotNull(testMethod22);
      assertEquals(testMethod21.getParameters()[0].getExtent(), 0);
      assertEquals(testMethod22.getParameters()[0].getExtent(), -32767);

      IMethodElement testMethod3 = null;
      for (IMethodElement elem : rci.getTypeInfo().getMethods()) {
        if ("testMethod3".equalsIgnoreCase(elem.getName()))
          testMethod3 = elem;
      }
      assertNotNull(testMethod3);
      assertNotNull(testMethod3.getParameters());
      assertEquals(testMethod3.getParameters().length, 2);
      assertEquals(testMethod3.getParameters()[0].getABLDataType(), DataType.HANDLE);
      assertEquals(testMethod3.getParameters()[0].getMode(), ParameterMode.INPUT);
      assertEquals(testMethod3.getParameters()[0].getParameterType(), ParameterType.TABLE);
      assertEquals(testMethod3.getParameters()[1].getABLDataType(), DataType.HANDLE);
      assertEquals(testMethod3.getParameters()[1].getMode(), ParameterMode.INPUT_OUTPUT);
      assertEquals(testMethod3.getParameters()[1].getParameterType(), ParameterType.DATASET);
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }
}
