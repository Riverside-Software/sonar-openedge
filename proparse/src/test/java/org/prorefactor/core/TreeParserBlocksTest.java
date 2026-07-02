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

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
    ParseUnit unit = getParseUnit("""
        def var zz1 as int.
        def var zz2 as int.
        def var zz3 as int.
        @MyAnnotation.
        @MyAnnotation2(a = b).
        message zz1.
        message zz2.

        procedure p1.
          display zz1.
          display zz2.
        end procedure.

        message zz3.
        @AnotherAnnotation.
        """, session);
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
    ParseUnit unit = getParseUnit("""
        if true then
          message "xx".
        else
          message "yy".

        if true then do:
          message "xx".
        end.
        else do:
          message "yy".
        end.
        """, session);
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
    ParseUnit unit = getParseUnit("""
        def var zz as int.

        procedure p1:
          do zz = 1 to 10:
            do zz = 2 to 20:
              display "xyz".
              do zz = 3 to 30:
                message "abc".
              end.
            end.
            message "xyz".
          end.
        end procedure.

        on write of customer do:
          message "Test".
          display "Test2".
        end.

        do zz = 1 to 10:
          create customer.
          create customer.
        end.
        """, session);
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
    ParseUnit unit = getParseUnit("""
        /* Forward declare doubler() */
        FUNCTION doubler RETURNS INTEGER (INPUT parm1 AS INTEGER) FORWARD.

        /* Reference doubler() */
        DISPLAY "doubler(0)=" doubler(0).
        DISPLAY "doubler(1)=" doubler(1).
        DISPLAY "doubler(2)=" doubler(2).

        /* Define doubler() */
        FUNCTION doubler RETURNS INTEGER (INPUT parm1 AS INTEGER):
          RETURN (2 * parm1).
        END FUNCTION.
        """, session);
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
    var unit = getParseUnit("""
        def var zz1 as int.
        def var zz2 as int.
        def var zz3 as int.
        @MyAnnotation.
        @MyAnnotation2(a = b).
        message zz1.
        message zz2.

        procedure p1.
          display zz1.
          display zz2.
        end procedure.

        message zz3.
        @AnotherAnnotation.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var r0 = unit.getRootScope().getRoutines().get(0);
    var graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 8); // 7 statements + RootNode

    assertEquals(getEdges(graph0, ABLNodeType.PROGRAM_ROOT, 0), "DEFINE:1");
    assertEquals(getEdges(graph0, ABLNodeType.DEFINE, 1), "DEFINE:2");
    assertEquals(getEdges(graph0, ABLNodeType.DEFINE, 2), "DEFINE:3");
    assertEquals(getEdges(graph0, ABLNodeType.DEFINE, 3), "MESSAGE:6");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 6), "MESSAGE:7");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 7), "MESSAGE:14");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 14), "ANNOTATION:15");
    assertEquals(getEdges(graph0, ABLNodeType.ANNOTATION, 15), "");

    assertEquals(getReverseEdges(graph0, ABLNodeType.DEFINE, 1), "PROGRAM_ROOT:0");
    assertEquals(getReverseEdges(graph0, ABLNodeType.DEFINE, 2), "DEFINE:1");
    assertEquals(getReverseEdges(graph0, ABLNodeType.DEFINE, 3), "DEFINE:2");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 6), "DEFINE:3");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 7), "MESSAGE:6");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 14), "MESSAGE:7");
    assertEquals(getReverseEdges(graph0, ABLNodeType.ANNOTATION, 15), "MESSAGE:14");

    var r1 = unit.getRootScope().getRoutines().get(1);
    var graph1 = r1.getExecutionGraph();
    assertEquals(graph1.getVertices().size(), 3); // 2 statements + Procedure node

    assertEquals(getEdges(graph1, ABLNodeType.PROCEDURE, 9), "DISPLAY:10");
    assertEquals(getEdges(graph1, ABLNodeType.DISPLAY, 10), "DISPLAY:11");
    assertEquals(getEdges(graph1, ABLNodeType.DISPLAY, 11), "");

    assertEquals(getReverseEdges(graph1, ABLNodeType.PROCEDURE, 9), "");
    assertEquals(getReverseEdges(graph1, ABLNodeType.DISPLAY, 10), "PROCEDURE:9");
    assertEquals(getReverseEdges(graph1, ABLNodeType.DISPLAY, 11), "DISPLAY:10");
  }

  @Test
  public void executionGraphTest02() {
    var unit = getParseUnit("""
        if true then
          message "xx".
        else
          message "yy".

        if true then do:
          message "xx".
        end.
        else do:
          message "yy".
        end.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var r0 = unit.getRootScope().getRoutines().get(0);
    var graph0 = r0.getExecutionGraph();

    assertEquals(getEdges(graph0, ABLNodeType.PROGRAM_ROOT, 0), "IF:1");
    assertEquals(getEdges(graph0, ABLNodeType.IF, 1), "IF:6 MESSAGE:2 MESSAGE:4");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 2), "IF:6");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 4), "IF:6");
    assertEquals(getEdges(graph0, ABLNodeType.IF, 6), "DO:6 DO:9");
    assertEquals(getEdges(graph0, ABLNodeType.DO, 6), "MESSAGE:7");
    assertEquals(getEdges(graph0, ABLNodeType.DO, 9), "MESSAGE:10");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 7), "");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 10), "");

    assertEquals(getReverseEdges(graph0, ABLNodeType.PROGRAM_ROOT, 0), "");
    assertEquals(getReverseEdges(graph0, ABLNodeType.IF, 1), "PROGRAM_ROOT:0");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 2), "IF:1");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 4), "IF:1");
    assertEquals(getReverseEdges(graph0, ABLNodeType.IF, 6), "IF:1 MESSAGE:2 MESSAGE:4");
    assertEquals(getReverseEdges(graph0, ABLNodeType.DO, 6), "IF:6");
    assertEquals(getReverseEdges(graph0, ABLNodeType.DO, 9), "IF:6");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 7), "DO:6");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 10), "DO:9");
  }

  @Test
  public void executionGraphTest03() {
    var unit = getParseUnit("""
        def var zz as int.

        procedure p1:
          do zz = 1 to 10:
            do zz = 2 to 20:
              display "xyz".
              do zz = 3 to 30:
                message "abc".
              end.
            end.
            message "xyz".
          end.
        end procedure.

        on write of customer do:
          message "Test".
          display "Test2".
        end.

        do zz = 1 to 10:
          create customer.
          create customer.
        end.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var r0 = unit.getRootScope().getRoutines().get(0);
    var graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 5); // 4 statements + RootNode
    assertEquals(getEdges(graph0, ABLNodeType.PROGRAM_ROOT, 0), "DEFINE:1");
    assertEquals(getEdges(graph0, ABLNodeType.DEFINE, 1), "DO:20");
    assertEquals(getEdges(graph0, ABLNodeType.DO, 20), "CREATE:21");
    assertEquals(getEdges(graph0, ABLNodeType.CREATE, 21), "CREATE:22");
    assertEquals(getEdges(graph0, ABLNodeType.CREATE, 22), "");

    var r1 = unit.getRootScope().getRoutines().get(1);
    var graph1 = r1.getExecutionGraph();
    assertEquals(graph1.getVertices().size(), 7); // 6 statements + Procedure node
    assertEquals(getEdges(graph1, ABLNodeType.PROCEDURE, 3), "DO:4");
    assertEquals(getEdges(graph1, ABLNodeType.DO, 4), "DO:5");
    assertEquals(getEdges(graph1, ABLNodeType.DO, 5), "DISPLAY:6 MESSAGE:11");
    assertEquals(getEdges(graph1, ABLNodeType.DISPLAY, 6), "DO:7");
    assertEquals(getEdges(graph1, ABLNodeType.DO, 7), "MESSAGE:11 MESSAGE:8");
    assertEquals(getEdges(graph1, ABLNodeType.MESSAGE, 8), "MESSAGE:11");
    assertEquals(getEdges(graph1, ABLNodeType.MESSAGE, 11), "");

    assertEquals(getReverseEdges(graph1, ABLNodeType.DO, 4), "PROCEDURE:3");
    assertEquals(getReverseEdges(graph1, ABLNodeType.DO, 5), "DO:4");
    assertEquals(getReverseEdges(graph1, ABLNodeType.DISPLAY, 6), "DO:5");
    assertEquals(getReverseEdges(graph1, ABLNodeType.DO, 7), "DISPLAY:6");
    assertEquals(getReverseEdges(graph1, ABLNodeType.MESSAGE, 8), "DO:7");
    assertEquals(getReverseEdges(graph1, ABLNodeType.MESSAGE, 11), "DO:5 DO:7 MESSAGE:8");
  }

  @Test
  public void executionGraphTest05() {
    var unit = getParseUnit("""
        if true then do:
          message "xx".
        end.
        else if false then do:
          message "yy".
        end.
        finally:
          message "zz".
        end finally.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var r0 = unit.getRootScope().getRoutines().get(0);
    var graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 9); // 2 IF + 2 DO + FINALLY + 3 MESSAGE + Root

    assertEquals(getEdges(graph0, ABLNodeType.PROGRAM_ROOT, 0), "IF:1");
    assertEquals(getEdges(graph0, ABLNodeType.IF, 1), "DO:1 FINALLY:7 IF:4");
    assertEquals(getEdges(graph0, ABLNodeType.DO, 1), "MESSAGE:2");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 2), "FINALLY:7");
    assertEquals(getEdges(graph0, ABLNodeType.IF, 4), "DO:4 FINALLY:7");
    assertEquals(getEdges(graph0, ABLNodeType.DO, 4), "MESSAGE:5");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 5), "FINALLY:7");
    assertEquals(getEdges(graph0, ABLNodeType.FINALLY, 7), "MESSAGE:8");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 8), "");

    assertEquals(getReverseEdges(graph0, ABLNodeType.IF, 1), "PROGRAM_ROOT:0");
    assertEquals(getReverseEdges(graph0, ABLNodeType.DO, 1), "IF:1");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 2), "DO:1");
    assertEquals(getReverseEdges(graph0, ABLNodeType.IF, 4), "IF:1");
    assertEquals(getReverseEdges(graph0, ABLNodeType.DO, 4), "IF:4");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 5), "DO:4");
    assertEquals(getReverseEdges(graph0, ABLNodeType.FINALLY, 7), "IF:1 IF:4 MESSAGE:2 MESSAGE:5");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 8), "FINALLY:7");
  }

  @Test
  public void executionGraphTest06() {
    var unit = getParseUnit("""
        if true then
          if true then do:
            message "xx".
          end.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var r0 = unit.getRootScope().getRoutines().get(0);
    var graph0 = r0.getExecutionGraph();
    assertEquals(graph0.getVertices().size(), 5); // 2 IF + 1 DO + 1 MESSAGE + Root
    assertEquals(getEdges(graph0, ABLNodeType.PROGRAM_ROOT, 0), "IF:1");
    assertEquals(getEdges(graph0, ABLNodeType.IF, 1), "IF:2");
    assertEquals(getEdges(graph0, ABLNodeType.IF, 2), "DO:2");
    assertEquals(getEdges(graph0, ABLNodeType.DO, 2), "MESSAGE:3");
    assertEquals(getEdges(graph0, ABLNodeType.MESSAGE, 3), "");

    var expectedMermaidString = """
        flowchart TD
        Node0[PROGRAM_ROOT 0:0]
        Node1[IF 1:0]
        Node2[IF 2:2]
        Node3[DO 2:15]
        Node4[MESSAGE 3:4]
        Node0 --> Node1
        Node1 --> Node2
        Node2 --> Node3
        Node3 --> Node4
        """;
    assertEquals(graph0.toMermaidString(), expectedMermaidString);

    assertEquals(getReverseEdges(graph0, ABLNodeType.PROGRAM_ROOT, 0), "");
    assertEquals(getReverseEdges(graph0, ABLNodeType.IF, 1), "PROGRAM_ROOT:0");
    assertEquals(getReverseEdges(graph0, ABLNodeType.IF, 2), "IF:1");
    assertEquals(getReverseEdges(graph0, ABLNodeType.DO, 2), "IF:2");
    assertEquals(getReverseEdges(graph0, ABLNodeType.MESSAGE, 3), "DO:2");
  }

  @Test
  public void test05() {
    // Only annotations
    ParseUnit unit = getParseUnit("""
        @Annotation1.
        @Annotation2.
        @Annotation3.
        @Annotation4.
        @Annotation5.
        """, session);
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
    ParseUnit unit = getParseUnit("""
        @Annotation1.
        procedure p1:
         @Annotation2.
         @Annotation3.
         message "hello".
         @Annotation4.
         @Annotation5.
        end procedure.
        """, session);
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
    ParseUnit unit = getParseUnit("""
        def var x1 as Progress.Lang.Object.

        if true then // +1
          x1 = new Object().
        else if true then // +1
          x1 = new Object().
        else // +1
          x1 = new Object().

        if true then // +1
          x1 = dynamic-new ('Progress.Lang.Object') ().
        else if true then // +1
          x1 = dynamic-new 'Progress.Lang.Object' ().
        else // +1
          x1 = dynamic-new ('Progress.Lang.Object') ().
        """, session);
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

  @Test
  public void testFinally01() {
    var unit = getParseUnit("""
        function foo logical private ( ) forward.

        define variable vl as logical.
        vl = foo().

        function foo return logical private ():
           define variable hq as handle no-undo.
           create query hq.
           return ?. // 'finally' statement is executed before leaving function
           finally:
              delete object hq.
           end finally.
        end function.

        function foo2 return logical private ():
          do while true:
            do while true:
              message "xx".
              return ?. // Go to finally
            end.
          end.
          message "xx".
          finally:
            delete object hq.
          end finally.
        end function.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var p1 = unit.getRootScope().getRoutines().get(1);
    assertEquals(p1.getName(), "foo");
    var g1 = p1.getExecutionGraph();
    assertEquals(g1.getVertices().size(), 6);

    var currStmt = p1.getExecutionGraph().getVertices().get(0).asIStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.FUNCTION);
    assertTrue(currStmt.asJPNode().isIStatementBlock());
    currStmt = ((IStatementBlock) currStmt).getFirstStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.CREATE);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.RETURN);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.FINALLY);
    assertTrue(currStmt.asJPNode().isIStatementBlock());
    currStmt = ((IStatementBlock) currStmt).getFirstStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.DELETE);
    currStmt = currStmt.getNextStatement();
    assertNull(currStmt);

    var p2 = unit.getRootScope().getRoutines().get(2);
    assertEquals(p2.getName(), "foo2");
    var g2 = p2.getExecutionGraph();
    assertEquals(g2.getVertices().size(), 8);
    assertEquals(g2.getVertices().get(4).getNodeType(), ABLNodeType.RETURN);
    assertEquals(g2.getVertices().get(5).getNodeType(), ABLNodeType.FINALLY);
    assertEquals(g2.getEdges(g2.getVertices().get(4)), List.of(5));
  }

  @Test
  public void testFinally02() {
    var unit = getParseUnit("""
        function foo logical private ( ) forward.

        define variable vl as logical.
        vl = foo().

        function foo return logical private ():
           define variable hq as handle no-undo.
           create query hq.
           quit. // 'finally' statement is not executed
           finally:
              delete object hq.
           end finally.
        end function.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var p1 = unit.getRootScope().getRoutines().get(1);
    assertEquals(p1.getName(), "foo");
    var g1 = p1.getExecutionGraph();
    assertEquals(g1.getVertices().size(), 4);
System.out.println(g1.toMermaidString());
    var currStmt = p1.getExecutionGraph().getVertices().get(0).asIStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.FUNCTION);
    assertTrue(currStmt.asJPNode().isIStatementBlock());
    currStmt = ((IStatementBlock) currStmt).getFirstStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.CREATE);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.QUIT);
    // QUIT is followed by FINALLY in the AST chain (lexical order), but FINALLY is NOT in the
    // execution graph because QUIT terminates the program without executing the FINALLY block.
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.FINALLY);
  }

  @Test
  public void testReturnNoFinally01() {
    // Return without FINALLY
    var unit = getParseUnit("""
        function foo logical private ( ) forward.

        define variable vl as logical.
        vl = foo().

        function foo return logical private ():
           define variable hq as handle no-undo.
           create query hq.
           return ?.
        end function.
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var p1 = unit.getRootScope().getRoutines().get(1);
    assertEquals(p1.getName(), "foo");
    var g1 = p1.getExecutionGraph();
    assertEquals(g1.getVertices().size(), 4);
    var currStmt = p1.getExecutionGraph().getVertices().get(0).asIStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.FUNCTION);
    assertTrue(currStmt.asJPNode().isIStatementBlock());
    currStmt = ((IStatementBlock) currStmt).getFirstStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.DEFINE);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.CREATE);
    currStmt = currStmt.getNextStatement();
    assertEquals(currStmt.getNodeType(), ABLNodeType.RETURN);
    currStmt = currStmt.getNextStatement();
    assertNull(currStmt);
  }

  @Test
  public void testReturn() {
    // Return without FINALLY
    var unit = getParseUnit("""
        define variable i1 as integer no-undo.

        do i1 = 1 to 10:
          if i1 then
            return.
        end.
        message "Hello".
        """, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var p1 = unit.getRootScope().getRoutines().get(0);
    assertEquals(p1.getName(), "");
    var g1 = p1.getExecutionGraph();
    assertEquals(g1.getVertices().size(), 6);
    assertEquals(g1.getEdges().get(2), List.of(3, 5));
  }

  // ExecutionGraph tests
  private static JPNode getIndex(ExecutionGraph graph, ABLNodeType nodeType, int line) {
    for (var n : graph.getVertices()) {
      if ((n.getNodeType() == nodeType) && (n.getLine() == line))
        return n;
    }
    return null;
  }

  private static String getEdges(ExecutionGraph graph, ABLNodeType nodeType, int line) {
    var idx = getIndex(graph, nodeType, line);
    if (idx == null)
      return "";
    return graph.getEdges(idx).stream() //
      .map(it -> graph.getVertices().get(it).getNodeType() + ":" + graph.getVertices().get(it).getLine()) //
      .sorted() //
      .collect(Collectors.joining(" "));
  }

  private static String getReverseEdges(ExecutionGraph graph, ABLNodeType nodeType, int line) {
    var idx = getIndex(graph, nodeType, line);
    if (idx == null)
      return "";
    return graph.getReverseEdges(idx).stream() //
      .map(it -> graph.getVertices().get(it).getNodeType() + ":" + graph.getVertices().get(it).getLine()) //
      .sorted() //
      .collect(Collectors.joining(" "));
  }

}
