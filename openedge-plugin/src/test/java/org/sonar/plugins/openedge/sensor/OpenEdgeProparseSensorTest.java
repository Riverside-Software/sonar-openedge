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
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.google.common.io.Files;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeProparseSensorTest {
  private final static String BASEDIR = "src/test/resources/project1";
  private final static String FILE3 = "src/procedures/test3.p";
  private final static String CLASS1 = "src/classes/rssw/testclass.cls";

  @Test
  public void testCPDPreprocessorExpansion() throws Exception {
    SensorContextTester context = createContext();
    context.settings().setProperty(Constants.CPD_ANNOTATIONS, "Generated,rssw.lang.Generated");
    context.settings().setProperty(Constants.CPD_METHODS, "TEST3");
    context.settings().setProperty(Constants.CPD_PROCEDURES, "adm-create-objects");

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.settings(), context.fileSystem());
    OpenEdgeComponents components = new OpenEdgeComponents(null, null);
    OpenEdgeProparseSensor sensor = new OpenEdgeProparseSensor(oeSettings, components);
    sensor.execute(context);

    Assert.assertNotNull(context.cpdTokens(BASEDIR + ":" + FILE3));
    Assert.assertEquals(context.cpdTokens(BASEDIR + ":" + FILE3).size(), 7);
    Assert.assertNotNull(context.cpdTokens(BASEDIR + ":" + CLASS1));
    Assert.assertEquals(context.cpdTokens(BASEDIR + ":" + CLASS1).size(), 11);
  }

  private SensorContextTester createContext() throws IOException {
    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.settings().setProperty("sonar.sources", "src");
    context.settings().setProperty("sonar.oe.binaries", "build");
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, FILE3).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, FILE3), Charset.defaultCharset())).build());
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, CLASS1).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, CLASS1), Charset.defaultCharset())).build());

    return context;
  }

}
