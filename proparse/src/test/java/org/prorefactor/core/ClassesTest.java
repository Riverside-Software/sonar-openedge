/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
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
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.proparse.antlr4.Proparse;
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
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertTrue(unit.getTopNode().query(ABLNodeType.ANNOTATION).size() == 1);
    assertEquals(unit.getTopNode().query(ABLNodeType.ANNOTATION).get(0).getAnnotationName(),
        "Progress.Lang.Deprecated");
    JPNode method = unit.getTopNode().query(ABLNodeType.ANNOTATION).get(0).getNextStatement();
    assertNotNull(method);
    assertEquals(method.getNodeType(), ABLNodeType.METHOD);
    assertNotNull(method.getAnnotations());
    assertEquals(method.getAnnotations().size(), 1);
    assertEquals(method.getAnnotations().get(0), "@Progress.Lang.Deprecated");
    assertTrue(method.hasAnnotation("@Progress.Lang.Deprecated"));
    assertFalse(method.hasAnnotation("@Progress.Deprecated"));
    JPNode inMethodStmt = method.getFirstDirectChild(ABLNodeType.CODE_BLOCK).queryStateHead(ABLNodeType.RETURN).get(0);
    assertTrue(inMethodStmt.hasAnnotation("@Progress.Lang.Deprecated"));
    assertFalse(inMethodStmt.hasAnnotation("@Progress.Deprecated"));
  }

  @Test
  public void test03() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/ScopeTest.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    // Only zz and zz2 properties should be there
    Variable zz = unit.getRootScope().getVariable("zz");
    Variable zz2 = unit.getRootScope().getVariable("zz2");
    assertEquals(unit.getRootScope().getVariables().size(), 2);
    assertNotNull(zz, "Property zz not in root scope");
    assertNotNull(zz2, "Property zz2 not in root scope");

    for (TreeParserSymbolScope sc : unit.getRootScope().getChildScopesDeep()) {
      if (sc.getRootBlock().getNode().getType() == Proparse.METHOD) continue;
      if (sc.getRootBlock().getNode().getType() == Proparse.CATCH) continue;
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
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable prop1 = unit.getRootScope().getVariable("prop1");
    Variable prop2 = unit.getRootScope().getVariable("prop2");
    Variable var1 = unit.getRootScope().getVariable("var1");
    assertNotNull(prop1);
    assertNotNull(prop2);
    assertNotNull(var1);
    assertEquals(prop1.getNumReads(), 1);
    assertEquals(prop1.getNumWrites(), 1);
    assertEquals(prop2.getNumReads(), 1);
    assertEquals(prop2.getNumWrites(), 1);
    assertEquals(var1.getNumReads(), 0);
    assertEquals(var1.getNumWrites(), 1);
  }

}
