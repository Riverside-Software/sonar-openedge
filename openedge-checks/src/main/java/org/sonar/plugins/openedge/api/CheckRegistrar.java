package org.sonar.plugins.openedge.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.checks.IXrefAnalyzer;

@BatchSide
public interface CheckRegistrar {
  
  /**
   * This method is called during an analysis to get the classes to use to instantiate checks. Based on the java-squid plugin
   * 
   * @param registrarContext the context that will be used by the openedgedb-plugin to retrieve the classes for checks.
   * @see https://github.com/SonarSource/sonar-java/blob/master/java-squid/src/main/java/org/sonar/plugins/java/api/CheckRegistrar.java
   */
  void register(RegistrarContext registrarContext);

  class RegistrarContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckRegistrar.RegistrarContext.class);

    private String repositoryKey;
    private Iterable<Class<? extends IXrefAnalyzer>> xrefCheckClasses;
    private Iterable<Class<? extends AbstractLintRule>> proparseCheckClasses;
    
    public void registerClassesForRepository(String repositoryKey, Iterable<Class<? extends IXrefAnalyzer>> xrefCheckClasses, Iterable<Class<? extends AbstractLintRule>> proparseChecks) {
      LOGGER.debug("Registering class for repository {}", repositoryKey);
      this.repositoryKey = repositoryKey;
      this.xrefCheckClasses = xrefCheckClasses;
      this.proparseCheckClasses = proparseChecks;
    }

    public String repositoryKey() {
      return repositoryKey;
    }

    public Iterable<Class<? extends IXrefAnalyzer>> getXrefCheckClasses() {
      return xrefCheckClasses;
    }

    public Iterable<Class<? extends AbstractLintRule>> getProparseCheckClasses() {
      return proparseCheckClasses;
    }
  }
}
