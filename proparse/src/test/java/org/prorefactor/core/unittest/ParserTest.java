/*******************************************************************************
 * Copyright (c) 2017 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ParserTest {
  private final static String SRC_DIR = "src/test/resources/data/parser";

  private RefactorSession session;

  @BeforeTest
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testAscending01() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending01.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(NodeTypes.DEFINE);
    for (JPNode stmt : stmts) {
      assertEquals(stmt.query(NodeTypes.ASC).size(), 0);
      assertEquals(stmt.query(NodeTypes.ASCENDING).size(), 1);
    }
    assertTrue(stmts.get(0).query(NodeTypes.ASCENDING).get(0).isAbbreviated());
    assertTrue(stmts.get(1).query(NodeTypes.ASCENDING).get(0).isAbbreviated());
    assertFalse(stmts.get(2).query(NodeTypes.ASCENDING).get(0).isAbbreviated());
  }

  @Test
  public void testAscending02() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending02.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(NodeTypes.SELECT);
    for (JPNode stmt : stmts) {
      assertEquals(stmt.query(NodeTypes.ASC).size(), 0);
      assertEquals(stmt.query(NodeTypes.ASCENDING).size(), 1);
    }
    assertTrue(stmts.get(0).query(NodeTypes.ASCENDING).get(0).isAbbreviated());
    assertTrue(stmts.get(1).query(NodeTypes.ASCENDING).get(0).isAbbreviated());
    assertFalse(stmts.get(2).query(NodeTypes.ASCENDING).get(0).isAbbreviated());
  }

  @Test
  public void testAscending03() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending03.p"), session);
    unit.parse();

    for (JPNode stmt : unit.getTopNode().queryStateHead(NodeTypes.MESSAGE)) {
      assertEquals(stmt.query(NodeTypes.ASC).size(), 2);
      assertEquals(stmt.query(NodeTypes.ASCENDING).size(), 0);
      for (JPNode ascNode : stmt.query(NodeTypes.ASC)) {
        assertFalse(ascNode.isAbbreviated());
      }
    }
  }

}
