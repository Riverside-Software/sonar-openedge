/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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

import org.sonar.api.SonarProduct;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Implement this interface to register licenses
 */
@ServerSide
@ScannerSide
@SonarLintSide
public interface LicenseRegistration {

  /**
   * Register set of licenses
   */
  default void register(Registrar registrar) { }

  interface Registrar {
    /**
     * Register customer license for a given permanentID and rules repository  
     */
    @Deprecated
    public void registerLicense(String permanentId, String customerName, String salt, String repoName, LicenseType type,
        byte[] signature, long expirationDate);

    public void registerLicense(String permanentId, SonarProduct product, String customerName, String salt,
        String repoName, LicenseType type, byte[] signature, long expirationDate);
  }

  public class License {
    private String permanentId;
    private SonarProduct product;
    private String customerName;
    private String repositoryName;
    private LicenseType type;
    private long expirationDate;
    private String salt;
    private byte[] signature;

    public License(String permanentId, SonarProduct product, String customerName, String salt, String repoName,
        LicenseType type, byte[] signature, long expirationDate) {
      this.permanentId = permanentId;
      this.product = product;
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

    public SonarProduct getProduct() {
      return product;
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
