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
import org.sonar.plugins.openedge.foundation.OpenEdgeMetrics;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeListingSensorTest {
  private final static String BASEDIR = "src/test/resources/project1";
  private final static String FILE1 = "src/procedures/test1.p";
  private final static String FILE2 = "src/procedures/test2.p";

  @Test
  public void testListing() throws Exception {
    SensorContextTester context = createContext();

    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.settings(), context.fileSystem());
    OpenEdgeListingSensor sensor = new OpenEdgeListingSensor(oeSettings, context.fileSystem());
    sensor.execute(context);

    Assert.assertEquals(context.measure(BASEDIR + ":" + FILE1, OpenEdgeMetrics.NUM_TRANSACTIONS_KEY).value(),
        1, "Wrong number of transactions");
    Assert.assertEquals(context.measure(BASEDIR + ":" + FILE2, OpenEdgeMetrics.NUM_TRANSACTIONS_KEY).value(),
        0, "Wrong number of transactions");
    Assert.assertEquals(context.allIssues().size(), 1, "Wrong total number of issues");
    Assert.assertEquals(context.allIssues().iterator().next().ruleKey().rule(),
        OpenEdgeRulesDefinition.LARGE_TRANSACTION_SCOPE, "Wrong issue type");
  }

  private SensorContextTester createContext() throws IOException {
    SensorContextTester context = SensorContextTester.create(new File(BASEDIR));
    context.settings().setProperty("sonar.sources", "src");
    context.settings().setProperty("sonar.oe.binaries", "build");
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, FILE1).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, FILE1), Charset.defaultCharset())).build());
    context.fileSystem().add(
        new TestInputFileBuilder(BASEDIR, FILE2).setLanguage(Constants.LANGUAGE_KEY).setType(
            Type.MAIN).initMetadata(Files.toString(new File(BASEDIR, FILE2), Charset.defaultCharset())).build());

    return context;
  }

}
