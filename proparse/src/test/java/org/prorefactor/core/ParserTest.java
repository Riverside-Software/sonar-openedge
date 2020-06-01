/********************************************************************************
 * Copyright (c) 2015-2020 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.atn.DecisionInfo;
import org.antlr.v4.runtime.atn.ParseInfo;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.schema.Database;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.ISchema;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.schema.Table;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ParserTest {
  private final static String SRC_DIR = "src/test/resources/data/parser";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testAscending01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending01.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.DEFINE);
    for (JPNode stmt : stmts) {
      assertEquals(stmt.query(ABLNodeType.ASC).size(), 0);
      assertEquals(stmt.query(ABLNodeType.ASCENDING).size(), 1);
    }
    assertTrue(stmts.get(0).query(ABLNodeType.ASCENDING).get(0).isAbbreviated());
    assertTrue(stmts.get(1).query(ABLNodeType.ASCENDING).get(0).isAbbreviated());
    assertFalse(stmts.get(2).query(ABLNodeType.ASCENDING).get(0).isAbbreviated());
  }

  // SQL not recognized anymore
  @Test(enabled=false)
  public void testAscending02() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending02.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.SELECT);
    for (JPNode stmt : stmts) {
      assertEquals(stmt.query(ABLNodeType.ASC).size(), 0);
      assertEquals(stmt.query(ABLNodeType.ASCENDING).size(), 1);
    }
    assertTrue(stmts.get(0).query(ABLNodeType.ASCENDING).get(0).isAbbreviated());
    assertTrue(stmts.get(1).query(ABLNodeType.ASCENDING).get(0).isAbbreviated());
    assertFalse(stmts.get(2).query(ABLNodeType.ASCENDING).get(0).isAbbreviated());
  }

  @Test
  public void testAscending03() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending03.p"), session);
    unit.parse();

    for (JPNode stmt : unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE)) {
      assertEquals(stmt.query(ABLNodeType.ASC).size(), 2);
      assertEquals(stmt.query(ABLNodeType.ASCENDING).size(), 0);
      for (JPNode ascNode : stmt.query(ABLNodeType.ASC)) {
        assertFalse(ascNode.isAbbreviated());
      }
    }
  }

  @Test
  public void testLogical01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "logical01.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.DEFINE);
    for (JPNode stmt : stmts) {
      assertEquals(stmt.query(ABLNodeType.LOG).size(), 0);
      assertEquals(stmt.query(ABLNodeType.LOGICAL).size(), 1);
    }
    assertTrue(stmts.get(0).query(ABLNodeType.LOGICAL).get(0).isAbbreviated());
    assertTrue(stmts.get(1).query(ABLNodeType.LOGICAL).get(0).isAbbreviated());
    assertFalse(stmts.get(2).query(ABLNodeType.LOGICAL).get(0).isAbbreviated());
    assertTrue(stmts.get(3).query(ABLNodeType.LOGICAL).get(0).isAbbreviated());
    assertTrue(stmts.get(4).query(ABLNodeType.LOGICAL).get(0).isAbbreviated());
    assertFalse(stmts.get(5).query(ABLNodeType.LOGICAL).get(0).isAbbreviated());
  }

  @Test
  public void testLogical02() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "logical02.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE);
    assertEquals(stmts.get(0).query(ABLNodeType.LOG).size(), 1);
    assertEquals(stmts.get(0).query(ABLNodeType.LOGICAL).size(), 0);
  }

  @Test(enabled = false)
  public void testObjectInDynamicFunction() {
    // Issue https://github.com/Riverside-Software/sonar-openedge/issues/673
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "objindynfunc.cls"), session);
    unit.parse();

    assertEquals(unit.getTopNode().query(ABLNodeType.DYNAMICFUNCTION).size(), 3);
  }


  @Test
  public void testGetCodepage() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "getcodepage.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE);
    assertEquals(stmts.get(0).query(ABLNodeType.GETCODEPAGE).size(), 1);
    assertEquals(stmts.get(0).query(ABLNodeType.GETCODEPAGES).size(), 0);
    assertEquals(stmts.get(1).query(ABLNodeType.GETCODEPAGE).size(), 1);
    assertEquals(stmts.get(1).query(ABLNodeType.GETCODEPAGES).size(), 0);
    assertEquals(stmts.get(2).query(ABLNodeType.GETCODEPAGE).size(), 0);
    assertEquals(stmts.get(2).query(ABLNodeType.GETCODEPAGES).size(), 1);
    assertEquals(stmts.get(3).query(ABLNodeType.GETCODEPAGE).size(), 0);
    assertEquals(stmts.get(3).query(ABLNodeType.GETCODEPAGES).size(), 1);
  }

  @Test
  public void testConnectDatabase() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("connect database dialog box".getBytes()), "<unnamed>", session);
    unit.parse();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CONNECT).size(), 1);
  }

  @Test
  public void testReservedKeyword() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("define temp-table xxx field to-rowid as character.".getBytes()), "<unnamed>", session);
    unit.parse();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
  }

  @Test
  public void testInputFunction() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "inputfunc.p"), session);
    unit.parse();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.ON).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.IF).get(0).queryStateHead().size(), 2);
  }

  @Test
  public void testParameterLike() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("define input parameter ipPrm no-undo like customer.custnum.".getBytes()), "<unnamed>", session);
    unit.parse();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    JPNode node = unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0);
    assertEquals(node.query(ABLNodeType.NOUNDO).size(), 1);
  }

  @Test
  public void testAnnotation01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("@Progress.Lang.Annotation. MESSAGE 'Hello1'. MESSAGE 'Hello2'.".getBytes()), "<unnamed>", session);
    unit.parse();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  @Test
  public void testAnnotation02() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("@Progress.Lang.Annotation(foo='bar'). MESSAGE 'Hello1'. MESSAGE 'Hello2'.".getBytes()), "<unnamed>", session);
    unit.parse();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  /**
   * TODO Yes, should probably move to TreeParserTest.  
   */
  @Test
  public void tesTTIndex01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ttindex01.p"), session);
    unit.treeParser01();

    TableBuffer tt01 = unit.getRootScope().lookupTempTable("tt01");
    assertNotNull(tt01);
    assertNotNull(tt01.getTable());
    assertEquals(tt01.getTable().getIndexes().size(), 3);

    TableBuffer tt02 = unit.getRootScope().lookupTempTable("tt02");
    assertNotNull(tt02);
    assertNotNull(tt02.getTable());
    assertEquals(tt02.getTable().getIndexes().size(), 5);

    TableBuffer tt03 = unit.getRootScope().lookupTempTable("tt03");
    assertNotNull(tt03);
    assertNotNull(tt03.getTable());
    assertEquals(tt03.getTable().getIndexes().size(), 2);

    TableBuffer tt04 = unit.getRootScope().lookupTempTable("tt04");
    assertNotNull(tt04);
    assertNotNull(tt04.getTable());
    assertEquals(tt04.getTable().getIndexes().size(), 2);

    TableBuffer tt05 = unit.getRootScope().lookupTempTable("tt05");
    assertNotNull(tt05);
    assertNotNull(tt05.getTable());
    assertEquals(tt05.getTable().getIndexes().size(), 1);

    TableBuffer tt06 = unit.getRootScope().lookupTempTable("tt06");
    assertNotNull(tt06);
    assertNotNull(tt06.getTable());
    assertEquals(tt06.getTable().getIndexes().size(), 3);

    TableBuffer tt07 = unit.getRootScope().lookupTempTable("tt07");
    assertNotNull(tt07);
    assertNotNull(tt07.getTable());
    assertEquals(tt07.getTable().getIndexes().size(), 2);

    TableBuffer tt08 = unit.getRootScope().lookupTempTable("tt08");
    assertNotNull(tt08);
    assertNotNull(tt08.getTable());
    assertEquals(tt08.getTable().getIndexes().size(), 2);

    TableBuffer tt09 = unit.getRootScope().lookupTempTable("tt09");
    assertNotNull(tt09);
    assertNotNull(tt09.getTable());
    assertEquals(tt09.getTable().getIndexes().size(), 1);
  }

  /**
   * TODO Yes, should probably move to TreeParserTest.  
   */
  @Test
  public void testRecordNameNode() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "recordName.p"), session);
    unit.treeParser01();

    for (JPNode node : unit.getTopNode().query(ABLNodeType.RECORD_NAME)) {
      RecordNameNode recNode = (RecordNameNode) node;
      String tbl = recNode.getTableBuffer().getTargetFullName();
      if (recNode.getLine() == 5)
        assertEquals(tbl, "tt01");
      if (recNode.getLine() == 6)
        assertEquals(tbl, "sports2000.Customer");
      if (recNode.getLine() == 8)
        assertEquals(tbl, "sports2000.Customer");
      if (recNode.getLine() == 9)
        assertEquals(tbl, "tt01");
      if (recNode.getLine() == 10)
        assertEquals(tbl, "tt01");
      if (recNode.getLine() == 11)
        assertEquals(tbl, "sports2000.Customer");
    }
  }

  @Test
  public void testPackagePrivate() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "package.cls"), session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 3);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.METHOD).size(), 2);
  }

  @Test
  public void testCreateWidgetPool() {
    // No widget-pool table, statement is about creating a widget-pool
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("create widget-pool. message 'hello'.".getBytes()), "<unnamed>", session);
    unit.treeParser01();
    assertNull(session.getSchema().lookupTable("widget-pool"));
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).get(0).getState2(), ABLNodeType.WIDGETPOOL.getType());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);

    // widget-pool table available, statement is still about creating a widget-pool
    // I don't know how to create a new row in this table...
    ISchema schema = new Schema(session.getSchema().lookupDatabase("sports2000"), createWidgetPoolDB());
    RefactorSession session2 = new RefactorSession(session.getProparseSettings(), schema);
    assertNotNull(session2.getSchema().lookupTable("widget-pool"));

    ParseUnit unit2 = new ParseUnit(new ByteArrayInputStream("create widget-pool. message 'hello'.".getBytes()), "<unnamed>", session2);
    unit2.treeParser01();
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.CREATE).size(), 1);
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.CREATE).get(0).getState2(), ABLNodeType.WIDGETPOOL.getType());
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);

  }

  private IDatabase createWidgetPoolDB() {
    IDatabase retVal = new Database("mydb");
    retVal.add(new Table("widget-pool", IConstants.ST_DBTABLE));

    return retVal;
  }

  @Test
  public void testExpression01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "def image img1 file 'f1' size 1 by 1. def frame f1 img1 at row 1 col 1. img1:load-image('f2') in frame f1.".getBytes()),
        "<unnamed>", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 2);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0).getState2(), ABLNodeType.IMAGE.getType());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(1).getState2(), ABLNodeType.FRAME.getType());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).get(0).query(ABLNodeType.FRAME).size(),
        1);
  }

  @Test
  public void testExpression02() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "def var xxx as widget-handle. def var yyy as char. def frame zzz yyy. create control-frame xxx. xxx:move-after(yyy:handle in frame zzz).".getBytes()),
        "<unnamed>", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 3);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0).getState2(),
        ABLNodeType.VARIABLE.getType());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(1).getState2(),
        ABLNodeType.VARIABLE.getType());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(2).getState2(), ABLNodeType.FRAME.getType());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).get(0).getState2(), ABLNodeType.WIDGET.getType());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
  }

  @Test
  public void testExpression03() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("def var xxx as handle. message xxx::yyy.".getBytes()),
        "<unnamed>", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);
  }

  @Test
  public void testExpression04() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "def var xxx as System.Reflection.PropertyInfo. xxx:SetValue('xxx', xxx as long, 'xx' + 'yy' + 'zz').".getBytes()),
        "<unnamed>", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
    JPNode expr = unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).get(0);
    assertEquals(expr.query(ABLNodeType.METHOD_PARAM_LIST).size(), 1);
    // Comma and paren are counted
    assertEquals(expr.query(ABLNodeType.METHOD_PARAM_LIST).get(0).getNumberOfChildren(), 7);
  }

  @Test
  public void testVeryLongMaxK01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "maxk.p"), session);
    unit.enableProfiler();
    unit.parse();
    ParseInfo info = unit.getParseInfo();

    // Not really a unit test, but if max_k is less then 450, then the grammar rules have changed (in a good way)
    Optional<DecisionInfo> decision = Arrays.stream(info.getDecisionInfo()).max(
        (d1, d2) -> Long.compare(d1.SLL_MaxLook, d2.SLL_MaxLook));
    assertTrue(decision.isPresent());
    assertTrue(decision.get().SLL_MaxLook > 90, "MaxK: " + decision.get().SLL_MaxLook + " less than threshold");
  }

  @Test
  public void testAmbiguityReport() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "maxk.p"), session);
    unit.reportAmbiguity();
    unit.parse();
  }

  @Test
  public void testRecordFunction01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "find first _file. display recid(_file).".getBytes()),
        "<unnamed>", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().query(ABLNodeType.RECORD_NAME).size(), 2);
  }

  @Test
  public void testOptionalArgFunction01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "get-db-client('sp2k'). get-db-client().".getBytes()),
        "<unnamed>", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.GETDBCLIENT).size(), 2);
  }

  @Test
  public void testDatatype01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream(
        "interface rssw.test: method public Progress.Lang.Object getService(input xxx as class Progress.Lang.Class). end interface.".getBytes()),
        "<unnamed>", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void testDirective() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "directive.p"), session);
    unit.parse();

    // Looking for the DEFINE node
    JPNode node1 = (JPNode) unit.getTopNode().findDirectChild(ABLNodeType.DEFINE);
    assertNotNull(node1);
    assertTrue(node1.isStateHead());

    // Looking for the NO-UNDO node, and trying to get the state-head node
    JPNode node2 = (JPNode) unit.getTopNode().query(ABLNodeType.NOUNDO).get(0);
    JPNode parent = node2;
    while (!parent.isStateHead()) {
      parent = parent.getPreviousNode();
    }
    assertEquals(node1, parent);

    // No proparse directive as nodes anymore
    JPNode left = node1.getPreviousSibling();
    assertNull(left);
    
    // But as ProToken
    ProToken tok = node1.getHiddenBefore();
    assertNotNull(tok);
    // First WS, then proparse directive
    tok = (ProToken) tok.getHiddenBefore();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getText(), "prolint-nowarn(shared)");

    // First WS
    tok = (ProToken) tok.getHiddenBefore();
    assertNotNull(tok);
    // Then previous directive
    tok = (ProToken) tok.getHiddenBefore();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getText(), "prolint-nowarn(something)");
  }

}
