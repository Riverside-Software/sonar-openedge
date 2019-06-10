/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary Licenses when the conditions for such
 * availability set forth in the Eclipse Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.core;

import static org.testng.Assert.assertEquals;

import java.io.File;

import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TypeNameSemanticPredicateTest {
  private final static String SRC_DIR = "src/test/resources/predicate";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema();
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "test01.cls"), session);
    unit.parse();

    assertEquals(unit.getTopNode().query(ABLNodeType.CLASS).size(), 1);
    assertEquals(unit.getTopNode().query(ABLNodeType.CLASS).get(0).nextNode().getNodeType(), ABLNodeType.TYPE_NAME);
    assertEquals(unit.getTopNode().query(ABLNodeType.CLASS).get(0).nextNode().getText(), "pkgname.test01");
    assertEquals(unit.getTopNode().query(ABLNodeType.INHERITS).size(), 1);
    assertEquals(unit.getTopNode().query(ABLNodeType.INHERITS).get(0).nextNode().getNodeType(), ABLNodeType.TYPE_NAME);
    assertEquals(unit.getTopNode().query(ABLNodeType.INHERITS).get(0).nextNode().getText(), "pkgname.2018010.clzName");
  }

}
