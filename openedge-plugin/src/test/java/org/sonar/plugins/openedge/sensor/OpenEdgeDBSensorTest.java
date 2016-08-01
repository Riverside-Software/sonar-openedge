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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.sonar.api.batch.fs.InputFile.Type;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.google.common.io.Files;
import org.sonar.plugins.openedge.foundation.OpenEdgeDB;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeDBSensorTest {
  private final File moduleBaseDir = new File("src/test/resources/project1");
  private final static String FILE1 = "src/schema/sp2k.df";

  @Test
  public void testSp2k() throws Exception {
    SensorContextTester context = createContext();
    OpenEdgeDBSensor sensor = new OpenEdgeDBSensor(context.fileSystem());
    sensor.execute(context);

    Assert.assertEquals(context.measure("file1:src/schema/sp2k.df", OpenEdgeMetrics.NUM_TABLES_KEY).value(), 25,
        "Wrong number of tables");
    Assert.assertEquals(context.measure("file1:src/schema/sp2k.df", OpenEdgeMetrics.NUM_SEQUENCES_KEY).value(), 13,
        "Wrong number of sequences");
  }

  private SensorContextTester createContext() throws IOException {
    SensorContextTester context = SensorContextTester.create(moduleBaseDir);
    context.settings().setProperty("sonar.sources", "src");
    context.settings().setProperty("sonar.oe.binaries", "build");
    context.fileSystem().add(
        new DefaultInputFile("file1", FILE1).setLanguage(OpenEdgeDB.KEY).setType(Type.MAIN).initMetadata(
            Files.toString(new File(moduleBaseDir, FILE1), Charset.defaultCharset())));

    return context;
  }
}
