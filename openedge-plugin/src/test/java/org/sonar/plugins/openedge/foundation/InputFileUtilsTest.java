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
package org.sonar.plugins.openedge.foundation;

import java.io.File;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InputFileUtilsTest {

  @SuppressWarnings("deprecation")
  @Test
  public void testSp2k() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    InputFile f1 = context.fileSystem().inputFile(context.fileSystem().predicates().hasFilename("test1.p"));
    Assert.assertEquals(InputFileUtils.getFile(f1), f1.file());
    System.out.println(f1.toString());
    Assert.assertEquals(InputFileUtils.getRelativePath(f1, context.fileSystem()), f1.relativePath());
  }

  @Test
  public void testDifferentFileSystem() throws Exception {
    // Only on Windows...
    if (!System.getProperty("os.name").toLowerCase().startsWith("win"))
      return;

    SensorContextTester context = TestProjectSensorContext.createContext();
    InputFile f1 = context.fileSystem().inputFile(context.fileSystem().predicates().hasFilename("test1.p"));
    // Different file system, we just return the file name
    // Will fail in the unexpected case when unit tests are executed on the X: drive
    Assert.assertEquals(InputFileUtils.getRelativePath(f1, new DefaultFileSystem(new File("X:\\"))), "test1.p");
  }

}
