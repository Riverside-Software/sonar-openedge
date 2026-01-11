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
package org.sonar.plugins.openedge.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.impl.server.RulesDefinitionContext;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RuleDescriptionSection.RuleDescriptionSectionKeys;
import org.sonar.api.utils.Version;
import org.sonar.check.Rule;
import org.sonar.plugins.openedge.api.model.CWE;
import org.sonar.plugins.openedge.api.model.OWASP;
import org.sonar.plugins.openedge.api.model.OWASP2021;
import org.sonar.plugins.openedge.api.model.SecurityHotspot;
import org.testng.annotations.Test;

public class AnnotationBasedRulesDefinitionTest {
  private SonarRuntime sqRuntime10 = SonarRuntimeImpl.forSonarQube(Version.create(10, 8), SonarQubeSide.SERVER,
      SonarEdition.COMMUNITY);


  @Rule(key = "CustomRule01", name = "Number 1", tags = {"security"})
  @CWE(values = {1, 2})
  private static class CustomRule01 {
    // Nothing
  }

  @Rule(key = "CustomRule02", name = "Number 2", tags = {"rssw"})
  @CWE(values = {3})
  @OWASP(values = {"A1"})
  @SecurityHotspot
  private static class CustomRule02 {
    // Nothing
  }

  @Rule(key = "CustomRule03", name = "Number 3", tags = {"rssw"})
  @CWE(values = {3})
  @OWASP(values = {"A1"})
  @SecurityHotspot(cwe = {4}, owasp = {"A2"})
  private static class CustomRule03 {
    // Nothing
  }

  @Rule(key = "CustomRule04", name = "Number 4", tags = {"rssw", "security"})
  @CWE(values = {10})
  @OWASP2021(values = {"A9"})
  private static class CustomRule04 {
    // Nothing
  }

  @Test
  public void testCustomRule() {
    var context = new RulesDefinitionContext();
    var repo = context.createRepository("repo1", "oe");
    var def = new AnnotationBasedRulesDefinition(repo, "oe", sqRuntime10);
    def.addRuleClasses(
        Arrays.asList(new Class[] {CustomRule01.class, CustomRule02.class, CustomRule03.class, CustomRule04.class}));
    repo.done();

    assertEquals(context.repository("repo1").rules().size(), 4);

    var rule1 = context.repository("repo1").rule("CustomRule01");
    assertEquals(rule1.name(), "Number 1");
    assertEquals(rule1.type(), RuleType.VULNERABILITY);
    assertTrue(rule1.securityStandards().contains("cwe:1"));
    assertTrue(rule1.securityStandards().contains("cwe:2"));
    assertTrue(rule1.htmlDescription().startsWith("<p>Introduction</p>"));
    assertEquals(rule1.ruleDescriptionSections().size(), 4);
    assertEquals(rule1.ruleDescriptionSections().get(0).getKey(), RuleDescriptionSectionKeys.INTRODUCTION_SECTION_KEY);
    assertEquals(rule1.ruleDescriptionSections().get(0).getHtmlContent(), "<p>Introduction</p>");
    assertEquals(rule1.ruleDescriptionSections().get(1).getKey(), RuleDescriptionSectionKeys.ROOT_CAUSE_SECTION_KEY);
    assertEquals(rule1.ruleDescriptionSections().get(1).getHtmlContent(), "<p>Why</p>");
    assertEquals(rule1.ruleDescriptionSections().get(2).getKey(), RuleDescriptionSectionKeys.HOW_TO_FIX_SECTION_KEY);
    assertEquals(rule1.ruleDescriptionSections().get(2).getHtmlContent(), "<p>How</p>");
    assertEquals(rule1.ruleDescriptionSections().get(3).getKey(), RuleDescriptionSectionKeys.RESOURCES_SECTION_KEY);
    assertEquals(rule1.ruleDescriptionSections().get(3).getHtmlContent(), "<p>Resources</p>");

    var rule2 = context.repository("repo1").rule("CustomRule02");
    assertEquals(rule2.name(), "Number 2");
    assertEquals(rule2.type(), RuleType.SECURITY_HOTSPOT);
    assertTrue(rule2.securityStandards().contains("cwe:3"));
    assertTrue(rule2.securityStandards().contains("owaspTop10:a1"));
    assertTrue(rule2.htmlDescription().startsWith("<p>Introduction 2</p>"));
    assertEquals(rule2.ruleDescriptionSections().size(), 6);
    assertEquals(rule2.ruleDescriptionSections().get(0).getKey(), RuleDescriptionSectionKeys.INTRODUCTION_SECTION_KEY);
    assertEquals(rule2.ruleDescriptionSections().get(0).getHtmlContent(), "<p>Introduction 2</p>");
    assertEquals(rule2.ruleDescriptionSections().get(1).getKey(), RuleDescriptionSectionKeys.ROOT_CAUSE_SECTION_KEY);
    assertEquals(rule2.ruleDescriptionSections().get(1).getHtmlContent(), "<p>Why 2</p>");
    assertEquals(rule2.ruleDescriptionSections().get(2).getKey(), RuleDescriptionSectionKeys.HOW_TO_FIX_SECTION_KEY);
    assertEquals(rule2.ruleDescriptionSections().get(2).getHtmlContent(), "<p>How 2.1</p>");
    assertEquals(rule2.ruleDescriptionSections().get(2).getContext().get().getKey(), "f1");
    assertEquals(rule2.ruleDescriptionSections().get(2).getContext().get().getDisplayName(), "F1");
    assertEquals(rule2.ruleDescriptionSections().get(3).getKey(), RuleDescriptionSectionKeys.HOW_TO_FIX_SECTION_KEY);
    assertEquals(rule2.ruleDescriptionSections().get(3).getHtmlContent(), "<p>How 2.2</p>");
    assertEquals(rule2.ruleDescriptionSections().get(3).getContext().get().getKey(), "f2");
    assertEquals(rule2.ruleDescriptionSections().get(3).getContext().get().getDisplayName(), "F2");
    assertEquals(rule2.ruleDescriptionSections().get(4).getKey(), RuleDescriptionSectionKeys.HOW_TO_FIX_SECTION_KEY);
    assertEquals(rule2.ruleDescriptionSections().get(4).getHtmlContent(), "<p>How 2.3</p>");
    assertEquals(rule2.ruleDescriptionSections().get(4).getContext().get().getKey(), "f3");
    assertEquals(rule2.ruleDescriptionSections().get(4).getContext().get().getDisplayName(), "F3");
    assertEquals(rule2.ruleDescriptionSections().get(5).getKey(), RuleDescriptionSectionKeys.RESOURCES_SECTION_KEY);
    assertEquals(rule2.ruleDescriptionSections().get(5).getHtmlContent(), "<p>Resources 2</p>");

    var rule3 = context.repository("repo1").rule("CustomRule03");
    assertEquals(rule3.name(), "Number 3");
    assertEquals(rule3.type(), RuleType.SECURITY_HOTSPOT);
    assertTrue(rule3.securityStandards().contains("cwe:3"));
    assertTrue(rule3.securityStandards().contains("owaspTop10:a1"));
    // CWE and OWASP attributes of @SecurityHotspot are not parsed anymore
    assertFalse(rule3.securityStandards().contains("cwe:4"));
    assertFalse(rule3.securityStandards().contains("owaspTop10:a2"));
    assertEquals(rule3.htmlDescription(), "<p>Rule03</p>");

    var rule4 = context.repository("repo1").rule("CustomRule04");
    assertEquals(rule4.name(), "Number 4");
    assertEquals(rule4.type(), RuleType.VULNERABILITY);
    assertTrue(rule4.securityStandards().contains("cwe:10"));
    assertTrue(rule4.securityStandards().contains("owaspTop10-2021:a9"));
    assertEquals(rule4.htmlDescription(), "<p>Rule04</p>");  }

}
