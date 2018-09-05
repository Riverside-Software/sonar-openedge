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
