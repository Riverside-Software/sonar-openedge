/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
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

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

public class PLReaderTest {

  @Test
  public void testRCodeInPL() throws IOException, InvalidRCodeException {
    PLReader pl = new PLReader(new File("src/test/resources/ablunit.pl"));
    Assert.assertNotNull(pl.getEntry("OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r"));
    RCodeInfo rci = new RCodeInfo(pl.getInputStream(pl.getEntry("OpenEdge/ABLUnit/Reflection/ClassAnnotationInfo.r")));
    Assert.assertTrue(rci.isClass());
    Assert.assertTrue(rci.getTypeInfo().getMethods().size() > 0);
    Assert.assertTrue(rci.getTypeInfo().getProperties().size() > 0);
    Assert.assertTrue(rci.getTypeInfo().getTables().size() == 0);
  }

}
