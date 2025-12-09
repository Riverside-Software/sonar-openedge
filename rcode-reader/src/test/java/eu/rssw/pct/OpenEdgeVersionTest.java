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
package eu.rssw.pct;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import eu.rssw.pct.mapping.OpenEdgeVersion;

public class OpenEdgeVersionTest {

  @Test
  public void test01() {
    assertEquals(OpenEdgeVersion.getVersion(null), OpenEdgeVersion.V128);
    assertEquals(OpenEdgeVersion.getVersion(""), OpenEdgeVersion.V128);
    assertEquals(OpenEdgeVersion.getVersion("11.5"), OpenEdgeVersion.V117);
    assertEquals(OpenEdgeVersion.getVersion("11.7"), OpenEdgeVersion.V117);
    assertEquals(OpenEdgeVersion.getVersion("13.0ALPHA"), OpenEdgeVersion.V130);
    assertEquals(OpenEdgeVersion.getVersion("12"), OpenEdgeVersion.V128);
    assertEquals(OpenEdgeVersion.getVersion("12.x"), OpenEdgeVersion.V122);
    assertEquals(OpenEdgeVersion.getVersion("12.0"), OpenEdgeVersion.V122);
    assertEquals(OpenEdgeVersion.getVersion("12.2"), OpenEdgeVersion.V122);
    assertEquals(OpenEdgeVersion.getVersion("12.3"), OpenEdgeVersion.V128);
  }
}
