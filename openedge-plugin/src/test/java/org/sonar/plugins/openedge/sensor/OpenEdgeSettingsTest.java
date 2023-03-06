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
package org.sonar.plugins.openedge.sensor;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;

import org.prorefactor.proparse.classdoc.ClassDocumentation;
import org.prorefactor.proparse.classdoc.ClassDocumentation.DeprecatedInfo;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.predicates.DefaultFilePredicates;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.sonar.plugins.openedge.utils.TestProjectSensorContextExtra;
import org.sonar.plugins.openedge.utils.TestProjectSensorRtbContext;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.ITypeInfo;

public class OpenEdgeSettingsTest {

  @Test
  public void testSameObject() throws Exception {
    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(new MapSettings());

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertSame(ppSess, oeSettings.getProparseSessions().getDefaultSession());
  }

  @Test
  public void testTwoSessions() throws Exception {
    SensorContextTester context = TestProjectSensorContextExtra.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertSame(ppSess, oeSettings.getProparseSessions().getDefaultSession());
    assertSame(ppSess, oeSettings.getProparseSessions().getSession("src/procedures/test1.p"));

    IProparseEnvironment sess2 = oeSettings.getProparseSessions().getSession("src/procedures/test4.p");
    assertNotSame(ppSess, sess2);
    assertNotNull(sess2.getSchema().lookupTable("extraTab1"));
    assertNull(sess2.getSchema().lookupTable("customer"));
  }

  @Test
  public void testRtbCompatibility() throws Exception {
    SensorContextTester context = TestProjectSensorRtbContext.createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    oeSettings.init();
    FilePredicates preds = new DefaultFilePredicates(Path.of(TestProjectSensorRtbContext.BASEDIR));
    InputFile input1 = context.fileSystem().inputFile(preds.hasFilename("test1.p"));
    assertTrue(oeSettings.getListingFile(input1).exists());
    assertTrue(oeSettings.getXrefFile(input1).exists());
    InputFile input2 = context.fileSystem().inputFile(preds.hasFilename("testclass.cls"));
    assertTrue(oeSettings.getListingFile(input2).exists());
    assertNull(oeSettings.getXrefFile(input2));
  }

  @Test
  public void testSonarDatabasesFromSonarQube01() throws Exception {
    // No database schema, verify no DB table available
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "");
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertNotNull(ppSess);
    assertNotNull(ppSess.getSchema());
    assertNull(ppSess.getSchema().lookupTable("item"));
  }

  @Test
  public void testSonarDatabasesFromSonarQube02() throws Exception {
    // Simple sports2000 database schema
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "src/schema/sp2k.df");
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertNotNull(ppSess);
    assertNotNull(ppSess.getSchema());
    assertNotNull(ppSess.getSchema().lookupDatabase("sp2k"));
    assertNull(ppSess.getSchema().lookupDatabase("sp3k"));

    assertNotNull(ppSess.getSchema().lookupTable("item"));
    assertNotNull(ppSess.getSchema().lookupTable("sp2k", "item"));
    assertNull(ppSess.getSchema().lookupTable("sp3k", "item"));

    assertNull(ppSess.getSchema().lookupTable("abcdef"));
  }

  @Test
  public void testSonarDatabasesFromSonarQube03() throws Exception {
    // Simple sports2000 database schema under a different logical name
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "src/schema/sp2k.df:rssw");
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertNotNull(ppSess);
    assertNotNull(ppSess.getSchema());
    assertNull(ppSess.getSchema().lookupDatabase("sp2k"));
    assertNotNull(ppSess.getSchema().lookupDatabase("rssw"));

    assertNotNull(ppSess.getSchema().lookupTable("item"));
    assertNull(ppSess.getSchema().lookupTable("sp2k", "item"));
    assertNotNull(ppSess.getSchema().lookupTable("rssw", "item"));

    assertNull(ppSess.getSchema().lookupTable("abcdef"));
  }

  @Test
  public void testSonarDatabasesFromSonarQube04() throws Exception {
    // Simple sports2000 database schema under a different logical name and with aliases
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "src/schema/sp2k.df:rssw");
    settings.setProperty(Constants.ALIASES, "rssw,alias1,alias2");
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertNotNull(ppSess);
    assertNotNull(ppSess.getSchema());
    assertNull(ppSess.getSchema().lookupDatabase("sp2k"));
    assertNotNull(ppSess.getSchema().lookupDatabase("rssw"));
    assertNotNull(ppSess.getSchema().lookupDatabase("alias1"));
    assertNotNull(ppSess.getSchema().lookupDatabase("alias2"));

    assertNotNull(ppSess.getSchema().lookupTable("item"));
    assertNull(ppSess.getSchema().lookupTable("sp2k", "item"));
    assertNotNull(ppSess.getSchema().lookupTable("rssw", "item"));
    assertNotNull(ppSess.getSchema().lookupTable("alias1", "item"));
    assertNotNull(ppSess.getSchema().lookupTable("alias2", "item"));

    assertNull(ppSess.getSchema().lookupTable("abcdef"));
  }

  @Test
  public void testSonarDatabasesFromSonarLint01() throws Exception {
    // Simple sports2000 database schema on SonarLint - This schema doesn't include table 'Benefits'
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.SLINT_DATABASES,
        new File(TestProjectSensorContext.BASEDIR, ".sonarlint/sp2k.schema").getAbsolutePath());
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARLINT_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertNotNull(ppSess);
    assertNotNull(ppSess.getSchema());
    assertNotNull(ppSess.getSchema().lookupDatabase("sp2k"));
    assertNull(ppSess.getSchema().lookupDatabase("sp3k"));

    assertNotNull(ppSess.getSchema().lookupTable("item"));
    assertNotNull(ppSess.getSchema().lookupTable("sp2k", "item"));
    assertNull(ppSess.getSchema().lookupTable("sp3k", "item"));

    assertNull(ppSess.getSchema().lookupTable("benefits"));
  }

  @Test
  public void testSonarDatabasesFromSonarLint02() throws Exception {
    // Simple sports2000 database schema under a different logical name and with aliases*
    // Override with standard schema
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.SLINT_DATABASES,
        new File(TestProjectSensorContext.BASEDIR, ".sonarlint/sp2k.schema").getAbsolutePath());
    settings.setProperty(Constants.DATABASES, "src/schema/sp2k.df:rssw");
    settings.setProperty("sonar.sources", "src");

    File cache = new File(TestProjectSensorContext.BASEDIR, ".sonarlint/src_schema_sp2k.df.bin");
    cache.delete();

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARLINT_RUNTIME);
    IProparseEnvironment ppSess = oeSettings.getProparseSessions().getDefaultSession();
    assertNotNull(ppSess);
    assertNotNull(ppSess.getSchema());
    assertNull(ppSess.getSchema().lookupDatabase("sp2k"));
    assertNotNull(ppSess.getSchema().lookupDatabase("rssw"));

    assertNotNull(ppSess.getSchema().lookupTable("item"));
    assertNotNull(ppSess.getSchema().lookupTable("rssw", "item"));
    assertNull(ppSess.getSchema().lookupTable("sp3k", "item"));

    assertNotNull(ppSess.getSchema().lookupTable("benefits"));
    assertTrue(cache.exists());
  }

  @Test
  public void testAssemblyCatalog() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.ASSEMBLY_CATALOG,
        new File(TestProjectSensorContext.BASEDIR, "assemblies.json").getAbsolutePath());
    settings.setProperty(Constants.DATABASES, "");
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARLINT_RUNTIME);
    IProparseEnvironment session = oeSettings.getProparseSessions().getDefaultSession();

    ITypeInfo info = session.getTypeInfo("Progress.Json.ObjectModel.JsonArray");
    assertNotNull(info);
    ITypeInfo info2 = session.getTypeInfo("Progress.Lang.Object");
    assertNotNull(info2);
    ITypeInfo info3 = session.getTypeInfo("System.AppContextDefaultValues");
    assertNotNull(info3);
    assertEquals(info3.getParentTypeName(), "System.Object");
    assertFalse(info3.isInterface());
    assertTrue(info3.isAbstract());
    assertTrue(info3.hasMethod("GetHashCode"));
    assertTrue(info3.hasMethod("PopulateDefaultValues"));
    ITypeInfo info4 = session.getTypeInfo("Microsoft.Win32.IInternetSecurityManager");
    assertNotNull(info4);
    assertTrue(info4.isAbstract());
    assertTrue(info4.isInterface());
  }

  @Test
  public void testClassDocumentation() throws Exception {
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.CLASS_DOCUMENTATION,
        new File(TestProjectSensorContext.BASEDIR, "netlib.json").getAbsolutePath() + ","
            + new File(TestProjectSensorContext.BASEDIR, "corelib.json").getAbsolutePath());
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    IProparseEnvironment session = oeSettings.getProparseSessions().getDefaultSession();

    ClassDocumentation doc = session.getClassDocumentation("OpenEdge.Core.ByteBucket");
    assertNotNull(doc);
    DeprecatedInfo dep = doc.objectDoc.get("M#PutString(ILC,ILC)");
    assertNotNull(dep);
    assertEquals(dep.since, "11.7.3");

    ClassDocumentation doc2 = session.getClassDocumentation("OpenEdge.Core.Collections.List");
    assertNotNull(doc2);
    assertNotNull(doc2.deprecated);

    ClassDocumentation doc3 = session.getClassDocumentation("OpenEdge.Net.HTTP.Lib.ABLSockets.ABLSocketLibrary");
    assertNotNull(doc3);
    DeprecatedInfo dep2 = doc3.objectDoc.get("M#MakeSyncRequest(IZOpenEdge.Net.ServerConnection.ClientSocket,IZOpenEdge.Net.HTTP.IHttpRequest,IZOpenEdge.Net.HTTP.IHttpResponse,IZOpenEdge.Core.ByteBucket)");
    assertNotNull(dep2);
    assertEquals(dep2.since, "12.5.0");

  }

}
