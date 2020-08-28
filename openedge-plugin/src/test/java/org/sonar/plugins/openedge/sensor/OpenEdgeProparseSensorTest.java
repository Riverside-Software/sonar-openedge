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
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.api.CheckRegistration;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.checks.ClumsySyntax;
import org.sonar.plugins.openedge.foundation.BasicChecksRegistration;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
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
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE3).size(), 7);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    assertEquals(context.cpdTokens(BASEDIR + ":" + CLASS1).size(), 11);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE4));
    assertEquals(context.cpdTokens(BASEDIR + ":" + FILE4).size(), 2);
  }

  @Test
  public void testCPDPreprocessorExpansion02() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.CPD_ANNOTATIONS, "Generated,rssw.lang.Generated");
    settings.setProperty(Constants.CPD_METHODS, "TEST3");
    settings.setProperty(Constants.CPD_PROCEDURES, "adm-create-objects");
    // No CPD data from ProparseSensor if simple CPD is enabled
    settings.setProperty(Constants.USE_SIMPLE_CPD, true);

    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, null, null);
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
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER,
        new CheckRegistration[] {new BasicChecksRegistration()}, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(components.getProparseRules().size(), 1);
  }

  @Test
  public void testListing() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, null, null);
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
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    assertFalse(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getWindowSystem(), "foobar");
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getOpSys(), OperatingSystem.UNIX);
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getProcessArchitecture(), Integer.valueOf(32));
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getProversion(), "12.0");
  }

  @Test
  public void testPreprocessorSettings02() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    assertTrue(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getProcessArchitecture(), Integer.valueOf(64));
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getProversion(), "11.7");
  }

  @Test
  public void testInvalidDBInSonarLint() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "src/schema/invalid.df");

    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setSettings(settings);
    context.setRuntime(OpenEdgePluginTest.SONARLINT_RUNTIME); 

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARLINT_RUNTIME, OpenEdgePluginTest.SERVER);
    try {
      oeSettings.getProparseSessions();
      fail("RuntimeException should have been thrown");
    } catch (RuntimeException caught) {

    }
  }

  @Test
  public void testInvalidDBInSonarQube() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "src/schema/invalid.df");

    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    try {
      oeSettings.getProparseSessions();
    } catch (RuntimeException caught) {
      fail("No RuntimeException should have been thrown");
    }
  }

  @Test
  public void testProparseError() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.allAnalysisErrors().size(), 0);
    assertEquals(context.allIssues().size(), 1);
    Issue issue = context.allIssues().iterator().next();
    assertEquals(issue.ruleKey().rule(), OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY);
  }

  @Test
  public void testProparseErrorSonarLint() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setRuntime(OpenEdgePluginTest.SONARLINT_RUNTIME);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARLINT_RUNTIME, OpenEdgePluginTest.SERVER);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.allIssues().size(), 0);
    assertEquals(context.allAnalysisErrors().size(), 1);
  }

  @Test
  public void testNoProparseError() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty("sonar.oe.proparse.recover", true);

    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME, OpenEdgePluginTest.SERVER);
    
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SERVER, null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.allAnalysisErrors().size(), 0);
    assertEquals(context.allIssues().size(), 0);
  }

}
