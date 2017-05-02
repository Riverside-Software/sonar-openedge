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
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.IIdProvider;
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeSensorTest {
  private final File moduleBaseDir = new File("src/test/resources/project1");
  private final static String FILE1 = "src/procedures/test1.p";
  private final static String FILE2 = "src/procedures/test2.p";

  @Test
  public void testMetrics() throws Exception {
    SensorContextTester context = createContext();

    OpenEdgeSensor sensor = new OpenEdgeSensor(context.fileSystem(), createIdProvider());
    sensor.execute(context);

    Assert.assertEquals(1, context.measure("file1:src/procedures/test1.p", OpenEdgeMetrics.PROCEDURES_KEY).value());
    Assert.assertEquals(1, context.measure("file2:src/procedures/test2.p", OpenEdgeMetrics.PROCEDURES_KEY).value());
  }

  private SensorContextTester createContext() throws IOException {
    SensorContextTester context = SensorContextTester.create(moduleBaseDir);
    context.settings().setProperty("sonar.sources", "src");
    context.settings().setProperty("sonar.oe.binaries", "build");
    context.fileSystem().add(
        new DefaultInputFile("file1", FILE1).setLanguage(Constants.LANGUAGE_KEY).setType(Type.MAIN).initMetadata(
            Files.toString(new File(moduleBaseDir, FILE1), Charset.defaultCharset())));
    context.fileSystem().add(
        new DefaultInputFile("file2", FILE2).setLanguage(Constants.LANGUAGE_KEY).setType(Type.MAIN).initMetadata(
            Files.toString(new File(moduleBaseDir, FILE2), Charset.defaultCharset())));

    return context;
  }

  private IIdProvider createIdProvider() {
    return new IIdProvider() {

      @Override
      public String getPermanentID() {
        return "";
      }

      @Override
      public boolean isSonarLintSide() {
        return false;
      }
    };
  }

}
