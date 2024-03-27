/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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

import java.io.IOException;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ParserRecoverTest extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    session.getSchema();
  }

  @Test
  public void test01() {
    ((ProparseSettings) session.getProparseSettings()).setAntlrRecover(true);
    ((ProparseSettings) session.getProparseSettings()).setAntlrTokenInsertion(true);
    ((ProparseSettings) session.getProparseSettings()).setAntlrTokenDeletion(true);
    // Everything should be fine here
    ParseUnit unit = getParseUnit("define variable xyz as character no-undo.", session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
  }

  @Test
  public void test02() {
    ((ProparseSettings) session.getProparseSettings()).setAntlrRecover(true);
    ((ProparseSettings) session.getProparseSettings()).setAntlrTokenInsertion(true);
    ((ProparseSettings) session.getProparseSettings()).setAntlrTokenDeletion(true);
    // Doesn't compile but recover is on, so should be silently discarded and token insertion is on
    ParseUnit unit = getParseUnit("define variable xyz character no-undo.", session);
    unit.treeParser01();
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 1);
    assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.PERIOD).size(), 0);
  }

  @Test(expectedExceptions = ParseCancellationException.class )
  public void test03() {
    ((ProparseSettings) session.getProparseSettings()).setAntlrRecover(false);
    ((ProparseSettings) session.getProparseSettings()).setAntlrTokenInsertion(false);
    ((ProparseSettings) session.getProparseSettings()).setAntlrTokenDeletion(false);
    // Doesn't compile and recover is off, so should throw ParseCancellationException
    ParseUnit unit = getParseUnit("define variable xyz character no-undo.", session);
    unit.treeParser01();
  }

}
