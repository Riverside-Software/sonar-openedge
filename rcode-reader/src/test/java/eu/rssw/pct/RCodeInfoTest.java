package eu.rssw.pct;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;

import org.testng.annotations.Test;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

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

}
