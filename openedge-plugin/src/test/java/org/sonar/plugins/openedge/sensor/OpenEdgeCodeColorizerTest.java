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
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST1;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST2;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.PROC_TEST3;

import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.openedge.OpenEdgePluginTest;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeCodeColorizerTest {

  @Test
  public void testSp2k() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(),
        OpenEdgePluginTest.SONARQUBE_RUNTIME);
    OpenEdgeComponents components = new OpenEdgeComponents(context.config());
    OpenEdgeCodeColorizer sensor = new OpenEdgeCodeColorizer(oeSettings, components);
    sensor.execute(context);

    // Comments
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST1, 1, 10));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST1, 1, 10).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST1, 1, 10).get(0), TypeOfText.COMMENT);

    // Quoted string
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST2, 5, 13));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST2, 5, 13).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST2, 5, 13).get(0), TypeOfText.STRING);

    // Keyword
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST2, 5, 5));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST2, 5, 5).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST2, 5, 5).get(0), TypeOfText.KEYWORD);

    // Preprocessor
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 3, 25));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 3, 25).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 3, 25).get(0),
        TypeOfText.PREPROCESS_DIRECTIVE);
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 4, 10));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 4, 10).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 4, 10).get(0), TypeOfText.STRING);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 4, 16).size(), 0);

    // Constants
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 14, 9));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 14, 9).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 14, 9).get(0), TypeOfText.CONSTANT);

    // Include file
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 21, 3));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 21, 3).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 21, 3).get(0),
        TypeOfText.PREPROCESS_DIRECTIVE);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 21, 20).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + PROC_TEST3, 21, 20).get(0),
        TypeOfText.PREPROCESS_DIRECTIVE);
  }

}
