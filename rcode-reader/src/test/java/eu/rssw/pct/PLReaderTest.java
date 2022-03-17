/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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
import java.nio.file.Paths;

import org.testng.annotations.Test;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

public class PLReaderTest {

  @Test
  public void testRCodeInPL() throws IOException, InvalidRCodeException {
    PLReader pl = new PLReader(Paths.get("src/test/resources/ablunit.pl"));
    assertNotNull(pl.getEntry("OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r"));
    RCodeInfo rci = new RCodeInfo(pl.getInputStream(pl.getEntry("OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r")));
    assertTrue(rci.isClass());
    assertTrue(rci.getTypeInfo().getMethods().size() > 0);
    assertTrue(rci.getTypeInfo().getProperties().size() > 0);
    assertTrue(rci.getTypeInfo().getTables().size() == 0);
  }

}
