package eu.rssw.antlr.database;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Index;
import eu.rssw.antlr.database.objects.Table;
import eu.rssw.antlr.database.objects.Trigger;
import eu.rssw.antlr.database.objects.TriggerType;

public class TestDumpFile {

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
    Assert.assertEquals(db2.getSequences().size(), db.getSequences().size());
    Assert.assertEquals(db2.getTables().size(), db.getTables().size());
    Assert.assertEquals(db2.getTable("BillTo").getField("Address").getDataType().toUpperCase(), "CHARACTER");
    Assert.assertEquals(db2.getTable("Bin").getField("Qty").getDataType().toUpperCase(), "INTEGER");
    Assert.assertEquals(db2.getTable("Customer").getField("CreditLimit").getDataType().toUpperCase(), "DECIMAL");
    Assert.assertEquals(db2.getTable("Salesrep").getField("monthquota").getExtent().intValue(), 12);
  }

}
