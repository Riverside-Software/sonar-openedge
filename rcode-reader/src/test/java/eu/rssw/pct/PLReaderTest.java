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
package eu.rssw.pct;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.testng.annotations.Test;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

public class PLReaderTest {

  @Test
  public void testRCodeInPL() throws IOException, InvalidRCodeException {
    var pl = new PLReader(Path.of("src/test/resources/ablunit.pl"));
    var entry = pl.getEntry("OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r");
    assertNotNull(entry);
    var rci = new RCodeInfo(pl.getInputStream(entry));
    assertTrue(rci.isClass());
    assertTrue(!rci.getTypeInfo().getMethods().isEmpty());
    assertTrue(!rci.getTypeInfo().getProperties().isEmpty());
    assertTrue(rci.getTypeInfo().getTables().isEmpty());
  }

}
