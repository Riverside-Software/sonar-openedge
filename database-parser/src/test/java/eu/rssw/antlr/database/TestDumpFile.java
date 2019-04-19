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
package eu.rssw.antlr.database;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.testng.annotations.Test;

import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Index;
import eu.rssw.antlr.database.objects.Table;
import eu.rssw.antlr.database.objects.Trigger;
import eu.rssw.antlr.database.objects.TriggerType;

public class TestDumpFile {

  @Test
  public void testInputStream() throws IOException {
    InputStream stream1 = new FileInputStream("src/test/resources/sp3k.df");
    DatabaseDescription db = DumpFileUtils.getDatabaseDescription(stream1, Charset.forName("utf-8"), "sp3k");
    assertEquals(db.getTable("Tbl1").getDescription(), "éç");

    InputStream stream2 = new FileInputStream("src/test/resources/sp4k.df");
    DatabaseDescription db2 = DumpFileUtils.getDatabaseDescription(stream2, Charset.forName("utf-8"), "sp4k");
    assertEquals(db2.getTable("Tbl1").getDescription(), "Ã©Ã§");
  }

  @Test
  public void testSports2000() throws IOException {
    DatabaseDescription db = DumpFileUtils.getDatabaseDescription(new File("src/test/resources/sp2k.df"));
    assertEquals(db.getTables().size(), 25);
    assertEquals(db.getSequences().size(), 13);

    Table tbl = db.getTable("Item");
    assertNotNull(tbl);
    assertEquals(tbl.getTriggers().size(), 3);

    Trigger trg1 = tbl.getTrigger(TriggerType.CREATE);
    assertNotNull(trg1);
    assertEquals(trg1.getProcedure(), "sports2000trgs/critem.p");
    assertEquals(trg1.getCrc(), "?");

    Trigger trg2 = tbl.getTrigger(TriggerType.DELETE);
    assertNotNull(trg2);
    assertEquals(trg2.getProcedure(), "sports2000trgs/delitem.p");
    assertEquals(trg2.getCrc(), "32704");

    Index idx1 = db.getTable("Warehouse").getIndex("warehousenum");
    assertTrue(idx1.isInAlternateBufferPool());
    Index idx2 = db.getTable("Warehouse").getIndex("warehousename");
    assertFalse(idx2.isInAlternateBufferPool());
    Index idx3 = db.getTable("BillTo").getIndex("custnumbillto");
    assertTrue(idx3.isUnique());
  }

  @Test
  public void testFieldTriggerNotAssign() throws IOException {
    // Invalid field trigger type shouldn't crash the visitor, and has to be reported
    DumpFileUtils.getDatabaseDescription(new File("src/test/resources/fieldTriggerAssign.df"));
  }

  @Test
  public void testNullAllowed() throws IOException {
    // NULL-ALLOWED is a valid argument
    DumpFileUtils.getDatabaseDescription(new File("src/test/resources/nullAllowed.df"));
  }

  @Test
  public void testTriggerDelete() throws IOException {
    // Delete triggers on table
    DumpFileUtils.getDatabaseDescription(new File("src/test/resources/triggerDelete.df"));
  }

  @Test
  public void testSerialize() throws IOException {
    DatabaseDescription db = DumpFileUtils.getDatabaseDescription(new File("src/test/resources/sp2k.df"));
    // Serialize object
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    db.serialize(bytes);
    // Deserialize
    InputStream in = new ByteArrayInputStream(bytes.toByteArray());
    DatabaseDescription db2 = DatabaseDescription.deserialize(in, "sp2k");
    // Compare
    assertEquals(db2.getSequences().size(), db.getSequences().size());
    assertEquals(db2.getTables().size(), db.getTables().size());
    assertEquals(db2.getTable("BillTo").getField("Address").getDataType().toUpperCase(), "CHARACTER");
    assertEquals(db2.getTable("Bin").getField("Qty").getDataType().toUpperCase(), "INTEGER");
    assertEquals(db2.getTable("Customer").getField("CreditLimit").getDataType().toUpperCase(), "DECIMAL");
    assertEquals(db2.getTable("Salesrep").getField("monthquota").getExtent().intValue(), 12);
    assertEquals(db2.getTable("Order").getIndexes().size(), 5);
    assertNotNull(db2.getTable("Order").getIndex("CustOrder"));
    assertEquals(db2.getTable("Order").getIndex("CustOrder").getFields().size(), 2);
  }

  @Test
  public void testAscIndex() throws IOException {
    // Delete triggers on table
    DatabaseDescription db = DumpFileUtils.getDatabaseDescription(new File("src/test/resources/ascIndex.df"));
    assertNotNull(db.getTable("Tab1"));
    assertNotNull(db.getTable("Tab1").getIndex("Idx1"));
    assertNotNull(db.getTable("Tab1").getIndex("Idx1").getFields());
    assertEquals(db.getTable("Tab1").getIndex("Idx1").getFields().size(), 3);
    assertTrue(db.getTable("Tab1").getIndex("Idx1").getFields().get(0).isAscending());
    assertTrue(db.getTable("Tab1").getIndex("Idx1").getFields().get(1).isAscending());
    assertFalse(db.getTable("Tab1").getIndex("Idx1").getFields().get(2).isAscending());
  }
}
