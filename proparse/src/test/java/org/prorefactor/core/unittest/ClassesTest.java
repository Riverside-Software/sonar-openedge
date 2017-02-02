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
    boolean zz = false;
    boolean zz2 = false;
    boolean oth = false;
    String othName = "";
    for (Variable v : unit.getRootScope().getVariables()) {
      if ("zz".equalsIgnoreCase(v.getName())) {
        zz = true;
      } else if ("zz2".equalsIgnoreCase(v.getName())) {
        zz2 = true;
      } else {
        oth = true;
        othName = v.getName();
      }
    }
    assertTrue(zz, "Property zz not in root scope");
    assertTrue(zz2, "Property zz2 not in root scope");
    assertFalse(oth, "Something else found in root scope : '" + othName + "' ; ");

    for (SymbolScope sc : unit.getRootScope().getChildScopesDeep()) {
      if (sc.getRootBlock().getNode().getType() == NodeTypes.METHOD) continue;
      if (sc.getRootBlock().getNode().getType() == NodeTypes.CATCH) continue;
      boolean arg = false, i = false, oth2 = false;
      String oth2Name = "";
      for (Variable v : sc.getVariables()) {
        if ("arg".equalsIgnoreCase(v.getName())) {
          arg = true;
        } else if ("i".equalsIgnoreCase(v.getName())) {
          i = true;
        } else {
          oth2= true;
          oth2Name = v.getName();
        }
      }
      assertTrue(arg, "Property var not in GET/SET scope");
      assertTrue(i, "Property i not in GET/SET scope");
      assertFalse(oth2, "Something else found in GET/SET scope : '" + oth2Name + "' ; ");
    }
  }

}
