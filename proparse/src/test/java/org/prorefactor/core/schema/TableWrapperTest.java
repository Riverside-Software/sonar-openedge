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
package org.prorefactor.core.schema;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Path;

import org.testng.annotations.Test;

public class TableWrapperTest {

  @Test
  public void testFromDotSchema01() throws IOException {
    var sch1 = new Schema(Path.of("src/test/resources/schema/sports2000.cache"));

    var fld1 = sch1.lookupUnqualifiedField("minqty", false);
    assertNotNull(fld1);
    var fld2 = sch1.lookupUnqualifiedField("minqt", false);
    assertNotNull(fld2);
    var fld3 = sch1.lookupUnqualifiedField("custnum", false);
    assertNotNull(fld3);
    assertEquals(fld3.getTable().getName(), "BillTo");

    var tbl1 = sch1.lookupTable("customer");
    assertNotNull(tbl1);
    var tbl2 = sch1.lookupTable("custome");
    assertNotNull(tbl2);
    var tbl3 = sch1.lookupTable("bin");
    assertNotNull(tbl3);
    var fld4 = tbl3.lookupField("binnum");
    assertNotNull(fld4);
    assertEquals(fld4.getName(), "BinNum");
    var fld5 = tbl3.lookupField("binnu");
    assertNotNull(fld5);
    assertEquals(fld5.getName(), "BinNum");
    var fld6 = tbl3.lookupField("binna");
    assertNotNull(fld6);
    assertEquals(fld6.getName(), "BinName");
    var tbl4 = sch1.lookupTable("XXXTbl");
    assertNotNull(tbl4);
    assertNotNull(tbl4.lookupField("guid"));
    assertNotNull(tbl4.lookupField("regext"));
    var tbl5 = sch1.lookupTable("Timesheet");
    assertNotNull(tbl5);
    // No metaschema injected in this test case
    assertNull(sch1.lookupTable("_File"));
    // TODO Ambiguous field, should return null
    // IField fld7 = tbl3.lookupField("binn");
    // Assert.assertNull(fld7);
  }

  @Test
  public void testFromDotSchema02() throws IOException {
    // Inject metaschema
    var sch1 = new Schema(Path.of("src/test/resources/schema/sports2000.cache"), true);
    assertNotNull(sch1.lookupTable("customer"));
    assertNotNull(sch1.lookupTable("_File"));
    assertNull(sch1.lookupTable("_FooBar"));
  }
}
