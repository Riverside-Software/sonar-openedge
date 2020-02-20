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

import java.io.IOException;
import java.util.Iterator;

import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.foundation.OpenEdgeRulesDefinition;
import org.sonar.plugins.openedge.foundation.OpenEdgeSettings;
import org.sonar.plugins.openedge.utils.TestProjectSensorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenEdgeWarningsSensorTest {
  private static final Version VERSION = Version.parse("7.5");

  @Test
  public void testWarnings() throws IOException {
    SensorContextTester context = TestProjectSensorContext.createContext();
    context.setActiveRules(createRules());
    OpenEdgeSettings oeSettings = new OpenEdgeSettings(context.config(), context.fileSystem(), SonarRuntimeImpl.forSonarQube(VERSION, SonarQubeSide.SCANNER, SonarEdition.COMMUNITY));
    OpenEdgeWarningsSensor sensor = new OpenEdgeWarningsSensor(oeSettings);
    sensor.execute(context);

    // Case-sensitive, so one issue can't be reported
    Assert.assertEquals(context.allIssues().size(), 3);
    Iterator<Issue> issues = context.allIssues().iterator();
    Issue issue;

    // Starts with ../
    issue = issues.next();
    assertEquals(issue.primaryLocation().inputComponent().key(),
        TestProjectSensorContext.BASEDIR + ":" + TestProjectSensorContext.FILE4);
    assertEquals(issue.primaryLocation().textRange().start().line(), 2);

    issue = issues.next();
    assertEquals(issue.primaryLocation().inputComponent().key(),
        TestProjectSensorContext.BASEDIR + ":" + TestProjectSensorContext.FILE4);
    assertEquals(issue.primaryLocation().textRange().start().line(), 3);

    issue = issues.next();
    assertEquals(issue.primaryLocation().inputComponent().key(),
        TestProjectSensorContext.BASEDIR + ":" + TestProjectSensorContext.FILE1);
    assertEquals(issue.primaryLocation().textRange().start().line(), 1);
    // Verify that leading 'WARNING' is removed, as well as the message number
    assertEquals(issue.primaryLocation().message(),
        "Program src\\procedures\\sample\\inc\\test.i, Line 1 is an expression statement that evaluates to a constant.");
  }

  private ActiveRules createRules() {
    return new ActiveRulesBuilder().addRule(new NewActiveRule.Builder().setRuleKey(
        RuleKey.of(Constants.STD_REPOSITORY_KEY, OpenEdgeRulesDefinition.COMPILER_WARNING_RULEKEY)).setLanguage(
            Constants.LANGUAGE_KEY).build()).build();
  }

}
