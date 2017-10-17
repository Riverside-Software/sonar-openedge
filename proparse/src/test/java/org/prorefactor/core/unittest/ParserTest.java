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

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

public class ParserTest {
  private final static String SRC_DIR = "src/test/resources/data/parser";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testAscending01() throws ANTLRException {
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

  @Test
  public void testAscending02() throws ANTLRException {
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
  public void testAscending03() throws ANTLRException {
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
  public void testLogical01() throws ANTLRException {
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
  public void testLogical02() throws ANTLRException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "logical02.p"), session);
    unit.parse();

    List<JPNode> stmts = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE);
    assertEquals(stmts.get(0).query(ABLNodeType.LOG).size(), 1);
    assertEquals(stmts.get(0).query(ABLNodeType.LOGICAL).size(), 0);
  }

  @Test
  public void testGetCodepage() throws ANTLRException {
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
}
