package org.prorefactor.treeparser.symbols;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.IOException;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.TreeParserRootSymbolScope;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SymbolsTest {
  private IProparseEnvironment session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void testSymbols() {
    var rootScope1 = new TreeParserRootSymbolScope(session);
    var rootScope2 = new TreeParserRootSymbolScope(session);

    var ds1 = new Dataset("ds1", rootScope1);
    assertEquals(ds1.getScope(), rootScope1);
    var ds2 = ds1.copy(rootScope2);
    assertNotEquals(ds1, ds2);
    assertEquals(ds2.getName(), "ds1");
    assertEquals(ds2.getScope(), rootScope2);

    var dSrc1 = new Datasource("dsrc1", rootScope1);
    assertEquals(dSrc1.getScope(), rootScope1);
    var dSrc2 = dSrc1.copy(rootScope2);
    assertNotEquals(dSrc1, dSrc2);
    assertEquals(dSrc2.getName(), "dsrc1");
    assertEquals(dSrc2.getScope(), rootScope2);

    var evt1 = new Datasource("evt1", rootScope1);
    assertEquals(evt1.getScope(), rootScope1);
    var evt2 = evt1.copy(rootScope2);
    assertNotEquals(evt1, evt2);
    assertEquals(evt2.getName(), "evt1");
    assertEquals(evt2.getScope(), rootScope2);

    var db1 = session.getSchema().getDatabases().iterator().next();
    var tbl1 = db1.getTableSet().stream().filter(it -> "customer".equalsIgnoreCase(it.getName())).findFirst().get();

    var tblBuf1 = new TableBuffer("buf1", rootScope1, tbl1);
    assertEquals(tblBuf1.getScope(), rootScope1);
    var tblBuf2 = tblBuf1.copy(rootScope2);
    assertEquals(tblBuf1, tblBuf2); // Equals is done differently for TableBuffer
    assertEquals(tblBuf2.getName(), "buf1");
    assertEquals(tblBuf2.getScope(), rootScope2);

    var fldBuf1 = new FieldBuffer(rootScope1, tblBuf1, tbl1.lookupField("custnum"));
    assertEquals(fldBuf1.getScope(), rootScope1);
    var fldBuf2 = fldBuf1.copy(rootScope2);
    assertNotEquals(fldBuf1, fldBuf2);
    assertEquals(fldBuf2.getName(), "CustNum");
    assertEquals(fldBuf2.getScope(), rootScope2);

    var qry1 = new Query("qry1", rootScope1);
    assertEquals(qry1.getScope(), rootScope1);
    var qry2 = qry1.copy(rootScope2);
    assertNotEquals(qry1, qry2);
    assertEquals(qry2.getName(), "qry1");
    assertEquals(qry2.getScope(), rootScope2);

    var strm1 = new Stream("strm1", rootScope1);
    assertEquals(strm1.getScope(), rootScope1);
    var strm2 = strm1.copy(rootScope2);
    assertNotEquals(strm1, strm2);
    assertEquals(strm2.getName(), "strm1");
    assertEquals(strm2.getScope(), rootScope2);
  }
}
