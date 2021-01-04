/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2019-2020 Riverside Software
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
package org.sonar.plugins.openedge.checks;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Iterator;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class FixedLineNumberIssueTest extends AbstractTest {
  private RuleKey ruleKey;

  @BeforeTest
  public void init() {
    ruleKey = RuleKey.parse("rssw-oe-main:FixedLineNumber");
  }

  @Test
  public void test1() {
    InputFile inputFile = getInputFile("fixed01.p");
    FixedLineNumberRule rule = new FixedLineNumberRule();
    rule.setContext(ruleKey, context, null);
    rule.sensorExecute(inputFile, getParseUnit(inputFile));

    assertEquals(context.allIssues().size(), 2);
    Iterator<Issue> iter = context.allIssues().iterator();
    Issue issue1 = iter.next();
    assertEquals(issue1.primaryLocation().textRange().start().line(), 1);
    Issue issue2 = iter.next();
    assertNull(issue2.primaryLocation().textRange());
  }

}
