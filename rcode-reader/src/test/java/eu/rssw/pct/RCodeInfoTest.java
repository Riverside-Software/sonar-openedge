/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IDatasetElement;
import eu.rssw.pct.elements.IEventElement;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITableElement;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.ParameterType;
import eu.rssw.pct.elements.PrimitiveDataType;
import eu.rssw.pct.elements.v12.TypeInfoV12;

public class RCodeInfoTest {

  @BeforeTest
  public void init() throws IOException {
    try {
      Files.createDirectories(Paths.get("target/kryo"));
    } catch (FileAlreadyExistsException caught) {
      // No-op
    }
  }

  @Test
  public void testV10() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/simpleV10.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
      assertEquals(rci.getCrc(), 1876);
      assertEquals(rci.getDigest(), "E762264216FF9D45EB82D4FFF4618578");
      assertNull(rci.getTypeInfo());

    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV10NoMd5() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/simpleV10NoMD5.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
      assertEquals(rci.getCrc(), 1876);
      assertNull(rci.getDigest());
      assertNull(rci.getTypeInfo());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV10LongSignatureSegment() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/longSigV10.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
      assertEquals(rci.getCrc(), 33974);
      assertEquals(rci.getDigest(), "F9FBF64A38EC9A6264A00CAAF9E136FC");
      assertNull(rci.getTypeInfo());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV11LongSignatureSegment() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/longSigV11.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
      assertEquals(rci.getCrc(), 33974);
      assertEquals(rci.getDigest(), "135A1AD0C1F088893A46D39ED9765BAC");
      assertNull(rci.getTypeInfo());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV12LongSignatureSegment() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/longSigV12.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
      assertEquals(rci.getCrc(), 33974);
      assertEquals(rci.getDigest(), "JKgWEeW5CtyJXjUM6zQqqAa3KqAQXY2ttIe9uepOFjQ=");
      assertNull(rci.getTypeInfo());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testEnumV11() throws IOException {
    testEnum("src/test/resources/rcode/MyEnumV11.r", false, 14646);
  }

  @Test
  public void testEnumV12() throws IOException {
    testEnum("src/test/resources/rcode/MyEnumV12.r", true, 14646);
  }

  private Kryo getKryo() {
    Kryo kryo = new Kryo();
    kryo.register(HashMap.class);
    kryo.register(ArrayList.class);
    kryo.register(EnumSet.class);
    eu.rssw.pct.elements.fixed.KryoSerializers.addSerializers(kryo);
    eu.rssw.pct.elements.v12.KryoSerializers.addSerializers(kryo);
    eu.rssw.pct.elements.v11.KryoSerializers.addSerializers(kryo);

    return kryo;
  }

  public void testEnum(String fileName, boolean checkEnumValues, long expectedCrc) throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get(fileName))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getCrc(), expectedCrc);
      assertNotNull(rci.getTypeInfo());
      assertNotNull(rci.getTypeInfo().getProperties());
      assertEquals(rci.getTypeInfo().getProperties().size(), 10);
      assertNotNull(rci.getTypeInfo().getMethods());
      assertEquals(rci.getTypeInfo().getMethods().size(), 0);

      assertNotNull(rci.getTypeInfo().getProperty("Delete"));
      if (checkEnumValues) {
        assertNotNull(rci.getTypeInfo().getProperty("Write").getEnumDescriptor());
        assertEquals(rci.getTypeInfo().getProperty("Write").getEnumDescriptor().getValue(), 0X2L);
        assertNotNull(rci.getTypeInfo().getProperty("Delete").getEnumDescriptor());
        assertEquals(rci.getTypeInfo().getProperty("Delete").getEnumDescriptor().getValue(), 0x179324681357L);
        assertNotNull(rci.getTypeInfo().getProperty("Execute").getEnumDescriptor());
        assertEquals(rci.getTypeInfo().getProperty("Execute").getEnumDescriptor().getValue(), 0x973113572468L);
        assertNotNull(rci.getTypeInfo().getProperty("Extra01").getEnumDescriptor());
        assertEquals(rci.getTypeInfo().getProperty("Extra01").getEnumDescriptor().getValue(), 0x1234L);
        assertNotNull(rci.getTypeInfo().getProperty("Extra02").getEnumDescriptor());
        assertEquals(rci.getTypeInfo().getProperty("Extra02").getEnumDescriptor().getValue(), 0x5678L);
      }

      Kryo kryo = getKryo();

      try (OutputStream output = Files.newOutputStream(Paths.get("target/rcode-kryo.bin"));
          Output data = new Output(output)) {
        kryo.writeObject(data, rci.getTypeInfo());
      } catch (IOException caught) {
        return;
      }

      try (Input input2 = new Input(new FileInputStream("target/rcode-kryo.bin"));) {
        TypeInfoV12 foo = kryo.readObject(input2, TypeInfoV12.class);
        System.out.println(foo);
      }

    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testInterface() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/IMyTest.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testClass() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/BackupDataCallback.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testClass2() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/propList.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      ITypeInfo info = rci.getTypeInfo();
      assertNotNull(info);
      assertNotNull(info.getProperties());
      assertEquals(info.getProperties().size(), 6);

      IPropertyElement prop1 = info.getProperty("prop1");
      assertNotNull(prop1);
      assertTrue(prop1.isPublic());
      assertEquals(prop1.hashCode(), -1942347255);

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
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/ttClass.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testClassMinSize() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/ClassMinSize.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getTypeInfo().getProperties().size(), 2);
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/compile.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure2() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/AbstractTTCollection.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure3() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/FileTypeRegistry.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testProcedure4() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/_dmpincr.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertFalse(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testInputStreamSkip() throws IOException {
    // Issue #1005:
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/_dmpincr.r"));
        InputStream input2 = new SpecialSkipInputStreamWrapper(input)) {
      RCodeInfo rci = new RCodeInfo(input2);
      assertFalse(rci.isClass());
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV11() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/WebRequestV11.r"))) {
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
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/WebRequestV12.r"))) {
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

      Kryo kryo = getKryo();

      try (OutputStream output = Files.newOutputStream(Paths.get("target/plop.bin"));
          Output data = new Output(output)) {
        kryo.writeObject(data, rci.getTypeInfo());
      } catch (IOException caught) {
        return;
      }

      try (Input input2 = new Input(new FileInputStream("target/plop.bin"));) {
        TypeInfoV12 foo = kryo.readObject(input2, TypeInfoV12.class);
        System.out.println(foo);
      }

    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testV121() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/NMSTrace.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getCrc(), 64163);
      assertNotNull(rci.getTypeInfo());
      assertEquals(rci.getTypeInfo().getProperties().size(), 5);
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testPackageProtected() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/PkgLevelAttr.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getCrc(), 18598);
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
  public void testTempTable() throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get("src/test/resources/rcode/TempTableAttrs.r"))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getCrc(), 56310);
      assertNotNull(rci.getTypeInfo());

      ITableElement tt1 = rci.getTypeInfo().getTempTable("tt1");
      assertNotNull(tt1);
      assertFalse(tt1.isNoUndo());
      ITableElement tt2 = rci.getTypeInfo().getTempTable("tt2");
      assertNotNull(tt2);
      assertTrue(tt2.isNoUndo());
      ITableElement tt3 = rci.getTypeInfo().getTempTable("tt3");
      assertNotNull(tt3);
      assertTrue(tt3.isSerializable());
      ITableElement tt4 = rci.getTypeInfo().getTempTable("tt4");
      assertNotNull(tt4);
      assertTrue(tt4.isNonSerializable());

      Kryo kryo = getKryo();

      try (OutputStream output = Files.newOutputStream(Paths.get("target/plop.bin"));
          Output data = new Output(output)) {
        kryo.writeObject(data, rci.getTypeInfo());
      } catch (IOException caught) {
        return;
      }

      try (Input input2 = new Input(new FileInputStream("target/plop.bin"));) {
        TypeInfoV12 foo = kryo.readObject(input2, TypeInfoV12.class);
        System.out.println(foo);
      }

    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  @Test
  public void testElementsV11() throws IOException {
    testElements("src/test/resources/rcode/TestClassElementsV11.r", 56984, "BBA93318F0B81F7840FA3D45FFE40A35");
    testElements2("src/test/resources/rcode/TestClassElementsChV11.r", 32156, "7945FD8804C9E910211A5E37708143D5");
    testElements3(Paths.get("src/test/resources/rcode/TestClassElementsV11.r"));
    testElements3(Paths.get("src/test/resources/rcode/TestClassElementsChV11.r"));
  }

  @Test
  public void testElementsV12() throws IOException {
    testElements("src/test/resources/rcode/TestClassElementsV12.r", 56984,
        "z0duspqsS+drLa5kEcDQrePMMTqRU6WNHqf7Rq/t6Ao=");
    testElements2("src/test/resources/rcode/TestClassElementsChV12.r", 32156,
        "JIg2azB7KXRZrAfnG2zFYlMiYuqdOCd+LO1MHgr9egg=");
    testElements3(Paths.get("src/test/resources/rcode/TestClassElementsV12.r"));
    testElements3(Paths.get("src/test/resources/rcode/TestClassElementsChV12.r"));
  }

  public void testElements(String fileName, long crc, String digest) throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get(fileName))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getCrc(), crc);
      assertEquals(rci.getDigest(), digest);
      assertNotNull(rci.getTypeInfo());

      assertNotNull(rci.getTypeInfo().getTables());
      assertEquals(rci.getTypeInfo().getTables().size(), 7);
      ITableElement tt1 = rci.getTypeInfo().getTempTable("tt1");
      assertNotNull(tt1);
      assertEquals(tt1.getFields().length, 3); // Always an empty field at the end (ROWID ?)
      assertEquals(tt1.getIndexes().length, 1);
      assertEquals(tt1.hashCode(), -2036140043);
      ITableElement tt2 = rci.getTypeInfo().getTempTable("tt2");
      assertNotNull(tt2);
      assertEquals(tt2.getFields().length, 3);
      assertEquals(tt2.getIndexes().length, 1);
      ITableElement tt3 = rci.getTypeInfo().getTempTable("tt3");
      assertNotNull(tt3);
      assertTrue(tt3.isNoUndo());
      ITableElement tt4 = rci.getTypeInfo().getTempTable("tt4");
      assertNotNull(tt4);
      assertFalse(tt4.isNoUndo());
      ITableElement tt5 = rci.getTypeInfo().getTempTable("tt5");
      assertNotNull(tt5);
      // Information is not available in rcode
      // assertTrue(tt5.isNoUndo());
      ITableElement tt6 = rci.getTypeInfo().getTempTable("tt6");
      assertNotNull(tt6);
      assertFalse(tt6.isNoUndo());
      ITableElement tt7 = rci.getTypeInfo().getTempTable("tt7");
      assertNotNull(tt7);
      // Information is not available in rcode
      // assertTrue(tt7.isNoUndo());

      assertNotNull(rci.getTypeInfo().getDatasets());
      assertEquals(rci.getTypeInfo().getDatasets().size(), 1);
      IDatasetElement ds1 = rci.getTypeInfo().getDataset("ds1");
      assertNotNull(ds1);
      assertTrue(ds1.isProtected());
      assertEquals(ds1.getBufferNames().length, 2);
      assertEquals(ds1.getBufferNames()[0], "tt1");
      assertEquals(ds1.getBufferNames()[1], "tt2");
      assertEquals(ds1.hashCode(), 1623550553);
      assertNull(rci.getTypeInfo().getDataset("ds2"));

      assertNotNull(rci.getTypeInfo().getEvents());
      assertEquals(rci.getTypeInfo().getEvents().size(), 1);
      IEventElement event1 = rci.getTypeInfo().getEvents().iterator().next();
      assertEquals(event1.getName(), "NewCustomer");
      assertEquals(event1.getReturnType(), DataType.VOID);
      assertEquals(event1.getParameters().length, 1);
      assertEquals(event1.getParameters()[0].getDataType(), DataType.CHARACTER);
      assertEquals(event1.hashCode(), 1303426073);

      IMethodElement testMethod = null;
      for (IMethodElement elem : rci.getTypeInfo().getMethods()) {
        if ("testMethod".equalsIgnoreCase(elem.getName()))
          testMethod = elem;
      }
      assertNotNull(testMethod);
      assertEquals(testMethod.getSignature(), "testMethod(IT,OD,II[])");
      assertEquals(testMethod.getIDESignature(), "testMethod(↑TBL tt1, ↓DS ds1, ↑INT[] xx)");
      assertEquals(testMethod.getExtent(), -32767);
      assertEquals(testMethod.getParameters().length, 3);
      assertEquals(testMethod.getParameters()[0].getParameterType(), ParameterType.TABLE);
      assertEquals(testMethod.getParameters()[0].getMode(), ParameterMode.INPUT);
      assertEquals(testMethod.getParameters()[1].getParameterType(), ParameterType.DATASET);
      assertEquals(testMethod.getParameters()[1].getMode(), ParameterMode.OUTPUT);
      assertEquals(testMethod.getParameters()[2].getParameterType(), ParameterType.VARIABLE);
      assertEquals(testMethod.getParameters()[2].getMode(), ParameterMode.INPUT);
      assertEquals(testMethod.getParameters()[2].getDataType(), DataType.INTEGER);
      assertEquals(testMethod.getParameters()[2].getExtent(), 3);
      assertEquals(testMethod.getReturnType().getPrimitive(), PrimitiveDataType.INTEGER);
      assertNull(testMethod.getReturnType().getClassName());
      assertEquals(testMethod.hashCode(), -901218374);

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
      assertEquals(testMethod21.getSignature(), "testMethod2(II)");
      assertEquals(testMethod21.getIDESignature(), "testMethod2(↑INT xx)");
      assertEquals(testMethod22.getSignature(), "testMethod2(II[])");
      assertEquals(testMethod22.getIDESignature(), "testMethod2(↑INT[] xx)");
      assertEquals(testMethod21.getReturnType().getPrimitive(), PrimitiveDataType.INTEGER);
      assertNull(testMethod21.getReturnType().getClassName());
      assertEquals(testMethod22.getReturnType().getPrimitive(), PrimitiveDataType.INTEGER);
      assertNull(testMethod22.getReturnType().getClassName());

      IMethodElement testMethod3 = null;
      for (IMethodElement elem : rci.getTypeInfo().getMethods()) {
        if ("testMethod3".equalsIgnoreCase(elem.getName()))
          testMethod3 = elem;
      }
      assertNotNull(testMethod3);
      assertEquals(testMethod3.getSignature(), "testMethod3(ITH,MDH)");
      assertEquals(testMethod3.getIDESignature(), "testMethod3(↑TBL-HDL htt1, ⇅DS-HDL hds1)");
      assertNotNull(testMethod3.getParameters());
      assertEquals(testMethod3.getParameters().length, 2);
      assertEquals(testMethod3.getParameters()[0].getDataType(), DataType.HANDLE);
      assertEquals(testMethod3.getParameters()[0].getMode(), ParameterMode.INPUT);
      assertEquals(testMethod3.getParameters()[0].getParameterType(), ParameterType.TABLE);
      assertEquals(testMethod3.getParameters()[1].getDataType(), DataType.HANDLE);
      assertEquals(testMethod3.getParameters()[1].getMode(), ParameterMode.INPUT_OUTPUT);
      assertEquals(testMethod3.getParameters()[1].getParameterType(), ParameterType.DATASET);
      assertEquals(testMethod3.getReturnType().getPrimitive(), PrimitiveDataType.INTEGER);
      assertNull(testMethod3.getReturnType().getClassName());

      IMethodElement testMethod4 = null;
      for (IMethodElement elem : rci.getTypeInfo().getMethods()) {
        if ("testMethod4".equalsIgnoreCase(elem.getName()))
          testMethod4 = elem;
      }
      assertNotNull(testMethod4);
      assertEquals(testMethod4.getSignature(),
          "testMethod4(IC,MZProgress.Lang.Object,OD,IDT,IDTZ,ODE,IH,I64,IB,ILC,OM,IRAW,IREC,IROW)");
      assertEquals(testMethod4.getIDESignature(),
          "testMethod4(↑CHAR x1, ⇅Progress.Lang.Object x2, ↓DT x3, ↑DTM x4, ↑DTMZ x5, ↓DEC x6, ↑HDL x7, ↑INT64 x8, ↑LOG x9, ↑CLOB x10, ↓MEMPTR x11, ↑RAW x12, ↑RECID x13, ↑ROWID x14)");
      assertEquals(testMethod4.getIDEInsertElement(false),
          "testMethod4(${1:x1}, input-output ${2:x2}, output ${3:x3}, ${4:x4}, ${5:x5}, output ${6:x6}, ${7:x7}, ${8:x8}, ${9:x9}, ${10:x10}, output ${11:x11}, ${12:x12}, ${13:x13}, ${14:x14})$0");
      assertEquals(testMethod4.getIDEInsertElement(true),
          "testMethod4(${1:x1}, INPUT-OUTPUT ${2:x2}, OUTPUT ${3:x3}, ${4:x4}, ${5:x5}, OUTPUT ${6:x6}, ${7:x7}, ${8:x8}, ${9:x9}, ${10:x10}, OUTPUT ${11:x11}, ${12:x12}, ${13:x13}, ${14:x14})$0");
      assertEquals(testMethod4.getReturnType().getPrimitive(), PrimitiveDataType.INTEGER);
      assertNull(testMethod4.getReturnType().getClassName());

      IMethodElement testMethod5 = null;
      for (IMethodElement elem : rci.getTypeInfo().getMethods()) {
        if ("testMethod5".equalsIgnoreCase(elem.getName()))
          testMethod5 = elem;
      }
      assertNotNull(testMethod5);
      assertEquals(testMethod5.getReturnType().getPrimitive(), PrimitiveDataType.CLASS);
      assertEquals(testMethod5.getReturnType().getClassName(), "Progress.Lang.Object");
    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  public void testElements2(String fileName, long crc, String digest) throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get(fileName))) {
      RCodeInfo rci = new RCodeInfo(input);
      assertTrue(rci.isClass());
      assertEquals(rci.getCrc(), crc);
      assertEquals(rci.getDigest(), digest);
      assertNotNull(rci.getTypeInfo());

      assertNotNull(rci.getTypeInfo().getTables());
      assertEquals(rci.getTypeInfo().getTables().size(), 2);
      ITableElement tt1 = rci.getTypeInfo().getTempTable("chtt1");
      assertFalse(tt1.isNoUndo());
      assertNotNull(tt1);
      ITableElement tt2 = rci.getTypeInfo().getTempTable("chtt2");
      assertNotNull(tt2);
      assertFalse(tt2.isNoUndo());

    } catch (InvalidRCodeException caught) {
      throw new RuntimeException("RCode should be valid", caught);
    }
  }

  public void testElements3(Path fileName) throws IOException {
    Kryo kryo = getKryo();
    RCodeInfo rci = null;
    try (InputStream input = Files.newInputStream(fileName);
        OutputStream output = Files.newOutputStream(
            Paths.get("target/kryo/", fileName.getFileName().toString() + ".bin"));
        Output data = new Output(output)) {
      rci = new RCodeInfo(input);
      kryo.writeClassAndObject(data, rci.getTypeInfo());
    } catch (InvalidRCodeException | IOException caught) {
      throw new RuntimeException("Test failure", caught);
    }

    ITypeInfo serObj = null;
    try (
        InputStream input = Files.newInputStream(Paths.get("target/kryo/", fileName.getFileName().toString() + ".bin"));
        Input input2 = new Input(input)) {
      Object obj = kryo.readClassAndObject(input2);
      assertTrue(obj instanceof ITypeInfo);
      serObj = (ITypeInfo) obj;
    }

    assertEquals(serObj.getProperties().size(), rci.getTypeInfo().getProperties().size());
    assertEquals(serObj.getBuffers().size(), rci.getTypeInfo().getBuffers().size());
    assertEquals(serObj.getDatasets().size(), rci.getTypeInfo().getDatasets().size());
    assertEquals(serObj.getInterfaces().size(), rci.getTypeInfo().getInterfaces().size());
    assertEquals(serObj.getMethods().size(), rci.getTypeInfo().getMethods().size());
    assertEquals(serObj.getTables().size(), rci.getTypeInfo().getTables().size());
    assertEquals(serObj.getVariables().size(), rci.getTypeInfo().getVariables().size());
  }

  @Test
  public void testKryoBuiltinClasses() throws IOException {
    Kryo kryo = getKryo();
    try (OutputStream output = Files.newOutputStream(Paths.get("target/kryo/builtin.bin"));
        Output data = new Output(output)) {
      for (ITypeInfo info : BuiltinClasses.getBuiltinClasses()) {
        kryo.writeClassAndObject(data, info);
      }
    }

    List<ITypeInfo> list = new ArrayList<>();
    try (InputStream input = Files.newInputStream(Paths.get("target/kryo/builtin.bin"));
        Input input2 = new Input(input)) {
      while (input2.available() > 0)  {
        Object obj = kryo.readClassAndObject(input2);
        assertTrue(obj instanceof ITypeInfo);
        list.add((ITypeInfo) obj);
      }
    }

    assertEquals(list.size(), BuiltinClasses.getBuiltinClasses().size());
  }
}
