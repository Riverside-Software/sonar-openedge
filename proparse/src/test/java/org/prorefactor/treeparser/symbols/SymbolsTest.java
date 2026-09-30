package org.prorefactor.treeparser.symbols;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;

import org.prorefactor.core.schema.Table;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.TreeParserRootSymbolScope;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.IDataRelationElement;
import eu.rssw.pct.elements.fixed.DatasetElement;
import eu.rssw.pct.elements.fixed.TypeInfo;

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

    var rel1 = new DataRelation("rel1", rootScope1, new TableBuffer("tt1", rootScope1, new Table("tt1")),
        new TableBuffer("tt2", rootScope1, new Table("tt2")));
    assertEquals(rel1.getScope(), rootScope1);
    var rel2 = rel1.copy(rootScope2);
    assertNotEquals(rel1, rel2);
    assertEquals(rel2.getName(), "rel1");
    assertEquals(rel2.getScope(), rootScope2);
    ds1.addRelation(rel1);
    assertEquals(ds1.getRelations().get(0).getName(), "rel1");

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

  @Test
  public void testSymbols2() {
    var rootScope1 = new TreeParserRootSymbolScope(session);

    var tblBuf1 = new TableBuffer("", rootScope1, null);
    assertEquals(tblBuf1.getScope(), rootScope1);
    assertEquals(tblBuf1.getName(), "");
  }

  /**
   * Test that lookupDataset finds datasets defined in the current class
   */
  @Test
  public void testLookupDatasetInCurrentClass() {
    // Create a TypeInfo with a dataset
    var typeInfo = new TypeInfo("rssw.TestClass", false, false, "Progress.Lang.Object", "");
    typeInfo.addDataset(new DatasetElement("dsTest", new String[] {"ttBuf1"}, new IDataRelationElement[] {}));
    session.injectTypeInfo(typeInfo);

    // Create root scope and set the type info
    var rootScope = new TreeParserRootSymbolScope(session);
    rootScope.setClassName("rssw.TestClass");
    rootScope.setTypeInfo(typeInfo);

    // lookupDataset should find the dataset from typeInfo
    var result = rootScope.lookupDataset("dsTest");
    assertNotNull(result);
    assertEquals(result.getName(), "dsTest");
  }

  /**
   * Test that lookupDataset finds datasets defined in parent class
   */
  @Test
  public void testLookupDatasetInParentClass() {
    // Create parent class with a dataset
    var parentTypeInfo = new TypeInfo("rssw.ParentClass", false, false, "Progress.Lang.Object", "");
    parentTypeInfo.addDataset(new DatasetElement("dsParent", new String[] {"ttBuf1"}, new IDataRelationElement[] {}));
    session.injectTypeInfo(parentTypeInfo);

    // Create child class without dataset
    var childTypeInfo = new TypeInfo("rssw.ChildClass", false, false, "rssw.ParentClass", "");
    session.injectTypeInfo(childTypeInfo);

    // Create root scope for the child class
    var rootScope = new TreeParserRootSymbolScope(session);
    rootScope.setClassName("rssw.ChildClass");
    rootScope.setTypeInfo(childTypeInfo);

    // lookupDataset should find the dataset from parent class
    var result = rootScope.lookupDataset("dsParent");
    assertNotNull(result);
    assertEquals(result.getName(), "dsParent");
  }

  /**
   * Test that lookupDataset finds datasets defined in grandparent class
   */
  @Test
  public void testLookupDatasetInGrandparentClass() {
    // Create grandparent class with a dataset
    var grandparentTypeInfo = new TypeInfo("rssw.GrandparentClass", false, false, "Progress.Lang.Object", "");
    grandparentTypeInfo.addDataset(new DatasetElement("dsGrandparent", new String[] {"ttBuf1"}, new IDataRelationElement[] {}));
    session.injectTypeInfo(grandparentTypeInfo);

    // Create parent class without dataset
    var parentTypeInfo = new TypeInfo("rssw.ParentClass2", false, false, "rssw.GrandparentClass", "");
    session.injectTypeInfo(parentTypeInfo);

    // Create child class without dataset
    var childTypeInfo = new TypeInfo("rssw.ChildClass2", false, false, "rssw.ParentClass2", "");
    session.injectTypeInfo(childTypeInfo);

    // Create root scope for the child class
    var rootScope = new TreeParserRootSymbolScope(session);
    rootScope.setClassName("rssw.ChildClass2");
    rootScope.setTypeInfo(childTypeInfo);

    // lookupDataset should find the dataset from grandparent class
    var result = rootScope.lookupDataset("dsGrandparent");
    assertNotNull(result);
    assertEquals(result.getName(), "dsGrandparent");
  }

  /**
   * Test that lookupDataset returns null when dataset is not found
   */
  @Test
  public void testLookupDatasetNotFound() {
    // Create class without dataset
    var typeInfo = new TypeInfo("rssw.EmptyClass", false, false, "Progress.Lang.Object", "");
    session.injectTypeInfo(typeInfo);

    // Create root scope
    var rootScope = new TreeParserRootSymbolScope(session);
    rootScope.setClassName("rssw.EmptyClass");
    rootScope.setTypeInfo(typeInfo);

    // lookupDataset should return null
    var result = rootScope.lookupDataset("nonExistentDataset");
    assertNull(result);
  }

  /**
   * Test that local dataset (defined in scope) takes precedence over parent class dataset
   */
  @Test
  public void testLocalDatasetTakesPrecedence() {
    // Create parent class with a dataset
    var parentTypeInfo = new TypeInfo("rssw.ParentClass3", false, false, "Progress.Lang.Object", "");
    parentTypeInfo.addDataset(new DatasetElement("dsTest", new String[] {"ttParent"}, new IDataRelationElement[] {}));
    session.injectTypeInfo(parentTypeInfo);

    // Create child class
    var childTypeInfo = new TypeInfo("rssw.ChildClass3", false, false, "rssw.ParentClass3", "");
    session.injectTypeInfo(childTypeInfo);

    // Create root scope for child class
    var rootScope = new TreeParserRootSymbolScope(session);
    rootScope.setClassName("rssw.ChildClass3");
    rootScope.setTypeInfo(childTypeInfo);

    // Add a local dataset to the scope
    var localDataset = new Dataset("dsTest", rootScope);
    rootScope.add(localDataset);

    // lookupDataset should return the local dataset, not the parent one
    var result = rootScope.lookupDataset("dsTest");
    assertNotNull(result);
    assertEquals(result, localDataset);
  }

}
