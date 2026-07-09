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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.SonarProduct;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.openedge.api.License;
import org.sonar.plugins.openedge.api.LicenseProvider;
import org.sonar.plugins.openedge.api.LicenseType;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
@ServerSide
public class LicenseRegistrar {
  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseRegistrar.class);

  private final List<License> licenses = new ArrayList<>();

  public LicenseRegistrar(LicenseProvider... providers) {
    LOGGER.debug("LicenseRegistrar created with {} providers", providers.length);
    for (var provider : providers) {
      for (var lic : provider.getLicenses()) {
        registerLicense(lic);
      }
    }
  }

  public Collection<License> getLicenses() {
    return Collections.unmodifiableList(licenses);
  }

  public License getLicense(SonarProduct product, String permId, String repoName) {
    if ((permId == null) || (repoName == null))
      return null;

    Optional<License> srch = licenses.stream() //
      .filter(lic -> (lic.getType() == LicenseType.COMMERCIAL) || (lic.getType() == LicenseType.PARTNER)) //
      .filter(lic -> lic.getProduct() == product) //
      .filter(lic -> repoName.equals(lic.getRepositoryName())) //
      .filter(lic -> permId.equals(lic.getPermanentId())) //
      .findFirst();
    if (srch.isPresent())
      return srch.get();
    srch = licenses.stream() //
      .filter(lic -> lic.getType() == LicenseType.EVALUATION) //
      .filter(lic -> lic.getProduct() == product) //
      .filter(lic -> repoName.equals(lic.getRepositoryName())) //
      .filter(lic -> permId.equals(lic.getPermanentId())) //
      .findFirst();
    if (srch.isPresent())
      return srch.get();

    return null;
  }

  private void registerLicense(@Nonnull License license) {
    if (LOGGER.isDebugEnabled())
      LOGGER.debug("Found {} license - Permanent ID '{}' - Customer '{}' - Repository '{}' - Expiration date {}",
          license.getType(), license.getPermanentId(), license.getCustomerName(), license.getRepositoryName(),
          LocalDateTime.ofEpochSecond(license.getExpirationDate() / 1_000, 0, ZoneOffset.UTC).format(
              DateTimeFormatter.ISO_LOCAL_DATE_TIME));

    // Only one license per product/ repository / permID
    var existingLic = hasRegisteredLicense(license.getProduct(), license.getRepositoryName(), license.getPermanentId());
    if (existingLic == null) {
      licenses.add(license);
    } else if (existingLic.getExpirationDate() < license.getExpirationDate()) {
      licenses.remove(existingLic);
      licenses.add(license);
    }
  }

  private License hasRegisteredLicense(SonarProduct product, String repoName, String permId) {
    if ((permId == null) || (repoName == null))
      return null;

    return licenses.stream() //
      .filter(lic -> lic.getProduct() == product) //
      .filter(lic -> (lic.getType() == LicenseType.COMMERCIAL) || (lic.getType() == LicenseType.PARTNER)) //
      .filter(lic -> permId.equals(lic.getPermanentId())) //
      .filter(lic -> repoName.equals(lic.getRepositoryName())) //
      .findFirst() //
      .orElse(null);
  }

}
