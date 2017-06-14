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

import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.BASEDIR;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.CLASS1;
import static org.sonar.plugins.openedge.utils.TestProjectSensorContext.FILE3;

import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeComponents;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeProparseSensorTest {

  @Test
  public void testCPDPreprocessorExpansion() throws Exception {
    SensorContextTester context = TestProjectSensorContext.createContext();
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

}
