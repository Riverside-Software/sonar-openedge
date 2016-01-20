package eu.rssw.antlr.database;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import eu.rssw.antlr.database.objects.DatabaseDescription;
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
  }

}
