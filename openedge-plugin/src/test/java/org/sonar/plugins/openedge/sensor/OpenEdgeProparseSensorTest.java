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

import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.BASEDIR;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.CLS_TESTCLASS;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST1;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST2;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST3;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST3_I;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_INVALID;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST3_I1;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.prorefactor.refactor.settings.ProparseSettings.OperatingSystem;
import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.api.CheckRegistration;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.checks.ClumsySyntax;
import org.sonar.plugins.openedge.checks.IntegerRule;
import org.sonar.plugins.openedge.checks.LineNumberRule;
import org.sonar.plugins.openedge.checks.TestChecksRegistration;
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
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertNotNull(context.cpdTokens(BASEDIR + ":" + PROC_TEST3));
    assertEquals(context.cpdTokens(BASEDIR + ":" + PROC_TEST3).size(), 7);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + CLS_TESTCLASS));
    assertEquals(context.cpdTokens(BASEDIR + ":" + CLS_TESTCLASS).size(), 11);
    assertNotNull(context.cpdTokens(BASEDIR + ":" + PROC_TEST3_I));
    assertEquals(context.cpdTokens(BASEDIR + ":" + PROC_TEST3_I).size(), 3);
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
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertNull(context.cpdTokens(BASEDIR + ":" + PROC_TEST3));
    assertNull(context.cpdTokens(BASEDIR + ":" + CLS_TESTCLASS));
    assertNull(context.cpdTokens(BASEDIR + ":" + PROC_TEST3_I));
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
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SETTINGS.asConfig(),
        new CheckRegistration[] {new BasicChecksRegistration()}, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(components.getProparseRules().size(), 1);
  }

  @Test
  public void testComplexity() throws Exception {
    var context = TestProjectSensorContext.createContext();
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), OpenEdgePluginTest.SONARQUBE_RUNTIME);
    var components = new OpenEdgeComponents(OpenEdgePluginTest.SETTINGS.asConfig(),
        new CheckRegistration[] {new TestChecksRegistration()}, null);
    var sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    var m1 = context.measure(BASEDIR + ":" + PROC_TEST1, CoreMetrics.COMPLEXITY);
    assertEquals(m1.value(), 1);
    var m1bis = context.measure(BASEDIR + ":" + PROC_TEST1, CoreMetrics.COGNITIVE_COMPLEXITY);
    assertEquals(m1bis.value(), 0);
    var m2 = context.measure(BASEDIR + ":" + PROC_TEST2, CoreMetrics.COMPLEXITY);
    assertEquals(m2.value(), 13);
    var m2bis = context.measure(BASEDIR + ":" + PROC_TEST2, CoreMetrics.COGNITIVE_COMPLEXITY);
    assertEquals(m2bis.value(), 2);
    var m3 = context.measure(BASEDIR + ":" + PROC_TEST3, CoreMetrics.COMPLEXITY);
    assertEquals(m3.value(), 8);
    var m3bis = context.measure(BASEDIR + ":" + PROC_TEST3, CoreMetrics.COGNITIVE_COMPLEXITY);
    assertEquals(m3bis.value(), 0);
    var m4 = context.measure(BASEDIR + ":" + CLS_TESTCLASS, CoreMetrics.COMPLEXITY);
    assertEquals(m4.value(), 14);
    var m4bis = context.measure(BASEDIR + ":" + CLS_TESTCLASS, CoreMetrics.COGNITIVE_COMPLEXITY);
    assertEquals(m4bis.value(), 0);
  }

  @Test
  public void testSonarQubeRule() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    rulesBuilder.addRule(new NewActiveRule.Builder().setRuleKey(
        RuleKey.of(Constants.STD_REPOSITORY_KEY, IntegerRule.class.getCanonicalName())).setLanguage(
            Constants.LANGUAGE_KEY).build());
    context.setActiveRules(rulesBuilder.build());
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SETTINGS.asConfig(),
        new CheckRegistration[] {new TestChecksRegistration()}, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(components.getProparseRules().size(), 1);
    // IntegerRule reports 16 issues on test2.p, 2 issues on test3.p + 1 from nested test3.i
    assertEquals(context.allIssues().stream().count(), 19);
  }

  @Test
  public void testSonarLintRule() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext().setRuntime(
        OpenEdgePluginTest.SONARLINT_RUNTIME);
    ActiveRulesBuilder rulesBuilder = new ActiveRulesBuilder();
    rulesBuilder.addRule(new NewActiveRule.Builder().setRuleKey(
        RuleKey.of(Constants.STD_REPOSITORY_KEY, IntegerRule.class.getCanonicalName())).setLanguage(
            Constants.LANGUAGE_KEY).build());
    context.setActiveRules(rulesBuilder.build());
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARLINT_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SETTINGS.asConfig(),
        new CheckRegistration[] {new TestChecksRegistration()}, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(components.getProparseRules().size(), 1);
    // IntegerRule reports 16 issues on test2.p, 2 issues on test3.p + 0 from nested test3.i as we're in SonarLint
    // context
    assertEquals(context.allIssues().stream().count(), 18);
  }

  @Test
  public void testNoIssueOnNonOEFiles() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    var rulesBuilder = new ActiveRulesBuilder();
    rulesBuilder.addRule(new NewActiveRule.Builder() //
      .setRuleKey(RuleKey.of(Constants.STD_REPOSITORY_KEY, LineNumberRule.class.getCanonicalName())) //
      .setLanguage(Constants.LANGUAGE_KEY) //
      .setParam("fileNums", "0,10;1,1;2,1") //
      .build());
    context.setActiveRules(rulesBuilder.build());
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(OpenEdgePluginTest.SETTINGS.asConfig(),
        new CheckRegistration[] {new TestChecksRegistration()}, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(components.getProparseRules().size(), 1);
    assertEquals(context.allIssues().stream().filter(
        it -> it.primaryLocation().inputComponent().key().endsWith("test3.p")).count(), 1);
    assertEquals(context.allIssues().stream().filter(
        it -> it.primaryLocation().inputComponent().key().endsWith("test3.i")).count(), 1);
    assertEquals(context.allIssues().stream().filter(
        it -> it.primaryLocation().inputComponent().key().endsWith("test3.i2")).count(), 0);
  }

  @Test
  public void testListing() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST1, OpenEdgeMetrics.NUM_TRANSACTIONS.getKey()).value(), 1,
        "Wrong number of transactions");
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST2, OpenEdgeMetrics.NUM_TRANSACTIONS.getKey()).value(), 0,
        "Wrong number of transactions");
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST2, OpenEdgeMetrics.DIRECTIVES.getKey()).value(), 2,
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
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    assertFalse(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getWindowSystem(),
        "foobar");
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getOpSys(),
        OperatingSystem.UNIX);
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getProcessArchitecture(),
        Integer.valueOf(32));
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getProversion(), "12.0");
  }

  @Test
  public void testPreprocessorSettings02() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    assertTrue(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getBatchMode());
    assertEquals(oeSettings.getProparseSessions().getDefaultSession().getProparseSettings().getProcessArchitecture(),
        Integer.valueOf(64));
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
        OpenEdgePluginTest.SONARLINT_RUNTIME);
    try {
      oeSettings.getProparseSessions();
      fail("RuntimeException should have been thrown");
    } catch (RuntimeException caught) {
      // Nothing
    }
  }

  @Test
  public void testInvalidDBInSonarQube() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "src/schema/invalid.df");

    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    try {
      oeSettings.getProparseSessions();
    } catch (RuntimeException caught) {
      fail("No RuntimeException should have been thrown");
    }
  }

  @Test
  public void testTokenStartChars01() throws Exception {
    MapSettings settings = new MapSettings();
    SensorContextTester context = TestProjectSensorContext.createContext(settings);

    context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, "src/procedures/test5.p") //
      .setLanguage(Constants.LANGUAGE_KEY) //
      .setType(Type.MAIN) //
      .setCharset(Charset.defaultCharset()) //
      .setContents(Files.readString(Path.of(BASEDIR,  "src/procedures/test5.p")))
      .build());

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.allAnalysisErrors().size(), 0);
    assertEquals(context.allIssues().size(), 2); // Invalid.p + !function
    Issue issue = context.allIssues().iterator().next();
    assertEquals(issue.ruleKey().rule(), OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY);
  }

  @Test
  public void testTokenStartChars02() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty("sonar.oe.proparse.tokenStartChars", ";!^");
    SensorContextTester context = TestProjectSensorContext.createContext(settings);

    context.fileSystem().add(TestInputFileBuilder.create(BASEDIR, "src/procedures/test5.p") //
        .setLanguage(Constants.LANGUAGE_KEY) //
        .setType(Type.MAIN) //
        .setCharset(Charset.defaultCharset()) //
        .setContents(Files.readString(Path.of(BASEDIR,  "src/procedures/test5.p")))
        .build());

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.allAnalysisErrors().size(), 0);
    assertEquals(context.allIssues().size(), 1); // Just invalid.p
    Issue issue = context.allIssues().iterator().next();
    assertEquals(issue.ruleKey().rule(), OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY);
  }

  @Test
  public void testProparseError() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
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
        OpenEdgePluginTest.SONARLINT_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.allIssues().size(), 0);
    assertEquals(context.allAnalysisErrors().size(), 1);
  }

  @Test
  public void testNoProparseError() throws Exception {
    var settings = new MapSettings();
    settings.setProperty("sonar.oe.proparse.recover", true);

    var context = TestProjectSensorContext.createContext(settings);
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), OpenEdgePluginTest.SONARQUBE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    assertEquals(context.allAnalysisErrors().size(), 0);
    assertEquals(context.allIssues().size(), 0);
  }

  @Test
  public void testProparseDebug01() throws Exception {
    var settings = new MapSettings();
    settings.setProperty(Constants.PROPARSE_DEBUG, true);

    var context = TestProjectSensorContext.createContext(settings);
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), OpenEdgePluginTest.SONARQUBE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor = new OpenEdgeProparseSensor(oeSettings, components);

    var dotProparseDir = context.fileSystem().baseDirPath().resolve(".proparse");
    deleteDirectoryRecursive(dotProparseDir);
    sensor.execute(context);
    assertTrue(Files.exists(dotProparseDir));
    assertTrue(Files.exists(dotProparseDir.resolve("index.json")));
    assertTrue(Files.exists(dotProparseDir.resolve("index.html")));
    assertFalse(Files.exists(dotProparseDir.resolve("files")));
  }

  @Test
  public void testProparseDebug02() throws Exception {
    var settings = new MapSettings();
    settings.setProperty(Constants.PROPARSE_DEBUG, true);
    settings.setProperty(Constants.PROPARSE_DEBUG_INCLUDES, "src/**/test1.p,src/procedures/test3.p");

    var context = TestProjectSensorContext.createContext(settings);
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), OpenEdgePluginTest.SONARQUBE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor = new OpenEdgeProparseSensor(oeSettings, components);

    var dotProparseDir = context.fileSystem().baseDirPath().resolve(".proparse");
    deleteDirectoryRecursive(dotProparseDir);
    sensor.execute(context);
    assertTrue(Files.exists(dotProparseDir));
    assertTrue(Files.exists(dotProparseDir.resolve("index.json")));
    assertTrue(Files.exists(dotProparseDir.resolve("index.html")));
    assertTrue(Files.exists(dotProparseDir.resolve("files")));
    assertTrue(Files.exists(dotProparseDir.resolve("files").resolve("src_procedures_test1_p.json")));
    assertFalse(Files.exists(dotProparseDir.resolve("files").resolve("src_procedures_test2_p.json")));
    assertTrue(Files.exists(dotProparseDir.resolve("files").resolve("src_procedures_test3_p.json")));
    assertFalse(Files.exists(dotProparseDir.resolve("files").resolve("src_classes_rssw_testclass_cls.json")));
    deleteDirectoryRecursive(dotProparseDir);
  }

  @Test
  public void testLOC01() throws Exception {
    var settings = new MapSettings();
    var context = TestProjectSensorContext.createContext(settings);
    var oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), OpenEdgePluginTest.SONARQUBE_RUNTIME);
    var components = new OpenEdgeComponents(context.config());
    var sensor = new OpenEdgeProparseSensor(oeSettings, components);

    sensor.execute(context);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST1, CoreMetrics.NCLOC.getKey()).value(), 0);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST1, OpenEdgeMetrics.OELIC_NCLOC.getKey()).value(), 0);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST2, CoreMetrics.NCLOC.getKey()).value(), 53);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST2, OpenEdgeMetrics.OELIC_NCLOC.getKey()).value(), 26);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST3, CoreMetrics.NCLOC.getKey()).value(), 12);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST3, OpenEdgeMetrics.OELIC_NCLOC.getKey()).value(), 10);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST3_I, CoreMetrics.NCLOC.getKey()).value(), 3);
    // Include with no lines of code don't have an OELIC_NCLOC measure
    assertNull(context.measure(BASEDIR + ":" + PROC_TEST3_I, OpenEdgeMetrics.OELIC_NCLOC.getKey()));
    // Parser failure means no measures available
    assertNull(context.measure(BASEDIR + ":" + PROC_INVALID, CoreMetrics.NCLOC.getKey()));
    assertNull(context.measure(BASEDIR + ":" + PROC_INVALID, OpenEdgeMetrics.OELIC_NCLOC.getKey()));
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST3_I1, CoreMetrics.NCLOC.getKey()).value(), 1);
    assertEquals(context.measure(BASEDIR + ":" + PROC_TEST3_I1, OpenEdgeMetrics.OELIC_NCLOC.getKey()).value(), 1);
    assertEquals(context.measure(BASEDIR + ":" + CLS_TESTCLASS, CoreMetrics.NCLOC.getKey()).value(), 20);
    assertEquals(context.measure(BASEDIR + ":" + CLS_TESTCLASS, OpenEdgeMetrics.OELIC_NCLOC.getKey()).value(), 17);
  }

  private void deleteDirectoryRecursive(Path pathToBeDeleted) throws IOException {
    if (!Files.exists(pathToBeDeleted))
      return;
    try (var paths = Files.walk(pathToBeDeleted)) {
      paths.sorted(Comparator.reverseOrder()).forEach(it -> {
        try {
          Files.delete(it);
        } catch (IOException uncaught) {
          // Nothing
        }
      });
    }
    assertFalse(Files.exists(pathToBeDeleted), "Directory still exists");
  }
}
