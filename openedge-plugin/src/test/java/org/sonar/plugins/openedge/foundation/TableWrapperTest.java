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
package org.sonar.plugins.openedge.foundation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.nio.file.Path;

import org.prorefactor.core.schema.Schema;
import org.sonar.plugins.openedge.api.objects.DatabaseWrapper;
import org.testng.annotations.Test;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.pct.mapping.OpenEdgeVersion;

public class TableWrapperTest {

  @Test
  public void testFromDotDF01() throws IOException {
    var dbDesc = DumpFileUtils.getDatabaseDescription(Path.of("src/test/resources/project1/src/schema/sp2k.df"));
    var sch = new Schema(new DatabaseWrapper(dbDesc));

    var fld1 = sch.lookupUnqualifiedField("minqty", false);
    assertNotNull(fld1);
    var fld2 = sch.lookupUnqualifiedField("minqt", false);
    assertNotNull(fld2);
    var fld3 = sch.lookupUnqualifiedField("custnum", false);
    assertNotNull(fld3);
    assertEquals(fld3.getTable().getName(), "BillTo");

    var tbl1 = sch.lookupTable("customer");
    assertNotNull(tbl1);
    var tbl2 = sch.lookupTable("custome");
    assertNotNull(tbl2);
    var tbl3 = sch.lookupTable("bin");
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
    // No metaschema injected in this test case
    assertNull(sch.lookupTable("_File"));
    // TODO Ambiguous field, should return null
    // IField fld7 = tbl3.lookupField("binn");
    // Assert.assertNull(fld7);
  }

  @Test
  public void testFromDotDF02() throws IOException {
    var dbDesc = DumpFileUtils.getDatabaseDescription(Path.of("src/test/resources/project1/src/schema/sp2k.df"));
    var sch = new Schema(new DatabaseWrapper(dbDesc, OpenEdgeVersion.V128));
    assertNotNull(sch.lookupTable("customer"));
    assertNotNull(sch.lookupTable("_File"));
    assertNull(sch.lookupTable("_FooBar"));
  }

}
