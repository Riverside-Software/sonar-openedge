/*
 * OpenEdge plugin for SonarQube
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
package org.sonar.plugins.openedge.foundation.licence;

import org.sonarsource.api.sonarlint.SonarLintSide;

/**
 * Dummy implementation when analysis is executed in SonarLint
 */
@SonarLintSide
public class SonarLintSideNameProvider implements IServerNameProvider {
  public SonarLintSideNameProvider() {
    System.out.println("Building SonarLintSideNameProvider");
  }
  @Override
  public String getServerName() {
    return "lint";
  }

  @Override
  public boolean isSonarLintSide() {
    return true;
  }
}
