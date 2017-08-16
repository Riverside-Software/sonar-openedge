/*
 * RCode library - OpenEdge plugin for SonarQube
 * Copyright (C) 2017 Riverside Software
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
