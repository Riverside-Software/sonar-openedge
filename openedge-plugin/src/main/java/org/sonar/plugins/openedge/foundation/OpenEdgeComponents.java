/*
 * OpenEdge DB plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.plugins.openedge.api.CheckRegistrar;
import org.sonar.plugins.openedge.api.checks.AbstractLintRule;
import org.sonar.plugins.openedge.api.checks.IXrefAnalyzer;

@BatchSide
public class OpenEdgeComponents {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeComponents.class);

  private final List<Class<? extends IXrefAnalyzer>> checks = new ArrayList<>();
  private final List<Class<? extends AbstractLintRule>> ppchecks = new ArrayList<>();

  public OpenEdgeComponents(CheckRegistrar[] registrars) {
    if (registrars != null) {
      CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
      for (CheckRegistrar reg : registrars) {
        reg.register(registrarContext);
        for (Class<? extends IXrefAnalyzer> analyzer : registrarContext.getXrefCheckClasses()) {
          LOG.debug("{} XREF analyzer registered", analyzer.getName());
          checks.add(analyzer);
        }
        for (Class<? extends AbstractLintRule> analyzer : registrarContext.getProparseCheckClasses()) {
          LOG.debug("{} Proparse analyzer registered", analyzer.getName());
          ppchecks.add(analyzer);
        }
      }
    }
  }

  public List<Class<? extends IXrefAnalyzer>> getChecks() {
    return checks;
  }

  public IXrefAnalyzer getXrefAnalyzer(String internalKey) throws ReflectiveOperationException {
    for (Class<? extends IXrefAnalyzer> clz : checks) {
      if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
        return clz.newInstance();
      }
    }
    return null;
  }
  
  public AbstractLintRule getProparseAnalyzer(String internalKey) throws ReflectiveOperationException {
    for (Class<? extends AbstractLintRule> clz : ppchecks) {
      if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
        return clz.newInstance();
      }
    }
    return null;
  }

}
