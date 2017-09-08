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

import java.io.File;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.unittest.util.UnitTestModule;
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
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor05.p"), session);
    unit.parse();
    Assert.assertEquals(unit.getTopNode().query(NodeTypes.PROPARSEDIRECTIVE).size(), 0);
    JPNode node1 = unit.getTopNode().query(NodeTypes.MESSAGE).get(0);
    JPNode node2 = unit.getTopNode().query(NodeTypes.MESSAGE).get(1);

    ProToken h1 = node1.getHiddenBefore();
    int numDirectives = 0;
    while (h1 != null) {
      if (h1.getType() == NodeTypes.PROPARSEDIRECTIVE) {
        numDirectives += 1;
      }
      h1 = (ProToken) h1.getHiddenBefore();
    }
    assertEquals(numDirectives, 1);

    numDirectives = 0;
    ProToken h2 = node2.getHiddenBefore();
    while (h2 != null) {
      if (h2.getType() == NodeTypes.PROPARSEDIRECTIVE) {
        numDirectives += 1;
      }
      h2 = (ProToken) h2.getHiddenBefore();
    }
    assertEquals(numDirectives, 2);
  }

  @Test
  public void test02() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor07.p"), session);
    unit.parse();
  }

}
