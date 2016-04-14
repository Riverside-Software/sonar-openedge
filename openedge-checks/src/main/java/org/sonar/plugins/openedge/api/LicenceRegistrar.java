package org.sonar.plugins.openedge.api;

import java.util.Date;

import org.sonar.api.batch.BatchSide;

@BatchSide
public interface LicenceRegistrar {
  
  /**
   * This method is called during an analysis to get the OpenEdge rules licenses
   * 
   * @param licenceContext The licence itself
   * @see https://github.com/SonarSource/sonar-java/blob/master/java-squid/src/main/java/org/sonar/plugins/java/api/CheckRegistrar.java
   */
  void register(Licence registrarContext);

  public class Licence {
    private String customerName;
    private String repositoryName;
    private Date expirationDate;
    private String salt;
    private byte[] signature;

    public void registerLicence(String customerName, String salt, String repoName, byte[] licence, Date expirationDate) {
      this.customerName = customerName;
      this.repositoryName = repoName;
      this.salt = salt;
      this.signature = licence;
      this.expirationDate = expirationDate;
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

    public Date getExpirationDate() {
      return expirationDate;
    }

    public String getSalt() {
      return salt;
    }
  }
}
