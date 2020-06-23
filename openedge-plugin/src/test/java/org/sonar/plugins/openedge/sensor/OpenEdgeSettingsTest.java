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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.prorefactor.refactor.RefactorSession;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.ITypeInfo;

public class OpenEdgeSettingsTest {
  private static final Version VERSION = Version.parse("7.5");
  private static final SonarRuntime SQ_RUNTIME = SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY);
  private static final SonarRuntime SL_RUNTIME = SonarRuntimeImpl.forSonarLint(VERSION);

  @Test
  public void testSonarDatabasesFromSonarQube01() throws Exception {
    // No database schema, verify no DB table available
    MapSettings settings = new MapSettings();
    settings.setProperty(Constants.DATABASES, "");
    settings.setProperty("sonar.sources", "src");

    SensorContextTester context = SensorContextTester.create(new File(TestProjectSensorContext.BASEDIR));
    context.setSettings(settings);

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SQ_RUNTIME);
    RefactorSession ppSess = oeSettings.getProparseSession();
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

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SQ_RUNTIME);
    RefactorSession ppSess = oeSettings.getProparseSession();
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

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SQ_RUNTIME);
    RefactorSession ppSess = oeSettings.getProparseSession();
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

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SQ_RUNTIME);
    RefactorSession ppSess = oeSettings.getProparseSession();
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

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SL_RUNTIME);
    RefactorSession ppSess = oeSettings.getProparseSession();
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

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SL_RUNTIME);
    RefactorSession ppSess = oeSettings.getProparseSession();
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

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SL_RUNTIME);
    RefactorSession session = oeSettings.getProparseSession();

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
}
