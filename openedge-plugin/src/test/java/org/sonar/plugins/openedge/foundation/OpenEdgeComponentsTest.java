/**
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2016 Riverside Software
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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.sonar.plugins.openedge.api.CheckRegistrar;
import org.sonar.plugins.openedge.api.LicenceRegistrar;
import org.testng.annotations.Test;

public class OpenEdgeComponentsTest {
  @Test
  public void testNoLicences() throws IOException {
    OpenEdgeComponents components1 = new OpenEdgeComponents();
    assertThat(components1.getLicences()).hasSize(0);
    OpenEdgeComponents components2 = new OpenEdgeComponents(new LicenceRegistrar[] {});
    assertThat(components2.getLicences()).hasSize(0);
    OpenEdgeComponents components3 = new OpenEdgeComponents(new CheckRegistrar[] {}, new LicenceRegistrar[] {});
    assertThat(components3.getLicences()).hasSize(0);
  }

  @Test
  public void testOneLicence() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(new LicenceRegistrar[] {LICENCE_ME_OE_2015});
    assertThat(components.getLicences()).hasSize(1);
  }

  @Test
  public void testTwoLicences() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(
        new LicenceRegistrar[] {LICENCE_ME_OE_2015, LICENCE_ME_OE_2025});
    assertThat(components.getLicences()).hasSize(1);
  }

  @Test
  public void testTwoPermId() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(
        new LicenceRegistrar[] {LICENCE_ME_OE_2015, LICENCE_YOU_OE_2025});
    assertThat(components.getLicences()).hasSize(2);
  }

  @Test
  public void testTwoRepositories() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(
        new LicenceRegistrar[] {LICENCE_ME_OE_2015, LICENCE_YOU_OTHER_2015});
    assertThat(components.getLicences()).hasSize(2);
  }

  @Test
  public void testThreeLicences() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(
        new LicenceRegistrar[] {LICENCE_ME_OE_2015, LICENCE_ME_OE_2030, LICENCE_ME_OE_2025});
    assertThat(components.getLicences()).hasSize(1);
  }

  @Test
  public void testMixedLicences() throws IOException {
    OpenEdgeComponents components = new OpenEdgeComponents(new LicenceRegistrar[] {
        LICENCE_ME_OE_2015, LICENCE_ME_OE_2030, LICENCE_ME_OE_2025, LICENCE_YOU_OTHER_2015, LICENCE_YOU_OE_2015,
        LICENCE_YOU_OE_2015});
    assertThat(components.getLicences()).hasSize(3);
  }

  private final static LicenceRegistrar LICENCE_ME_OE_2015 = new LicenceRegistrar() {
    @Override
    public void register(Licence context) {
      context.registerLicence("123456789", "Me", "salt", "rssw-oe-main", LicenceRegistrar.LicenceType.COMMERCIAL,
          new byte[] {}, 1420074061000L);
    }
  };
  private final static LicenceRegistrar LICENCE_YOU_OE_2015 = new LicenceRegistrar() {
    @Override
    public void register(Licence context) {
      context.registerLicence("987654321", "You", "salt", "rssw-oe-main", LicenceRegistrar.LicenceType.COMMERCIAL,
          new byte[] {}, 1420074061000L);
    }
  };
  private final static LicenceRegistrar LICENCE_YOU_OTHER_2015 = new LicenceRegistrar() {
    @Override
    public void register(Licence context) {
      context.registerLicence("987654321", "You", "salt", "other-repo", LicenceRegistrar.LicenceType.COMMERCIAL,
          new byte[] {}, 1420074061000L);
    }
  };
  private final static LicenceRegistrar LICENCE_ME_OE_2025 = new LicenceRegistrar() {
    @Override
    public void register(Licence context) {
      context.registerLicence("123456789", "Me", "salt", "rssw-oe-main", LicenceRegistrar.LicenceType.COMMERCIAL,
          new byte[] {}, 1735693261000L);
    }
  };
  private final static LicenceRegistrar LICENCE_YOU_OE_2025 = new LicenceRegistrar() {
    @Override
    public void register(Licence context) {
      context.registerLicence("987654321", "You", "salt", "rssw-oe-main", LicenceRegistrar.LicenceType.COMMERCIAL,
          new byte[] {}, 1735693261000L);
    }
  };
  private final static LicenceRegistrar LICENCE_ME_OE_2030 = new LicenceRegistrar() {
    @Override
    public void register(Licence context) {
      context.registerLicence("123456789", "Me", "salt", "rssw-oe-main", LicenceRegistrar.LicenceType.COMMERCIAL,
          new byte[] {}, 1893459661000L);
    }
  };
}
