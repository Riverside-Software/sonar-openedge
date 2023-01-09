/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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

import java.util.Objects;

import org.sonar.api.SonarProduct;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

import com.google.common.base.Strings;

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
  default void register(Registrar registrar) {
  }

  interface Registrar {
    /**
     * Register customer license for a given permanentID and rules repository
     */
    @Deprecated
    default void registerLicense(String permanentId, String customerName, String salt, String repoName,
        LicenseType type, byte[] signature, long expirationDate) {
      registerLicense(1, permanentId.replace("sonarlint-", ""),
          permanentId.startsWith("sonarlint") ? SonarProduct.SONARLINT : SonarProduct.SONARQUBE, customerName, salt,
          repoName, type, signature, expirationDate, 0);
    }

    @Deprecated
    default void registerLicense(String permanentId, SonarProduct product, String customerName, String salt,
        String repoName, LicenseType type, byte[] signature, long expirationDate) {
      registerLicense(1, permanentId, product, customerName, salt, repoName, type, signature, expirationDate, 0);
    }

    public void registerLicense(int version, String permanentId, SonarProduct product, String customerName, String salt,
        String repoName, LicenseType type, byte[] signature, long expirationDate, long lines);
  }

  public class License {
    private int version;
    private String permanentId;
    private SonarProduct product;
    private String customerName;
    private String repositoryName;
    private LicenseType type;
    private long expirationDate;
    private long lines;
    private String salt;
    private byte[] signature;

    private License() {
      // Use Builder pattern
    }

    public int getVersion() {
      return version;
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

    public long getLines() {
      return lines;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (obj == null)
        return false;
      if (this.getClass() == obj.getClass()) {
        License other = (License) obj;

        return customerName.equals(other.customerName) && (product == other.product)
            && permanentId.equals(other.permanentId) && repositoryName.equals(other.repositoryName)
            && (expirationDate == other.expirationDate) && (salt.equals(other.salt)) && (type == other.type)
            && (lines == other.lines);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(customerName, product, permanentId, repositoryName, expirationDate, salt, type, lines);
    }

    public static class Builder {
      private int version = 1;
      private String permanentId = "";
      private String customerName = "";
      private String repositoryName = "";
      private String salt= "";
      private SonarProduct product;
      private LicenseType type;
      private long expirationDate;
      private long lines;
      private byte[] signature;

      public Builder setVersion(int version) {
        this.version = version;
        return this;
      }

      public Builder setPermanentId(String permanentId) {
        this.permanentId = Strings.nullToEmpty(permanentId);
        return this;
      }

      public Builder setCustomerName(String customerName) {
        this.customerName = Strings.nullToEmpty(customerName);
        return this;
      }

      public Builder setRepositoryName(String repositoryName) {
        this.repositoryName = Strings.nullToEmpty(repositoryName);
        return this;
      }

      public Builder setSalt(String salt) {
        this.salt = Strings.nullToEmpty(salt);
        return this;
      }

      public Builder setProduct(SonarProduct product) {
        this.product = product;
        return this;
      }

      public Builder setType(LicenseType type) {
        this.type = type;
        return this;
      }

      public Builder setExpirationDate(long expirationDate) {
        this.expirationDate = expirationDate;
        return this;
      }

      public Builder setLines(long lines) {
        this.lines = lines;
        return this;
      }

      public Builder setSignature(byte[] signature) {
        this.signature = (signature == null) || (signature.length != 256) ? new byte[0] : signature;
        return this;
      }

      public License build() {
        License license = new License();
        license.version = version;
        license.permanentId = permanentId;
        license.customerName = customerName;
        license.repositoryName = repositoryName;
        license.salt = salt;
        license.product = product == null ? SonarProduct.SONARQUBE : product;
        license.type = type == null ? LicenseType.EVALUATION : type;
        license.expirationDate = expirationDate;
        license.lines = lines;
        license.signature = signature == null ? new byte[0] : signature;

        return license;
      }
    }
  }

  public enum LicenseType {
    EVALUATION,
    COMMERCIAL,
    PARTNER;
  }
}
