/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.core.nodetypes.StatementBlockNode;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ExecutionGraph;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.Routine;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TreeParserBlocksTest extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IStatement currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 1);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    IStatement prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 2);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 3);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.asJPNode().getLine(), 6);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertEquals(currStmt.getAnnotations().size(), 2);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.asJPNode().getLine(), 7);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.PROCEDURE);
    assertEquals(currStmt.asJPNode().getLine(), 9);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertTrue(currStmt.asJPNode().isIStatementBlock());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    IStatement currSubStmt = currStmt.asJPNode().asIStatementBlock().getFirstStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currSubStmt.asJPNode().getLine(), 10);
    assertEquals(currSubStmt.getParentStatement(), currStmt);
    assertNull(currSubStmt.getPreviousStatement());
    assertNotNull(currSubStmt.getNextStatement());

    IStatement prevSubStmt = currSubStmt;
    currSubStmt = currSubStmt.getNextStatement();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currSubStmt.asJPNode().getLine(), 11);
    assertEquals(currSubStmt.getParentStatement(), currStmt);
    assertEquals(currSubStmt.getPreviousStatement(), prevSubStmt);
    assertNull(currSubStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.asJPNode().getLine(), 14);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    // Last annotation can't be attached
    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(currStmt.asJPNode().getLine(), 15);
    assertEquals(currStmt.getParentStatement(), unit.getTopNode());
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNull(currStmt.getNextStatement());
  }

  @Test
  public void test02() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test02.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IStatement stmt1 = unit.getTopNode().getFirstStatement();
    assertNotNull(stmt1);
    assertEquals(stmt1.getNodeType(), ABLNodeType.IF);
    assertEquals(stmt1.asJPNode().getLine(), 1);
    assertEquals(stmt1.getParentStatement(), unit.getTopNode());
    assertEquals(unit.getTopNode().getFirstStatement(), stmt1);
    assertNull(stmt1.getPreviousStatement());
    assertNotNull(stmt1.getNextStatement());

    IStatement stmt2 = stmt1.getNextStatement();
    assertNotNull(stmt2);
    assertEquals(stmt2.getNodeType(), ABLNodeType.IF);
    assertEquals(stmt2.asJPNode().getLine(), 6);
    assertEquals(stmt2.getParentStatement(), unit.getTopNode());
    assertEquals(stmt2.getPreviousStatement(), stmt1);
    assertNull(stmt2.getNextStatement());

    IStatement currSubStmt = ((IfStatementNode) stmt1).getThenBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt.asJPNode().getLine(), 2);
    assertEquals(currSubStmt.getParentStatement(), stmt1);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt =  ((IfStatementNode) stmt1).getElseBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt.asJPNode().getLine(), 4);
    assertEquals(currSubStmt.getParentStatement(), stmt1);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt = ((IfStatementNode) stmt2).getThenBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DO);
    assertEquals(currSubStmt.asJPNode().getLine(), 6);
    assertEquals(currSubStmt.getParentStatement(), stmt2);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    IStatement currSubStmt2 = ((StatementBlockNode) currSubStmt).getFirstStatement();
    assertNotNull(currSubStmt2);
    assertEquals(currSubStmt2.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currSubStmt2.asJPNode().getLine(), 7);
    assertEquals(currSubStmt2.getParentStatement(), currSubStmt);
    assertNull(currSubStmt2.getPreviousStatement());
    assertNull(currSubStmt2.getNextStatement());

    currSubStmt = ((IfStatementNode) stmt2).getElseBlockOrNode();
    assertNotNull(currSubStmt);
    assertEquals(currSubStmt.getNodeType(), ABLNodeType.DO);
    assertEquals(currSubStmt.asJPNode().getLine(), 9);
    assertEquals(currSubStmt.getParentStatement(), stmt2);
    assertNull(currSubStmt.getPreviousStatement());
    assertNull(currSubStmt.getNextStatement());

    currSubStmt2 = ((StatementBlockNode) currSubStmt).getFirstStatement();
    assertNotNull(currSubStmt2);
    assertEquals(currSubStmt2.getNodeType(), ABLNodeType.MESSAGE);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test03.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IStatement currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 1);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    IStatement prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.PROCEDURE);
    assertEquals(currStmt.asJPNode().getLine(), 3);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNotNull(currStmt.getNextStatement());

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.ON);
    assertEquals(currStmt.asJPNode().getLine(), 15);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);

    prevStmt = currStmt;
    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DO);
    assertEquals(currStmt.asJPNode().getLine(), 20);
    assertEquals(currStmt.getPreviousStatement(), prevStmt);
    assertNull(currStmt.getNextStatement());

    IStatement subNode1 = ((StatementBlockNode) prevStmt.getPreviousStatement()).getFirstStatement();
    assertNotNull(subNode1);
    assertEquals(subNode1.getNodeType(), ABLNodeType.DO);
    assertEquals(subNode1.asJPNode().getLine(), 4);
    assertNull(subNode1.getPreviousStatement());
    assertNull(subNode1.getNextStatement());

    IStatement subNode2 = ((StatementBlockNode) subNode1).getFirstStatement();
    assertNotNull(subNode2);
    assertEquals(subNode2.getNodeType(), ABLNodeType.DO);
    assertEquals(subNode2.asJPNode().getLine(), 5);
    assertNull(subNode2.getPreviousStatement());
    assertNotNull(subNode2.getNextStatement());

    subNode2 = subNode2.getNextStatement();
    assertNotNull(subNode2);
    assertEquals(subNode2.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(subNode2.asJPNode().getLine(), 11);
    assertNotNull(subNode2.getPreviousStatement());
    assertNull(subNode2.getNextStatement());

    IStatement subNode3 = subNode2.asJPNode().getPreviousNode().asIStatementBlock().getFirstStatement();
    assertNotNull(subNode3);
    assertEquals(subNode3.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(subNode3.asJPNode().getLine(), 6);
    assertNull(subNode3.getPreviousStatement());
    assertNotNull(subNode3.getNextStatement());

    subNode3 = subNode3.getNextStatement();
    assertNotNull(subNode3);
    assertEquals(subNode3.getNodeType(), ABLNodeType.DO);
    assertEquals(subNode3.asJPNode().getLine(), 7);
    assertNotNull(subNode3.getPreviousStatement());
    assertNull(subNode3.getNextStatement());

    IStatement subNode4 = ((StatementBlockNode) subNode3).getFirstStatement();
    assertNotNull(subNode4);
    assertEquals(subNode4.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(subNode4.asJPNode().getLine(), 8);
    assertNull(subNode4.getPreviousStatement());
    assertNull(subNode4.getNextStatement());

    // Back to the last DO
    IStatement subNode5 = ((StatementBlockNode) currStmt).getFirstStatement();
    assertNotNull(subNode5);
    assertEquals(subNode5.getNodeType(), ABLNodeType.CREATE);
    assertEquals(subNode5.asJPNode().getLine(), 21);
    assertNull(subNode5.getPreviousStatement());
    assertNotNull(subNode5.getNextStatement());
  }

  @Test
  public void test04() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test04.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IStatement currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.FUNCTION);
    assertEquals(currStmt.asJPNode().getLine(), 2);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.asJPNode().getLine(), 5);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.asJPNode().getLine(), 6);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(currStmt.asJPNode().getLine(), 7);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.FUNCTION);
    assertEquals(currStmt.asJPNode().getLine(), 10);
    assertNotNull(currStmt.getPreviousStatement());
    assertNull(currStmt.getNextStatement());
  }

  @Test
  public void executionGraphTest01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Routine r0 = unit.getRootScope().getRoutines().get(0);
    ExecutionGraph graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 8); // 7 statements + RootNode
    assertEquals(graph0.getVertices().get(0).getNodeType(), ABLNodeType.PROGRAM_ROOT);
    // Very simple flow
    assertEquals(graph0.getEdges().get(0), Arrays.asList(1));
    assertEquals(graph0.getEdges().get(1), Arrays.asList(2));
    assertEquals(graph0.getEdges().get(2), Arrays.asList(3));
    assertEquals(graph0.getEdges().get(3), Arrays.asList(4));
    assertEquals(graph0.getEdges().get(4), Arrays.asList(5));
    assertEquals(graph0.getEdges().get(5), Arrays.asList(6));
    assertEquals(graph0.getEdges().get(6), Arrays.asList(7));
    assertTrue(graph0.getEdges().get(7).isEmpty());

    Routine r1 = unit.getRootScope().getRoutines().get(1);
    ExecutionGraph graph1 = r1.getExecutionGraph();
    assertEquals(graph1.getVertices().size(), 3); // 2 statements + Procedure node
    assertEquals(graph1.getVertices().get(0).getNodeType(), ABLNodeType.PROCEDURE);
    assertEquals(graph1.getVertices().get(1).getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(graph1.getVertices().get(2).getNodeType(), ABLNodeType.DISPLAY);
    // Very simple flow
    assertEquals(graph1.getEdges().get(0), Arrays.asList(1));
    assertEquals(graph1.getEdges().get(1), Arrays.asList(2));
    assertTrue(graph1.getEdges().get(2).isEmpty());
  }

  @Test
  public void executionGraphTest02() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test02.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Routine r0 = unit.getRootScope().getRoutines().get(0);
    ExecutionGraph graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 9); // 8 statements + RootNode
    assertEquals(graph0.getVertices().get(0).getNodeType(), ABLNodeType.PROGRAM_ROOT);
    assertEquals(graph0.getEdges().get(0), Arrays.asList(1));
    assertEquals(graph0.getEdges().get(1), Arrays.asList(2, 3, 4)); // IF
    assertTrue(graph0.getEdges().get(2).isEmpty()); // THEN
    assertTrue(graph0.getEdges().get(3).isEmpty()); // ELSE
    assertEquals(graph0.getEdges().get(4), Arrays.asList(5, 7)); // IF
    assertEquals(graph0.getEdges().get(5), Arrays.asList(6));
    assertEquals(graph0.getEdges().get(7), Arrays.asList(8));
    assertTrue(graph0.getEdges().get(6).isEmpty());
    assertTrue(graph0.getEdges().get(8).isEmpty());
    }

  @Test
  public void executionGraphTest03() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test03.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Routine r0 = unit.getRootScope().getRoutines().get(0);
    ExecutionGraph graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 5); // 4 statements + RootNode
    assertEquals(graph0.getEdges().get(0), Arrays.asList(1));
    assertEquals(graph0.getEdges().get(1), Arrays.asList(2));
    assertEquals(graph0.getEdges().get(2), Arrays.asList(3));
    assertEquals(graph0.getEdges().get(3), Arrays.asList(4));
    assertTrue(graph0.getEdges().get(4).isEmpty());

    Routine r1 = unit.getRootScope().getRoutines().get(1);
    ExecutionGraph graph1 = r1.getExecutionGraph();
    assertEquals(graph1.getVertices().size(), 7); // 6 statements + Procedure node
    assertEquals(graph1.getEdges().get(0), Arrays.asList(1));
    assertEquals(graph1.getEdges().get(1), Arrays.asList(2));
    assertEquals(graph1.getEdges().get(2), Arrays.asList(3, 6));
    assertEquals(graph1.getEdges().get(3), Arrays.asList(4));
    assertEquals(graph1.getEdges().get(4), Arrays.asList(5));
    assertTrue(graph1.getEdges().get(5).isEmpty());
    assertTrue(graph1.getEdges().get(6).isEmpty());
  }

  @Test
  public void executionGraphTest05() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test05.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Routine r0 = unit.getRootScope().getRoutines().get(0);
    ExecutionGraph graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 9); // 2 IF + 2 DO + FINALLY + 3 MESSAGE + Root
    assertEquals(graph0.getEdges().get(0), Arrays.asList(1));
    assertEquals(graph0.getEdges().get(1), Arrays.asList(2, 4, 7));
    assertEquals(graph0.getEdges().get(2), Arrays.asList(3));
    assertTrue(graph0.getEdges().get(3).isEmpty());
    assertEquals(graph0.getEdges().get(4), Arrays.asList(5));
    assertEquals(graph0.getEdges().get(5), Arrays.asList(6));
    assertTrue(graph0.getEdges().get(6).isEmpty());
    assertEquals(graph0.getEdges().get(7), Arrays.asList(8));
    assertTrue(graph0.getEdges().get(8).isEmpty());
  }

  @Test
  public void executionGraphTest06() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test06.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Routine r0 = unit.getRootScope().getRoutines().get(0);
    ExecutionGraph graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 5); // 2 IF + 1 DO + 1 MESSAGE + Root
    assertEquals(graph0.getEdges().get(0), Arrays.asList(1));
    assertEquals(graph0.getEdges().get(1), Arrays.asList(2));
    assertEquals(graph0.getEdges().get(2), Arrays.asList(3));
    assertEquals(graph0.getEdges().get(3), Arrays.asList(4));
    assertTrue(graph0.getEdges().get(4).isEmpty());
  }

  @Test
  public void test05() {
    // Only annotations
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test07.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IStatement currStmt = unit.getTopNode().getFirstStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    currStmt = currStmt.getNextStatement();
    assertNull(currStmt);
  }

  @Test
  public void test06() {
    // Only annotations
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test08.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Routine p1 = unit.getRootScope().getRoutines().get(1);
    assertEquals(p1.getName(), "p1");
    ExecutionGraph g1 = p1.getExecutionGraph();
    assertEquals(g1.getVertices().size(), 4); // The last 2 annotations are still there
    IStatement currStmt = p1.getExecutionGraph().getVertices().get(0).asIStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.PROCEDURE);
    assertTrue(currStmt.asJPNode().isIStatementBlock());
    currStmt = ((IStatementBlock) currStmt).getFirstStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(currStmt.getAnnotations().size(), 2);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.ANNOTATION);
    currStmt = currStmt.getNextStatement();
    assertNull(currStmt);
  }

  @Test
  public void test09() {
    // Only annotations
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser05/test09.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var currStmt = unit.getTopNode().getFirstStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    assertEquals(currStmt.asJPNode().getLine(), 1);
    assertNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.IF);
    assertEquals(currStmt.asJPNode().getLine(), 3);
    assertNotNull(currStmt.getPreviousStatement());
    assertNotNull(currStmt.getNextStatement());

    var thenStmt = ((IfStatementNode) currStmt).getThenBlockOrNode();
    assertNotNull(thenStmt);
    assertEquals(thenStmt.getNodeType(), ABLNodeType.ASSIGN);
    assertEquals(thenStmt.asJPNode().firstNaturalChild().getLine(), 4);
    assertNull(thenStmt.getPreviousStatement());
    assertNull(thenStmt.getNextStatement());
    assertEquals(thenStmt.getParentStatement(), currStmt);

    currStmt = currStmt.getNextStatement();
    assertNotNull(currStmt);
    assertEquals(currStmt.getNodeType(), ABLNodeType.IF);
    assertEquals(currStmt.asJPNode().getLine(), 10);

    thenStmt = ((IfStatementNode) currStmt).getThenBlockOrNode();
    assertNotNull(thenStmt);
    assertEquals(thenStmt.getNodeType(), ABLNodeType.ASSIGN_DYNAMIC_NEW);
    assertEquals(thenStmt.asJPNode().firstNaturalChild().getLine(), 11);
    assertNull(thenStmt.getPreviousStatement());
    assertNull(thenStmt.getNextStatement());
    assertEquals(thenStmt.getParentStatement(), currStmt);
  }
}
