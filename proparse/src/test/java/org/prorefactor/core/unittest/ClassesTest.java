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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.Variable;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ClassesTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/LoadLogger.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertTrue(unit.getTopNode().query(ABLNodeType.ANNOTATION).size() == 1);
    assertEquals("Progress.Lang.Deprecated", unit.getTopNode().query(ABLNodeType.ANNOTATION).get(0).getAnnotationName());
  }

  @Test
  public void test03() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/ScopeTest.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    // Only zz and zz2 properties should be there
    Variable zz = unit.getRootScope().getVariable("zz");
    Variable zz2 = unit.getRootScope().getVariable("zz2");
    assertEquals(unit.getRootScope().getVariables().size(), 2);
    assertNotNull(zz, "Property zz not in root scope");
    assertNotNull(zz2, "Property zz2 not in root scope");

    for (TreeParserSymbolScope sc : unit.getRootScope().getChildScopesDeep()) {
      if (sc.getRootBlock().getNode().getType() == ProParserTokenTypes.METHOD) continue;
      if (sc.getRootBlock().getNode().getType() == ProParserTokenTypes.CATCH) continue;
      Variable arg = sc.getVariable("arg");
      Variable i = sc.getVariable("i");
      assertEquals(sc.getVariables().size(), 2);
      assertNotNull(arg, "Property var not in GET/SET scope");
      assertNotNull(i, "Property i not in GET/SET scope");
    }
  }

  @Test
  public void testThisObject() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/TestThisObject.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable prop1 = unit.getRootScope().getVariable("prop1");
    Variable prop2 = unit.getRootScope().getVariable("prop2");
    assertNotNull(prop1);
    assertNotNull(prop2);
    assertEquals(prop1.getNumReads(), 1);
    assertEquals(prop1.getNumWrites(), 1);
    assertEquals(prop2.getNumReads(), 1);
    assertEquals(prop2.getNumWrites(), 1);
  }

}
