/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2023 Riverside Software
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
import java.io.IOException;

import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.core.nodetypes.StatementBlockNode;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TreeParserBlocksTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser05/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IStatement currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 1);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    IStatement prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 2);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 3);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.asJPNode().getLine(), 5);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.asJPNode().getLine(), 6);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.PROCEDURE);
    assertEquals(currStmt.asJPNode().getLine(), 8);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertTrue(currStmt.asJPNode().isIStatementBlock());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    IStatement currSubStmt = currStmt.asJPNode().asIStatementBlock().getFirstStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.asJPNode().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currSubStmt.asJPNode().getLine(), 9);
    assertEquals(currSubStmt.getParentStatement(), currStmt);
    assertNull(currSubStmt.getPreviousStatement());
    assertNotNull(currSubStmt.getNextStatement());

    IStatement prevSubStmt = currSubStmt;
    currSubStmt = currSubStmt.getNextStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.asJPNode().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currSubStmt.asJPNode().getLine(), 10);
    assertEquals(currSubStmt.getParentStatement(), currStmt);
    assertEquals(currSubStmt.getPreviousStatement(), prevSubStmt);
    assertNull(currSubStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.asJPNode().getLine(), 13);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
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

    IStatement stmt1 = unit.getTopNode().getFirstStatement();
    assertNotNull(stmt1);
    assertEquals(stmt1.asJPNode().getNodeType(), ABLNodeType.IF);
    assertEquals(stmt1.asJPNode().getLine(), 1);
    assertEquals(stmt1.getParentStatement(), unit.getTopNode());
    assertEquals(unit.getTopNode().getFirstStatement(), stmt1);
    assertNull(stmt1.getPreviousStatement());
    assertNotNull(stmt1.getNextStatement());

    IStatement stmt2 = stmt1.getNextStatement();
    assertNotNull(stmt2);
    assertEquals(stmt2.asJPNode().getNodeType(), ABLNodeType.IF);
    assertEquals(stmt2.asJPNode().getLine(), 6);
    assertEquals(stmt2.getParentStatement(), unit.getTopNode());
    assertEquals(stmt2.getPreviousStatement(), stmt1);
    assertNull(stmt2.getNextStatement());

    IStatement currSubStmt = ((IfStatementNode) stmt1).getThenBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt.asJPNode().getLine(), 2);
    assertEquals(currSubStmt.getParentStatement(), stmt1);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt =  ((IfStatementNode) stmt1).getElseBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt.asJPNode().getLine(), 4);
    assertEquals(currSubStmt.getParentStatement(), stmt1);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt = ((IfStatementNode) stmt2).getThenBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.asJPNode().getNodeType(), ABLNodeType.DO);
    assertEquals(currSubStmt.asJPNode().getLine(), 6);
    assertEquals(currSubStmt.getParentStatement(), stmt2);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    IStatement currSubStmt2 = ((StatementBlockNode) currSubStmt).getFirstStatement();
    assertNotNull(currSubStmt2);
    assertEquals(currSubStmt2.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt2.asJPNode().getLine(), 7);
    assertEquals(currSubStmt2.getParentStatement(), currSubStmt);
    assertNull(currSubStmt2.getPreviousStatement());
    assertNull(currSubStmt2.getNextStatement());

    currSubStmt = ((IfStatementNode) stmt2).getElseBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.asJPNode().getNodeType(), ABLNodeType.DO);
    assertEquals(currSubStmt.asJPNode().getLine(), 9);
    assertEquals(currSubStmt.getParentStatement(), stmt2);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt2 = ((StatementBlockNode) currSubStmt).getFirstStatement();
    assertNotNull(currSubStmt2);
    assertEquals(currSubStmt2.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt2.asJPNode().getLine(), 10);
    assertEquals(currSubStmt2.getParentStatement(), currSubStmt);
    assertNull(currSubStmt2.getPreviousStatement());
    assertNull(currSubStmt2.getNextStatement());

    // Test getNextNode()
    JPNode tmp = unit.getTopNode();
    int count = 0;
    while (tmp != null) {
      count++;
      tmp = tmp.getNextNode();
    }
    assertEquals(count, 42);
  }

  @Test
  public void test03() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser05/test03.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IStatement currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 1);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    IStatement prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.PROCEDURE);
    assertEquals(currStmt.asJPNode().getLine(), 3);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DO);
    assertEquals(currStmt.asJPNode().getLine(), 15);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNull(currStmt.getNextStatement());

    IStatement subNode1 = ((StatementBlockNode) prevStmt).getFirstStatement();
    assertNotNull(subNode1);
    assertEquals(subNode1.asJPNode().getNodeType(), ABLNodeType.DO);
    assertEquals(subNode1.asJPNode().getLine(), 4);
    assertNull(subNode1.getPreviousStatement());
    assertNull(subNode1.getNextStatement());

    IStatement subNode2 = ((StatementBlockNode) subNode1).getFirstStatement();
    assertNotNull(subNode2);
    assertEquals(subNode2.asJPNode().getNodeType(), ABLNodeType.DO);
    assertEquals(subNode2.asJPNode().getLine(), 5);
    assertNull(subNode2.getPreviousStatement());
    assertNotNull(subNode2.getNextStatement());

    subNode2 = subNode2.getNextStatement();
    assertNotNull(subNode2);
    assertEquals(subNode2.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(subNode2.asJPNode().getLine(), 11);
    assertNotNull(subNode2.getPreviousStatement());
    assertNull(subNode2.getNextStatement());

    IStatement subNode3 = subNode2.asJPNode().getPreviousNode().asIStatementBlock().getFirstStatement();
    assertNotNull(subNode3);
    assertEquals(subNode3.asJPNode().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(subNode3.asJPNode().getLine(), 6);
    assertNull(subNode3.getPreviousStatement());
    assertNotNull(subNode3.getNextStatement());

    subNode3 = subNode3.getNextStatement();
    assertNotNull(subNode3);
    assertEquals(subNode3.asJPNode().getNodeType(), ABLNodeType.DO);
    assertEquals(subNode3.asJPNode().getLine(), 7);
    assertNotNull(subNode3.getPreviousStatement());
    assertNull(subNode3.getNextStatement());

    IStatement subNode4 = ((StatementBlockNode) subNode3).getFirstStatement();
    assertNotNull(subNode4);
    assertEquals(subNode4.asJPNode().getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(subNode4.asJPNode().getLine(), 8);
    assertNull(subNode4.getPreviousStatement());
    assertNull(subNode4.getNextStatement());

    // Back to the last DO
    IStatement subNode5 = ((StatementBlockNode) currStmt).getFirstStatement();
    assertNotNull(subNode5);
    assertEquals(subNode5.asJPNode().getNodeType(), ABLNodeType.CREATE);
    assertEquals(subNode5.asJPNode().getLine(), 16);
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

    IStatement currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.FUNCTION);
    assertEquals(currStmt.asJPNode().getLine(), 2);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.asJPNode().getLine(), 5);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.asJPNode().getLine(), 6);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.asJPNode().getLine(), 7);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.asJPNode().getNodeType(), ABLNodeType.FUNCTION);
    assertEquals(currStmt.asJPNode().getLine(), 10);
    assertNotNull(currStmt.getPreviousStatement());
    assertNull(currStmt.getNextStatement());
  }

}
