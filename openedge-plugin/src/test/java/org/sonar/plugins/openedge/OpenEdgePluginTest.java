/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
package org.sonar.plugins.openedge;

import static org.testng.Assert.assertEquals;

import org.sonar.api.Plugin;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.platform.Server;
import org.sonar.api.utils.Version;
import org.sonar.plugins.openedge.sensor.TestServer;
import org.testng.annotations.Test;

public class OpenEdgePluginTest {
  public static final Version VERSION = Version.parse("7.6");

  public static final SonarRuntime SONARLINT_RUNTIME = SonarRuntimeImpl.forSonarLint(VERSION);
  public static final SonarRuntime SONARQUBE_RUNTIME = SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER,
      SonarEdition.COMMUNITY);

  public static final Server SERVER = new TestServer();
  public static final MapSettings SETTINGS = new MapSettings();

  @Test
  public void testExtensionsSonarLint() {
    Plugin.Context context = new Plugin.Context(SONARLINT_RUNTIME);
    new OpenEdgePlugin().define(context);
    assertEquals(context.getExtensions().size(), 25);
  }

  @Test
  public void testExtensionsSonarQube() {
    Plugin.Context context = new Plugin.Context(SONARQUBE_RUNTIME);
    new OpenEdgePlugin().define(context);
    assertEquals(context.getExtensions().size(), 29);
  }

}
