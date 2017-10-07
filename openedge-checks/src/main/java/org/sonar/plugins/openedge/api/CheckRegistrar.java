package org.sonar.plugins.openedge.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.ScannerSide;
import org.sonar.plugins.openedge.api.checks.OpenEdgeDumpFileCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
public interface CheckRegistrar {

  /**
   * This method is called during an analysis to get the classes to use to instantiate checks. Based on the java-squid
   * plugin
   * 
   * @param registrarContext the context that will be used by the openedgedb-plugin to retrieve the classes for checks.
   * @see https://github.com/SonarSource/sonar-java/blob/master/java-squid/src/main/java/org/sonar/plugins/java/api/
   *      CheckRegistrar.java
   */
  void register(RegistrarContext registrarContext);

  class RegistrarContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRegistrar.RegistrarContext.class);

    private Iterable<Class<? extends OpenEdgeProparseCheck>> proparseCheckClasses;
    private Iterable<Class<? extends OpenEdgeDumpFileCheck>> dbCheckClasses;

    public void registerClassesForRepository(String repositoryKey,
        Iterable<Class<? extends OpenEdgeProparseCheck>> proparseChecks,
        Iterable<Class<? extends OpenEdgeDumpFileCheck>> dbChecks) {
      LOGGER.debug("Registering class for repository {}", repositoryKey);
      this.proparseCheckClasses = proparseChecks;
      this.dbCheckClasses = dbChecks;
    }

    public Iterable<Class<? extends OpenEdgeDumpFileCheck>> getDbCheckClasses() {
      return dbCheckClasses;
    }

    public Iterable<Class<? extends OpenEdgeProparseCheck>> getProparseCheckClasses() {
      return proparseCheckClasses;
    }
  }
}
