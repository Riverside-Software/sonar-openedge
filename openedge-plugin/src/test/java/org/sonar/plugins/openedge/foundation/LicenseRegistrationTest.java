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
package org.sonar.plugins.openedge.foundation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.List;

import org.sonar.api.SonarProduct;
import org.sonar.plugins.openedge.api.License;
import org.sonar.plugins.openedge.api.LicenseType;
import org.testng.annotations.Test;

public class LicenseRegistrationTest {
  @Test
  public void testNoLicenses() {
    var registrar = new LicenseRegistrar();
    assertEquals(registrar.getLicenses().size(), 0);
  }

  @Test
  public void testNewLicenses() {
    var lic01 = new License.Builder() //
      .setVersion(3) //
      .setPermanentId("987654321") //
      .setProduct(SonarProduct.SONARLINT) //
      .setCustomerName("You") //
      .setSalt("salt") //
      .setRepositoryName("rssw-oe-main") //
      .setType(LicenseType.get(1)) // Commercial
      .setSignature(new byte[] {}) //
      .setExpirationDate(1735693261000L) //
      .setLines(1000L) //
      .build();
    var registrar = new LicenseRegistrar(new LicenseRegistrar.LicenseProvider() {
      public List<License> getLicenses() {
        return List.of(lic01);
      }
    });

    assertEquals(registrar.getLicenses().size(), 1);
    var iter = registrar.getLicenses().iterator();
    var lic1 = iter.next();
    assertEquals(lic1.getProduct(), SonarProduct.SONARLINT);
    assertEquals(lic1.getPermanentId(), "987654321");
    assertEquals(lic1.getLines(), 1000L);
  }

  @Test
  public void testTwoLicenses01() {
    var lic01 = new License.Builder() //
      .setVersion(3) //
      .setPermanentId("987654321") //
      .setProduct(SonarProduct.SONARLINT) //
      .setCustomerName("You") //
      .setSalt("salt") //
      .setRepositoryName("rssw-oe-main") //
      .setType(LicenseType.get(1)) // Commercial
      .setSignature(new byte[] {}) //
      .setExpirationDate(1735693261000L) //
      .setLines(1000L) //
      .build();
    var lic02 = new License.Builder() //
      .setVersion(3) //
      .setPermanentId("987654321") //
      .setProduct(SonarProduct.SONARLINT) //
      .setCustomerName("You") //
      .setSalt("salt") //
      .setRepositoryName("rssw-oe-main") //
      .setType(LicenseType.COMMERCIAL) //
      .setSignature(new byte[] {}) //
      .setExpirationDate(2735693261000L) //
      .setLines(2000L) //
      .build();
    var registrar = new LicenseRegistrar(new LicenseRegistrar.LicenseProvider() {
      public List<License> getLicenses() {
        return List.of(lic01, lic02);
      }
    });

    assertEquals(registrar.getLicenses().size(), 1);
    var iter = registrar.getLicenses().iterator();
    var lic1 = iter.next();
    assertEquals(lic1, lic02);
    assertEquals(lic1.getProduct(), SonarProduct.SONARLINT);
    assertEquals(lic1.getPermanentId(), "987654321");
    assertEquals(lic1.getLines(), 2000L);
  }

  @Test
  public void testTwoLicenses02() {
    var lic01 = new License.Builder() //
      .setVersion(3) //
      .setPermanentId("987654321") //
      .setProduct(SonarProduct.SONARLINT) //
      .setCustomerName("You") //
      .setSalt("salt") //
      .setRepositoryName("rssw-oe-main") //
      .setType(LicenseType.get(1)) // Commercial
      .setSignature(new byte[] {}) //
      .setExpirationDate(1735693261000L) //
      .setLines(1000L) //
      .build();
    var lic02 = new License.Builder() //
      .setVersion(3) //
      .setPermanentId("987654321") //
      .setProduct(SonarProduct.SONARLINT) //
      .setCustomerName("You") //
      .setSalt("salt") //
      .setRepositoryName("rssw-oe-main") //
      .setType(LicenseType.COMMERCIAL) //
      .setSignature(new byte[] {}) //
      .setExpirationDate(2735693261000L) //
      .setLines(2000L) //
      .build();
    var registrar = new LicenseRegistrar(new LicenseRegistrar.LicenseProvider() {
      public List<License> getLicenses() {
        return List.of(lic02, lic01);
      }
    });

    assertEquals(registrar.getLicenses().size(), 1);
    var iter = registrar.getLicenses().iterator();
    var lic1 = iter.next();
    assertEquals(lic1, lic02);
    assertEquals(lic1.getProduct(), SonarProduct.SONARLINT);
    assertEquals(lic1.getPermanentId(), "987654321");
    assertEquals(lic1.getLines(), 2000L);

    assertNull(registrar.getLicense(SonarProduct.SONARLINT, null, "rssw-oe-main"));
    assertNull(registrar.getLicense(SonarProduct.SONARLINT, "987654321", null));
    assertEquals(registrar.getLicense(SonarProduct.SONARLINT, "987654321", "rssw-oe-main"), lic02);
  }

  @Test
  public void testTwoLicenses03() {
    var lic01 = new License.Builder() //
      .setVersion(3) //
      .setPermanentId("987654321") //
      .setProduct(SonarProduct.SONARQUBE) //
      .setCustomerName("You") //
      .setSalt("salt") //
      .setRepositoryName("rssw-oe-main") //
      .setType(LicenseType.EVALUATION) //
      .setSignature(new byte[] {}) //
      .setExpirationDate(1735693261000L) //
      .setLines(1000L) //
      .build();
    var lic02 = new License.Builder() //
      .setVersion(3) //
      .setPermanentId("987654321") //
      .setProduct(SonarProduct.SONARQUBE) //
      .setCustomerName("You") //
      .setSalt("salt") //
      .setRepositoryName("rssw-oe-main") //
      .setType(LicenseType.COMMERCIAL) //
      .setSignature(new byte[] {}) //
      .setExpirationDate(2735693261000L) //
      .setLines(2000L) //
      .build();
    var registrar = new LicenseRegistrar(new LicenseRegistrar.LicenseProvider() {
      public List<License> getLicenses() {
        return List.of(lic01, lic02);
      }
    });

    assertEquals(registrar.getLicense(SonarProduct.SONARQUBE, "987654321", "rssw-oe-main"), lic02);
  }
}
