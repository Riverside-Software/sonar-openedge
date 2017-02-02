/*******************************************************************************
 * Copyright (c) 2003-2015 Gilles Querret
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.SymbolScope;
import org.prorefactor.treeparser.Variable;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ClassesTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/LoadLogger.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertTrue(unit.getTopNode().query(NodeTypes.ANNOTATION).size() == 1);
    assertEquals("Progress.Lang.Deprecated", unit.getTopNode().query(NodeTypes.ANNOTATION).get(0).getAnnotationName());
  }

  @Test
  public void test03() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/ScopeTest.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    // Only zz and zz2 properties should be there
    Variable zz = unit.getRootScope().getVariable("zz");
    Variable zz2 = unit.getRootScope().getVariable("zz2");
    assertNotNull(zz, "Property zz not in root scope");
    assertNotNull(zz2, "Property zz2 not in root scope");
    assertEquals(unit.getRootScope().getVariables().size(), 2);

    for (SymbolScope sc : unit.getRootScope().getChildScopesDeep()) {
      if (sc.getRootBlock().getNode().getType() == NodeTypes.METHOD) continue;
      if (sc.getRootBlock().getNode().getType() == NodeTypes.CATCH) continue;
      Variable arg = sc.getVariable("arg");
      Variable i = sc.getVariable("i");
      assertNotNull(arg, "Property var not in GET/SET scope");
      assertNotNull(i, "Property i not in GET/SET scope");
      assertEquals(sc.getVariables().size(), 2);
    }
  }

  @Test
  public void testThisObject() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/TestThisObject.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable prop1 = unit.getRootScope().getVariable("prop1");
    Variable prop2 = unit.getRootScope().getVariable("prop2");
    assertNotNull(prop1);
    assertNotNull(prop1);
    assertTrue(prop2.getNumReads() == 1);
    assertTrue(prop2.getNumWrites() == 1);
    assertTrue(prop1.getNumReads() == 1);
    assertTrue(prop1.getNumWrites() == 1);
  }

}
