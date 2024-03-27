/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2024 Riverside Software
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.function.Function;

import org.testng.annotations.Test;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.PrimitiveDataType;

public class DataTypeTest {
  private static final Function<String, ITypeInfo> TYPE_INFO_PROVIDER = name -> //
  BuiltinClasses.getBuiltinClasses().stream() //
    .filter(it -> it.getTypeName().equals(name)) //
    .findFirst() //
    .orElse(null);

  @Test
  public void test1() {
    assertEquals(PrimitiveDataType.getDataType(-1), PrimitiveDataType.UNKNOWN);
    assertEquals(PrimitiveDataType.getDataType(0), PrimitiveDataType.VOID);
    assertEquals(PrimitiveDataType.getDataType(48), PrimitiveDataType.RUNTYPE);
    assertEquals(PrimitiveDataType.getDataType(49), PrimitiveDataType.UNKNOWN);
  }

  @Test
  public void test1bis() {
    for (int zz = 0; zz <= PrimitiveDataType.LAST_VALUE; zz++) {
      assertNotNull(DataType.get(zz));
      if ((zz != 6)&&(zz!=9)&&(zz!=16)&&(zz!=30)&&(zz!=38)&&(zz!=42)&&(zz!=45)&&(zz!=47))
      assertNotEquals(DataType.get(zz), DataType.UNKNOWN, "Unknown datatype for value " + zz);
    }
    assertEquals(DataType.get(10), DataType.HANDLE);
    assertEquals(DataType.get(11), DataType.MEMPTR);
  }

  @Test
  public void test2() {
    assertEquals(PrimitiveDataType.getDataType("-1"), PrimitiveDataType.UNKNOWN);
    assertEquals(PrimitiveDataType.getDataType("0"), PrimitiveDataType.VOID);
    assertEquals(PrimitiveDataType.getDataType("48"), PrimitiveDataType.RUNTYPE);
    assertEquals(PrimitiveDataType.getDataType("49"), PrimitiveDataType.UNKNOWN);
    // Really ?
    assertEquals(PrimitiveDataType.getDataType(""), PrimitiveDataType.CLASS);
    assertEquals(PrimitiveDataType.getDataType("Progress.Lang.Object"), PrimitiveDataType.CLASS);
  }

  @Test
  public void test2bis() {
    assertEquals(DataType.get("HANDLE"), DataType.HANDLE);
    assertEquals(DataType.get("memptr"), DataType.MEMPTR);
    assertEquals(DataType.get("ROWID"), DataType.ROWID);
    assertEquals(DataType.get("com_handle"), DataType.COMPONENT_HANDLE);
    assertEquals(DataType.get("COMPONENT_HANDLE"), DataType.COMPONENT_HANDLE);
    assertEquals(DataType.get("unsigned-short"), DataType.UNSIGNED_SHORT);
    assertEquals(DataType.get("unsigned-BYTE"), DataType.UNSIGNED_BYTE);
    assertEquals(DataType.get("unsigned-integer"), DataType.UNSIGNED_INTEGER);
    assertEquals(DataType.get("unsigned_INT64"), DataType.UNSIGNED_INT64);
    assertEquals(DataType.get("currency"), DataType.CURRENCY);
    assertEquals(DataType.get("single-character"), DataType.SINGLE_CHARACTER);
    assertEquals(DataType.get("foobar"), DataType.UNKNOWN);
    assertEquals(DataType.get(""), DataType.UNKNOWN);
    assertEquals(DataType.get(null), DataType.UNKNOWN);
  }

  @Test
  public void testEquals() {
    assertTrue(DataType.HANDLE.equals(DataType.get("HANDLE")));
    assertFalse(DataType.HANDLE.equals(DataType.CHARACTER));
    assertTrue(new DataType("Progress.Lang.Object").equals(new DataType("Progress.Lang.Object")));
    assertFalse(new DataType("Progress.Lang.Object").equals(new DataType("Progress.Lang2.Object")));
  }

  @Test
  public void testIsCompatible() {
    assertTrue(DataType.LONGCHAR.isCompatible(DataType.CHARACTER, null));
    assertFalse(DataType.CHARACTER.isCompatible(DataType.LONGCHAR, null));
    assertTrue(DataType.DECIMAL.isCompatible(DataType.INTEGER, null));
    assertFalse(DataType.INTEGER.isCompatible(DataType.DECIMAL, null));
    // Classes
    assertTrue(new DataType("Progress.Lang.Object").isCompatible(new DataType("Progress.Lang.Enum"), TYPE_INFO_PROVIDER));
    assertTrue(new DataType("Progress.Lang.Object").isCompatible(new DataType("Progress.BPM.BPMError"), TYPE_INFO_PROVIDER));
    assertTrue(new DataType("Progress.Lang.SysError").isCompatible(new DataType("Progress.BPM.BPMError"), TYPE_INFO_PROVIDER));
    assertTrue(new DataType("Progress.Lang.Error").isCompatible(new DataType("Progress.BPM.BPMError"), TYPE_INFO_PROVIDER));
    assertFalse(new DataType("Progress.Lang.Enum").isCompatible(new DataType("Progress.Lang.Object"), TYPE_INFO_PROVIDER));
  }
}
