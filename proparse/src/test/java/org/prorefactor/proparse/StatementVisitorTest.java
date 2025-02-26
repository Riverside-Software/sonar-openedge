/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
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

import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class StatementVisitorTest extends AbstractProparseTest {
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

  private static class StatementCount extends StatementVisitor {
    int stmtCount;
    int ifStmtCount;

    @Override
    void visitStatement(IStatement stmt) {
      if (stmt instanceof IfStatementNode)
        ifStmtCount++;
      else
        stmtCount++;
    }
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

  private static class TestStatementVisitor extends StatementVisitor {
    int stmtCount;

    @Override
    void visitStatement(IStatement stmt) {
      stmtCount++;
    }

    @Override
    boolean preVisitStatementBlock(IStatementBlock block) {
      if (block.asJPNode().isIStatement()) {
        return !block.asJPNode().asIStatement().getAnnotations().contains("@InitializeComponents");
      } else
        return true;
    }
  }

}
