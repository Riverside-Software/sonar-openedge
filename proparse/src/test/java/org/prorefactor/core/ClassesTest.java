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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Variable;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ClassesTest extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/data/rssw/pct/LoadLogger.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertTrue(unit.getTopNode().query(ABLNodeType.ANNOTATION).size() == 3);
    assertEquals(unit.getTopNode().query(ABLNodeType.ANNOTATION).get(0).getAnnotationName(),
        "Progress.Lang.Deprecated");
    JPNode method = unit.getTopNode().query(ABLNodeType.ANNOTATION).get(0).asIStatement().getNextStatement().asJPNode();
    assertNotNull(method);
    assertEquals(method.getNodeType(), ABLNodeType.METHOD);
    assertNotNull(method.asIStatement().getAnnotations());
    assertEquals(method.asIStatement().getAnnotations().size(), 1);
    assertEquals(method.asIStatement().getAnnotations().get(0), "@Progress.Lang.Deprecated");
    assertTrue(method.hasAnnotation("@Progress.Lang.Deprecated"));
    assertFalse(method.hasAnnotation("@Progress.Deprecated"));
    JPNode method2 = unit.getTopNode().query(ABLNodeType.ANNOTATION).get(1).asIStatement().getNextStatement().asJPNode();
    assertNotNull(method2);
    assertEquals(method2.getNodeType(), ABLNodeType.METHOD);
    assertNotNull(method2.asIStatement().getAnnotations());
    assertEquals(method2.asIStatement().getAnnotations().size(), 1);
    assertEquals(method2.asIStatement().getAnnotations().get(0), "@Progress.Lang.Deprecated(message = 'foobar')");
    assertTrue(method2.hasAnnotation("@Progress.Lang.Deprecated"));
    assertFalse(method2.hasAnnotation("@Progress.Lang.Deprecatedd"));
    JPNode method3 = unit.getTopNode().query(ABLNodeType.ANNOTATION).get(2).asIStatement().getNextStatement().asJPNode();
    assertNotNull(method3);
    assertEquals(method3.getNodeType(), ABLNodeType.METHOD);
    assertNotNull(method3.asIStatement().getAnnotations());
    assertEquals(method3.asIStatement().getAnnotations().size(), 1);
    assertEquals(method3.asIStatement().getAnnotations().get(0), "@Progress.Lang.Deprecated(since= '1.1', message = 'foobar' )");
    assertTrue(method3.hasAnnotation("@Progress.Lang.Deprecated"));

    JPNode inMethodStmt = method.findDirectChild(ABLNodeType.CODE_BLOCK).queryStateHead(ABLNodeType.RETURN).get(0);
    assertTrue(inMethodStmt.hasAnnotation("@Progress.Lang.Deprecated"));
    assertFalse(inMethodStmt.hasAnnotation("@Progress.Deprecated"));

    List<Routine> lst0 = unit.getRootScope().lookupRoutines("LoadLogger");
    assertNotNull(lst0);
    assertEquals(lst0.size(), 1);
    assertEquals(lst0.get(0).getSignature(), "LoadLogger(II)");
    assertEquals(lst0.get(0).getIDESignature(), "LoadLogger(↑INT)");
    assertEquals(lst0.get(0).getIDESignature(false), "LoadLogger(↑INT)");
    assertEquals(lst0.get(0).getIDESignature(true), "LoadLogger(↓INT)");

    List<Routine> lst1 = unit.getRootScope().lookupRoutines("addError");
    assertNotNull(lst1);
    assertEquals(lst1.size(), 2);
    assertEquals(lst1.get(0).getSignature(), "AddError(IZProgress.Lang.Error)");
    assertEquals(lst1.get(0).getIDESignature(), "AddError(↑Progress.Lang.Error) : VOID");
    assertEquals(lst1.get(1).getSignature(), "AddError(IC)");
    assertEquals(lst1.get(1).getIDESignature(), "AddError(↑CHAR) : VOID");
    Routine addError1 = unit.getRootScope().lookupRoutineBySignature("AddError(IZProgress.Lang.Error)");
    assertNotNull(addError1);
    assertEquals(addError1.getDefineNode().getLine(), 28);
    Routine addError2 = unit.getRootScope().lookupRoutineBySignature("AddError(IC)");
    assertNotNull(addError2);
    assertEquals(addError2.getDefineNode().getLine(), 38);

    List<Routine> lst2 = unit.getRootScope().lookupRoutines("addWarnings");
    assertNotNull(lst2);
    assertEquals(lst2.size(), 1);
    assertEquals(lst2.get(0).getSignature(), "AddWarnings(IC[])");
    assertEquals(lst2.get(0).getIDESignature(), "AddWarnings(↑CHAR[]) : VOID");
  }

  @Test
  public void test03() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/data/rssw/pct/ScopeTest.cls"), session);
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
      if (sc.getRootBlock().getNode().getNodeType() == ABLNodeType.METHOD) continue;
      if (sc.getRootBlock().getNode().getNodeType() == ABLNodeType.CATCH) continue;
      Variable arg = sc.getVariable("arg");
      Variable i = sc.getVariable("i");
      assertEquals(sc.getVariables().size(), 2);
      assertNotNull(arg, "Property var not in GET/SET scope");
      assertNotNull(i, "Property i not in GET/SET scope");
    }
  }

  @Test
  public void testThisObject() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/data/rssw/pct/TestThisObject.cls"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable prop1 = unit.getRootScope().getVariable("prop1");
    Variable prop2 = unit.getRootScope().getVariable("prop2");
    Variable var1 = unit.getRootScope().getVariable("var1");
    Variable var2 = unit.getRootScope().getVariable("var2");
    Variable var3 = unit.getRootScope().getVariable("var3");
    assertNotNull(prop1);
    assertNotNull(prop2);
    assertNotNull(var1);
    assertNotNull(var2);
    assertNotNull(var3);
    assertEquals(prop1.getNumReads(), 1);
    assertEquals(prop1.getNumWrites(), 1);
    assertEquals(prop2.getNumReads(), 1);
    assertEquals(prop2.getNumWrites(), 1);
    assertEquals(var1.getNumReads(), 0);
    assertEquals(var1.getNumWrites(), 1);
    assertEquals(var2.getNumReads(), 0);
    assertEquals(var2.getNumWrites(), 2);
    assertEquals(var3.getNumReads(), 1);
    assertEquals(var3.getNumWrites(), 1);
  }

}
