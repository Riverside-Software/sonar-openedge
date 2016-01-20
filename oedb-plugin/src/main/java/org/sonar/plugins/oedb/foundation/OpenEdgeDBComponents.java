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
package org.sonar.plugins.oedb.foundation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.plugins.oedb.api.CheckRegistrar;
import org.sonar.plugins.oedb.api.checks.IDumpFileAnalyzer;

@BatchSide
public class OpenEdgeDBComponents {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeDBComponents.class);

  private final List<Class<? extends IDumpFileAnalyzer>> checks = new ArrayList<>();

  public OpenEdgeDBComponents(CheckRegistrar[] registrars) {
    if (registrars != null) {
      CheckRegistrar.RegistrarContext registrarContext = new CheckRegistrar.RegistrarContext();
      for (CheckRegistrar reg : registrars) {
        reg.register(registrarContext);
        for (Class<? extends IDumpFileAnalyzer> analyzer : registrarContext.checkClasses()) {
          LOG.debug("{} analyzer registered", analyzer.getName());
          checks.add(analyzer);
        }
      }
    }
  }

  @SuppressWarnings("rawtypes")
  public Collection<Class> getAllAnalyzers() {
    Collection<Class> rslt = new ArrayList<>();
    rslt.addAll(checks);
    return rslt;
  }

  public IDumpFileAnalyzer getAnalyzer(String internalKey) throws ReflectiveOperationException {
    for (Class<? extends IDumpFileAnalyzer> clz : checks) {
      if (clz.getCanonicalName().equalsIgnoreCase(internalKey)) {
        return clz.newInstance();
      }
    }
    return null;
  }
}
