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

import java.io.IOException;

import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeWarningsSensorTest {
  @Test
  public void testWarnings() throws IOException {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setActiveRules(createRules());
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.settings(), context.fileSystem());
    OpenEdgeWarningsSensor sensor = new OpenEdgeWarningsSensor(oeSettings);
    sensor.execute(context);

    Assert.assertEquals(context.allIssues().size(), 4);
  }

  private ActiveRules createRules() {
    return new ActiveRulesBuilder().create(RuleKey.of(Constants.STD_REPOSITORY_KEY,
        OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY)).activate().build();
  }

}
