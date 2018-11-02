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

import java.io.File;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class JPNodeTest {
  private final static String SRC_DIR = "src/test/resources/data/parser";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testStatements() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "query01.p"), session);
    unit.parse();

    List<JPNode> doStmts = unit.getTopNode().queryStateHead(ABLNodeType.DO);
    List<JPNode> msgStmts = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE);
    assertEquals(doStmts.size(), 2);
    assertEquals(msgStmts.size(), 3);

    assertEquals(doStmts.get(0).query(ABLNodeType.VIEWAS).size(), 3);
    assertEquals(doStmts.get(0).queryCurrentStatement(ABLNodeType.VIEWAS).size(), 0);
    assertEquals(doStmts.get(1).query(ABLNodeType.VIEWAS).size(), 1);
    assertEquals(doStmts.get(1).queryCurrentStatement(ABLNodeType.VIEWAS).size(), 0);

    assertEquals(msgStmts.get(0).query(ABLNodeType.VIEWAS).size(), 1);
    assertEquals(msgStmts.get(1).query(ABLNodeType.VIEWAS).size(), 1);
    assertEquals(msgStmts.get(2).query(ABLNodeType.VIEWAS).size(), 1);
  }

}
