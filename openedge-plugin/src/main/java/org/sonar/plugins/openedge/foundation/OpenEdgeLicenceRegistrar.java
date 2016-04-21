/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2015 Riverside Software
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.openedge.api.LicenceRegistrar;

public class OpenEdgeLicenceRegistrar implements LicenceRegistrar {
  private static final Logger LOGGER = LoggerFactory.getLogger(OpenEdgeLicenceRegistrar.class);

  /**
   * Register the classes that will be used to instantiate checks during analysis.
   */
  @Override
  public void register(Licence registrarContext) {
    LOGGER.debug("BETA PERIOD - Adding licence {}", OpenEdgeLicenceRegistrar.class.toString());
    registrarContext.registerLicence("", "To infinity... and beyond", "omqzlrfvws;nfs", "rssw-oe-main",
        LicenceRegistrar.LicenceType.COMMERCIAL,
        new byte[] {
            -125, -7, -57, 52, 18, -45, 103, -92, -17, 59, -54, 122, 111, 48, 12, -75, -88, 6, -18, -96, 109, -77, -86,
            -19, 62, -52, -28, -18, -23, 123, -88, -31, 88, 7, 116, 5, -60, -60, 17, -27, -87, 107, 25, -23, 89, 45,
            -121, 107, -94, 97, -78, -34, 113, -86, -21, 102, 78, -34, 52, -68, 22, -45, -42, -67, 64, 85, 91, -20, 85,
            44, 13, -104, 92, 49, 107, -52, 15, 0, 35, -18, -27, -98, -15, 99, 12, 69, -44, -42, -68, 2, 121, -6, 8,
            -94, 56, 98, 77, -125, -26, 5, 71, -71, 1, 41, -34, -45, -20, 119, 58, 84, -77, 95, 45, 83, -16, 16, -62,
            -45, -102, 111, -103, 59, -50, -84, -42, -19, 23, 44, -122, 89, 1, -112, 61, -61, 121, 9, -73, 68, -98, 5,
            6, 32, 12, 125, 121, 104, -111, -95, -96, 88, 103, -71, -97, 50, 106, -87, 107, -28, -12, -20, -28, 67, 27,
            111, 96, -3, 114, 34, -36, -70, 20, -8, -113, 127, 58, -11, 78, -120, -104, 29, 65, -38, -20, -27, 70, 107,
            15, -97, -101, 102, -88, -41, 116, -56, -44, 37, 78, 94, -98, -125, 67, 103, -19, 43, 56, 24, 79, 29, -41,
            7, 108, 88, 112, 119, -52, -25, 87, -99, 58, 124, 12, 59, -14, -128, -47, -12, 73, -95, -99, 56, 83, 53,
            -87, 12, -128, 21, -30, 38, -120, 80, 74, -85, 38, -34, -28, -104, 118, 73, 79, 118, -85, 35, 127, 100, 78,
            63},
        1467324028000L);
  }
}
