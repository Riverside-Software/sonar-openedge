package org.sonar.plugins.openedge.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import org.sonar.api.SonarProduct;
import org.sonar.plugins.openedge.api.LicenseRegistration.License;
import org.sonar.plugins.openedge.api.LicenseRegistration.LicenseType;
import org.testng.annotations.Test;

public class LicenseTest {

  @Test
  public void testDefaultValues() throws IOException {
    License lic1 = new License.Builder().setCustomerName("rssw").setPermanentId("123456").build();
    assertNotNull(lic1.getSig());
    assertEquals(lic1.getProduct(), SonarProduct.SONARQUBE);
    assertEquals(lic1.getType(), LicenseType.EVALUATION);
    assertEquals(lic1.getLines(), 0L);
    assertEquals(lic1.getVersion(), 1);
  }

  @Test
  public void testEquals() throws IOException {
    License lic1 = new License.Builder().setCustomerName("rssw").setPermanentId("123456").build();
    License lic2 = new License.Builder().setCustomerName("rssw").setPermanentId("123456").build();
    License lic3 = new License.Builder().setCustomerName("rssw2").setPermanentId("123456").build();
    assertEquals(lic1, lic1);
    assertNotEquals(lic1, null);
    assertEquals(lic1, lic2);
    assertEquals(lic1.hashCode(), lic2.hashCode());
    assertNotEquals(lic1, lic3);
    assertNotEquals(lic1.hashCode(), lic3.hashCode());
  }

  @Test
  public void testEqualsSignature() throws IOException {
    // Signature is not part of equality
    License lic1 = new License.Builder().setCustomerName("rssw").setPermanentId("123456").setSignature(
        new byte[] {1, 2, 3}).build();
    License lic2 = new License.Builder().setCustomerName("rssw").setPermanentId("123456").setSignature(
        new byte[] {4, 5, 6}).build();
    assertEquals(lic1, lic2);
    assertEquals(lic1.hashCode(), lic2.hashCode());
  }

}
