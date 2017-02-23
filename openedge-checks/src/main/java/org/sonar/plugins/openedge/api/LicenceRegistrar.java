package org.sonar.plugins.openedge.api;

import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.ScannerSide;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
@BatchSide
public interface LicenceRegistrar {

  /**
   * This method is called during an analysis to register a licence
   * 
   * @param licenceContext The licence itself
   * @see https://github.com/SonarSource/sonar-java/blob/master/java-squid/src/main/java/org/sonar/plugins/java/api/
   *      CheckRegistrar.java
   */
  void register(Licence registrarContext);

  public class Licence {
    private String permanentId;
    private String customerName;
    private String repositoryName;
    private LicenceType type;
    private long expirationDate;
    private String salt;
    private byte[] signature;

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
