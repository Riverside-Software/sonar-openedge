/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
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
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.openedge.api.checks.OpenEdgeCheck;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
@ServerSide
public class CheckRegistrar {
  private static final Logger LOGGER = LoggerFactory.getLogger(CheckRegistrar.class);

  @SuppressWarnings("rawtypes")
  private final List<Class<? extends OpenEdgeCheck>> allChecks = new ArrayList<>();

  public CheckRegistrar(CheckProvider... providers) {
    LOGGER.debug("CheckRegistrar created with {} providers" , providers.length);
    for (var provider : providers) {
      allChecks.addAll(provider.getChecks());
    }
  }

  @SuppressWarnings("rawtypes")
  public Class<? extends OpenEdgeCheck> getCheck(String className) {
    for (var clz : allChecks) {
      if (clz.getCanonicalName().equalsIgnoreCase(className))
        return clz;
    }
    return null;
  }

  public static interface CheckProvider {
    @SuppressWarnings("rawtypes")
    public List<Class<? extends OpenEdgeCheck>> getChecks();
  }
}
