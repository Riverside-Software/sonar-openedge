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

import java.io.File;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PreprocessorTest {
  private final static String SRC_DIR = "src/test/resources/data/preprocessor";

  private RefactorSession session;
  private ParseUnit unit;

  @BeforeTest
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    unit = new ParseUnit(new File(SRC_DIR, "preprocessor01.p"), session);
    unit.parse();
  }

  private void testVariable(JPNode topNode, String variable) {
    for (JPNode node : topNode.query(NodeTypes.ID)) {
      if (node.getText().equals(variable)) {
        return;
      }
    }
    Assert.fail("Variable " + variable + " not found");
  }

  private void testNoVariable(JPNode topNode, String variable) {
    for (JPNode node : topNode.query(NodeTypes.ID)) {
      if (node.getText().equals(variable)) {
        Assert.fail("Variable " + variable + " not found");
      }
    }
  }

  @Test
  public void testTrue() throws Exception {
    testVariable(unit.getTopNode(), "var1");
  }

  @Test
  public void testFalse() throws Exception {
    testVariable(unit.getTopNode(), "var2");
  }

  @Test
  public void testGT() throws Exception {
    testVariable(unit.getTopNode(), "var3");
  }

  @Test
  public void testLT() throws Exception {
    testVariable(unit.getTopNode(), "var4");
  }

  @Test
  public void testAnd() throws Exception {
    testNoVariable(unit.getTopNode(), "var5");
  }

  @Test
  public void testOr() throws Exception {
    testVariable(unit.getTopNode(), "var6");
  }

  @Test
  public void testExpr1() throws Exception {
    testVariable(unit.getTopNode(), "var7");
  }

  @Test
  public void testDefined() throws Exception {
    testVariable(unit.getTopNode(), "var8");
  }

  @Test
  public void testDefined2() throws Exception {
    testVariable(unit.getTopNode(), "var9");
  }

  @Test
  public void testDefined3() throws Exception {
    testNoVariable(unit.getTopNode(), "var10");
  }

  @Test
  public void testExpression1() throws Exception {
    testVariable(unit.getTopNode(), "var11");
  }

  @Test
  public void testExpression2() throws Exception {
    testVariable(unit.getTopNode(), "var12");
  }

  @Test
  public void testGreaterEquals1() throws Exception {
    testVariable(unit.getTopNode(), "var13");
  }

  @Test
  public void testGreaterEquals2() throws Exception {
    testNoVariable(unit.getTopNode(), "var14");
  }

  @Test
  public void testLesserEquals1() throws Exception {
    testVariable(unit.getTopNode(), "var15");
  }

  @Test
  public void testLesserEquals2() throws Exception {
    testNoVariable(unit.getTopNode(), "var16");
  }

  @Test
  public void testSubstring() throws Exception {
    testVariable(unit.getTopNode(), "var17");
  }

  @Test
  public void testSubstring2() throws Exception {
    testNoVariable(unit.getTopNode(), "var18");
  }

  @Test
  public void testExpression3() throws Exception {
    testVariable(unit.getTopNode(), "var19");
  }

  @Test(enabled = false)
  public void testAbsolute1() throws Exception {
    testVariable(unit.getTopNode(), "var20");
  }

  @Test(enabled = false)
  public void testAbsolute2() throws Exception {
    testVariable(unit.getTopNode(), "var21");
  }

  @Test
  public void testDecimal1() throws Exception {
    testVariable(unit.getTopNode(), "var22");
  }

  @Test
  public void testEntry1() throws Exception {
    testVariable(unit.getTopNode(), "var23");
  }

  @Test
  public void testEntry2() throws Exception {
    testVariable(unit.getTopNode(), "var24");
  }

  @Test
  public void testEntry3() throws Exception {
    testNoVariable(unit.getTopNode(), "var25");
  }

  @Test
  public void testIndex1() throws Exception {
    testVariable(unit.getTopNode(), "var26");
  }

  @Test
  public void testInteger1() throws Exception {
    testVariable(unit.getTopNode(), "var27");
  }

  @Test
  public void testInt641() throws Exception {
    testVariable(unit.getTopNode(), "var28");
  }

  // TODO KEYWORD and KEYWORD-ALL

  @Test
  public void testLeftTrim1() throws Exception {
    testVariable(unit.getTopNode(), "var29");
  }

  @Test
  public void testLength1() throws Exception {
    testVariable(unit.getTopNode(), "var30");
  }

  @Test
  public void testLookup1() throws Exception {
    testVariable(unit.getTopNode(), "var31");
  }

  @Test
  public void testMaximum1() throws Exception {
    testVariable(unit.getTopNode(), "var32");
  }

  @Test
  public void testMinimum1() throws Exception {
    testVariable(unit.getTopNode(), "var33");
  }

  @Test
  public void testNumEntries1() throws Exception {
    testVariable(unit.getTopNode(), "var34");
  }

  // TODO OPSYS, PROVERSION and PROPATH functions

  @Test
  public void testRIndex1() throws Exception {
    testVariable(unit.getTopNode(), "var35");
  }

  @Test
  public void testReplace1() throws Exception {
    testVariable(unit.getTopNode(), "var36");
  }

  @Test
  public void testRightTrim1() throws Exception {
    testVariable(unit.getTopNode(), "var37");
  }

  @Test
  public void testSubstring1() throws Exception {
    testVariable(unit.getTopNode(), "var38");
  }

  @Test
  public void testTrim1() throws Exception {
    testVariable(unit.getTopNode(), "var39");
  }

}
