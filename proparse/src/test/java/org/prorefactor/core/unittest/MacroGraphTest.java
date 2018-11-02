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

import java.io.File;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.macrolevel.MacroEvent;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MacroGraphTest {
  private final static String SRC_DIR = "src/test/resources/data/preprocessor";

  private RefactorSession session;
  private ParseUnit unit;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    unit = new ParseUnit(new File(SRC_DIR, "preprocessor02.p"), session);
    unit.parse();
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
  public void testGlobalDefine() {
    testVariable(unit.getTopNode(), "var1");
    testVariable(unit.getTopNode(), "var2");
    testNoVariable(unit.getTopNode(), "var3");
  }

  @Test
  public void testScopedDefine() {
    testNoVariable(unit.getTopNode(), "var4");
  }

  @Test
  public void testMacroGraph() {
    Assert.assertEquals(unit.getMacroGraph().findExternalMacroReferences().size(), 1);
    Assert.assertEquals(unit.getMacroGraph().findExternalMacroReferences(new int[] {1,1}, new int[] {5,1}).size(), 1);
    Assert.assertEquals(unit.getMacroGraph().findExternalMacroReferences(new int[] {1,1}, new int[] {2,1}).size(), 0);
//    Assert.assertEquals(unit.getMacroGraph().findIncludeReferences(0), 1);
//    Assert.assertEquals(unit.getMacroGraph().findIncludeReferences(1), 0);
  }

  @Test
  public void testMacroGraphPosition() {
    List<MacroEvent> list = unit.getMacroGraph().findExternalMacroReferences();
    Assert.assertEquals(list.get(0).getPosition().getLine(), 3);
    Assert.assertEquals(list.get(0).getPosition().getColumn(), 5);
    Assert.assertEquals(list.get(0).getPosition().getFileNum(), 0);
  }
}
