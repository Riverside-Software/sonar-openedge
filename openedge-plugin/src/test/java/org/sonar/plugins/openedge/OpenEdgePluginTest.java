/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
package org.sonar.plugins.openedge;

import org.sonar.api.Plugin;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenEdgePluginTest {

  @Test
  public void testExtensionsSonarLint() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(Version.parse("6.2"));
    Plugin.Context context = new Plugin.Context(runtime);
    new OpenEdgePlugin().define(context);
    assertThat(context.getExtensions()).hasSize(25);
  }

  @Test
  public void testExtensionsSonarQube() {
    SonarRuntime runtime = SonarRuntimeImpl.forSonarQube(Version.parse("6.2"), SonarQubeSide.SCANNER);
    Plugin.Context context = new Plugin.Context(runtime);
    new OpenEdgePlugin().define(context);
    assertThat(context.getExtensions()).hasSize(28);
  }

}
