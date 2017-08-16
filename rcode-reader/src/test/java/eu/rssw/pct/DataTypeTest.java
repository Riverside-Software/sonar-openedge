/*
 * RCode library - OpenEdge plugin for SonarQube
 * Copyright (C) 2017 Riverside Software
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

public class DataTypeTest {

  @Test
  public void test1() {
    Assert.assertEquals(DataType.getDataType(-1), DataType.UNKNOWN);
    Assert.assertEquals(DataType.getDataType(0), DataType.VOID);
    Assert.assertEquals(DataType.getDataType(48), DataType.RUNTYPE);
    Assert.assertEquals(DataType.getDataType(49), DataType.UNKNOWN);
  }

  @Test
  public void test2() {
    Assert.assertEquals(DataType.getDataType("-1"), DataType.UNKNOWN);
    Assert.assertEquals(DataType.getDataType("0"), DataType.VOID);
    Assert.assertEquals(DataType.getDataType("48"), DataType.RUNTYPE);
    Assert.assertEquals(DataType.getDataType("49"), DataType.UNKNOWN);
    // Really ?
    Assert.assertEquals(DataType.getDataType(""), DataType.CLASS);
    Assert.assertEquals(DataType.getDataType("Progress.Lang.Object"), DataType.CLASS);
  }

}
