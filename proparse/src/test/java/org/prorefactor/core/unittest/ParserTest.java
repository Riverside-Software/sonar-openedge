/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.unittest.util.UnitTestModule;
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
    session.getSchema();
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
}
