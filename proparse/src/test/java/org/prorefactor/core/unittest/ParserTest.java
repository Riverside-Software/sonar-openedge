/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertEquals;

import java.io.File;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
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

    for (JPNode stmt: unit.getTopNode().queryStateHead(NodeTypes.DEFINE)) {
      assertEquals(stmt.query(NodeTypes.ASC).size(), 0);
      assertEquals(stmt.query(NodeTypes.ASCENDING).size(), 1);
    }
  }

  @Test
  public void testAscending02() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending02.p"), session);
    unit.parse();

    for (JPNode stmt: unit.getTopNode().queryStateHead(NodeTypes.SELECT)) {
      assertEquals(stmt.query(NodeTypes.ASC).size(), 0);
      assertEquals(stmt.query(NodeTypes.ASCENDING).size(), 1);
    }
  }

  @Test
  public void testAscending03() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "ascending03.p"), session);
    unit.parse();

    for (JPNode stmt: unit.getTopNode().queryStateHead(NodeTypes.MESSAGE)) {
      assertEquals(stmt.query(NodeTypes.ASC).size(), 2);
      assertEquals(stmt.query(NodeTypes.ASCENDING).size(), 0);
    }
  }

}
