/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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
import static org.testng.Assert.expectThrows;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.atn.DecisionInfo;
import org.antlr.v4.runtime.atn.ParseInfo;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.schema.Database;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.ISchema;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.schema.Table;
import org.prorefactor.core.util.SP2KSchema;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.PrimitiveDataType;

public class ParserTest extends AbstractProparseTest {
  private static final String SRC_DIR = "src/test/resources/data/parser";

  private RefactorSession session;

  @BeforeMethod
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void testNameDot01() {
    // Issue #897
    String code = """
        DEFINE TEMP-TABLE tt
          FIELD fld1 AS CHAR.

        // Space between table name and dot ? Sure, no problem...
        FIND tt WHERE tt  .fld1 = "1".
        FIND tt WHERE "1" = tt .fld1 .
        FIND tt WHERE "1" = tt  .fld1 .fld1. // Last .fld1 has to be merged in tt.fld1.
        FIND tt WHERE "1" = tt.fld1. fld1. // But not here
        // Comments on top of that ? Hold my beer...
        FIND tt WHERE tt /* my eyes are bleeding */ /* yes */ .fld1 = "1".
        // Has to work on DB tables too
        FIND customer WHERE customer .name = "1".
        // And fully qualified too
        FIND sports2000   .customer WHERE sports2000  .customer /* foo */   .name = "1".
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        DEFINE TEMP-TABLE tt1 FIELD f1 AS CHAR INDEX i1 IS PRIMARY f1 ASC.
        DEFINE TEMP-TABLE tt2 FIELD f1 AS CHAR INDEX i1 IS PRIMARY f1 ASCEN.
        DEFINE TEMP-TABLE tt3 FIELD f1 AS CHAR INDEX i1 IS PRIMARY f1 ASCENDING.
        """;
    ParseUnit unit = getParseUnit(code, session);
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

  @Test
  public void testDynamicFunction01() {
    String code = """
        CLASS TestClass:

          METHOD VOID m1():
            DYNAMIC-FUNCTION('foo' IN m2():m3()).
            DYNAMIC-FUNCTION('foo' IN m2():m3(), 1).
            DYNAMIC-FUNCTION('foo' IN m2():m3(), 1, 2, " ").
          END METHOD.

          METHOD TestClass m2():
            RETURN THIS-OBJECT.
          END METHOD.

          METHOD HANDLE m3():
            RETURN ?.
          END METHOD.

        END CLASS.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    // TODO Add extra tests to make sure syntax is correctly recognized
  }

  // SQL not recognized anymore
  @Test(enabled = false)
  public void testAscending02() {
    String code = """
        SELECT * FROM customer BY custnum ASC.
        SELECT * FROM customer BY custnum ASCEN.
        SELECT * FROM customer BY custnum ASCENDING.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        MESSAGE ASC('A') + ASC   /*   XXXX */ /* ZZZZ

        */  ('B').
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        DEFINE TEMP-TABLE tt1 FIELD f1 AS LOG.
        DEFINE TEMP-TABLE tt2 FIELD f1 AS LOGI.
        DEFINE TEMP-TABLE tt3 FIELD f1 AS LOGICAL.
        DEFINE VARIABLE xxx1 AS LOG.
        DEFINE VARIABLE xxx2 AS LOGI.
        DEFINE VARIABLE xxx2 AS LOGICAL.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        MESSAGE STRING(LOG(123)).
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE);
    assertEquals(stmts.get(0).query(ABLNodeType.LOG).size(), 1);
    assertEquals(stmts.get(0).query(ABLNodeType.LOGICAL).size(), 0);
  }

  @Test(enabled = false)
  public void testObjectInDynamicFunction() {
    // Issue https://github.com/Riverside-Software/sonar-openedge/issues/673
    String code = """
        class Test:

          method public void method1():
            define variable obj as Progress.Lang.Object no-undo.

            dynamic-function(obj:getString(toString())).
            dynamic-function('foobar' in obj:getHandle()).
            dynamic-function('foobar' in obj:getHandle(toString())).
          end.

        end.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    assertEquals(unit.getTopNode().query(ABLNodeType.DYNAMICFUNCTION).size(), 3);
  }

  @Test
  public void testGetCodepage() {
    String code = """
        DEFINE VARIABLE xxx AS LONGCHAR.

        MESSAGE GET-CODEPAGE(xxx).
        MESSAGE GET-CODEPAGES(xxx).
        MESSAGE GET-CODEPAGE.
        MESSAGE GET-CODEPAGES.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    ParseUnit unit = getParseUnit(
        "message xx. catch err as Progress.Lang.Error: message err:GetClass():TypeName. end.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  @Test
  public void testConnectDatabase() {
    ParseUnit unit = getParseUnit("connect database dialog box.", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.CONNECT).size(), 1);
  }

  @Test
  public void testReservedKeyword() {
    ParseUnit unit = getParseUnit("define temp-table xxx field to-rowid as character.", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
  }

  @Test
  public void testInputFunction() {
    String code = """
        define variable fillIn1 as int view-as fill-in.
        define variable fillIn2 as int view-as fill-in.
        define frame frm1 fillIn1 fillIn2.

        on leave of fillIn1 in frame frm1 do:
          input fillIn2 no-error.
          if input frame frm1 fill1 eq '' then do:
            // Something
          end.
        end.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.ON).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.IF).get(0).queryStateHead(ABLNodeType.DO).size(), 1);
  }

  @Test
  public void testParameterLike() {
    ParseUnit unit = getParseUnit("define input parameter ipPrm no-undo like customer.custnum.", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    JPNode node = unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0);
    assertEquals(node.query(ABLNodeType.NOUNDO).size(), 1);
  }

  @Test
  public void testAnnotation01() {
    ParseUnit unit = getParseUnit("@Progress.Lang.Annotation. MESSAGE 'Hello1'. MESSAGE 'Hello2'.", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  @Test
  public void testAnnotation02() {
    ParseUnit unit = getParseUnit("@Progress.Lang.Annotation(foo='bar'). MESSAGE 'Hello1'. MESSAGE 'Hello2'.",
        session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

  /**
   * TODO Yes, should probably move to TreeParserTest.
   */
  @Test
  public void tesTTIndex01() {
    String code = """
        define temp-table tt01
          field fld1 as char
          field fld2 as char
          field fld3 as char
          field fld4 as char
          index idx1 is primary unique fld1
          index idx2 fld1 fld2
          index idx3 fld2 fld3 fld4.

        define temp-table tt02 like customer.
        define temp-table tt03 like customer use-index Comments use-index CountryPost.
        define temp-table tt04 like customer use-index comments
          index idx1 EmailAddress.
        define temp-table tt05 like customer index idx1 emailaddress.

        define temp-table tt06 like tt01.
        define temp-table tt07 like tt01 use-index idx1 use-index idx2.
        define temp-table tt08 like tt01 use-index idx1 index idx4 fld2.
        define temp-table tt09 like tt01 index idx4 fld2.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        define temp-table tt01
          field fld1 as char
          index idx1 is primary unique fld1.

        define buffer tt02 for tt01.
        define buffer tt03 for customer.

        find first customer.
        find first tt01.
        find first tt02.
        find first tt03.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        class MyPackage.Foobar:
          define package-protected event NewCustomer
            signature void ( input custName as character ).
          define package-private variable v1 as int.
          define package-protected property v2 as int get.
          define public property v3 as int package-private get. package-protected set.

          constructor public foobar():
            //
          end constructor.

          method package-protected void m1() :
            //
          end method.

          method package-private void m1() :
            //
          end method.

        end class.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 4);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.METHOD).size(), 2);
  }

  @Test
  public void testTriggerInClass() {
    String code = """
        class package.foobar:

          define private static property prop1 as int64 no-undo get.

          on 'entry':u anywhere do:
            // Yes, we can add triggers...
          end.

          method private static int64 xxx(zz as int64):
            //
          end method.

        end class.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.METHOD).size(), 1);
  }

  @Test
  public void testCreateWidgetPool() {
    // No widget-pool table, statement is about creating a widget-pool
    ParseUnit unit = getParseUnit("create widget-pool. message 'hello'.", session);
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

    ParseUnit unit2 = getParseUnit("create widget-pool. message 'hello'.", session2);
    unit2.treeParser01();
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.CREATE).size(), 1);
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.CREATE).get(0).asIStatement().getNodeType2(),
        ABLNodeType.WIDGETPOOL);
    assertEquals(unit2.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);

  }

  private IDatabase createWidgetPoolDB() {
    var retVal = new Database("mydb");
    retVal.add(new Table("widget-pool", retVal));

    return retVal;
  }

  @Test
  public void testExpression01() {
    ParseUnit unit = getParseUnit(
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
    ParseUnit unit = getParseUnit(
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
    ParseUnit unit = getParseUnit("def var xxx as handle. message xxx::yyy.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);
  }

  @Test
  public void testExpression04() {
    ParseUnit unit = getParseUnit(
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
    ParseUnit unit = getParseUnit("System.Math:Max(1 + 2 as unsigned-byte, 3 * 4 as unsigned-byte).", session);
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
    String code = """
        // Two parameters required, otherwise max-k stays at very low value
        xxx:ADD-NEW-FIELD('WebToHdlr', 'CHAR').

        DEFINE NEW GLOBAL SHARED VARIABLE GATEWAY_INTERFACE AS character NO-UNDO.

        DEFINE NEW GLOBAL SHARED VARIABLE SERVER_SOFTWARE   AS character FORMAT "x(20)":U NO-UNDO.
        DEFINE NEW GLOBAL SHARED VARIABLE SERVER_PROTOCOL   AS character NO-UNDO.
        DEFINE NEW GLOBAL SHARED VARIABLE SERVER_NAME       AS character FORMAT "x(40)":U NO-UNDO.

        FUNCTION getEnv                RETURNS CHARACTER
          (INPUT p_name                 AS CHARACTER) in web-utilities-hdl.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        // Two parameters required, otherwise max-k stays at very low value
        xxx:ADD-NEW-FIELD('WebToHdlr', 'CHAR').

        DEFINE NEW GLOBAL SHARED VARIABLE GATEWAY_INTERFACE AS character NO-UNDO.

        DEFINE NEW GLOBAL SHARED VARIABLE SERVER_SOFTWARE   AS character FORMAT "x(20)":U NO-UNDO.
        DEFINE NEW GLOBAL SHARED VARIABLE SERVER_PROTOCOL   AS character NO-UNDO.
        DEFINE NEW GLOBAL SHARED VARIABLE SERVER_NAME       AS character FORMAT "x(40)":U NO-UNDO.

        FUNCTION getEnv                RETURNS CHARACTER
          (INPUT p_name                 AS CHARACTER) in web-utilities-hdl.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.reportAmbiguity();
    unit.parse();
    assertFalse(unit.hasSyntaxError());
  }

  @Test
  public void testSwitchLL() {
    ParseUnit unit = getParseUnit("run procName(input frame frame-a fldName).", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    // Switch to LL is NOT good, but unit test is there to be aware of any change here
    assertTrue(unit.hasSwitchedToLL());
  }

  @Test
  public void testSuperStatement() {
    ParseUnit unit = getParseUnit("class foo: constructor foo(): super(). end. end class.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().query(ABLNodeType.SUPER).size(), 1);
    assertEquals(unit.getTopNode().query(ABLNodeType.SUPER).get(0).getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.PERIOD).size(), 0);
  }

  @Test
  public void testRecordFunction01() {
    ParseUnit unit = getParseUnit("find first _file. display recid(_file).", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().query(ABLNodeType.RECORD_NAME).size(), 2);
  }

  @Test
  public void testOptionalArgFunction01() {
    ParseUnit unit = getParseUnit("get-db-client('sp2k'). get-db-client().", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.GETDBCLIENT).size(), 2);
  }

  @Test
  public void testDatatype01() {
    ParseUnit unit = getParseUnit(
        "interface rssw.test: method public Progress.Lang.Object getService(input xxx as class Progress.Lang.Class). end interface.",
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void testNamedMember() {
    String code = """
        DEFINE VARIABLE b1 AS HANDLE.
        CREATE BUFFER b1 FOR TABLE "SalesRep".
        b1:BUFFER-CREATE().
        ASSIGN b1::RepName = 'A'.
        ASSIGN b1::MonthQuota(1) = 1000.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertFalse(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER).isEmpty());
    assertFalse(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER_ARRAY).isEmpty());
    assertEquals(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER).get(0).firstNaturalChild().getLine(), 4);
    assertEquals(unit.getTopNode().query(ABLNodeType.NAMED_MEMBER_ARRAY).get(0).firstNaturalChild().getLine(), 5);
  }

  @Test
  public void testDirective() {
    String code = """
        {&_proparse_ prolint-nowarn(something)}
        {&_proparse_ prolint-nowarn(shared)}
        DEFINE NEW GLOBAL SHARED VARIABLE shared_e AS INTEGER NO-UNDO.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    String code = """
        var Progress.Collections.List<Progress.Lang.Object> thelist.
        var Progress.Collections.IIterator<Progress.Lang.Object> iterator.
        var Progress.Lang.Object president1.
        var Progress.Lang.Object president2.
        var HashMap<Object, Object> map1.

        // Create the list
        thelist = new Progress.Collections.List<Progress.Lang.Object>().

        // Add 3 elements to the list
        thelist:Add(new Progress.Lang.Object("George")).
        thelist:Add(new Progress.Lang.Object ("John")).
        thelist:Add(new Progress.Lang.Object("Thomas")).

        // Retrieve the first element from the list
        president1 = thelist:Get(1).
        message president1:ToString().

        // Replace the first element in the list with a fully named president.
        thelist:Set(1, new Progress.Lang.Object("George Washington")).
        president2 = thelist:Get(1).
        message president2:ToString().

        // Remove the second president from the list
        thelist:RemoveAt(2).

        // Print out the number of presidents in the list
        message thelist:Count.

        // Iterate over the entries in the list
        iterator = thelist:GetIterator().
        repeat while iterator:MoveNext():
          message iterator:Current:ToString().
        end.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

  }

  @Test
  public void testExpressionEngine01() {
    String code = """
        def var x  as int.
        def var x1 as int.
        def var x2 as int.
        def var x3 as int.

        x1 + x2 + x3.
        x1 + x2 * x3.
        x1 = x2 = x3.
        // Perfectly valid code... Should be reported by a rule
        x1 + x2 = x3.
        x1 < x2 or x3 > x2 or x1 + x2 * x3 = x3.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    ParseUnit unit = getParseUnit("VAR CHAR s1, s2, s3, s4.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement02() {
    ParseUnit unit = getParseUnit("VAR INT x, y, z = 3.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement03() {
    ParseUnit unit = getParseUnit("VAR CLASS mypackage.subdir.myclass myobj1, myobj2, myobj3.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement04() {
    ParseUnit unit = getParseUnit("VAR mypackage.subdir.myclass myobj1.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement05() {
    ParseUnit unit = getParseUnit("VAR DATE d1, d2 = 1/1/2020, d3 = TODAY.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement06() {
    ParseUnit unit = getParseUnit("VAR PROTECTED DATE d1, d2 = 1/1/2020.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement07() {
    ParseUnit unit = getParseUnit("VAR INT[3] x = [1, 2], y, z = [100, 200, 300].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement08() {
    ParseUnit unit = getParseUnit("VAR INT[] x, y.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement09() {
    ParseUnit unit = getParseUnit("VAR INT[] x, y = [1,2,3].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement10() {
    ParseUnit unit = getParseUnit("VAR INT[] x = [1,2], y = [1,2,3].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement11() {
    ParseUnit unit = getParseUnit("VAR CLASS foo[2] classArray.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement12() {
    ParseUnit unit = getParseUnit("VAR \"System.Collections.Generic.List<char>\" cList.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement13() {
    ParseUnit unit = getParseUnit("VAR INT a, b, x = a + b, y = a - b, z = x - y.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testVarStatement14() {
    ParseUnit unit = getParseUnit("VAR INT a, b. VAR INT[] x = [ a + b, a - b ].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void testVarStatement15() {
    ParseUnit unit = getParseUnit("USING Progress.Lang.Object. VAR Object x = NEW Object().", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void testVarStatement16() {
    ParseUnit unit = getParseUnit("VAR DATETIME dtm = DATETIME(TODAY,MTIME).", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testDbQualifierSports2000() {
    String code = """
        define buffer bcust for customer.
        find customer. // Has to work whatever the case
        find SPOrts2000.customer.
        // Looks weird to me, but this is valid syntax
        find sports2000.bcust. // Works with database name
        find SPOrts2000.bcust. // Works with database name
        // Note: FIND aliasName.customer works
        // But FIND aliasName.bufferName doesn't work
        """;
    // Standard schema, lower-case database name
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    // Looking for the FIND node
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.FIND);
    assertNotNull(nodes);
    assertEquals(nodes.size(), 4);

    JPNode findNode = nodes.get(0);
    JPNode ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    JPNode ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    RecordNameNode rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");

    findNode = nodes.get(1);
    ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");

    findNode = nodes.get(2);
    ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");

    findNode = nodes.get(3);
    ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "sports2000.Customer");
  }

  @Test
  public void testDbQualifierSP2K() throws IOException {
    String code = """
        define buffer bcust for customer.
        find SP2K.customer. // Has to work whatever the case
        find sp2K.customer.
        // Looks weird to me, but this is valid syntax
        find SP2K.bcust. // Works with database name
        find sp2K.bcust. // Works with database name
        // Note: FIND aliasName.customer works
        // But FIND aliasName.bufferName doesn't work
        """;
    RefactorSession session2 = new RefactorSession(new UnitTestProparseSettings(), new SP2KSchema());

    ParseUnit unit = getParseUnit(code, session2);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    // Looking for the FIND node
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.FIND);
    assertNotNull(nodes);
    assertEquals(nodes.size(), 4);

    JPNode findNode = nodes.get(0);
    JPNode ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    JPNode ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    RecordNameNode rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");

    findNode = nodes.get(1);
    ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");

    findNode = nodes.get(2);
    ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");

    findNode = nodes.get(3);
    ch0 = findNode.getFirstChild();
    assertNotNull(ch0);
    assertEquals(ch0.getNodeType(), ABLNodeType.RECORD_SEARCH);
    ch1 = ch0.getFirstChild();
    assertNotNull(ch1);
    assertEquals(ch1.getNodeType(), ABLNodeType.RECORD_NAME);
    rec1 = (RecordNameNode) ch1;
    assertNotNull(rec1.getTableBuffer());
    assertEquals(rec1.getTableBuffer().getTargetFullName(), "SP2K.Customer");
  }

  @Test
  public void testEnum01() {
    String code = """
        enum rssw.enum01:
           define enum val1 = 1.
           define enum val2 = 2.
        end enum.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getSupport().getClassName(), "rssw.enum01");
    assertTrue(unit.getSupport().isEnum());
    assertEquals(unit.getClassName(), "rssw.enum01");
    assertTrue(unit.isEnum());
  }

  @Test
  public void testEnum02() {
    String code = """
        enum rssw.enum02:

        end enum.
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getSupport().getClassName(), "rssw.enum02");
    assertTrue(unit.getSupport().isEnum());
    assertEquals(unit.getClassName(), "rssw.enum02");
    assertTrue(unit.isEnum());
  }

  @Test
  public void testEntered01() {
    String code = """
        find first SalesRep.
        if SalesRep.MonthQuota[1] entered then do:
          //
        end.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
  }

  @Test
  public void testElvis01() {
    String code = """
        def var xx as Progress.Lang.Object.
        xx = new Progress.Lang.Object().
        message xx?:toString().
        message xx?:previous-sibling.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 4);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.ELVIS).get(0);
    assertEquals(node1.getLine(), 3);
    assertEquals(node1.getEndLine(), 3);
    assertEquals(node1.getColumn(), 10);
    assertEquals(node1.getEndColumn(), 12);
    assertEquals(node1.getText(), "?:");
  }

  @Test
  public void testElvis02() {
    String code = """
        for each customer where customer.name eq ?:
          display customer.
        end.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.UNKNOWNVALUE).get(0);
    JPNode node2 = unit.getTopNode().query(ABLNodeType.LEXCOLON).get(0);
    JPNode node3 = unit.getTopNode().query(ABLNodeType.DISPLAY).get(0);
    assertEquals(node1.getLine(), 1);
    assertEquals(node1.getColumn(), 41);
    assertEquals(node1.getEndColumn(), 42);
    assertEquals(node1.getText(), "?");
    assertEquals(node2.getLine(), 1);
    assertEquals(node2.getColumn(), 42);
    assertEquals(node2.getEndColumn(), 43);
    assertEquals(node2.getText(), ":");
    assertEquals(node3.getLine(), 2);
    assertEquals(node3.getColumn(), 2);
    assertEquals(node3.getEndColumn(), 9);
    assertNotNull(node3.getHiddenBefore());
    assertNull(node3.getHiddenBefore().getHiddenBefore());
    assertEquals(node3.getHiddenBefore().getLine(), 1);
    assertEquals(node3.getHiddenBefore().getEndLine(), 2);
    assertEquals(node3.getHiddenBefore().getCharPositionInLine(), 43);
    assertEquals(node3.getHiddenBefore().getEndCharPositionInLine(), 2);
  }

  @Test
  public void testAccumulateSum01() {
    String code = """
        // SUM and TOTAL are identical

        FOR EACH Customer NO-LOCK BREAK BY state:
          ACCUMULATE Customer.CreditLimit (TOTAL BY state).
          DISPLAY state  SKIP "Total: " ACCUM TOTAL Customer.CreditLimit.
        END.

        FOR EACH Customer NO-LOCK BREAK BY state:
          ACCUMULATE Customer.CreditLimit (SUM BY state).
          DISPLAY state  SKIP "Sum: " ACCUM SUM Customer.CreditLimit.
        END.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 6);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.ACCUMULATE).get(1);
    JPNode node2 = unit.getTopNode().query(ABLNodeType.ACCUMULATE).get(3);
    assertEquals(node1.getFirstChild().getNodeType(), ABLNodeType.TOTAL);
    assertEquals(node2.getFirstChild().getNodeType(), ABLNodeType.SUM);
  }

  @Test
  public void testAccumulateSum02() {
    String code = """
        FOR EACH Customer NO-LOCK BREAK BY state:
          accumulate Customer.CreditLimit (TOTAL max avg BY state).
          display accum avg Customer.CreditLimit.
        END.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.ACCUMULATE).get(1);
    assertEquals(node1.getFirstChild().getNodeType(), ABLNodeType.AVG);
  }

  @Test
  public void testAggregate01() {
    String code = """
        VAR INTEGER numCustomers.

        AGGREGATE numCustomers = COUNT(CustNum) FOR Customer.
        AGGREGATE numCustomers = COUNT(Customer.CustNum) FOR Customer.
        AGGREGATE numCustomers = COUNT(sp2k.customer.CustNum) FOR Customer.

        MESSAGE "Number of customers: " numCustomers
          VIEW-AS ALERT-BOX.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 5);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.AGGREGATE).get(0);
    assertNotNull(node1);
  }

  @Test
  public void testAggregate02() {
    String code = """
        VAR INTEGER numCustomers.

        AGGREGATE numCustomers = Count(CustNum) FOR Customer
          WHERE Country EQ 'USA' AND City EQ 'Boston'.

        MESSAGE "Number of customers: " numCustomers
          VIEW-AS ALERT-BOX.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.AGGREGATE).get(0);
    assertNotNull(node1);
  }

  @Test
  public void testAggregate03() {
    String code = """
        VAR DECIMAL avgBalance.

        AGGREGATE avgBalance = AVERAGE(Balance) FOR Customer
          WHERE Country EQ 'USA' AND City EQ 'Chicago'.

        MESSAGE "Average balance: " avgBalance
          VIEW-AS ALERT-BOX.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.AGGREGATE).get(0);
    assertNotNull(node1);
  }

  @Test
  public void testAggregate04() {
    String code = """
        VAR DECIMAL totalBalance.

        AGGREGATE totalBalance = TOTAL(Balance) FOR Customer
          WHERE Country EQ 'USA' AND City EQ 'Los Angeles'.

        MESSAGE "Total balance: " totalBalance
          VIEW-AS ALERT-BOX.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.AGGREGATE).get(0);
    assertNotNull(node1);
  }

  @Test
  public void testInClassStatement() {
    String code = """
        class package.FormStmtInClass:
          def var cc as char.
          form header cc.
          constructor FormStmtInClass():
            // do something
          end constructor.
        end class.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 4);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.FORMAT).get(0);
    assertEquals(node1.getParent().getFirstChild().getNodeType(), ABLNodeType.DEFINE);
    assertEquals(node1.getParent().getDirectChildren().get(2).getNodeType(), ABLNodeType.CONSTRUCTOR);
  }

  @Test(enabled = false)
  public void testDotComment01() {
    String code = """
        define variable hExcel as com-handle.
        define variable hWorkbook as com-handle.
        define variable hWorksheet as com-handle.

        create "Excel.Application" hExcel.
        hExcel:visible = yes.
        hWorkbook = hExcel:Workbooks:Add().
        hWorkSheet = hExcel:WorkSheets(1).
        hExcel:Worksheets(1):Cells(1, 1)  = "XXX".
        .hExcel:Worksheets(1):Cells(1, 2) = "YYY".
        hExcel:Worksheets(1):Cells(1, 3)  = "ZZZ".
        .hExcel:Application:Workbooks:close() no-error.
        release object hExcel.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 10);
  }

  @Test
  public void testVarName() {
    String code = """
        define temp-table tt1 no-undo
          field fld1 as integer
          field var  as charater
          index idx1 is primary unique fld1.

        define variable var as char no-undo.
        var = 'abc'.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();

    // Temp-table tt1 should be there
    var b1 = unit.getRootScope().getBufferSymbol("tt1");
    assertNotNull(b1);
    // Field 'var' is defined, while 'var1' is not
    assertTrue(b1.getFieldBufferList().stream().anyMatch(it -> "var".equals(it.getName())));
    assertFalse(b1.getFieldBufferList().stream().anyMatch(it -> "var1".equals(it.getName())));
    // Check that variable 'var' is defined
    assertEquals(unit.getRootScope().getVariables().size(), 1);
    var v1 = unit.getRootScope().getVariable("var");
    assertNotNull(v1);
    assertEquals(v1.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
  }

  @Test
  public void testRequire() {
    // This procedure doesn't compile, but Proparse still accepts it. Not particularly correct,
    // but that won't be fixed for now. In order to be aware of this behavior, we keep this unit test.
    // The Regex ID is considered a field buffer (abbreviated) pointing to a DB table
    String code = """
        using System.Text.RegularExpressions.Regex from assembly.
        message Regex:Escape("test").
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.FIELD_REF).size(), 1);
    JPNode n1 = unit.getTopNode().query(ABLNodeType.FIELD_REF).get(0);
    assertNotNull(n1.getSymbol());
    assertTrue(n1.getSymbol() instanceof FieldBuffer);
  }

  @Test
  public void testRequireBis() {
    // This procedure doesn't compile, but Proparse still accepts it. Not particularly correct,
    // but that won't be fixed for now. In order to be aware of this behavior, we keep this unit test.
    // The Regex ID is considered a field buffer (abbreviated) pointing to a DB table
    String code = """
        using System.Text.RegularExpressions.Regex from assembly.
        message Regex:Escape("test").
        """;
    ((ProparseSettings) session.getProparseSettings()).setRequireFullName(true);
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.FIELD_REF).size(), 1);
    JPNode n1 = unit.getTopNode().query(ABLNodeType.FIELD_REF).get(0);
    assertNull(n1.getSymbol());
  }

  @Test
  public void testRequire02() {
    String code = """
        find first custom.
        display custom.addre.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.RECORD_NAME).size(), 1);
    JPNode n1 = unit.getTopNode().query(ABLNodeType.FIELD_REF).get(0);
    assertNotNull(n1.getSymbol());
  }

  @Test
  public void testRequire02Bis() {
    String code = """
        find first custom.
        display custom.addre.
        """;
    ((ProparseSettings) session.getProparseSettings()).setRequireFullName(true);
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.RECORD_NAME).size(), 1);
    JPNode n1 = unit.getTopNode().query(ABLNodeType.FIELD_REF).get(0);
    assertNull(n1.getSymbol());
  }

  @Test
  public void testTempTableWithLabel() {
    String code = """
        define temp-table tt1 label "lbl1"
          field id as int
          index ix is unique id.
        define temp-table tt2 like tt1 label 'xyz'.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    assertEquals(unit.getTopNode().query(ABLNodeType.DEFINE).size(), 2);
    JPNode n1 = unit.getTopNode().query(ABLNodeType.DEFINE).get(0);
    assertTrue(n1.isIStatement());
    assertEquals(n1.asIStatement().getNodeType2(), ABLNodeType.TEMPTABLE);
    JPNode n2 = unit.getTopNode().query(ABLNodeType.DEFINE).get(1);
    assertTrue(n2.isIStatement());
    assertEquals(n2.asIStatement().getNodeType2(), ABLNodeType.TEMPTABLE);
  }

  @Test
  public void testPublishFrom() {
    String code = """
        class package.publishFrom:

          method void test01():
            define variable hp as handle no-undo.
            // Ensure that the from option is just "hp" and not "hp(?)"
            // (?) has to be the parameter of the publish statement
            publish 'xxx' from hp ( ? ).
          end method.

        end class.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().query(ABLNodeType.PUBLISH).size(), 1);
    JPNode n1 = unit.getTopNode().query(ABLNodeType.PUBLISH).get(0);
    assertNotNull(n1.findDirectChild(ABLNodeType.PARAMETER_LIST));
  }

  @Test
  public void testUnknownTable() {
    ParseUnit unit = getParseUnit(
        "FIND customer. DISPLAY customer.address. FIND sp2k.plopmachin. DISPLAY sp2k.plopmachin.fld1. ", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    List<JPNode> list = unit.getTopNode().query(ABLNodeType.RECORD_NAME);
    assertEquals(list.size(), 2);
    RecordNameNode recNode1 = (RecordNameNode) list.get(0);
    RecordNameNode recNode2 = (RecordNameNode) list.get(1);
    assertNotNull(recNode1.getSymbol());
    assertNull(recNode2.getSymbol());
    assertEquals(recNode1.getTableBuffer().getName(), "Customer");

    List<JPNode> list2 = unit.getTopNode().query(ABLNodeType.FIELD_REF);
    assertEquals(list2.size(), 2);
    FieldRefNode field1 = (FieldRefNode) list2.get(0);
    FieldRefNode field2 = (FieldRefNode) list2.get(1);
    assertNotNull(field1.getSymbol());
    assertEquals(field1.getSymbol().getName(), "Address");
    assertNull(field2.getSymbol());
  }

  @Test
  public void testMultipleIncludes01() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "multi_inc_01.p"), session);
    unit.treeParser01();

    List<JPNode> list = unit.getTopNode().queryStateHead(ABLNodeType.PROCEDURE);
    assertEquals(list.size(), 3);
    assertEquals(list.get(0).getFileIndex(), 1);
    assertEquals(list.get(1).getFileIndex(), 1);
    assertEquals(list.get(2).getFileIndex(), 1);
    assertEquals(list.get(0).getLine(), 2);
    assertEquals(list.get(1).getLine(), 8);
    assertEquals(list.get(2).getLine(), 14);

    List<String> includes = unit.getIncludeFilesList();
    assertEquals(includes.size(), 2);
    assertEquals(includes.get(0).replace('\\', '/'), SRC_DIR + "/multi_inc_01.p");
    assertTrue(includes.get(1).replace('\\', '/').endsWith(SRC_DIR + "/multi_inc_01.i"));
  }

  @Test
  public void testMultipleIncludes02() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "multi_inc_02.p"), session);
    unit.treeParser01();

    List<JPNode> list = unit.getTopNode().queryStateHead(ABLNodeType.PROCEDURE);
    assertEquals(list.size(), 3);
    assertEquals(list.get(0).getFileIndex(), 1);
    assertEquals(list.get(1).getFileIndex(), 1);
    assertEquals(list.get(2).getFileIndex(), 1);
    assertEquals(list.get(0).getLine(), 2);
    assertEquals(list.get(1).getLine(), 8);
    assertEquals(list.get(2).getLine(), 14);

    List<String> includes = unit.getIncludeFilesList();
    assertEquals(includes.size(), 2);
    assertEquals(includes.get(0).replace('\\', '/'), SRC_DIR + "/multi_inc_02.p");
    assertTrue(includes.get(1).replace('\\', '/').endsWith(SRC_DIR + "/multi_inc_01.i"));
  }

  @Test
  public void testIncludeNotFound01() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "inc_not_found.p"), session);
    expectThrows(UncheckedIOException.class, unit::treeParser01);
  }

  @Test
  public void testEventScope() {
    String code = """
        class TooManyParams:

          define public event event01 signature void ( input sender as Progress.Lang.Object, input e as Progress.Lang.Object ).
          define public event event02 signature void ( input sender as Progress.Lang.Object, input e as Progress.Lang.Object ).
          define public event event03 signature void ( input sender as Progress.Lang.Object, input e as Progress.Lang.Object, input xyz as int ).

          define public property prop01 as integer get. set.

        end class.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertEquals(unit.getRootScope().getRoutine().getParameters().size(), 0);
    assertEquals(unit.getRootScope().getVariables().size(), 1);
    assertEquals(unit.getRootScope().getEventRoutines().size(), 3);
    assertEquals(unit.getRootScope().getEventRoutines().get(0).getParameters().size(), 2);
    assertEquals(unit.getRootScope().getEventRoutines().get(1).getParameters().size(), 2);
    assertEquals(unit.getRootScope().getEventRoutines().get(2).getParameters().size(), 3);
  }

  @Test
  public void testTokenChars01() throws IOException {
    var settings = new UnitTestProparseSettings();
    var localSession =  new RefactorSession(settings, new SportsSchema());
    var unit = getParseUnit(new File(SRC_DIR, "tokenChars01.p"), localSession);
    expectThrows(ParseCancellationException.class, unit::parse);
  }

  @Test
  public void testTokenChars02() throws IOException {
    var settings = new UnitTestProparseSettings();
    settings.setTokenStartChars(new char[] {'#', '!'});
    var localSession =  new RefactorSession(settings, new SportsSchema());
    var unit = getParseUnit(new File(SRC_DIR, "tokenChars01.p"), localSession);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
  }

  @Test
  public void testRunAsync01() {
    var code = """
        var handle xHdl.
        run xxx.p on server xHdl asynchronous event-handler 'xyz' event-handler-context this-object (input 1, input 2).
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    var list01 = unit.getTopNode().queryStateHead(ABLNodeType.RUN);
    assertEquals(list01.size(), 1);
    var stmt01 = list01.get(0);
    var list02 = stmt01.query(ABLNodeType.TYPELESS_TOKEN);
    assertTrue(list02.isEmpty());
    var list03 = stmt01.query(ABLNodeType.PARAMETER_LIST);
    assertFalse(list03.isEmpty());
    assertEquals(list03.get(0).getDirectChildren(ABLNodeType.PARAMETER_ITEM).size(), 2);
  }

  @Test
  public void testRunAsync02() {
    var code = """
        var handle xHdl.
        run xxx.p on server xHdl asynchronous event-handler 'xyz' (input 1, input 2).
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    var list01 = unit.getTopNode().queryStateHead(ABLNodeType.RUN);
    assertEquals(list01.size(), 1);
    var stmt01 = list01.get(0);
    var list02 = stmt01.query(ABLNodeType.TYPELESS_TOKEN);
    assertTrue(list02.isEmpty());
    var list03 = stmt01.query(ABLNodeType.PARAMETER_LIST);
    assertFalse(list03.isEmpty());
    assertEquals(list03.get(0).getDirectChildren(ABLNodeType.PARAMETER_ITEM).size(), 2);
  }

  @Test
  public void testWaitForSet() {
    var unit = getParseUnit("WAIT-FOR xObj:methodName() SET this-object:attrName.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void testUndoThrowNew() {
    var code = """
        undo, throw new Progress.Lang.Error('', '').
        undo, throw new Progress.Lang.Error(available customer, ambiguous item).
        """;
    var unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
    var exprList = unit.getTopNode().queryExpressions();
    assertEquals(exprList.size(), 2); // NEW Progress.Lang...
  }

}
