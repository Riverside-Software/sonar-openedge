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
package org.sonar.plugins.openedge.foundation;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.sonar.api.SonarProduct;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.api.CheckRegistration;
import org.sonar.plugins.openedge.api.LicenseRegistration;
import org.testng.annotations.Test;

public class OpenEdgeComponentsTest {

  @Test
  public void testNoLicenses() throws IOException {
    OpenEdgeComponents components1 = new OpenEdgeComponents(OpenEdgePluginTest.SERVER);
    assertEquals(components1.getLicenses().size(), 0);
    OpenEdgeComponents components2 = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {});
    assertEquals(components2.getLicenses().size(), 0);
    OpenEdgeComponents components3 = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {});
    assertEquals(components3.getLicenses().size(), 0);
  }

  @Test
  public void testNoServer() throws IOException {
    OpenEdgeComponents components1 = new OpenEdgeComponents();
    assertEquals(components1.getServerId(), "");
    assertEquals(components1.getLicenses().size(), 0);
    OpenEdgeComponents components2 = new OpenEdgeComponents(new CheckRegistration[] {});
    assertEquals(components2.getServerId(), "");
    assertEquals(components2.getLicenses().size(), 0);
    OpenEdgeComponents components3 = new OpenEdgeComponents(new CheckRegistration[] {}, new LicenseRegistration[] {});
    assertEquals(components3.getServerId(), "");
    assertEquals(components3.getLicenses().size(), 0);
  }

  @Test
  public void testOneLicense() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {LICENSE_ME_OE_2015});
    assertEquals(components.getLicenses().size(), 1);
  }

  @Test
  public void testTwoLicenses() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {LICENSE_ME_OE_2015, LICENSE_ME_OE_2025});
    assertEquals(components.getLicenses().size(), 1);
  }

  @Test
  public void testTwoPermId() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {LICENSE_ME_OE_2015, LICENSE_YOU_OE_2025});
    assertEquals(components.getLicenses().size(), 2);
  }

  @Test
  public void testTwoRepositories() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {LICENSE_ME_OE_2015, LICENSE_YOU_OTHER_2015});
    assertEquals(components.getLicenses().size(), 2);
  }

  @Test
  public void testThreeLicenses() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {LICENSE_ME_OE_2015, LICENSE_ME_OE_2030, LICENSE_ME_OE_2025});
    assertEquals(components.getLicenses().size(), 1);
  }

  @Test
  public void testMixedLicenses() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {
            LICENSE_ME_OE_2015, LICENSE_ME_OE_2030, LICENSE_ME_OE_2025, LICENSE_YOU_OTHER_2015, LICENSE_YOU_OE_2015,
            LICENSE_YOU_OE_2015});
    assertEquals(components.getLicenses().size(), 3);
  }

  @Test
  public void testSLPlusSQLicenses() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, new CheckRegistration[] {},
        new LicenseRegistration[] {LICENSE_ME_OE_2025, LICENSE_SL_ME_OE_2025, LICENSE_SL_YOU_OE_2025});
    assertEquals(components.getLicenses().size(), 3);
  }

  private final static LicenseRegistration LICENSE_ME_OE_2015 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("123456789", SonarProduct.SONARQUBE, "Me", "salt", "rssw-oe-main",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1420074061000L);
    }
  };
  private final static LicenseRegistration LICENSE_YOU_OE_2015 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("987654321", SonarProduct.SONARQUBE, "You", "salt", "rssw-oe-main",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1420074061000L);
    }
  };
  private final static LicenseRegistration LICENSE_YOU_OTHER_2015 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("987654321", SonarProduct.SONARQUBE, "You", "salt", "other-repo",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1420074061000L);
    }
  };
  private final static LicenseRegistration LICENSE_ME_OE_2025 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("123456789", SonarProduct.SONARQUBE, "Me", "salt", "rssw-oe-main",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1735693261000L);
    }
  };
  private final static LicenseRegistration LICENSE_YOU_OE_2025 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("987654321", SonarProduct.SONARQUBE, "You", "salt", "rssw-oe-main",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1735693261000L);
    }
  };
  private final static LicenseRegistration LICENSE_ME_OE_2030 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("123456789", SonarProduct.SONARQUBE, "Me", "salt", "rssw-oe-main",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1893459661000L);
    }
  };
  private final static LicenseRegistration LICENSE_SL_ME_OE_2025 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("123456789", SonarProduct.SONARLINT, "Me", "salt", "rssw-oe-main",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1735693261000L);
    }
  };
  private final static LicenseRegistration LICENSE_SL_YOU_OE_2025 = new LicenseRegistration() {
    @Override
    public void register(Registrar context) {
      context.registerLicense("987654321", SonarProduct.SONARLINT, "You", "salt", "rssw-oe-main",
          LicenseRegistration.LicenseType.COMMERCIAL, new byte[] {}, 1735693261000L);
    }
  };
}
