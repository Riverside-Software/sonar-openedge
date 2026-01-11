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
package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.Pair;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class StatementListenerTest extends AbstractProparseTest {
  private static final String SRC_DIR = "src/test/resources/stmt_visitor";

  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test01() {
    var unit = getParseUnit(new File(SRC_DIR + "/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var visitor = new StatementCount();
    visitor.walkStatementBlock(unit.getTopNode());
    assertEquals(visitor.stmtCount, 7);
    assertEquals(visitor.ifStmtCount, 3);
  }

  @Test
  public void test01bis() {
    var unit = getParseUnit(new File(SRC_DIR + "/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var visitor = new Tracker();
    visitor.walkStatementBlock(unit.getTopNode());
    assertEquals(visitor.path.size(), 38);
    assertEquals(visitor.path.get(0).getO1(), EventType.ENTER_BLOCK);
    assertTrue(visitor.path.get(0).getO2() instanceof ProgramRootNode);
    assertEquals(visitor.path.get(visitor.path.size() - 1).getO1(), EventType.EXIT_BLOCK);
    assertTrue(visitor.path.get(visitor.path.size() - 1).getO2() instanceof ProgramRootNode);

    assertEquals(visitor.path.stream().filter(it -> it.getO1() == EventType.ENTER_THEN).count(), 3);
    assertEquals(visitor.path.stream().filter(it -> it.getO1() == EventType.EXIT_THEN).count(), 3);
    assertEquals(visitor.path.stream().filter(it -> it.getO1() == EventType.ENTER_ELSE).count(), 2);
    assertEquals(visitor.path.stream().filter(it -> it.getO1() == EventType.EXIT_ELSE).count(), 2);
    assertEquals(visitor.path.stream().filter(it -> it.getO1() == EventType.ENTER_BLOCK).count(), 4);
    assertEquals(visitor.path.stream().filter(it -> it.getO1() == EventType.EXIT_BLOCK).count(), 4);
    assertEquals(
        visitor.path.stream().filter(
            it -> it.getO1() == EventType.ENTER_BLOCK && it.getO2().getNodeType() == ABLNodeType.PROGRAM_ROOT).count(),
        1);
    assertEquals(
        visitor.path.stream().filter(
            it -> it.getO1() == EventType.EXIT_BLOCK && it.getO2().getNodeType() == ABLNodeType.PROGRAM_ROOT).count(),
        1);
    assertEquals(visitor.path.stream().filter(
        it -> it.getO1() == EventType.ENTER_BLOCK && it.getO2().getNodeType() == ABLNodeType.DO).count(), 3);
    assertEquals(visitor.path.stream().filter(
        it -> it.getO1() == EventType.EXIT_BLOCK && it.getO2().getNodeType() == ABLNodeType.DO).count(), 3);
  }

  @Test
  public void test02() {
    var unit = getParseUnit(new File(SRC_DIR + "/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var visitor = new LinesOfCodeVisitor();
    visitor.walkStatementBlock(unit.getTopNode());
    var counts = visitor.getCounts();
    assertTrue(counts.containsKey(0));
    var count0 = counts.get(0);
    assertEquals(count0.size(), 12);
    assertFalse(count0.contains(9));
    assertFalse(count0.contains(12));
  }

  @Test
  public void test03() {
    var unit = getParseUnit(new File(SRC_DIR + "/test02.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var visitor = new TestStatementVisitor();
    visitor.walkStatementBlock(unit.getTopNode());
    assertEquals(visitor.stmtCount, 5);
  }

  private static class StatementCount extends StatementListener {
    int stmtCount;
    int ifStmtCount;

    @Override
    public void enterStatement(IStatement stmt) {
      if (stmt instanceof IfStatementNode)
        ifStmtCount++;
      else
        stmtCount++;
    }

    @Override
    public void exitStatement(IStatement stmt) {
      // Nothing
    }
  }

  private static class TestStatementVisitor extends StatementListener {
    int stmtCount;

    @Override
    public void enterStatement(IStatement stmt) {
      stmtCount++;
    }

    @Override
    public void exitStatement(IStatement stmt) {
      // Nothing
    }

    @Override
    public boolean enterStatementBlock(IStatementBlock block) {
      if (block.asJPNode().isIStatement()) {
        return !block.asJPNode().asIStatement().getAnnotations().contains("@InitializeComponents");
      } else
        return true;
    }
  }

  // Keeps list of enter / exit
  private static class Tracker extends StatementListener {
    private final List<Pair<EventType, JPNode>> path = new ArrayList<>();

    @Override
    public void enterStatement(IStatement stmt) {
      path.add(Pair.of(EventType.ENTER_STMT, stmt.asJPNode()));
    }

    @Override
    public void exitStatement(IStatement stmt) {
      path.add(Pair.of(EventType.EXIT_STMT, stmt.asJPNode()));
    }

    @Override
    public void enterThen(IStatement stmt) {
      path.add(Pair.of(EventType.ENTER_THEN, stmt.asJPNode()));
    }

    @Override
    public void exitThen(IStatement stmt) {
      path.add(Pair.of(EventType.EXIT_THEN, stmt.asJPNode()));
    }

    @Override
    public void enterElse(IStatement stmt) {
      path.add(Pair.of(EventType.ENTER_ELSE, stmt.asJPNode()));
    }

    @Override
    public void exitElse(IStatement stmt) {
      path.add(Pair.of(EventType.EXIT_ELSE, stmt.asJPNode()));
    }

    @Override
    public boolean enterStatementBlock(IStatementBlock block) {
      path.add(Pair.of(EventType.ENTER_BLOCK, block.asJPNode()));
      return true;
    }

    @Override
    public void exitStatementBlock(IStatementBlock block) {
      path.add(Pair.of(EventType.EXIT_BLOCK, block.asJPNode()));
    }
  }

  private static enum EventType {
    ENTER_STMT,
    EXIT_STMT,
    ENTER_THEN,
    EXIT_THEN,
    ENTER_ELSE,
    EXIT_ELSE,
    ENTER_BLOCK,
    EXIT_BLOCK;
  }
}
