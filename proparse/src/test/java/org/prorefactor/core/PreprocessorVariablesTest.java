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
package org.prorefactor.core;

import static org.testng.Assert.assertFalse;

import java.io.File;
import java.io.IOException;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class PreprocessorVariablesTest extends AbstractProparseTest {
  private final static String SRC_DIR = "src/test/resources/data/preprocessor";

  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test03() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor03.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    testVariable(unit.getTopNode(), "var01");
  }

  private void testVariable(JPNode topNode, String variable) {
    for (JPNode node : topNode.query(ABLNodeType.ID)) {
      if (node.getText().equals(variable)) {
        return;
      }
    }
    Assert.fail("Variable " + variable + " not found");
  }

  private void testNoVariable(JPNode topNode, String variable) {
    for (JPNode node : topNode.query(ABLNodeType.ID)) {
      if (node.getText().equals(variable)) {
        Assert.fail("Variable " + variable + " not found");
      }
    }
  }

  @Test
  public void test04() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor04.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    testVariable(unit.getTopNode(), "var01");
    testNoVariable(unit.getTopNode(), "var02");
    testVariable(unit.getTopNode(), "var03");
    testVariable(unit.getTopNode(), "var04");
    testNoVariable(unit.getTopNode(), "var05");
    testVariable(unit.getTopNode(), "var06");
    testNoVariable(unit.getTopNode(), "var07");
    testVariable(unit.getTopNode(), "var08");
    testVariable(unit.getTopNode(), "var09");
    testVariable(unit.getTopNode(), "var10");
  }

  @Test
  public void test06() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor06.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    Assert.assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 1);
  }

  @Test
  public void test08() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor08.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    Assert.assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).size(), 0);
  }

  @Test(enabled = false)
  public void test24() {
    // Valid ABL code, but not recognized by Proparse (issue #911)
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor24.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());
    Assert.assertEquals(unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).size(), 2);
  }

}
