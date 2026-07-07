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
package org.sonar.plugins.openedge.sensor;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.openedge.foundation.BasicChecksProvider;
import org.sonar.plugins.openedge.foundation.CheckRegistrar;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.annotations.Test;

public class BasicCheckProviderTest {

  @Test
  public void testChecks() throws Exception {
    var settings = new MapSettings();
    var context = TestProjectSensorContext.createContext();
    context.setSettings(settings);

    var checkReg = new BasicChecksProvider();
    var checkRegistrar = new CheckRegistrar(checkReg);
    assertNotNull(checkRegistrar.getCheck("org.sonar.plugins.openedge.checks.ClumsySyntax"));
    assertNotNull(checkRegistrar.getCheck("org.sonar.plugins.openedge.checks.NoOpDatabaseRule"));
    assertNull(checkRegistrar.getCheck("org.sonar.plugins.openedge.checks.NotARule"));
  }
}
