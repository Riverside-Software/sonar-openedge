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
package org.sonar.plugins.openedge.foundation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.impl.server.RulesDefinitionContext;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.testng.annotations.Test;

public class OpenEdgeRulesDefinitionTest {
  private SonarRuntime sqRuntime10 = SonarRuntimeImpl.forSonarQube(Version.create(10, 8), SonarQubeSide.SERVER,
      SonarEdition.COMMUNITY);

  @Test
  public void testMainRules() {
    var rulesDef = new OpenEdgeRulesDefinition(sqRuntime10);
    var context = new RulesDefinitionContext();
    rulesDef.define(context);

    var repo = context.repository("rssw-oe");
    assertNotNull(repo);
    assertEquals(repo.name(), "Standard rules");
    assertEquals(repo.language(), "oe");

    assertEquals(repo.rules().stream().filter(it -> it.key().startsWith("compiler.warning")).count(), 14);
    assertEquals(repo.rules().stream().filter(it -> !it.key().startsWith("compiler.warning")).count(), 3);
    var cw = repo.rule("compiler.warning");
    assertNotNull(cw);
    assertTrue(cw.tags().contains("compiler-warnings"));

    var proparseError = repo.rule(OpenEdgeRulesDefinition.PROPARSE_ERROR_RULEKEY);
    assertNotNull(proparseError);
  }

  @Test
  public void testDbRules() {
    var rulesDef = new OpenEdgeRulesDefinition(sqRuntime10);
    var context = new RulesDefinitionContext();
    rulesDef.define(context);

    var repo = context.repository("rssw-oedb");
    assertNotNull(repo);
    assertEquals(repo.name(), "Standard rules");
    assertEquals(repo.language(), "oedb");

    assertEquals(repo.rules().size(), 1);
  }

}
