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

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PreprocessorParserTest {
  private final static String SRC_DIR = "src/test/resources/data/preprocessor";

  private RefactorSession session;
  private ParseUnit unit;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    try {
      unit = new ParseUnit(new File(SRC_DIR, "preprocessor01.p"), session);
      unit.parse();
    } catch (RuntimeException caught) {
      // Just so that tests will throw NPE and fail (and not just be skipped)
      unit = null;
    }
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
  public void testTrue() {
    testVariable(unit.getTopNode(), "var1");
  }

  @Test
  public void testFalse()  {
    testVariable(unit.getTopNode(), "var2");
  }

  @Test
  public void testGT()  {
    testVariable(unit.getTopNode(), "var3");
  }

  @Test
  public void testLT()  {
    testVariable(unit.getTopNode(), "var4");
  }

  @Test
  public void testAnd()  {
    testNoVariable(unit.getTopNode(), "var5");
  }

  @Test
  public void testOr1()  {
    testVariable(unit.getTopNode(), "var6");
    testVariable(unit.getTopNode(), "var46");
  }

  @Test
  public void testExpr1()  {
    testVariable(unit.getTopNode(), "var7");
  }

  @Test
  public void testDefined()  {
    testVariable(unit.getTopNode(), "var8");
  }

  @Test
  public void testDefined2()  {
    testVariable(unit.getTopNode(), "var9");
  }

  @Test
  public void testDefined3()  {
    testNoVariable(unit.getTopNode(), "var10");
  }

  @Test
  public void testExpression1()  {
    testVariable(unit.getTopNode(), "var11");
  }

  @Test
  public void testExpression2()  {
    testVariable(unit.getTopNode(), "var12");
  }

  @Test
  public void testGreaterEquals1()  {
    testVariable(unit.getTopNode(), "var13");
  }

  @Test
  public void testGreaterEquals2()  {
    testNoVariable(unit.getTopNode(), "var14");
  }

  @Test
  public void testGreaterEquals3()  {
    testVariable(unit.getTopNode(), "var44");
  }

  @Test
  public void testGreaterEquals4()  {
    testNoVariable(unit.getTopNode(), "var45");
  }

  @Test
  public void testLesserEquals1()  {
    testVariable(unit.getTopNode(), "var15");
  }

  @Test
  public void testLesserEquals2()  {
    testNoVariable(unit.getTopNode(), "var16");
  }

  @Test
  public void testLesserEquals3()  {
    testVariable(unit.getTopNode(), "var42");
  }

  @Test
  public void testLesserEquals4()  {
    testNoVariable(unit.getTopNode(), "var43");
  }

  @Test
  public void testSubstring()  {
    testVariable(unit.getTopNode(), "var17");
  }

  @Test
  public void testSubstring2()  {
    testNoVariable(unit.getTopNode(), "var18");
  }

  @Test
  public void testExpression3()  {
    testVariable(unit.getTopNode(), "var19");
  }

  @Test(enabled = false)
  public void testAbsolute1()  {
    testVariable(unit.getTopNode(), "var20");
  }

  @Test(enabled = false)
  public void testAbsolute2()  {
    testVariable(unit.getTopNode(), "var21");
  }

  @Test
  public void testDecimal1()  {
    testVariable(unit.getTopNode(), "var22");
  }

  @Test
  public void testEntry1()  {
    testVariable(unit.getTopNode(), "var23");
  }

  @Test
  public void testEntry2()  {
    testVariable(unit.getTopNode(), "var24");
  }

  @Test
  public void testEntry3()  {
    testNoVariable(unit.getTopNode(), "var25");
  }

  @Test
  public void testEntry4()  {
    testVariable(unit.getTopNode(), "var66");
    testNoVariable(unit.getTopNode(), "var67");
  }

  @Test
  public void testEntry5()  {
    testVariable(unit.getTopNode(), "var68");
    testNoVariable(unit.getTopNode(), "var69");
  }

  @Test
  public void testIndex1()  {
    testVariable(unit.getTopNode(), "var26");
  }

  @Test
  public void testInteger1()  {
    testVariable(unit.getTopNode(), "var27");
  }

  @Test
  public void testInt641()  {
    testVariable(unit.getTopNode(), "var28");
  }

  // TODO KEYWORD and KEYWORD-ALL

  @Test
  public void testLeftTrim1()  {
    testVariable(unit.getTopNode(), "var29");
  }

  @Test
  public void testLength1()  {
    testVariable(unit.getTopNode(), "var30");
  }

  @Test
  public void testLookup1()  {
    testVariable(unit.getTopNode(), "var31");
  }

  @Test
  public void testMaximum1()  {
    testVariable(unit.getTopNode(), "var32");
  }

  @Test
  public void testMinimum1()  {
    testVariable(unit.getTopNode(), "var33");
  }

  @Test
  public void testNumEntries1()  {
    testVariable(unit.getTopNode(), "var34");
  }

  @Test
  public void testRIndex1()  {
    testVariable(unit.getTopNode(), "var35");
  }

  @Test
  public void testReplace1()  {
    testVariable(unit.getTopNode(), "var36");
  }

  @Test
  public void testRightTrim1()  {
    testVariable(unit.getTopNode(), "var37");
  }

  @Test
  public void testSubstring1()  {
    testVariable(unit.getTopNode(), "var38");
  }

  @Test
  public void testTrim1()  {
    testVariable(unit.getTopNode(), "var39");
  }

  @Test
  public void testSubstring3()  {
    testVariable(unit.getTopNode(), "var40");
  }

  @Test
  public void testSubstring4()  {
    testVariable(unit.getTopNode(), "var41");
  }

  @Test
  public void testMatches()  {
    testVariable(unit.getTopNode(), "var47");
    testNoVariable(unit.getTopNode(), "var48");
  }

  @Test
  public void testBegins()  {
    testVariable(unit.getTopNode(), "var49");
    testNoVariable(unit.getTopNode(), "var50");
  }

  @Test
  public void testNotEquals()  {
    testVariable(unit.getTopNode(), "var51");
    testNoVariable(unit.getTopNode(), "var52");
  }

  @Test
  public void testNot()  {
    testVariable(unit.getTopNode(), "var53");
  }

  @Test
  public void testUnaryMinus()  {
    testVariable(unit.getTopNode(), "var54");
    testVariable(unit.getTopNode(), "var55");
    testNoVariable(unit.getTopNode(), "var56");
  }

  @Test
  public void testUnknown()  {
    testNoVariable(unit.getTopNode(), "var57");
    testVariable(unit.getTopNode(), "var58");
  }

  @Test
  public void testDbType()  {
    testVariable(unit.getTopNode(), "var59");
    testNoVariable(unit.getTopNode(), "var60");
  }

  @Test
  public void testAndOrNotImplemented()  {
    testNoVariable(unit.getTopNode(), "var61");
    testNoVariable(unit.getTopNode(), "var62");
  }

  @Test
  public void testIfElseIf()  {
    testNoVariable(unit.getTopNode(), "var63");
    testVariable(unit.getTopNode(), "var64");
  }

  @Test
  public void testBugIf()  {
    testVariable(unit.getTopNode(), "var65");
  }

  @Test
  public void testPriority() {
    testVariable(unit.getTopNode(), "var70");
    testNoVariable(unit.getTopNode(), "var71");
    testVariable(unit.getTopNode(), "var72");
    testNoVariable(unit.getTopNode(), "var73");
    testVariable(unit.getTopNode(), "var74");
  }

  @Test
  public void testSubstring5()  {
    testVariable(unit.getTopNode(), "var75");
  }

  // TODO OPSYS, PROVERSION functions
  @Test
  public void testPropath() {
    testVariable(unit.getTopNode(), "var76");
  }

  // TODO OPSYS, PROVERSION functions
  @Test
  public void testPropath2() {
    testNoVariable(unit.getTopNode(), "var77");
  }

  @Test
  public void testProcessArchitecture1() {
    testNoVariable(unit.getTopNode(), "var78");
  }

  @Test
  public void testProcessArchitecture2() {
    testVariable(unit.getTopNode(), "var79");
  }
}
