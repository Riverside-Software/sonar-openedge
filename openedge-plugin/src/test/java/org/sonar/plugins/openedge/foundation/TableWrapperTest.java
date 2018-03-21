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
package org.sonar.plugins.openedge.foundation;

import java.io.File;
import java.io.IOException;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.Schema;
import org.sonar.plugins.openedge.api.objects.DatabaseWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.antlr.database.objects.DatabaseDescription;

public class TableWrapperTest {
  
  @Test
  public void testFromDotSchema() throws IOException {
    Schema sch = new Schema("src/test/resources/project1/src/schema/sp2k.cache");
    IField fld1 = sch.lookupUnqualifiedField("minqty");
    Assert.assertNotNull(fld1);
    IField fld2 = sch.lookupUnqualifiedField("minqt");
    Assert.assertNotNull(fld2);
    IField fld3 = sch.lookupUnqualifiedField("custnum");
    Assert.assertNotNull(fld3);
    Assert.assertEquals(fld3.getTable().getName(), "BillTo");

    ITable tbl1 = sch.lookupTable("customer");
    Assert.assertNotNull(tbl1);
    ITable tbl2 = sch.lookupTable("custome");
    Assert.assertNotNull(tbl2);
  }

  @Test
  public void testFromDotDF() throws IOException {
    DatabaseDescription dbDesc = DumpFileUtils.getDatabaseDescription(new File("src/test/resources/project1/src/schema/sp2k.df"));
    Schema sch = new Schema(new DatabaseWrapper(dbDesc));
    IField fld1 = sch.lookupUnqualifiedField("minqty");
    Assert.assertNotNull(fld1);
    IField fld2 = sch.lookupUnqualifiedField("minqt");
    Assert.assertNotNull(fld2);
    IField fld3 = sch.lookupUnqualifiedField("custnum");
    Assert.assertNotNull(fld3);
    Assert.assertEquals(fld3.getTable().getName(), "BillTo");

    ITable tbl1 = sch.lookupTable("customer");
    Assert.assertNotNull(tbl1);
    ITable tbl2 = sch.lookupTable("custome");
    Assert.assertNotNull(tbl2);
  }

}
