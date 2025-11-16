/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
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
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.SP2K_DF;

import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeDBColorizerTest {

  @Test
  public void testSp2k() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
    OpenEdgeDBColorizer sensor = new OpenEdgeDBColorizer();
    sensor.execute(context);

    // SEQUENCE keyword
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 19, 10));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 19, 10).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 19, 10).get(0), TypeOfText.KEYWORD);

    // Quoted string
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 19, 20));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 19, 20).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 19, 20).get(0), TypeOfText.STRING);

    // Constant
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 20, 12));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 20, 12).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 20, 12).get(0), TypeOfText.CONSTANT);

    // Annotation
    Assert.assertNotNull(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 79, 10));
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 79, 10).size(), 1);
    Assert.assertEquals(context.highlightingTypeAt(BASEDIR + ":" + SP2K_DF, 79, 10).get(0), TypeOfText.ANNOTATION);
  }

}
