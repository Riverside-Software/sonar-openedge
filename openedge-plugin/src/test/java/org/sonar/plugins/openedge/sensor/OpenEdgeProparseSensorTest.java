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

public class OpenEdgeProparseSensorTest {
  private static final Version VERSION = Version.parse("7.5");

  @SuppressWarnings("deprecation")
  @Test
  public void testCPDPreprocessorExpansion() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.settings().setProperty(Constants.CPD_ANNOTATIONS, "Generated,rssw.lang.Generated");
    context.settings().setProperty(Constants.CPD_METHODS, "TEST3");
    context.settings().setProperty(Constants.CPD_PROCEDURES, "adm-create-objects");
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    OpenEdgeComponents components = new OpenEdgeComponents(null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE3).size(), 7);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertEquals(context.cpdTokens(BASEDIR + ":" + CLASS1).size(), 11);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE4));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE4).size(), 2);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testCPDPreprocessorExpansion02() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.settings().setProperty(Constants.CPD_ANNOTATIONS, "Generated,rssw.lang.Generated");
    context.settings().setProperty(Constants.CPD_METHODS, "TEST3");
    context.settings().setProperty(Constants.CPD_PROCEDURES, "adm-create-objects");
    // No CPD data from ProparseSensor if simple CPD is enabled
    context.settings().setProperty(Constants.USE_SIMPLE_CPD, true);
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    OpenEdgeComponents components = new OpenEdgeComponents(null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertNull(context.cpdTokens(BASEDIR + ":" + FILE4));
  }

  @Test
  public void testRules() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    rulesBuilder.addRule(new NewActiveRule.Builder().setRuleKey(
        RuleKey.of(Constants.STD_REPOSITORY_KEY, ClumsySyntax.class.getCanonicalName())).setLanguage(
            Constants.LANGUAGE_KEY).build());
    context.setActiveRules(rulesBuilder.build());
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    OpenEdgeComponents components = new OpenEdgeComponents(new CheckRegistration[] {new BasicChecksRegistration()},
        null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(components.getProparseRules().size(), 1);
  }

  @Test
  public void testListing() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    OpenEdgeComponents components = new OpenEdgeComponents(null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.measure(BASEDIR + ":" + FILE1, OpenEdgeMetrics.NUM_TRANSACTIONS_KEY).value(), 1,
        "Wrong number of transactions");
    assertEquals(context.measure(BASEDIR + ":" + FILE2, OpenEdgeMetrics.NUM_TRANSACTIONS_KEY).value(), 0,
        "Wrong number of transactions");
  }

  @SuppressWarnings("deprecation")
  @Test
  public void testPreprocessorSettings01() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.settings().setProperty("sonar.oe.preprocessor.window-system", "foobar");
    context.settings().setProperty("sonar.oe.preprocessor.opsys", "unix");
    context.settings().setProperty("sonar.oe.preprocessor.batch-mode", "false");
    context.settings().setProperty("sonar.oe.preprocessor.process-architecture", "32");
    context.settings().setProperty("sonar.oe.preprocessor.proversion", "12.0");

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    assertFalse(oeSettings.getProparseSession().getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSession().getProparseSettings().getWindowSystem(), "foobar");
    assertEquals(oeSettings.getProparseSession().getProparseSettings().getOpSys(), OperatingSystem.UNIX);
    assertEquals(oeSettings.getProparseSession().getProparseSettings().getProcessArchitecture(), Integer.valueOf(32));
    assertEquals(oeSettings.getProparseSession().getProparseSettings().getProversion(), "12.0");
  }

  @Test
  public void testPreprocessorSettings02() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    assertTrue(oeSettings.getProparseSession().getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSession().getProparseSettings().getProcessArchitecture(), Integer.valueOf(64));
    assertEquals(oeSettings.getProparseSession().getProparseSettings().getProversion(), "11.7");
  }

  @Test
  public void testInvalidDBInSonarLint() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.settings().setProperty(Constants.DATABASES, "src/schema/invalid.df");
    
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarLint(VERSION));
    try {
      oeSettings.getProparseSession();
      fail("RuntimeException should have been thrown");
    } catch (RuntimeException caught) {
      
    }
  }

  @Test
  public void testInvalidDBInSonarQube() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.settings().setProperty(Constants.DATABASES, "src/schema/invalid.df");
    
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    try {
      oeSettings.getProparseSession();
    } catch (RuntimeException caught) {
      fail("No RuntimeException should have been thrown");
    }
  }

}
