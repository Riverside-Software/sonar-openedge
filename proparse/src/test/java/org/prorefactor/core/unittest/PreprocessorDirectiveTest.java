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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PreprocessorDirectiveTest {
  private final static String SRC_DIR = "src/test/resources/data/preprocessor";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor05.p"), session);
    unit.parse();
    Assert.assertEquals(unit.getTopNode().query(ABLNodeType.PROPARSEDIRECTIVE).size(), 0);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.MESSAGE).get(0);
    JPNode node2 = unit.getTopNode().query(ABLNodeType.MESSAGE).get(1);

    ProToken h1 = node1.getHiddenBefore();
    int numDirectives = 0;
    while (h1 != null) {
      if (h1.getType() == ProParserTokenTypes.PROPARSEDIRECTIVE) {
        numDirectives += 1;
      }
      h1 = (ProToken) h1.getHiddenBefore();
    }
    assertEquals(numDirectives, 1);
    assertTrue(node1.hasProparseDirective("xyz"));
    assertFalse(node1.hasProparseDirective("abc"));

    numDirectives = 0;
    ProToken h2 = node2.getHiddenBefore();
    while (h2 != null) {
      if (h2.getType() == ProParserTokenTypes.PROPARSEDIRECTIVE) {
        numDirectives += 1;
      }
      h2 = (ProToken) h2.getHiddenBefore();
    }
    assertEquals(numDirectives, 2);
    assertTrue(node2.hasProparseDirective("abc"));
    assertTrue(node2.hasProparseDirective("def"));
    assertTrue(node2.hasProparseDirective("hij"));
    assertFalse(node2.hasProparseDirective("klm"));
  }

  @Test(enabled = false)
  public void test02() {
    // See issue #341 - Won't fix
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor07.p"), session);
    unit.parse();
  }

}
