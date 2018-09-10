/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package org.sonar.plugins.openedge.api;

import java.util.ArrayList;
import java.util.Collection;

import org.sonar.api.batch.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Implement this interface to add OpenEdge rules licenses
 */
@ScannerSide
@SonarLintSide
@ServerSide
public interface LicenseRegistrar {

  /**
   * Register set of licenses
   * @param context
   */
  default void register(LicenseContext context) { }

  public class LicenseContext {
    private Collection<License> licenses = new ArrayList<>();

    public final Iterable<License> getLicenses() {
      return licenses;
    }

    public void registerLicense(String permanentId, String customerName, String salt, String repoName, LicenseType type,
        byte[] signature, long expirationDate) {
      licenses.add(new License(permanentId, customerName, salt, repoName, type, signature, expirationDate));
    }
  }

  public class License {
    private String permanentId;
    private String customerName;
    private String repositoryName;
    private LicenseType type;
    private long expirationDate;
    private String salt;
    private byte[] signature;

    public License() {
      
    }

    public License(String permanentId, String customerName, String salt, String repoName, LicenseType type,
        byte[] signature, long expirationDate) {
      this.permanentId = permanentId;
      this.customerName = customerName;
      this.repositoryName = repoName;
      this.salt = salt;
      this.type = type;
      this.signature = signature;
      this.expirationDate = expirationDate;
    }

    public String getPermanentId() {
      return permanentId;
    }

    public String getCustomerName() {
      return customerName;
    }

    public String getRepositoryName() {
      return repositoryName;
    }

    public byte[] getSig() {
      return signature;
    }

    public long getExpirationDate() {
      return expirationDate;
    }

    public String getSalt() {
      return salt;
    }

    public LicenseType getType() {
      return type;
    }
  }

  public enum LicenseType {
    EVALUATION, COMMERCIAL;
  }
}
