/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2021 Riverside Software
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

import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.IfNode;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TreeParserBlocksTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser05/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    JPNode currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.getLine(), 1);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    JPNode prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.getLine(), 2);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.getLine(), 3);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.getLine(), 5);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.getLine(), 6);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.PROCEDURE);
    assertEquals(currStmt.getLine(), 8);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());
    assertTrue(currStmt instanceof BlockNode);

    JPNode currSubStmt = ((BlockNode) currStmt).getFirstStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currSubStmt.getLine(), 9);
    assertNull(currSubStmt.getPreviousStatement());
    assertNotNull(currSubStmt.getNextStatement());

    JPNode prevSubStmt = currSubStmt;
    currSubStmt = currSubStmt.getNextStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currSubStmt.getLine(), 10);
    assertEquals(currSubStmt.getPreviousStatement(), prevSubStmt);
    assertNull(currSubStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.getLine(), 13);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNull(currStmt.getNextStatement());
  }

  @Test
  public void test02() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser05/test02.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    JPNode stmt1 = unit.getTopNode().getFirstStatement();
    assertNotNull(stmt1);
    assertEquals(stmt1.getNodeType(), ABLNodeType.IF);
    assertEquals(stmt1.getLine(), 1);
    assertNull(stmt1.getPreviousStatement());
    assertNotNull(stmt1.getNextStatement());

    JPNode stmt2 = stmt1.getNextStatement();
    assertNotNull(stmt2);
    assertEquals(stmt2.getNodeType(), ABLNodeType.IF);
    assertEquals(stmt2.getLine(), 6);
    assertEquals(stmt2.getPreviousStatement(), stmt1);
    assertNull(stmt2.getNextStatement());

    JPNode currSubStmt = ((IfNode) stmt1).getIfStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt.getLine(), 2);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt = ((IfNode) stmt1).getElseStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt.getLine(), 4);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt = ((IfNode) stmt2).getIfStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DO);
    assertEquals(currSubStmt.getLine(), 6);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    JPNode currSubStmt2 = ((BlockNode) currSubStmt).getFirstStatement();
    assertNotNull(currSubStmt2);
    assertEquals(currSubStmt2.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt2.getLine(), 7);
    assertNull(currSubStmt2.getPreviousStatement());
    assertNull(currSubStmt2.getNextStatement());

    currSubStmt = ((IfNode) stmt2).getElseStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DO);
    assertEquals(currSubStmt.getLine(), 9);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt2 = ((BlockNode) currSubStmt).getFirstStatement();
    assertNotNull(currSubStmt2);
    assertEquals(currSubStmt2.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt2.getLine(), 10);
    assertNull(currSubStmt2.getPreviousStatement());
    assertNull(currSubStmt2.getNextStatement());
  }

  @Test
  public void test03() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser05/test03.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    JPNode currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.getLine(), 1);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    JPNode prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.PROCEDURE);
    assertEquals(currStmt.getLine(), 3);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DO);
    assertEquals(currStmt.getLine(), 15);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNull(currStmt.getNextStatement());

    JPNode subNode1 = ((BlockNode) prevStmt).getFirstStatement();
    assertNotNull(subNode1);
    assertEquals(subNode1.getNodeType(), ABLNodeType.DO);
    assertEquals(subNode1.getLine(), 4);
    assertNull(subNode1.getPreviousStatement());
    assertNull(subNode1.getNextStatement());

    JPNode subNode2 = ((BlockNode) subNode1).getFirstStatement();
    assertNotNull(subNode2);
    assertEquals(subNode2.getNodeType(), ABLNodeType.DO);
    assertEquals(subNode2.getLine(), 5);
    assertNull(subNode2.getPreviousStatement());
    assertNotNull(subNode2.getNextStatement());

    subNode2 = subNode2.getNextStatement();
    assertNotNull(subNode2);
    assertEquals(subNode2.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(subNode2.getLine(), 11);
    assertNotNull(subNode2.getPreviousStatement());
    assertNull(subNode2.getNextStatement());

    JPNode subNode3 = ((BlockNode) subNode2.getPreviousNode()).getFirstStatement();
    assertNotNull(subNode3);
    assertEquals(subNode3.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(subNode3.getLine(), 6);
    assertNull(subNode3.getPreviousStatement());
    assertNotNull(subNode3.getNextStatement());

    subNode3 = subNode3.getNextStatement();
    assertNotNull(subNode3);
    assertEquals(subNode3.getNodeType(), ABLNodeType.DO);
    assertEquals(subNode3.getLine(), 7);
    assertNotNull(subNode3.getPreviousStatement());
    assertNull(subNode3.getNextStatement());

    JPNode subNode4 = ((BlockNode) subNode3).getFirstStatement();
    assertNotNull(subNode4);
    assertEquals(subNode4.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(subNode4.getLine(), 8);
    assertNull(subNode4.getPreviousStatement());
    assertNull(subNode4.getNextStatement());

    // Back to the last DO
    JPNode subNode5 = ((BlockNode) currStmt).getFirstStatement();
    assertNotNull(subNode5);
    assertEquals(subNode5.getNodeType(), ABLNodeType.CREATE);
    assertEquals(subNode5.getLine(), 16);
    assertNull(subNode5.getPreviousStatement());
    assertNotNull(subNode5.getNextStatement());
  }

  @Test
  public void test04() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser05/test04.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    JPNode currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.FUNCTION);
    assertEquals(currStmt.getLine(), 2);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.getLine(), 5);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.getLine(), 6);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.getLine(), 7);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.FUNCTION);
    assertEquals(currStmt.getLine(), 10);
    assertNotNull(currStmt.getPreviousStatement());
    assertNull(currStmt.getNextStatement());
  }
}
