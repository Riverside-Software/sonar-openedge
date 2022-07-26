/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
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
import org.prorefactor.core.util.UnitTestModule2;
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
  public void testNameDot01() {
    // Issue #897
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "namedot01.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.FIND);
    assertEquals(stmts.size(), 7);
    JPNode ref1 = stmts.get(0).query(ABLNodeType.FIELD_REF).get(0);
    assertEquals(ref1.getNumberOfChildren(), 1);
    assertEquals(ref1.getFirstChild().getText(), "tt.fld1");
    assertEquals(ref1.getFirstChild().getRawText(), "tt  .fld1");
    JPNode ref2 = stmts.get(1).query(ABLNodeType.FIELD_REF).get(0);
    assertEquals(ref2.getNumberOfChildren(), 1);
    assertEquals(ref2.getFirstChild().getText(), "tt.fld1");
    assertEquals(ref2.getFirstChild().getRawText(), "tt .fld1");
    JPNode ref3 = stmts.get(2).query(ABLNodeType.FIELD_REF).get(0);
    assertEquals(ref3.getNumberOfChildren(), 1);
    assertEquals(ref3.getFirstChild().getText(), "tt.fld1.fld1");
    assertEquals(ref3.getFirstChild().getRawText(), "tt  .fld1 .fld1");
    JPNode ref4 = stmts.get(3).query(ABLNodeType.FIELD_REF).get(0);
    assertEquals(ref4.getNumberOfChildren(), 1);
    assertEquals(ref4.getFirstChild().getText(), "tt.fld1");
    assertNull(ref4.getFirstChild().getRawText());
    JPNode ref5 = stmts.get(4).query(ABLNodeType.FIELD_REF).get(0);
    assertEquals(ref5.getNumberOfChildren(), 1);
    assertEquals(ref5.getFirstChild().getText(), "tt.fld1");
    assertEquals(ref5.getFirstChild().getRawText(), "tt /* my eyes are bleeding */ /* yes */ .fld1");
    JPNode ref6 = stmts.get(5).query(ABLNodeType.FIELD_REF).get(0);
    assertEquals(ref6.getNumberOfChildren(), 1);
    assertEquals(ref6.getFirstChild().getText(), "customer.name");
    assertEquals(ref6.getFirstChild().getRawText(), "customer .name");
    JPNode ref7 = stmts.get(6).query(ABLNodeType.FIELD_REF).get(0);
    assertEquals(ref7.getNumberOfChildren(), 1);
    assertEquals(ref7.getFirstChild().getText(), "sports2000.customer.name");
    assertEquals(ref7.getFirstChild().getRawText(), "sports2000  .customer /* foo */   .name");
    JPNode rec1 = stmts.get(6).query(ABLNodeType.RECORD_NAME).get(0);
    assertEquals(rec1.getNumberOfChildren(), 0);
    assertEquals(rec1.getText(), "sports2000.customer");
    assertEquals(rec1.getRawText(), "sports2000   .customer");

    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DOT_COMMENT).size(), 0);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).get(0).query(ABLNodeType.FIELD_REF).get(
        0).getFirstChild().getLine(), 8);
  }

  @Test
  public void testAscending01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending01.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

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
  @Test(enabled = false)
  public void testAscending02() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending02.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

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
    assertFalse(unit.hasSyntaxError());

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
    assertFalse(unit.hasSyntaxError());

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
    assertFalse(unit.hasSyntaxError());

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE);
    assertEquals(stmts.get(0).query(ABLNodeType.LOG).size(), 1);
    assertEquals(stmts.get(0).query(ABLNodeType.LOGICAL).size(), 0);
  }

  @Test(enabled = false)
  public void testObjectInDynamicFunction() {
    // Issue https://github.com/Riverside-Software/sonar-openedge/issues/673
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "objindynfunc.cls"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    assertEquals(unit.getTopNode().query(ABLNodeType.DYNAMICFUNCTION).size(), 3);
  }

  @Test
  public void testGetCodepage() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "getcodepage.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

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
  public void testExpr() {
    ParseUnit unit = new ParseUnit(
        "message xx. catch err as Progress.Lang.Error: message err:GetClass():TypeName. end.", "<unnamed>", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  @Test
  public void testConnectDatabase() {
    ParseUnit unit = new ParseUnit("connect database dialog box.", "<unnamed>", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CONNECT).size(), 1);
  }

  @Test
  public void testReservedKeyword() {
    ParseUnit unit = new ParseUnit("define temp-table xxx field to-rowid as character.", "<unnamed>", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
  }

  @Test
  public void testInputFunction() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "inputfunc.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.ON).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.IF).get(0).queryStateHead(ABLNodeType.DO).size(), 1);
  }

  @Test
  public void testParameterLike() {
    ParseUnit unit = new ParseUnit("define input parameter ipPrm no-undo like customer.custnum.", "<unnamed>", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    JPNode node = unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0);
    assertEquals(node.query(ABLNodeType.NOUNDO).size(), 1);
  }

  @Test
  public void testAnnotation01() {
    ParseUnit unit = new ParseUnit("@Progress.Lang.Annotation. MESSAGE 'Hello1'. MESSAGE 'Hello2'.", "<unnamed>",
        session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  @Test
  public void testAnnotation02() {
    ParseUnit unit = new ParseUnit("@Progress.Lang.Annotation(foo='bar'). MESSAGE 'Hello1'. MESSAGE 'Hello2'.",
        "<unnamed>", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  /**
   * TODO Yes, should probably move to TreeParserTest.
   */
  @Test
  public void tesTTIndex01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ttindex01.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

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
    assertFalse(unit.hasSyntaxError());

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
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 4);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.METHOD).size(), 2);
  }

  @Test
  public void testTriggerInClass() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "TriggerInClass.cls"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.METHOD).size(), 1);
  }

  @Test
  public void testCreateWidgetPool() {
    // No widget-pool table, statement is about creating a widget-pool
    ParseUnit unit = new ParseUnit("create widget-pool. message 'hello'.", "<unnamed>", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNull(session.getSchema().lookupTable("widget-pool"));
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).get(0).asIStatement().getNodeType2(),
        ABLNodeType.WIDGETPOOL);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);

    // widget-pool table available, statement is still about creating a widget-pool
    // I don't know how to create a new row in this table...
    ISchema schema = new Schema(session.getSchema().lookupDatabase("sports2000"), createWidgetPoolDB());
    RefactorSession session2 = new RefactorSession(session.getProparseSettings(), schema);
    assertNotNull(session2.getSchema().lookupTable("widget-pool"));

    ParseUnit unit2 = new ParseUnit("create widget-pool. message 'hello'.", "<unnamed>", session2);
    unit2.treeParser01();
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.CREATE).size(), 1);
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.CREATE).get(0).asIStatement().getNodeType2(),
        ABLNodeType.WIDGETPOOL);
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);

  }

  private IDatabase createWidgetPoolDB() {
    IDatabase retVal = new Database("mydb");
    retVal.add(new Table("widget-pool", retVal));

    return retVal;
  }

  @Test
  public void testExpression01() {
    ParseUnit unit = new ParseUnit(
        "def image img1 file 'f1' size 1 by 1. def frame f1 img1 at row 1 col 1. img1:load-image('f2') in frame f1.",
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 2);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0).asIStatement().getNodeType2(),
        ABLNodeType.IMAGE);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(1).asIStatement().getNodeType2(),
        ABLNodeType.FRAME);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).get(0).query(ABLNodeType.FRAME).size(),
        1);
  }

  @Test
  public void testExpression02() {
    ParseUnit unit = new ParseUnit(
        "def var xxx as widget-handle. def var yyy as char. def frame zzz yyy. create control-frame xxx. xxx:move-after(yyy:handle in frame zzz).",
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 3);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0).asIStatement().getNodeType2(),
        ABLNodeType.VARIABLE);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(1).asIStatement().getNodeType2(),
        ABLNodeType.VARIABLE);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(2).asIStatement().getNodeType2(),
        ABLNodeType.FRAME);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CREATE).get(0).asIStatement().getNodeType2(),
        ABLNodeType.WIDGET);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
  }

  @Test
  public void testExpression03() {
    ParseUnit unit = new ParseUnit("def var xxx as handle. message xxx::yyy.", "<unnamed>", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);
  }

  @Test
  public void testExpression04() {
    ParseUnit unit = new ParseUnit(
        "def var xxx as System.Reflection.PropertyInfo. xxx:SetValue('xxx', xxx as long, 'xx' + 'yy' + 'zz').",
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
    JPNode expr = unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).get(0);
    assertEquals(expr.query(ABLNodeType.METHOD_PARAM_LIST).size(), 1);
    // Comma and paren are counted
    assertEquals(expr.query(ABLNodeType.METHOD_PARAM_LIST).get(0).getNumberOfChildren(), 7);
  }

  @Test
  public void testExpression05() {
    ParseUnit unit = new ParseUnit("System.Math:Max(1 + 2 as unsigned-byte, 3 * 4 as unsigned-byte).", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).size(), 1);
    JPNode expr = unit.getTopNode().queryStateHead(ABLNodeType.EXPR_STATEMENT).get(0);
    assertEquals(expr.query(ABLNodeType.METHOD_PARAM_LIST).size(), 1);
    // Comma and paren are counted
    assertEquals(expr.query(ABLNodeType.METHOD_PARAM_LIST).get(0).getNumberOfChildren(), 5);
  }

  @Test
  public void testShortMaxK01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "maxk.p"), session);
    unit.enableProfiler();
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    ParseInfo info = unit.getParseInfo();

    // Once upon a time, that was a test to see if there were grammar improvements on some specific syntax
    // MaxK is the maximum number of lookahead tokens required to decide between two rules. The shortest the number, the
    // fastest the parser. An unrelated change on April '21 changed a large maxK to a very small value. Cause is
    // unknown and probably related to ANTLR4 internals.
    Optional<DecisionInfo> decision = Arrays.stream(info.getDecisionInfo()).max(
        (d1, d2) -> Long.compare(d1.SLL_MaxLook, d2.SLL_MaxLook));
    assertTrue(decision.isPresent());
    assertTrue(decision.get().SLL_MaxLook < 20, "MaxK: " + decision.get().SLL_MaxLook + " less than threshold");
  }

  @Test
  public void testAmbiguityReport() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "maxk.p"), session);
    unit.reportAmbiguity();
    unit.parse();
    assertFalse(unit.hasSyntaxError());
  }

  @Test
  public void testSwitchLL() {
    ParseUnit unit = new ParseUnit("run procName(input frame frame-a fldName).", "<unnamed>", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    // Switch to LL is NOT good, but unit test is there to be aware of any change here
    assertTrue(unit.hasSwitchedToLL());
  }

  @Test
  public void testSuperStatement() {
    ParseUnit unit = new ParseUnit("class foo: constructor foo(): super(). end. end class.", "<unnamed>", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().query(ABLNodeType.SUPER).size(), 1);
    assertEquals(unit.getTopNode().query(ABLNodeType.SUPER).get(0).getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.PERIOD).size(), 0);
  }

  @Test
  public void testRecordFunction01() {
    ParseUnit unit = new ParseUnit("find first _file. display recid(_file).", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().query(ABLNodeType.RECORD_NAME).size(), 2);
  }

  @Test
  public void testOptionalArgFunction01() {
    ParseUnit unit = new ParseUnit("get-db-client('sp2k'). get-db-client().", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.GETDBCLIENT).size(), 2);
  }

  @Test
  public void testDatatype01() {
    ParseUnit unit = new ParseUnit(
        "interface rssw.test: method public Progress.Lang.Object getService(input xxx as class Progress.Lang.Class). end interface.",
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void testNamedMember() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "namedMember01.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertFalse(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER).isEmpty());
    assertFalse(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER_ARRAY).isEmpty());
    assertEquals(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER).get(0).firstNaturalChild().getLine(), 4);
    assertEquals(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER_ARRAY).get(0).firstNaturalChild().getLine(), 5);
  }

  @Test
  public void testDirective() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "directive.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    // Looking for the DEFINE node
    JPNode node1 = unit.getTopNode().findDirectChild(ABLNodeType.DEFINE);
    assertNotNull(node1);
    assertTrue(node1.isStateHead());

    // Looking for the NO-UNDO node, and trying to get the state-head node
    JPNode node2 = unit.getTopNode().query(ABLNodeType.NOUNDO).get(0);
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

  @Test
  public void testGenerics01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "generics01.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

  }

  @Test
  public void testExpressionEngine01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "expression01.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    // Looking for the DEFINE node
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.EXPR_STATEMENT);
    assertNotNull(nodes);
    assertEquals(nodes.size(), 4);

    JPNode expr1 = nodes.get(0).getFirstChild();
    assertEquals(expr1.getNodeType(), ABLNodeType.PLUS);
    assertEquals(expr1.getDirectChildren().get(0).getNodeType(), ABLNodeType.PLUS);
    JPNode expr2 = nodes.get(1).getFirstChild();
    assertEquals(expr2.getNodeType(), ABLNodeType.PLUS);
    assertEquals(expr2.getDirectChildren().get(1).getNodeType(), ABLNodeType.MULTIPLY);
    JPNode expr3 = nodes.get(2).getFirstChild();
    assertEquals(expr3.getNodeType(), ABLNodeType.EQ);
    assertEquals(expr3.getFirstChild().getNodeType(), ABLNodeType.PLUS);
    JPNode expr4 = nodes.get(3).getFirstChild();
    assertEquals(expr4.getNodeType(), ABLNodeType.OR);
    assertEquals(expr4.getFirstChild().getNodeType(), ABLNodeType.OR);
    assertEquals(expr4.getDirectChildren().get(1).getNodeType(), ABLNodeType.EQ);
    assertEquals(expr4.getFirstChild().getFirstChild().getNodeType(), ABLNodeType.LTHAN);
    assertEquals(expr4.getFirstChild().getDirectChildren().get(1).getNodeType(), ABLNodeType.GTHAN);
    JPNode eqExpr = expr4.getDirectChildren().get(1);
    assertEquals(eqExpr.getDirectChildren().get(1).getNodeType(), ABLNodeType.FIELD_REF);
    assertEquals(eqExpr.getFirstChild().getNodeType(), ABLNodeType.PLUS);
    assertEquals(eqExpr.getFirstChild().getDirectChildren().get(1).getNodeType(), ABLNodeType.MULTIPLY);

    nodes = unit.getTopNode().query(ABLNodeType.ASSIGN);
    assertNotNull(nodes);
    assertEquals(nodes.size(), 1);

    JPNode assign1 = nodes.get(0);
    assertEquals(assign1.getFirstChild().getNodeType(), ABLNodeType.EQUAL);
    assertEquals(assign1.getFirstChild().getDirectChildren().get(1).getNodeType(), ABLNodeType.EQ);
  }

  @Test
  public void testVarStatement01() {
    ParseUnit unit = new ParseUnit("VAR CHAR s1, s2, s3, s4.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement02() {
    ParseUnit unit = new ParseUnit("VAR INT x, y, z = 3.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement03() {
    ParseUnit unit = new ParseUnit("VAR CLASS mypackage.subdir.myclass myobj1, myobj2, myobj3.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement04() {
    ParseUnit unit = new ParseUnit("VAR mypackage.subdir.myclass myobj1.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement05() {
    ParseUnit unit = new ParseUnit("VAR DATE d1, d2 = 1/1/2020, d3 = TODAY.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement06() {
    ParseUnit unit = new ParseUnit("VAR PROTECTED DATE d1, d2 = 1/1/2020.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement07() {
    ParseUnit unit = new ParseUnit("VAR INT[3] x = [1, 2], y, z = [100, 200, 300].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement08() {
    ParseUnit unit = new ParseUnit("VAR INT[] x, y.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement09() {
    ParseUnit unit = new ParseUnit("VAR INT[] x, y = [1,2,3].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement10() {
    ParseUnit unit = new ParseUnit("VAR INT[] x = [1,2], y = [1,2,3].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement11() {
    ParseUnit unit = new ParseUnit("VAR CLASS foo[2] classArray.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement12() {
    ParseUnit unit = new ParseUnit("VAR \"System.Collections.Generic.List<char>\" cList.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement13() {
    ParseUnit unit = new ParseUnit("VAR INT a, b, x = a + b, y = a - b, z = x - y.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement14() {
    ParseUnit unit = new ParseUnit("VAR INT a, b. VAR INT[] x = [ a + b, a - b ].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void testVarStatement15() {
    ParseUnit unit = new ParseUnit("USING Progress.Lang.Object. VAR Object x = NEW Object().", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void testVarStatement16() {
    ParseUnit unit = new ParseUnit("VAR DATETIME dtm = DATETIME(TODAY,MTIME).", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testDbQualifierSports2000() {
    // Standard schema, lower-case database name
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "dbqualifier01.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    // Looking for the FIND node
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.FIND);
    assertNotNull(nodes);
    assertEquals(nodes.size(), 4);

    JPNode findNode = nodes.get(0);
    JPNode ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    RecordNameNode rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");

    findNode = nodes.get(1);
    ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");

    findNode = nodes.get(2);
    ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");

    findNode = nodes.get(3);
    ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");
  }

  @Test
  public void testDbQualifierSP2K() {
    Injector injector = Guice.createInjector(new UnitTestModule2());
    RefactorSession session2 = injector.getInstance(RefactorSession.class);

    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "dbqualifier02.p"), session2);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    // Looking for the FIND node
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.FIND);
    assertNotNull(nodes);
    assertEquals(nodes.size(), 4);

    JPNode findNode = nodes.get(0);
    JPNode ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    RecordNameNode rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");

    findNode = nodes.get(1);
    ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");

    findNode = nodes.get(2);
    ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");

    findNode = nodes.get(3);
    ch1 = findNode.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");
  }

  @Test
  public void testEnum() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "enum01.cls"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getSupport().getClassName(), "rssw.enum01");
    assertTrue(unit.getSupport().isEnum());
    assertEquals(unit.getClassName(), "rssw.enum01");
    assertTrue(unit.isEnum());
  }

  @Test
  public void testElvis01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "elvis01.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 4);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.ELVIS).get(0);
    assertEquals(node1.getLine(), 3);
    assertEquals(node1.getEndLine(), 3);
    assertEquals(node1.getColumn(), 11);
    assertEquals(node1.getEndColumn(), 12);
    assertEquals(node1.getText(), "?:");
  }

  @Test
  public void testElvis02() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "elvis02.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.UNKNOWNVALUE).get(0);
    JPNode node2 = unit.getTopNode().query(ABLNodeType.LEXCOLON).get(0);
    JPNode node3 = unit.getTopNode().query(ABLNodeType.DISPLAY).get(0);
    assertEquals(node1.getLine(), 1);
    assertEquals(node1.getColumn(), 42);
    assertEquals(node1.getEndColumn(), 42);
    assertEquals(node1.getText(), "?");
    assertEquals(node2.getLine(), 1);
    assertEquals(node2.getColumn(), 43);
    assertEquals(node2.getEndColumn(), 43);
    assertEquals(node2.getText(), ":");
    assertEquals(node3.getLine(), 2);
    assertEquals(node3.getColumn(), 3);
    assertEquals(node3.getEndColumn(), 9);
    assertNotNull(node3.getHiddenBefore());
    assertNull(node3.getHiddenBefore().getHiddenBefore());
    assertEquals(node3.getHiddenBefore().getLine(), 1);
    assertEquals(node3.getHiddenBefore().getEndLine(), 2);
    assertEquals(node3.getHiddenBefore().getCharPositionInLine(), 44);
    assertEquals(node3.getHiddenBefore().getEndCharPositionInLine(), 2);
  }

  @Test
  public void testAccumulateSum() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "accumulate01.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 6);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.ACCUMULATE).get(1);
    JPNode node2 = unit.getTopNode().query(ABLNodeType.ACCUMULATE).get(3);
    assertEquals(node1.getFirstChild().getNodeType(), ABLNodeType.TOTAL);
    assertEquals(node2.getFirstChild().getNodeType(), ABLNodeType.SUM);
  }
}
