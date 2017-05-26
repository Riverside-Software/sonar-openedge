package org.sonar.plugins.openedge.foundation;

import java.io.File;
import java.io.IOException;

import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.Schema;
import org.sonar.plugins.openedge.api.objects.DatabaseWrapper;
import org.testng.Assert;
import org.testng.annotations.Test;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.antlr.database.objects.DatabaseDescription;

public class TableWrapperTest {
  
  @Test
  public void testFromDotSchema() throws IOException {
    Schema sch = new Schema("src/test/resources/project1/src/schema/sp2k.schema");
    IField fld1 = sch.lookupUnqualifiedField("minqty");
    Assert.assertNotNull(fld1);
    IField fld2 = sch.lookupUnqualifiedField("minqt");
    Assert.assertNotNull(fld2);
  }

  @Test
  public void testFromDotDF() throws IOException {
    DatabaseDescription dbDesc = DumpFileUtils.getDatabaseDescription(new File("src/test/resources/project1/src/schema/sp2k.df"));
    Schema sch = new Schema(new DatabaseWrapper(dbDesc));
    IField fld1 = sch.lookupUnqualifiedField("minqty");
    Assert.assertNotNull(fld1);
    IField fld2 = sch.lookupUnqualifiedField("minqt");
    Assert.assertNotNull(fld2);
  }

}
