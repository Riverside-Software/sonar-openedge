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

import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.server.ServerSide;
import org.sonar.plugins.openedge.api.CheckProvider;
import org.sonar.plugins.openedge.api.checks.OpenEdgeCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeDumpFileCheck;
import org.sonar.plugins.openedge.api.checks.OpenEdgeProparseCheck;
import org.sonar.plugins.openedge.checks.ClumsySyntax;
import org.sonar.plugins.openedge.checks.LargeTransactionScope;
import org.sonar.plugins.openedge.checks.NoOpDatabaseRule;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
@ServerSide
public class BasicChecksProvider implements CheckProvider {

  static List<Class<? extends OpenEdgeProparseCheck>> ppCheckClasses() {
    return List.of(LargeTransactionScope.class, ClumsySyntax.class);
  }

  static List<Class<? extends OpenEdgeDumpFileCheck>> dbCheckClasses() {
    return List.of(NoOpDatabaseRule.class);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public List<Class<? extends OpenEdgeCheck>> getChecks() {
    List<Class<? extends OpenEdgeCheck>> list = new ArrayList<>();
    list.addAll(ppCheckClasses());
    list.addAll(dbCheckClasses());

    return list;
  }
}
