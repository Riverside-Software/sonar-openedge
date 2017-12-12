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
public interface LicenceRegistrar {

  @Deprecated
  default void register(Licence license) { }

  /**
   * Register set of licenses
   * @param context
   */
  default void register(LicenseContext context) { }

  public class LicenseContext {
    private Collection<Licence> licenses = new ArrayList<>();

    public final Iterable<Licence> getLicenses() {
      return licenses;
    }

    public void registerLicence(String permanentId, String customerName, String salt, String repoName, LicenceType type,
        byte[] signature, long expirationDate) {
      licenses.add(new Licence(permanentId, customerName, salt, repoName, type, signature, expirationDate));
    }
  }

  public class Licence {
    private String permanentId;
    private String customerName;
    private String repositoryName;
    private LicenceType type;
    private long expirationDate;
    private String salt;
    private byte[] signature;

    @Deprecated
    public Licence() {
      // For legacy licenses
    }

    public Licence(String permanentId, String customerName, String salt, String repoName, LicenceType type,
        byte[] signature, long expirationDate) {
      this.permanentId = permanentId;
      this.customerName = customerName;
      this.repositoryName = repoName;
      this.salt = salt;
      this.type = type;
      this.signature = signature;
      this.expirationDate = expirationDate;
    }

    @Deprecated
    public void registerLicence(String permanentId, String customerName, String salt, String repoName, LicenceType type,
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

    public LicenceType getType() {
      return type;
    }
  }

  public enum LicenceType {
    EVALUATION, COMMERCIAL;
  }
}
