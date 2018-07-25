/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2018 Riverside Software
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

import java.util.Iterator;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ClumsySyntaxTest extends AbstractTest {
  private RuleKey ruleKey;

  @BeforeTest
  public void init() {
    ruleKey = RuleKey.parse("rssw-oe-main:ClumsySyntax");
  }

  @Test(enabled=false)
  public void test1() {
    InputFile inputFile = getInputFile("clumsy01.p");
    ClumsySyntax rule = new ClumsySyntax(ruleKey, context, null);
    rule.sensorExecute(inputFile, getParseUnit(inputFile));

    Assert.assertEquals(context.allIssues().size(), 3);
    Iterator<Issue> iter = context.allIssues().iterator();
    Issue issue1 = iter.next();
    Issue issue2 = iter.next();
    Issue issue3 = iter.next();
    Assert.assertEquals(issue1.primaryLocation().textRange().start().line(), 1);
    Assert.assertEquals(issue2.primaryLocation().textRange().start().line(), 2);
    Assert.assertEquals(issue3.primaryLocation().textRange().start().line(), 3);
  }

  @Test
  public void test2() {
    InputFile inputFile = getInputFile("clumsy02.cls");
    ClumsySyntax rule = new ClumsySyntax(ruleKey, context, null);
    rule.sensorExecute(inputFile, getParseUnit(inputFile));

    Assert.assertEquals(context.allIssues().size(), 1);
    Iterator<Issue> iter = context.allIssues().iterator();
    Issue issue1 = iter.next();
    Assert.assertEquals(issue1.primaryLocation().textRange().start().line(), 5);
  }

}
