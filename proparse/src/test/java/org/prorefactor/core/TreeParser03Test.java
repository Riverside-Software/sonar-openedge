/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Date;
import java.util.List;

import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Parameter;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.Modifier;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.Variable.ReadWrite;
import org.prorefactor.treeparser.symbols.Variable.ReadWriteReference;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This class simply runs the tree parser through various code, and as long as the tree parser does not throw any
 * errors, then the tests pass.
 */
public class TreeParser03Test {
  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void test02() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test02.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void test03() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test03.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    boolean found1 = false;
    boolean found2 = false;
    for (JPNode node : unit.getTopNode().query(ABLNodeType.DEFINE)) {
      if ((node.getState2() == ABLNodeType.TEMPTABLE.getType())
          && "myTT2".equals(node.getNextNode().getNextNode().getText())) {
        assertEquals(node.query(ABLNodeType.USEINDEX).get(0).getNextNode().attrGet(IConstants.INVALID_USEINDEX),
            IConstants.TRUE);
        found1 = true;
      }
      if ((node.getState2() == ABLNodeType.TEMPTABLE.getType())
          && "myTT3".equals(node.getNextNode().getNextNode().getText())) {
        assertEquals(node.query(ABLNodeType.USEINDEX).get(0).getNextNode().attrGet(IConstants.INVALID_USEINDEX), 0);
        found2 = true;
      }
    }
    assertTrue(found1);
    assertTrue(found2);
  }

  @Test
  public void test04() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test04.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    Variable xx = unit.getRootScope().getVariable("xx");
    assertNotNull(xx);
    Variable yy = unit.getRootScope().getVariable("yy");
    assertNotNull(yy);
    Variable zz = unit.getRootScope().getVariable("zz");
    assertNotNull(zz);
  }

  @Test
  public void test05() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test05.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    List<Routine> lst = unit.getRootScope().lookupRoutines("f1");
    assertEquals(lst.size(), 1);
    Routine f1 = lst.get(0);
    assertEquals(f1.getSignature(), "f1(II)");
    assertEquals(f1.getParameters().size(), 1);
    Variable var1 = (Variable) f1.getParameters().get(0).getSymbol();
    assertEquals(var1.getName(), "zz");
    assertEquals(var1.getNumReads(), 1);
    assertEquals(var1.getNumWrites(), 0);
    assertEquals(var1.getReadWriteReferences().size(), 1);
    ReadWriteReference ref1 = var1.getReadWriteReferences().get(0);
    assertEquals(ref1.getType(), ReadWrite.READ);
    assertEquals(ref1.getNode().getStatement().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(ref1.getNode().getStatement().getLine(), 8);

    List<Routine> lst2 = unit.getRootScope().lookupRoutines("f2");
    assertEquals(lst2.size(), 1);
    Routine f2 = lst2.get(0);
    assertEquals(f2.getSignature(), "f2(II,II)");
    assertEquals(f2.getParameters().size(), 2);
    assertEquals(f2.getParameters().get(0).getSymbol().getName(), "a");
    assertEquals(f2.getParameters().get(0).getSymbol().getNumReads(), 0);
    assertEquals(f2.getParameters().get(0).getSymbol().getNumWrites(), 0);
    Variable var2 = (Variable) f2.getParameters().get(1).getSymbol();
    assertEquals(var2.getName(), "zz");
    assertEquals(var2.getNumReads(), 1);
    assertEquals(var2.getNumWrites(), 0);
    ReadWriteReference ref2 = var2.getReadWriteReferences().get(0);
    assertEquals(ref2.getType(), ReadWrite.READ);
    assertEquals(ref2.getNode().getStatement().getNodeType(), ABLNodeType.DISPLAY);
    assertEquals(ref2.getNode().getStatement().getLine(), 13);

    List<Routine> lst3 = unit.getRootScope().lookupRoutines("f3");
    assertEquals(lst3.size(), 1);
    Routine f3 = lst3.get(0);
    assertEquals(f3.getSignature(), "f3(II)");
    assertEquals(f3.getParameters().size(), 1);
    assertEquals(f3.getParameters().get(0).getSymbol().getName(), "a");
    assertEquals(f3.getParameters().get(0).getSymbol().getNumReads(), 1);
    assertEquals(f3.getParameters().get(0).getSymbol().getNumWrites(), 0);

    List<Routine> lst4 = unit.getRootScope().lookupRoutines("f4");
    assertEquals(lst4.size(), 1);
    Routine f4 = lst4.get(0);
    assertEquals(f4.getSignature(), "f4()");
    assertEquals(f4.getParameters().size(), 0);

    List<Routine> lst5 = unit.getRootScope().lookupRoutines("f5");
    assertEquals(lst5.size(), 1);
    Routine f5 = lst5.get(0);
    assertEquals(f5.getSignature(), "f5()");
    assertEquals(f5.getParameters().size(), 0);
  }

  @Test
  public void test06() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test06.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void test07() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test07.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    Variable prop = unit.getRootScope().getVariable("cNextSalesRepName");
    assertNotNull(prop);
    assertEquals(prop.getNumReads(), 1);
    assertEquals(prop.getNumWrites(), 0);
  }

  @Test
  public void test08() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test08.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    Variable xx = unit.getRootScope().getChildScopes().get(0).getVariable("xx");
    assertNotNull(xx);
    assertEquals(xx.getNumReads(), 1);
    assertEquals(xx.getNumWrites(), 0);
  }

  @Test
  public void test09() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test09.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    Variable xxx = unit.getRootScope().getVariable("xxx");
    assertNotNull(xxx);
  }

  @Test
  public void test10() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("define input parameter ipPrm no-undo like customer.custnum.".getBytes()), "<unnamed>", session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    Variable ipPrm = unit.getRootScope().getVariable("ipPrm");
    assertNotNull(ipPrm);
    assertEquals(ipPrm.getDataType(), DataType.INTEGER);
  }

  @Test
  public void test11() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test11.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    List<Routine> lst = unit.getRootScope().lookupRoutines("foo1");
    assertEquals(lst.size(), 1);
    Routine r1 = lst.get(0);
    assertEquals(r1.getSignature(), "foo1(OI)");
    assertEquals(r1.getParameters().size(), 1);
    Parameter p1 = r1.getParameters().get(0);
    Symbol s1 = p1.getSymbol();
    assertNotNull(s1);
    assertEquals(s1.getName(), "ipPrm");
    assertTrue(s1 instanceof Variable);
    Variable v1 = (Variable) s1;
    assertEquals(v1.getDataType(), DataType.INTEGER);
    assertEquals(v1.getModifiers().size(), 1);
    assertTrue(v1.containsModifier(Modifier.OUTPUT));
    assertNotNull(s1.getDefineNode());
    assertEquals(s1.getDefineNode().getNodeType(), ABLNodeType.ID);
    assertEquals(s1.getDefineNode().getText(), "ipPrm");
  }

  @Test
  public void test12() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test12.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    List<Routine> lst = unit.getRootScope().lookupRoutines("foo1");
    assertEquals(lst.size(), 1);
    Routine r1 = lst.get(0);
    assertEquals(r1.getReturnDatatypeNode(), DataType.CLASS);

    lst = unit.getRootScope().lookupRoutines("foo2");
    assertEquals(lst.size(), 1);
    Routine r2 = lst.get(0);
    assertEquals(r2.getReturnDatatypeNode(), DataType.CLASS);

    lst = unit.getRootScope().lookupRoutines("foo3");
    assertEquals(lst.size(), 1);
    Routine r3 = lst.get(0);
    assertEquals(r3.getReturnDatatypeNode(), DataType.INTEGER);

    lst = unit.getRootScope().lookupRoutines("foo4");
    assertEquals(lst.size(), 1);
    Routine r4 = lst.get(0);
    assertEquals(r4.getReturnDatatypeNode(), DataType.CHARACTER);
  }

  @Test
  public void test13() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test13.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    Variable xxx = unit.getRootScope().getVariable("xxx");
    assertNotNull(xxx);
    assertEquals(xxx.getNumReads(), 1);
    assertEquals(xxx.getNumWrites(), 0);
    Variable yyy = unit.getRootScope().getVariable("yyy");
    assertNotNull(yyy);
    assertEquals(yyy.getNumReads(), 0);
    assertEquals(yyy.getNumWrites(), 1);
  }

  @Test
  public void test14() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test14.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable xxx = unit.getRootScope().getVariable("xxx");
    assertNotNull(xxx);
    assertEquals(xxx.getNumReads(), 1);
    assertEquals(xxx.getNumWrites(), 3);
  }

  @Test
  public void test15() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test15.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = unit.getRootScope().getVariable("v1");
    Variable v2 = unit.getRootScope().getVariable("v2");
    assertNotNull(v1);
    assertNotNull(v2);
    List<Routine> lst = unit.getRootScope().lookupRoutines("dummy");
    assertEquals(lst.size(), 1);
    Routine dummy = lst.get(0);
    assertEquals(dummy.getParameters().size(), 1);
    assertEquals(dummy.getParameters().get(0).getSymbol().getName(), "p1");
  }

  @Test
  public void test16() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test16.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable hInstance = unit.getRootScope().getVariable("hInstance");
    assertNotNull(hInstance);

    List<Routine> lst = unit.getRootScope().lookupRoutines("dummy");
    assertEquals(lst.size(), 1);
    Routine dummy = lst.get(0);
    assertEquals(dummy.getParameters().size(), 1);
    assertEquals(dummy.getParameters().get(0).getSymbol().getName(), "picVariable");

    List<Routine> lst2 = unit.getRootScope().lookupRoutines("doit");
    assertEquals(lst2.size(), 1);
    Routine doIt = lst2.get(0);
    assertNotNull(doIt);
    assertEquals(doIt.getParameters().size(), 1);
    assertEquals(doIt.getParameters().get(0).getSymbol().getName(), "picVariable");

    // Should not be the same object
    assertNotEquals(dummy.getParameters().get(0), doIt.getParameters().get(0));
    assertNotEquals(dummy.getParameters().get(0).getSymbol(), doIt.getParameters().get(0).getSymbol());
  }

  @Test
  public void test17() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test17.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 4);

    Variable hMenuItem = unit.getRootScope().getVariable("hMenuItem");
    assertNotNull(hMenuItem);
    assertTrue(hMenuItem.isGraphicalComponent());
    Variable hQuery = unit.getRootScope().getVariable("hQuery");
    assertNotNull(hQuery);
    assertFalse(hQuery.isGraphicalComponent());
    Variable hbCust = unit.getRootScope().getVariable("hbCust");
    assertNotNull(hbCust);
    assertTrue(hbCust.isGraphicalComponent());
    Variable hSock = unit.getRootScope().getVariable("hSock");
    assertNotNull(hSock);
    assertFalse(hSock.isGraphicalComponent());
  }

  @Test
  public void test18() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test18.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 11);
    Variable var1 = unit.getRootScope().getVariable("prm1");
    assertEquals(var1.getDataType(), DataType.INTEGER);
    Variable var2 = unit.getRootScope().getVariable("prm2");
    assertEquals(var2.getDataType(), DataType.INTEGER);
    Variable var3 = unit.getRootScope().getVariable("prm3");
    assertEquals(var3.getDataType(), DataType.INTEGER);
    Variable var4 = unit.getRootScope().getVariable("prm4");
    assertEquals(var4.getDataType(), DataType.INTEGER);
    Variable var5 = unit.getRootScope().getVariable("prm5");
    assertEquals(var5.getDataType(), DataType.INTEGER);
    Variable var6 = unit.getRootScope().getVariable("prm6");
    assertEquals(var6.getDataType(), DataType.CHARACTER);
    Variable var7 = unit.getRootScope().getVariable("prm7");
    assertEquals(var7.getDataType(), DataType.LONGCHAR);
    Variable var8 = unit.getRootScope().getVariable("prm8");
    assertEquals(var8.getDataType(), DataType.HANDLE);
    Variable var9 = unit.getRootScope().getVariable("prm9");
    assertEquals(var9.getDataType(), DataType.CLASS);
    assertEquals(var9.getClassName(), "Progress.Lang.Object");
    Variable var10 = unit.getRootScope().getVariable("prm10");
    assertEquals(var10.getDataType(), DataType.CLASS);
    assertEquals(var10.getClassName(), "Progress.Lang.Object");
    Variable var11 = unit.getRootScope().getVariable("prm11");
    assertEquals(var11.getDataType(), DataType.CLASS);
    assertEquals(var9.getClassName(), "Progress.Lang.Object");
  }

  @Test
  public void test19() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test19.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable var1 = unit.getRootScope().getVariable("xxx1");
    assertEquals(var1.getNumReads(), 1);
    assertEquals(var1.getNumWrites(), 1);
  }

  @Test
  public void test20() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test20.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().query(ABLNodeType.DISPLAY).size(), 1);
  }

  @Test
  public void test21() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test21.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    JPNode node = unit.getTopNode().findDirectChild(ABLNodeType.DEFINE);
    assertEquals(ABLNodeType.VARIABLE.getType(), node.attrGet(IConstants.STATE2));
  }

  @Test
  public void test22() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test22.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable var1 = unit.getRootScope().getVariable("yyy");
    assertEquals(var1.getNumReads(), 1);
    assertEquals(var1.getNumWrites(), 1);
  }

  @Test
  public void test23() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test23.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable var1 = unit.getRootScope().getVariable("xxx");
    assertEquals(var1.getNumReads(), 0);
    assertEquals(var1.getNumWrites(), 1);
    Variable var2 = unit.getRootScope().getVariable("yyy");
    assertEquals(var2.getNumReads(), 1);
    assertEquals(var2.getNumWrites(), 2);
    
    assertEquals(unit.getRootScope().getChildScopes().size(), 2);
    Variable var3 = unit.getRootScope().getChildScopes().get(0).getVariable("xxx");
    assertNotNull(var3);
    assertEquals(var3.getNumReads(), 0);
    assertEquals(var3.getNumWrites(), 1);

    JPNode fooBlock = unit.getRootScope().getChildScopes().get(0).getRootBlock().getNode().findDirectChild(
        ABLNodeType.CODE_BLOCK);
    assertEquals(fooBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.WIDGET_REF);
    assertEquals(fooBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getSymbol(), var1);

    JPNode barBlock = unit.getRootScope().getChildScopes().get(1).getRootBlock().getNode().findDirectChild(
        ABLNodeType.CODE_BLOCK);
    assertEquals(barBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.WIDGET_REF);
    assertEquals(barBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getSymbol(), var2);
    assertEquals(barBlock.getDirectChildren().get(1).getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.WIDGET_REF);
    assertNull(barBlock.getDirectChildren().get(1).getFirstChild().getFirstChild().getSymbol());
  }

  @Test
  public void test24() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/treeparser03/test24.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    /* assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable var1 = unit.getRootScope().getVariable("xxx");
    assertEquals(var1.getNumReads(), 0);
    assertEquals(var1.getNumWrites(), 1);
    Variable var2 = unit.getRootScope().getVariable("yyy");
    assertEquals(var2.getNumReads(), 1);
    assertEquals(var2.getNumWrites(), 2);
    
    assertEquals(unit.getRootScope().getChildScopes().size(), 2);
    Variable var3 = unit.getRootScope().getChildScopes().get(0).getVariable("xxx");
    assertNotNull(var3);
    assertEquals(var3.getNumReads(), 0);
    assertEquals(var3.getNumWrites(), 1);*/
  }

  @Test
  public void testVarStatement01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR CHAR s1, s2, s3.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("s1".equals(var.getName()))
        v1 = var;
      if ("s2".equals(var.getName()))
        v2 = var;
      if ("s3".equals(var.getName()))
        v3 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);
    assertEquals(v1.getDataType(), DataType.CHARACTER);
    assertEquals(v2.getDataType(), DataType.CHARACTER);
    assertEquals(v3.getDataType(), DataType.CHARACTER);
    assertNotNull(v1.getDefineNode());
    assertNotNull(v2.getDefineNode());
    assertNotNull(v3.getDefineNode());
    assertEquals(v1.getDefineNode().getNodeType(), ABLNodeType.ID);
    assertEquals(v2.getDefineNode().getNodeType(), ABLNodeType.ID);
    assertEquals(v3.getDefineNode().getNodeType(), ABLNodeType.ID);
    assertEquals(v1.getDefineNode().getText(), "s1");
    assertEquals(v2.getDefineNode().getText(), "s2");
    assertEquals(v3.getDefineNode().getText(), "s3");
  }

  @Test
  public void testVarStatement02() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR INT s1, s2, s3 = 3.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("s1".equals(var.getName()))
        v1 = var;
      if ("s2".equals(var.getName()))
        v2 = var;
      if ("s3".equals(var.getName()))
        v3 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);
    assertEquals(v1.getDataType(), DataType.INTEGER);
    assertEquals(v2.getDataType(), DataType.INTEGER);
    assertEquals(v3.getDataType(), DataType.INTEGER);
    assertNull(v1.getInitialValue());
    assertNull(v2.getInitialValue());
    assertNotNull(v3.getInitialValue());
    assertEquals(v3.getInitialValue(), Double.valueOf(3));
  }

  @Test
  public void testVarStatement03() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR CLASS mypackage.subdir.myclass myobj1, myobj2, myobj3.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("myobj1".equals(var.getName()))
        v1 = var;
      if ("myobj2".equals(var.getName()))
        v2 = var;
      if ("myobj3".equals(var.getName()))
        v3 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);
    assertEquals(v1.getDataType(), DataType.CLASS);
    assertEquals(v2.getDataType(), DataType.CLASS);
    assertEquals(v3.getDataType(), DataType.CLASS);
    assertEquals(v1.getClassName(), "mypackage.subdir.myclass");
    assertEquals(v2.getClassName(), "mypackage.subdir.myclass");
    assertEquals(v3.getClassName(), "mypackage.subdir.myclass");
  }

  @Test
  public void testVarStatement04() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR mypackage.subdir.myclass myobj1.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable v1 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("myobj1".equals(var.getName()))
        v1 = var;
    }
    assertNotNull(v1);
    assertEquals(v1.getDataType(), DataType.CLASS);
    assertEquals(v1.getClassName(), "mypackage.subdir.myclass");
  }

  @Test
  public void testVarStatement05() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR DATE d1, d2 = 1/1/2020, d3 = TODAY.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("d1".equals(var.getName()))
        v1 = var;
      if ("d2".equals(var.getName()))
        v2 = var;
      if ("d3".equals(var.getName()))
        v3 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);
    assertEquals(v1.getDataType(), DataType.DATE);
    assertEquals(v2.getDataType(), DataType.DATE);
    assertEquals(v3.getDataType(), DataType.DATE);
    assertNull(v1.getInitialValue());
    assertNotNull(v2.getInitialValue());
    assertNotNull(v3.getInitialValue());
    assertTrue(v2.getInitialValue() instanceof Date);
    assertEquals(v3.getInitialValue(), Variable.CONSTANT_TODAY);
  }

  @Test
  public void testVarStatement06() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR PROTECTED DATE d1, d2 = 1/1/2020.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("d1".equals(var.getName()))
        v1 = var;
      if ("d2".equals(var.getName()))
        v2 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertEquals(v1.getDataType(), DataType.DATE);
    assertEquals(v2.getDataType(), DataType.DATE);
    assertTrue(v1.containsModifier(Modifier.PROTECTED));
    assertTrue(v2.containsModifier(Modifier.PROTECTED));
  }

  @Test
  public void testVarStatement07() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR INT[3] x = [1, 2], y, z = [100, 200, 300].".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("x".equals(var.getName()))
        v1 = var;
      if ("y".equals(var.getName()))
        v2 = var;
      if ("z".equals(var.getName()))
        v3 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);
    assertEquals(v1.getDataType(), DataType.INTEGER);
    assertEquals(v2.getDataType(), DataType.INTEGER);
    assertEquals(v3.getDataType(), DataType.INTEGER);
    assertEquals(v1.getExtent(), 3);
    assertEquals(v2.getExtent(), 3);
    assertEquals(v3.getExtent(), 3);
    assertNotNull(v1.getInitialValue());
    assertNull(v2.getInitialValue());
    assertNotNull(v3.getInitialValue());
    assertEquals(v1.getInitialValue(), Variable.CONSTANT_ARRAY);
    assertEquals(v3.getInitialValue(), Variable.CONSTANT_ARRAY);
  }

  @Test
  public void testVarStatement08() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR INT[] x, y.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("x".equals(var.getName()))
        v1 = var;
      if ("y".equals(var.getName()))
        v2 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertEquals(v1.getDataType(), DataType.INTEGER);
    assertEquals(v2.getDataType(), DataType.INTEGER);
    assertEquals(v1.getExtent(), -32767);
    assertEquals(v2.getExtent(), -32767);
  }

  @Test
  public void testVarStatement09() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR INT[] x, y = [1,2,3].".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("x".equals(var.getName()))
        v1 = var;
      if ("y".equals(var.getName()))
        v2 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertEquals(v1.getDataType(), DataType.INTEGER);
    assertEquals(v2.getDataType(), DataType.INTEGER);
    assertEquals(v1.getExtent(), -32767);
    assertEquals(v2.getExtent(), -32767);
  }

  @Test
  public void testVarStatement10() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR INT[] x = [1,2], y = [1,2,3].".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("x".equals(var.getName()))
        v1 = var;
      if ("y".equals(var.getName()))
        v2 = var;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertEquals(v1.getDataType(), DataType.INTEGER);
    assertEquals(v2.getDataType(), DataType.INTEGER);
    assertEquals(v1.getInitialValue(), Variable.CONSTANT_ARRAY);
    assertEquals(v2.getInitialValue(), Variable.CONSTANT_ARRAY);
    assertEquals(v1.getExtent(), -32767);
    assertEquals(v2.getExtent(), -32767);
  }

  @Test
  public void testVarStatement11() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR CLASS foo[2] classArray.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
    
    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable v1 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("classArray".equals(var.getName()))
        v1 = var;
    }
    assertNotNull(v1);
    assertEquals(v1.getDataType(), DataType.CLASS);
    assertEquals(v1.getClassName(), "foo");
    assertEquals(v1.getExtent(), 2);
  }

  @Test
  public void testVarStatement12() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR \"System.Collections.Generic.List<char>\" cList.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable v1 = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("cList".equals(var.getName()))
        v1 = var;
    }
    assertNotNull(v1);
    assertEquals(v1.getDataType(), DataType.CLASS);
    assertEquals(v1.getClassName(), "System.Collections.Generic.List<char>");
    assertEquals(v1.getExtent(), 0);
  }

  @Test
  public void testVarStatement13() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("VAR INT a, b, x = a + b, y = a - b, z = x - y.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 5);
    Variable varA = null;
    Variable varB = null;
    Variable varX = null;
    Variable varY = null;
    Variable varZ = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("a".equals(var.getName()))
        varA = var;
      else if ("b".equals(var.getName()))
        varB = var;
      else if ("x".equals(var.getName()))
        varX = var;
      else if ("y".equals(var.getName()))
        varY = var;
      else if ("z".equals(var.getName()))
        varZ = var;
    }
    assertNotNull(varA);
    assertEquals(varA.getDataType(), DataType.INTEGER);
    assertEquals(varA.getExtent(), 0);
    assertEquals(varA.getInitialValue(), null);
    assertEquals(varA.getNumReads(), 2);
    assertEquals(varA.getNumWrites(), 0);

    assertNotNull(varB);
    assertEquals(varB.getDataType(), DataType.INTEGER);
    assertEquals(varB.getExtent(), 0);
    assertEquals(varB.getInitialValue(), null);
    assertEquals(varB.getNumReads(), 2);
    assertEquals(varB.getNumWrites(), 0);

    assertNotNull(varX);
    assertEquals(varX.getDataType(), DataType.INTEGER);
    assertEquals(varX.getExtent(), 0);
    assertEquals(varX.getInitialValue(), Variable.CONSTANT_EXPRESSION);
    assertEquals(varX.getNumReads(), 1);
    assertEquals(varX.getNumWrites(), 1);

    assertNotNull(varY);
    assertEquals(varY.getDataType(), DataType.INTEGER);
    assertEquals(varY.getExtent(), 0);
    assertEquals(varY.getInitialValue(), Variable.CONSTANT_EXPRESSION);
    assertEquals(varY.getNumReads(), 1);
    assertEquals(varY.getNumWrites(), 1);

    assertNotNull(varZ);
    assertEquals(varZ.getDataType(), DataType.INTEGER);
    assertEquals(varZ.getExtent(), 0);
    assertEquals(varZ.getInitialValue(), Variable.CONSTANT_EXPRESSION);
    assertEquals(varZ.getNumWrites(), 1);
  }

  @Test
  public void testVarStatement14() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR INT a, b. VAR INT[] x = [ a + b, a - b ].".getBytes()),
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable varA = null;
    Variable varB = null;
    Variable varX = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("a".equals(var.getName()))
        varA = var;
      else if ("b".equals(var.getName()))
        varB = var;
      else if ("x".equals(var.getName()))
        varX = var;
    }
    assertNotNull(varA);
    assertEquals(varA.getDataType(), DataType.INTEGER);
    assertEquals(varA.getExtent(), 0);
    assertEquals(varA.getInitialValue(), null);
    assertEquals(varA.getNumReads(), 2);
    assertEquals(varA.getNumWrites(), 0);

    assertNotNull(varB);
    assertEquals(varB.getDataType(), DataType.INTEGER);
    assertEquals(varB.getExtent(), 0);
    assertEquals(varB.getInitialValue(), null);
    assertEquals(varB.getNumReads(), 2);
    assertEquals(varB.getNumWrites(), 0);

    assertNotNull(varX);
    assertEquals(varX.getDataType(), DataType.INTEGER);
    assertEquals(varX.getExtent(), -32767);
    assertEquals(varX.getInitialValue(), Variable.CONSTANT_ARRAY);
    assertEquals(varX.getNumReads(), 0);
    assertEquals(varX.getNumWrites(), 1);
  }

  @Test
  public void testVarStatement15() {
    ParseUnit unit = new ParseUnit(
        new ByteArrayInputStream("USING Progress.Lang.Object. VAR Object x = NEW Object().".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable varX = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("x".equals(var.getName()))
        varX = var;

      assertNotNull(varX);
      assertEquals(varX.getDataType(), DataType.CLASS);
      assertEquals(varX.getClassName(), "Progress.Lang.Object");
      assertEquals(varX.getExtent(), 0);
      assertEquals(varX.getInitialValue(), Variable.CONSTANT_EXPRESSION);
      assertEquals(varX.getNumReads(), 0);
      assertEquals(varX.getNumWrites(), 1);
    }
  }

  @Test
  public void testVarStatement16() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR DATETIME dtm = DATETIME(TODAY,MTIME).".getBytes()),
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable varX = null;
    for (Variable var : unit.getRootScope().getVariables()) {
      if ("dtm".equals(var.getName()))
        varX = var;

      assertNotNull(varX);
      assertEquals(varX.getDataType(), DataType.DATETIME);
      assertEquals(varX.getExtent(), 0);
      assertEquals(varX.getInitialValue(), Variable.CONSTANT_EXPRESSION);
      assertEquals(varX.getNumReads(), 0);
      assertEquals(varX.getNumWrites(), 1);
    }
  }

  @Test
  public void testShorthandOperator01() {
    ParseUnit unit = new ParseUnit(new ByteArrayInputStream("VAR INT i1. ASSIGN i1 += 1.".getBytes()), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable i1 =  unit.getRootScope().getVariable("i1");
    assertNotNull(i1);
    assertEquals(i1.getNumReads(), 1);
    assertEquals(i1.getNumWrites(), 1);
  }
}
