/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;

import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class simply runs the tree parser through various code, and as long as the tree parser does not throw any
 * errors, then the tests pass.
 */
public class TreeParser03Test {
  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void test02() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test02.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

}
