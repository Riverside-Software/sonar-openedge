/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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

import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.BASEDIR;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.CLASS1;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.FILE1;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.FILE3;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.FILE4;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.annotations.Test;

public class OpenEdgeCPDSensorTest {

  @Test
  public void testCPDSensor() throws Exception {
    var settings = new MapSettings();
    settings.setProperty(Constants.USE_SIMPLE_CPD, true);

    var context = TestProjectSensorContext.createContext();
    context.setSettings(settings);

    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), OpenEdgePluginTest.SONARQUBE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor = new OpenEdgeCPDSensor(oeSettings, components);
    sensor.execute(context);

    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE3).size(), 15);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertEquals(context.cpdTokens(BASEDIR + ":" + CLASS1).size(), 19);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE4));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE4).size(), 3);
  }

  @Test
  public void testCPDSensor02() throws Exception {
    var context = TestProjectSensorContext.createContext();
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), OpenEdgePluginTest.SONARQUBE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor = new OpenEdgeCPDSensor(oeSettings, components);
    sensor.execute(context);

    assertNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertNull(context.cpdTokens(BASEDIR + ":" + FILE4));
  }

  @Test
  public void testCPDSensorNoBranch() throws Exception {
    var settings = new MapSettings();
    settings.setProperty(Constants.USE_SIMPLE_CPD, true);
    var context = TestProjectSensorContext.createContext();
    context.setSettings(settings);
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_DE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor = new OpenEdgeCPDSensor(oeSettings, components);
    sensor.execute(context);

    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE4));
  }

  @Test
  public void testCPDSensorBranch() throws Exception {
    var context = TestProjectSensorContext.createContext();
    context.settings().setProperty(Constants.USE_SIMPLE_CPD, true);
    context.setCanSkipUnchangedFiles(true);
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_DE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor01 = new OpenEdgeDependenciesSensor(oeSettings, components);
    sensor01.execute(context);
    var test3 = context.fileSystem().inputFile(
        context.fileSystem().predicates().hasRelativePath("src/procedures/test3.p"));
    assertNotNull(test3);
    var deps = components.getIncludeDependencies(test3.uri().toString());
    assertFalse(deps.isEmpty());
    var str = deps.get(0);
    assertEquals(str, "src\\procedures\\test3.i");

    var sensor = new OpenEdgeCPDSensor(oeSettings, components);
    sensor.execute(context);

    // No CPD tokens on unchanged files
    assertNull(context.cpdTokens(BASEDIR + ":" + FILE1));
    // FILE3 is SAME, but references FILE4 (file3.i) which is ADDED
    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    // CPD tokens on ADDED files
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE4));
  }

}
