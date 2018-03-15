/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2016 Riverside Software
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
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.FILE2;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.FILE3;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.prorefactor.refactor.settings.ProparseSettings.OperatingSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.annotations.Test;

public class OpenEdgeProparseSensorTest {

  @Test
  public void testCPDPreprocessorExpansion() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.CPD_ANNOTATIONS, "Generated,rssw.lang.Generated");
    settings.setProperty(Constants.CPD_METHODS, "TEST3");
    settings.setProperty(Constants.CPD_PROCEDURES, "adm-create-objects");

    SensorContextTester context = TestProjectSensorContext.createContext();
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(settings.asConfig(), context.fileSystem());
    OpenEdgeComponents components = new OpenEdgeComponents(null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE3).size(), 7);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertEquals(context.cpdTokens(BASEDIR + ":" + CLASS1).size(), 11);
  }

  @Test
  public void testListing() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem());
    OpenEdgeComponents components = new OpenEdgeComponents(null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.measure(BASEDIR + ":" + FILE1, OpenEdgeMetrics.NUM_TRANSACTIONS_KEY).value(), 1,
        "Wrong number of transactions");
    assertEquals(context.measure(BASEDIR + ":" + FILE2, OpenEdgeMetrics.NUM_TRANSACTIONS_KEY).value(), 0,
        "Wrong number of transactions");
  }

  @Test
  public void testPreprocessorSettings01() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty("sonar.oe.preprocessor.window-system", "foobar");
    settings.setProperty("sonar.oe.preprocessor.opsys", "unix");
    settings.setProperty("sonar.oe.preprocessor.batch-mode", "false");
    settings.setProperty("sonar.oe.preprocessor.process-architecture", "32");
    settings.setProperty("sonar.oe.preprocessor.proversion", "12.0");

    SensorContextTester context = TestProjectSensorContext.createContext();
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(settings.asConfig(), context.fileSystem());
    assertFalse(oeSettings.getProparseSession(false).getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSession(false).getProparseSettings().getWindowSystem(), "foobar");
    assertEquals(oeSettings.getProparseSession(false).getProparseSettings().getOpSys(), OperatingSystem.UNIX);
    assertEquals(oeSettings.getProparseSession(false).getProparseSettings().getProcessArchitecture(), "32");
    assertEquals(oeSettings.getProparseSession(false).getProparseSettings().getProversion(), "12.0");
  }

  @Test
  public void testPreprocessorSettings02() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem());
    assertTrue(oeSettings.getProparseSession(false).getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSession(false).getProparseSettings().getProcessArchitecture(), "64");
    assertEquals(oeSettings.getProparseSession(false).getProparseSettings().getProversion(), "11.7");
  }

}
