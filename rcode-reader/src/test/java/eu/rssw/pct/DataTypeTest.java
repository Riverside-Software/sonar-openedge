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

import org.testng.Assert;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.PrimitiveDataType;

public class DataTypeTest {

  @Test
  public void test1() {
    Assert.assertEquals(PrimitiveDataType.getDataType(-1), PrimitiveDataType.UNKNOWN);
    Assert.assertEquals(PrimitiveDataType.getDataType(0), PrimitiveDataType.VOID);
    Assert.assertEquals(PrimitiveDataType.getDataType(48), PrimitiveDataType.RUNTYPE);
    Assert.assertEquals(PrimitiveDataType.getDataType(49), PrimitiveDataType.UNKNOWN);
  }

  @Test
  public void test2() {
    Assert.assertEquals(PrimitiveDataType.getDataType("-1"), PrimitiveDataType.UNKNOWN);
    Assert.assertEquals(PrimitiveDataType.getDataType("0"), PrimitiveDataType.VOID);
    Assert.assertEquals(PrimitiveDataType.getDataType("48"), PrimitiveDataType.RUNTYPE);
    Assert.assertEquals(PrimitiveDataType.getDataType("49"), PrimitiveDataType.UNKNOWN);
    // Really ?
    Assert.assertEquals(PrimitiveDataType.getDataType(""), PrimitiveDataType.CLASS);
    Assert.assertEquals(PrimitiveDataType.getDataType("Progress.Lang.Object"), PrimitiveDataType.CLASS);
  }

}
