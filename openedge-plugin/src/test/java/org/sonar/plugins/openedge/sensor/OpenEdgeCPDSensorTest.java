/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.FILE4;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.prorefactor.refactor.settings.ProparseSettings.OperatingSystem;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.plugins.openedge.api.CheckRegistration;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.checks.ClumsySyntax;
import org.sonar.plugins.openedge.foundation.BasicChecksRegistration;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.annotations.Test;

public class OpenEdgeCPDSensorTest {
  private static final Version VERSION = Version.parse("7.5");

  @SuppressWarnings("deprecation")
  @Test
  public void testCPDSensor() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.settings().setProperty(Constants.USE_SIMPLE_CPD, true);
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    OpenEdgeCPDSensor sensor = new OpenEdgeCPDSensor(oeSettings);
    sensor.execute(context);

    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE3).size(), 14);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertEquals(context.cpdTokens(BASEDIR + ":" + CLASS1).size(), 19);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE4));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE4).size(), 2);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testCPDSensor02() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    OpenEdgeCPDSensor sensor = new OpenEdgeCPDSensor(oeSettings);
    sensor.execute(context);

    assertNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertNull(context.cpdTokens(BASEDIR + ":" + FILE4));
  }
}
