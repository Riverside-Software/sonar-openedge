/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2026 Riverside Software
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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.Parameter;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.Event;
import org.prorefactor.treeparser.symbols.Modifier;
import org.prorefactor.treeparser.symbols.Query;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.Variable.ReadWrite;
import org.prorefactor.treeparser.symbols.Variable.ReadWriteReference;
import org.prorefactor.treeparser.symbols.widgets.IFieldLevelWidget;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.PrimitiveDataType;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.TypeInfo;

/**
 * This class simply runs the tree parser through various code, and as long as the tree parser does not throw any
 * errors, then the tests pass.
 */
public class TreeParser03Test extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());

    var test43 = new TypeInfo("test43", false, false, BuiltinClasses.PLO_CLASSNAME, "");
    var test43Foo1 = new MethodElement("foo1", false, DataType.VOID, new eu.rssw.pct.elements.fixed.Parameter[] {
        new eu.rssw.pct.elements.fixed.Parameter(0, "ipPrm", 0, ParameterMode.OUTPUT, DataType.INTEGER)});
    var test43Foo2 = new MethodElement("foo2", false, DataType.INTEGER, new eu.rssw.pct.elements.fixed.Parameter[] {});
    var test43Foo21 = new MethodElement("foo2", false, DataType.INTEGER, new eu.rssw.pct.elements.fixed.Parameter[] {
        new eu.rssw.pct.elements.fixed.Parameter(0, "xx", 0, ParameterMode.INPUT, DataType.INTEGER)});
    var test43Foo22 = new MethodElement("foo2", false, DataType.INTEGER,
        new eu.rssw.pct.elements.fixed.Parameter[] {
            new eu.rssw.pct.elements.fixed.Parameter(0, "xx", 0, ParameterMode.INPUT, DataType.INTEGER),
            new eu.rssw.pct.elements.fixed.Parameter(1, "yy", 0, ParameterMode.INPUT, DataType.CHARACTER)});
    test43.addMethod(test43Foo1);
    test43.addMethod(test43Foo2);
    test43.addMethod(test43Foo21);
    test43.addMethod(test43Foo22);
    session.injectTypeInfo(test43);
  }

  @Test
  public void test01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test01.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 0);
  }

  @Test
  public void test02() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test02.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 0);
  }

  @Test
  public void test03() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test03.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 0);

    boolean found1 = false;
    boolean found2 = false;
    for (JPNode node : unit.getTopNode().query(ABLNodeType.DEFINE)) {
      if ((node.asIStatement().getNodeType2() == ABLNodeType.TEMPTABLE)
          && "myTT2".equals(node.getNextNode().getNextNode().getText())) {
        assertTrue(node.query(ABLNodeType.USEINDEX).get(0).getNextNode().isInvalidUseIndex());
        found1 = true;
      }
      if ((node.asIStatement().getNodeType2() == ABLNodeType.TEMPTABLE)
          && "myTT3".equals(node.getNextNode().getNextNode().getText())) {
        assertFalse(node.query(ABLNodeType.USEINDEX).get(0).getNextNode().isInvalidUseIndex());
        found2 = true;
      }
    }
    assertTrue(found1);
    assertTrue(found2);
  }

  @Test
  public void test04() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test04.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 0);

    Variable xx = unit.getRootScope().getVariable("xx");
    assertNotNull(xx);
    Variable yy = unit.getRootScope().getVariable("yy");
    assertNotNull(yy);
    Variable zz = unit.getRootScope().getVariable("zz");
    assertNotNull(zz);
  }

  @Test
  public void test05() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test05.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 0);

    List<Routine> lst = unit.getRootScope().lookupRoutines("f1");
    assertEquals(lst.size(), 1);
    Routine f1 = lst.get(0);
    assertEquals(f1.getSignature(), "f1(II)");
    assertEquals(f1.getIDESignature(), "f1(↑INT) : INT");
    assertEquals(f1.getIDESignature(false), "f1(↑INT) : INT");
    assertEquals(f1.getIDESignature(true), "f1(↓INT) : INT");
    assertEquals(f1.getIDEInsertElement(true), "f1(${1:zz})$0");
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
    assertEquals(f2.getIDESignature(), "f2(↑INT, ↑INT) : INT");
    assertEquals(f2.getIDESignature(false), "f2(↑INT, ↑INT) : INT");
    assertEquals(f2.getIDESignature(true), "f2(↓INT, ↓INT) : INT");
    assertEquals(f2.getIDEInsertElement(true), "f2(${1:a}, ${2:zz})$0");
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
    assertEquals(f3.getIDESignature(), "f3(↑INT) : INT");
    assertEquals(f3.getIDEInsertElement(true), "f3(${1:a})$0");
    assertEquals(f3.getParameters().size(), 1);
    assertEquals(f3.getParameters().get(0).getSymbol().getName(), "a");
    assertEquals(f3.getParameters().get(0).getSymbol().getNumReads(), 1);
    assertEquals(f3.getParameters().get(0).getSymbol().getNumWrites(), 0);

    List<Routine> lst4 = unit.getRootScope().lookupRoutines("f4");
    assertEquals(lst4.size(), 1);
    Routine f4 = lst4.get(0);
    assertEquals(f4.getSignature(), "f4()");
    assertEquals(f4.getIDESignature(), "f4() : INT");
    assertEquals(f4.getIDEInsertElement(true), "f4()$0");
    assertEquals(f4.getParameters().size(), 0);

    List<Routine> lst5 = unit.getRootScope().lookupRoutines("f5");
    assertEquals(lst5.size(), 1);
    Routine f5 = lst5.get(0);
    assertEquals(f5.getSignature(), "f5()");
    assertEquals(f5.getIDESignature(), "f5() : INT");
    assertEquals(f5.getIDEInsertElement(true), "f5()$0");
    assertEquals(f5.getParameters().size(), 0);

    List<Routine> lst6 = unit.getRootScope().lookupRoutines("f6");
    assertEquals(lst6.size(), 1);
    Routine f6 = lst6.get(0);
    assertEquals(f6.getSignature(), "f6(MD)");
    assertEquals(f6.getIDESignature(), "f6(⇅DS) : INT");
    assertEquals(f6.getIDEInsertElement(true), "f6(INPUT-OUTPUT ${1:arg1})$0");
    assertEquals(f6.getIDEInsertElement(false), "f6(input-output ${1:arg1})$0");
    assertEquals(f6.getParameters().size(), 1);

    List<Routine> lst7 = unit.getRootScope().lookupRoutines("f7");
    assertEquals(lst7.size(), 1);
    Routine f7 = lst7.get(0);
    assertEquals(f7.getSignature(), "f7(MDE)");
    assertEquals(f7.getIDESignature(), "f7(⇅DEC) : INT");
    assertEquals(f7.getIDEInsertElement(true), "f7(INPUT-OUTPUT ${1:xx})$0");
    assertEquals(f7.getIDEInsertElement(false), "f7(input-output ${1:xx})$0");
    assertEquals(f7.getParameters().size(), 1);

    List<Routine> lst8 = unit.getRootScope().lookupRoutines("f8");
    assertEquals(lst8.size(), 1);
    Routine f8 = lst8.get(0);
    assertEquals(f8.getSignature(), "f8(ODE)");
    assertEquals(f8.getIDESignature(), "f8(↓DEC) : INT");
    assertEquals(f8.getIDESignature(false), "f8(↓DEC) : INT");
    assertEquals(f8.getIDESignature(true), "f8(↑DEC) : INT");
    assertEquals(f8.getIDEInsertElement(true), "f8(OUTPUT ${1:xx})$0");
    assertEquals(f8.getIDEInsertElement(false), "f8(output ${1:xx})$0");
    assertEquals(f8.getParameters().size(), 1);

    var lst9 = unit.getRootScope().lookupRoutines("f9");
    assertEquals(lst9.size(), 1);
    var f9 = lst9.get(0);
    assertEquals(f9.getSignature(), "f9(II,IT,OTH,ID,IDH)");
    assertEquals(f9.getIDESignature(), "f9(↑INT, ↑TBL, ↓TBL-HDL, ↑DS, ↑DS-HDL) : INT");
    assertEquals(f9.getIDEInsertElement(true), "f9(${1:prm1}, ${2:ttCustomer}, OUTPUT ${3:h1}, ${4:arg4}, ${5:h2})$0");
    assertEquals(f9.getIDEInsertElement(false), "f9(${1:prm1}, ${2:ttCustomer}, output ${3:h1}, ${4:arg4}, ${5:h2})$0");
    assertEquals(f9.getParameters().size(), 5);

    // Test TreeParserSymbolScope#getTokenSymbolScope()
    assertEquals(unit.getRootScope().getTokenSymbolScope(205), unit.getRootScope());
    assertEquals(unit.getRootScope().getTokenSymbolScope(150).getRoutine().getName(), "f3");
  }

  @Test
  public void test06() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test06.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertNotNull(unit.getRootScope().getRootBlock());
    // One REPEAT block in main block
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 1);
  }

  @Test
  public void test07() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test07.cls"), session);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test08.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 0);
    Variable xx = unit.getRootScope().getChildScopes().get(0).getVariable("xx");
    assertNotNull(xx);
    assertEquals(xx.getNumReads(), 1);
    assertEquals(xx.getNumWrites(), 0);
  }

  @Test
  public void test09() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test09.p"), session);
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
    ParseUnit unit = getParseUnit("define input parameter ipPrm no-undo like customer.custnum.", session);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test11.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    List<Routine> lst = unit.getRootScope().lookupRoutines("foo1");
    assertEquals(lst.size(), 1);
    Routine r1 = lst.get(0);
    assertEquals(r1.getSignature(), "foo1(OI)");
    assertEquals(r1.getIDESignature(), "foo1(↓INT) : VOID");
    assertEquals(r1.getParameters().size(), 1);
    Parameter p1 = r1.getParameters().get(0);
    assertEquals(p1.getDefinitionNode().getNodeType(), ABLNodeType.ID);
    assertEquals(p1.getDefinitionNode().getLine(), 3);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test12.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    List<Routine> lst = unit.getRootScope().lookupRoutines("foo1");
    assertEquals(lst.size(), 1);
    Routine r1 = lst.get(0);
    assertEquals(r1.getReturnDatatypeNode().getPrimitive(), PrimitiveDataType.CLASS);

    lst = unit.getRootScope().lookupRoutines("foo2");
    assertEquals(lst.size(), 1);
    Routine r2 = lst.get(0);
    assertEquals(r2.getReturnDatatypeNode().getPrimitive(), PrimitiveDataType.CLASS);

    lst = unit.getRootScope().lookupRoutines("foo3");
    assertEquals(lst.size(), 1);
    Routine r3 = lst.get(0);
    assertEquals(r3.getReturnDatatypeNode(), DataType.INTEGER);

    lst = unit.getRootScope().lookupRoutines("foo4");
    assertEquals(lst.size(), 1);
    Routine r4 = lst.get(0);
    assertEquals(r4.getReturnDatatypeNode(), DataType.CHARACTER);

    Event e1 = unit.getRootScope().lookupEvent("NewCustomer1");
    assertNotNull(e1);
    assertEquals(e1.getDefineNode().getLine(), 23);
    Event e2 = unit.getRootScope().lookupEvent("NewCustomer2");
    assertNotNull(e2);
    assertEquals(e2.getDefineNode().getLine(), 24);
    Event e3 = unit.getRootScope().lookupEvent("NewCustomer3");
    assertNotNull(e3);
    assertEquals(e3.getDefineNode().getLine(), 25);
  }

  @Test
  public void test13() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test13.p"), session);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test14.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable xxx = unit.getRootScope().getVariable("xxx");
    assertNotNull(xxx);
    assertEquals(xxx.getNumReads(), 1); // In the MESSAGE statement
    assertEquals(xxx.getNumWrites(), 3); // In OVERLAY, SUBSTRING, ENTRY
  }

  @Test
  public void test15() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test15.p"), session);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test16.cls"), session);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test17.p"), session);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test18.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 11);
    Variable var1 = unit.getRootScope().getVariable("prm1");
    assertEquals(var1.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
    Variable var2 = unit.getRootScope().getVariable("prm2");
    assertEquals(var2.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
    Variable var3 = unit.getRootScope().getVariable("prm3");
    assertEquals(var3.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
    Variable var4 = unit.getRootScope().getVariable("prm4");
    assertEquals(var4.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
    Variable var5 = unit.getRootScope().getVariable("prm5");
    assertEquals(var5.getDataType().getPrimitive(), PrimitiveDataType.INTEGER);
    Variable var6 = unit.getRootScope().getVariable("prm6");
    assertEquals(var6.getDataType().getPrimitive(), PrimitiveDataType.CHARACTER);
    Variable var7 = unit.getRootScope().getVariable("prm7");
    assertEquals(var7.getDataType().getPrimitive(), PrimitiveDataType.LONGCHAR);
    Variable var8 = unit.getRootScope().getVariable("prm8");
    assertEquals(var8.getDataType().getPrimitive(), PrimitiveDataType.HANDLE);
    Variable var9 = unit.getRootScope().getVariable("prm9");
    assertEquals(var9.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(var9.getDataType().getClassName(), "Progress.Lang.Object");
    Variable var10 = unit.getRootScope().getVariable("prm10");
    assertEquals(var10.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(var10.getDataType().getClassName(), "Progress.Lang.Object");
    Variable var11 = unit.getRootScope().getVariable("prm11");
    assertEquals(var11.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(var11.getDataType().getClassName(), "Progress.Lang.Object");
  }

  @Test
  public void test19() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test19.p"), session);
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
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test20.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().query(ABLNodeType.DISPLAY).size(), 1);
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 1);
  }

  @Test
  public void test21() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test21.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    JPNode node = unit.getTopNode().findDirectChild(ABLNodeType.DEFINE);
    assertEquals(node.asIStatement().getNodeType2(), ABLNodeType.VARIABLE);
    assertNotNull(unit.getRootScope().getRootBlock());
    assertEquals(unit.getRootScope().getRootBlock().getChildren().size(), 2);
    assertEquals(unit.getRootScope().getRootBlock().getChildren().get(0).getChildren().size(), 1);
    assertEquals(unit.getRootScope().getRootBlock().getChildren().get(1).getChildren().size(), 1);
  }

  @Test
  public void test22() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test22.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable var1 = unit.getRootScope().getVariable("yyy");
    assertEquals(var1.getNumReads(), 1);
    assertEquals(var1.getNumWrites(), 1);
    Variable lvar1 = unit.getRootScope().lookupVariable("yyy");
    assertEquals(var1, lvar1);
  }

  @Test
  public void test23() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test23.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable var1 = unit.getRootScope().getVariable("xxx");
    assertEquals(var1.getNumReads(), 0);
    assertEquals(var1.getNumWrites(), 1);
    Variable lvar1 = unit.getRootScope().lookupVariable("xxx");
    assertEquals(var1, lvar1);
    Variable var2 = unit.getRootScope().getVariable("yyy");
    assertEquals(var2.getNumReads(), 1);
    assertEquals(var2.getNumWrites(), 1);
    Variable lvar2 = unit.getRootScope().lookupVariable("yyy");
    assertEquals(var2, lvar2);

    assertEquals(unit.getRootScope().getChildScopes().size(), 2);
    Variable var3 = unit.getRootScope().getChildScopes().get(0).getVariable("xxx");
    assertNotNull(var3);
    assertEquals(var3.getNumReads(), 0);
    assertEquals(var3.getNumWrites(), 1);

    JPNode fooBlock = unit.getRootScope().getChildScopes().get(0).getRootBlock().getNode().findDirectChild(
        ABLNodeType.CODE_BLOCK);
    assertEquals(fooBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.LEFT_PART);
    assertEquals(fooBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.ATTRIBUTE_REF);
    assertEquals(fooBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getFirstChild().getSymbol(), var1);

    JPNode barBlock = unit.getRootScope().getChildScopes().get(1).getRootBlock().getNode().findDirectChild(
        ABLNodeType.CODE_BLOCK);
    assertEquals(barBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.LEFT_PART);
    assertEquals(barBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.ATTRIBUTE_REF);
    assertEquals(barBlock.getDirectChildren().get(0).getFirstChild().getFirstChild().getFirstChild().getSymbol(), var2);
    assertEquals(barBlock.getDirectChildren().get(1).getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.LEFT_PART);
    assertEquals(barBlock.getDirectChildren().get(1).getFirstChild().getFirstChild().getFirstChild().getNodeType(),
        ABLNodeType.ATTRIBUTE_REF);
    assertNull(barBlock.getDirectChildren().get(1).getFirstChild().getFirstChild().getFirstChild().getSymbol());
  }

  @Test
  public void test24() {
    var unit = getParseUnit(new File("src/test/resources/treeparser03/test24.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    var var1 = unit.getRootScope().getVariable("xxx");
    assertEquals(var1.getNumReads(), 1);
    assertEquals(var1.getNumWrites(), 0);
  }

  @Test
  public void testVarStatement01() {
    ParseUnit unit = getParseUnit("VAR CHAR s1, s2, s3.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("s1".equals(vv.getName()))
        v1 = vv;
      if ("s2".equals(vv.getName()))
        v2 = vv;
      if ("s3".equals(vv.getName()))
        v3 = vv;
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
    ParseUnit unit = getParseUnit("VAR INT s1, s2, s3 = 3.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("s1".equals(vv.getName()))
        v1 = vv;
      if ("s2".equals(vv.getName()))
        v2 = vv;
      if ("s3".equals(vv.getName()))
        v3 = vv;
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
    ParseUnit unit = getParseUnit("VAR CLASS mypackage.subdir.myclass myobj1, myobj2, myobj3.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("myobj1".equals(vv.getName()))
        v1 = vv;
      if ("myobj2".equals(vv.getName()))
        v2 = vv;
      if ("myobj3".equals(vv.getName()))
        v3 = vv;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);
    assertEquals(v1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(v2.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(v3.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(v1.getDataType().getClassName(), "mypackage.subdir.myclass");
    assertEquals(v2.getDataType().getClassName(), "mypackage.subdir.myclass");
    assertEquals(v3.getDataType().getClassName(), "mypackage.subdir.myclass");
  }

  @Test
  public void testVarStatement04() {
    ParseUnit unit = getParseUnit("VAR mypackage.subdir.myclass myobj1.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable v1 = null;
    for (Variable v : unit.getRootScope().getVariables()) {
      if ("myobj1".equals(v.getName()))
        v1 = v;
    }
    assertNotNull(v1);
    assertEquals(v1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(v1.getDataType().getClassName(), "mypackage.subdir.myclass");
  }

  @Test
  public void testVarStatement05() {
    ParseUnit unit = getParseUnit("VAR DATE d1, d2 = 1/1/2020, d3 = TODAY.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("d1".equals(vv.getName()))
        v1 = vv;
      if ("d2".equals(vv.getName()))
        v2 = vv;
      if ("d3".equals(vv.getName()))
        v3 = vv;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertNotNull(v3);
    assertEquals(v1.getDataType().getPrimitive(), PrimitiveDataType.DATE);
    assertEquals(v2.getDataType().getPrimitive(), PrimitiveDataType.DATE);
    assertEquals(v3.getDataType().getPrimitive(), PrimitiveDataType.DATE);
    assertNull(v1.getInitialValue());
    assertNotNull(v2.getInitialValue());
    assertNotNull(v3.getInitialValue());
    assertTrue(v2.getInitialValue() instanceof Date);
    assertEquals(v3.getInitialValue(), Variable.CONSTANT_TODAY);
  }

  @Test
  public void testVarStatement06() {
    ParseUnit unit = getParseUnit("VAR PROTECTED DATE d1, d2 = 1/1/2020.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("d1".equals(vv.getName()))
        v1 = vv;
      if ("d2".equals(vv.getName()))
        v2 = vv;
    }
    assertNotNull(v1);
    assertNotNull(v2);
    assertEquals(v1.getDataType().getPrimitive(), PrimitiveDataType.DATE);
    assertEquals(v2.getDataType().getPrimitive(), PrimitiveDataType.DATE);
    assertTrue(v1.containsModifier(Modifier.PROTECTED));
    assertTrue(v2.containsModifier(Modifier.PROTECTED));
  }

  @Test
  public void testVarStatement07() {
    ParseUnit unit = getParseUnit("VAR INT[3] x = [1, 2], y, z = [100, 200, 300].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable v1 = null, v2 = null, v3 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("x".equals(vv.getName()))
        v1 = vv;
      if ("y".equals(vv.getName()))
        v2 = vv;
      if ("z".equals(vv.getName()))
        v3 = vv;
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
    ParseUnit unit = getParseUnit("VAR INT[] x, y.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("x".equals(vv.getName()))
        v1 = vv;
      if ("y".equals(vv.getName()))
        v2 = vv;
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
    ParseUnit unit = getParseUnit("VAR INT[] x, y = [1,2,3].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("x".equals(vv.getName()))
        v1 = vv;
      if ("y".equals(vv.getName()))
        v2 = vv;
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
    ParseUnit unit = getParseUnit("VAR INT[] x = [1,2], y = [1,2,3].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 2);
    Variable v1 = null, v2 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("x".equals(vv.getName()))
        v1 = vv;
      if ("y".equals(vv.getName()))
        v2 = vv;
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
    ParseUnit unit = getParseUnit("VAR CLASS foo[2] classArray.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable v1 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("classArray".equals(vv.getName()))
        v1 = vv;
    }
    assertNotNull(v1);
    assertEquals(v1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(v1.getDataType().getClassName(), "foo");
    assertEquals(v1.getExtent(), 2);
  }

  @Test
  public void testVarStatement12() {
    ParseUnit unit = getParseUnit("VAR \"System.Collections.Generic.List<char>\" cList.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable v1 = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("cList".equals(vv.getName()))
        v1 = vv;
    }
    assertNotNull(v1);
    assertEquals(v1.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(v1.getDataType().getClassName(), "System.Collections.Generic.List");
    assertEquals(v1.getExtent(), 0);
  }

  @Test
  public void testVarStatement13() {
    ParseUnit unit = getParseUnit("VAR INT a, b, x = a + b, y = a - b, z = x - y.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 5);
    Variable varA = null;
    Variable varB = null;
    Variable varX = null;
    Variable varY = null;
    Variable varZ = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("a".equals(vv.getName()))
        varA = vv;
      else if ("b".equals(vv.getName()))
        varB = vv;
      else if ("x".equals(vv.getName()))
        varX = vv;
      else if ("y".equals(vv.getName()))
        varY = vv;
      else if ("z".equals(vv.getName()))
        varZ = vv;
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
    ParseUnit unit = getParseUnit("VAR INT a, b. VAR INT[] x = [ a + b, a - b ].", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);

    assertEquals(unit.getRootScope().getVariables().size(), 3);
    Variable varA = null;
    Variable varB = null;
    Variable varX = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("a".equals(vv.getName()))
        varA = vv;
      else if ("b".equals(vv.getName()))
        varB = vv;
      else if ("x".equals(vv.getName()))
        varX = vv;
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
    ParseUnit unit = getParseUnit("USING Progress.Lang.Object. VAR Object x = NEW Object().", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable varX = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("x".equals(vv.getName()))
        varX = vv;

      assertNotNull(varX);
      assertEquals(varX.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
      assertEquals(varX.getDataType().getClassName(), "Progress.Lang.Object");
      assertEquals(varX.getExtent(), 0);
      assertEquals(varX.getInitialValue(), Variable.CONSTANT_EXPRESSION);
      assertEquals(varX.getNumReads(), 0);
      assertEquals(varX.getNumWrites(), 1);
    }
  }

  @Test
  public void testVarStatement16() {
    ParseUnit unit = getParseUnit("VAR DATETIME dtm = DATETIME(TODAY,MTIME).", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable varX = null;
    for (Variable vv : unit.getRootScope().getVariables()) {
      if ("dtm".equals(vv.getName()))
        varX = vv;

      assertNotNull(varX);
      assertEquals(varX.getDataType().getPrimitive(), PrimitiveDataType.DATETIME);
      assertEquals(varX.getExtent(), 0);
      assertEquals(varX.getInitialValue(), Variable.CONSTANT_EXPRESSION);
      assertEquals(varX.getNumReads(), 0);
      assertEquals(varX.getNumWrites(), 1);
    }
  }

  @Test
  public void testShorthandOperator01() {
    ParseUnit unit = getParseUnit("VAR INT i1. ASSIGN i1 += 1.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    Variable i1 = unit.getRootScope().getVariable("i1");
    assertNotNull(i1);
    assertEquals(i1.getNumReads(), 1);
    assertEquals(i1.getNumWrites(), 1);
  }

  @Test
  public void testParameterAs() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test25.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getVariables().size(), 6);

    Variable x1 = unit.getRootScope().getVariable("x1");
    assertNotNull(x1);
    assertEquals(x1.getNumReads(), 1);
    assertEquals(x1.getNumWrites(), 0);

    Variable x2 = unit.getRootScope().getVariable("x2");
    assertNotNull(x2);
    assertEquals(x2.getNumReads(), 1);
    assertEquals(x2.getNumWrites(), 0);

    Variable x3 = unit.getRootScope().getVariable("x3");
    assertNotNull(x3);
    assertEquals(x3.getNumReads(), 0);
    assertEquals(x3.getNumWrites(), 1);

    Variable x4 = unit.getRootScope().getVariable("x4");
    assertNotNull(x4);
    assertEquals(x4.getNumReads(), 0);
    assertEquals(x4.getNumWrites(), 1);

    Variable x5 = unit.getRootScope().getVariable("x5");
    assertNotNull(x5);
    assertEquals(x5.getNumReads(), 1);
    assertEquals(x5.getNumWrites(), 1);

    Variable x6 = unit.getRootScope().getVariable("x6");
    assertNotNull(x6);
    assertEquals(x6.getNumReads(), 1);
    assertEquals(x6.getNumWrites(), 1);
  }

  @Test
  public void testAssignmentList() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test26.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void testBufferCompare() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test27.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable logVar = unit.getRootScope().getVariable("logVar");
    assertNotNull(logVar);
    assertEquals(logVar.getNumReads(), 0);
    assertEquals(logVar.getNumWrites(), 1);
  }

  @Test
  public void testChoose() {
    var unit = getParseUnit(new File("src/test/resources/treeparser03/test28.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var menu = unit.getRootScope().getVariable("menu");
    assertNotNull(menu);
    assertEquals(menu.getNumReads(), 1);
    assertEquals(menu.getNumWrites(), 2);
  }

  @Test
  public void testLexAt() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test29.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable x1 = unit.getRootScope().getVariable("x1");
    assertNotNull(x1);
    assertEquals(x1.getNumReads(), 1);
    assertEquals(x1.getNumReferenced(), 0);
    assertEquals(x1.getNumWrites(), 0);

    Variable x2 = unit.getRootScope().getVariable("x2");
    assertNotNull(x2);
    assertEquals(x2.getNumReads(), 0);
    assertEquals(x2.getNumReferenced(), 1);
    assertEquals(x2.getNumWrites(), 0);
  }

  @Test
  public void testDefBrowseDisplay() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test30.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void testParameters() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test31.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable prm1 = unit.getRootScope().getVariable("prm1");
    assertNotNull(prm1);
    assertEquals(prm1.getDataType(), DataType.INTEGER);
    assertNull(prm1.getInitialValue());
    Variable prm2 = unit.getRootScope().getVariable("prm2");
    assertNotNull(prm2);
    assertEquals(prm2.getDataType(), DataType.INTEGER);
    assertEquals(prm2.getInitialValue(), Double.valueOf(2));
    Variable prm3 = unit.getRootScope().getVariable("prm3");
    assertNotNull(prm3);
    assertEquals(prm3.getDataType(), DataType.TABLE_HANDLE);
    Variable prm4 = unit.getRootScope().getVariable("prm4");
    assertNotNull(prm4);
    assertEquals(prm4.getDataType(), DataType.DATASET_HANDLE);
  }

  @Test
  public void testEntered() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test32.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getTopNode().query(ABLNodeType.IF).size(), 2);
    JPNode ifNode = unit.getTopNode().query(ABLNodeType.IF).get(0);
    assertNotNull(ifNode.getFirstChild());
    assertEquals(ifNode.getFirstChild().getNodeType(), ABLNodeType.ENTERED_FUNC);
    assertNotNull(ifNode.getFirstChild().getFirstChild());
    assertEquals(ifNode.getFirstChild().getFirstChild().getNodeType(), ABLNodeType.FIELD_REF);
  }

  @Test
  public void testImgLike() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test33.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    IFieldLevelWidget x1 = unit.getRootScope().lookupFieldLevelWidget("img01");
    assertNotNull(x1);
    assertEquals(x1.getNumReads(), 0);
    assertEquals(x1.getNumReferenced(), 1);
    assertEquals(x1.getNumWrites(), 0);

    IFieldLevelWidget x2 = unit.getRootScope().lookupFieldLevelWidget("img02");
    assertNotNull(x2);
    assertEquals(x2.getNumReads(), 0);
    assertEquals(x2.getNumReferenced(), 0);
    assertEquals(x2.getNumWrites(), 0);

    IFieldLevelWidget x3 = unit.getRootScope().lookupFieldLevelWidget("rect01");
    assertNotNull(x3);
    assertEquals(x3.getNumReads(), 0);
    assertEquals(x3.getNumReferenced(), 1);
    assertEquals(x3.getNumWrites(), 0);

    IFieldLevelWidget x4 = unit.getRootScope().lookupFieldLevelWidget("rect02");
    assertNotNull(x4);
    assertEquals(x4.getNumReads(), 0);
    assertEquals(x4.getNumReferenced(), 0);
    assertEquals(x4.getNumWrites(), 0);
  }

  @Test
  public void testImportExport() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test34.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void testExternalDataTypes() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test35.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertEquals(unit.getRootScope().getChildScopes().size(), 1);
    TreeParserSymbolScope scope = unit.getRootScope().getChildScopes().get(0);
    assertEquals(scope.getVariable("prm1").getDataType(), DataType.BLOB);
    assertEquals(scope.getVariable("prm2").getDataType(), DataType.CLOB);
    assertEquals(scope.getVariable("prm3").getDataType(), DataType.BYTE);
    assertEquals(scope.getVariable("prm4").getDataType(), DataType.SHORT);
    assertEquals(scope.getVariable("prm5").getDataType(), DataType.FLOAT);
    assertEquals(scope.getVariable("prm6").getDataType(), DataType.DOUBLE);
    assertEquals(scope.getVariable("prm7").getDataType(), DataType.UNSIGNED_SHORT);
    assertEquals(scope.getVariable("prm8").getDataType(), DataType.UNSIGNED_BYTE);
    assertEquals(scope.getVariable("prm9").getDataType(), DataType.UNSIGNED_INTEGER);
  }

  @Test
  public void testSuper01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test36.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    List<JPNode> list = unit.getTopNode().query(ABLNodeType.SUPER);
    assertEquals(list.size(), 4);
    assertEquals(list.get(0).getParent().getNodeType(), ABLNodeType.BUILTIN_FUNCTION);
    assertEquals(list.get(1).getParent().getNodeType(), ABLNodeType.BUILTIN_FUNCTION);
    assertEquals(list.get(2).getParent().getNodeType(), ABLNodeType.BUILTIN_FUNCTION);
    assertEquals(list.get(3).getParent().getNodeType(), ABLNodeType.SYSTEM_HANDLE_REF);
  }

  @Test
  public void testSuper02() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test36.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    List<JPNode> list = unit.getTopNode().query(ABLNodeType.SUPER);
    assertEquals(list.size(), 3);
    assertEquals(list.get(0).getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertTrue(list.get(0).getParent().isIExpression());
    assertTrue(list.get(0).getParent().getParent().isStateHead());
    assertEquals(list.get(1).getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertTrue(list.get(1).getParent().isIExpression());
    assertTrue(list.get(1).getParent().getParent().isStateHead());
    assertEquals(list.get(2).getParent().getNodeType(), ABLNodeType.SYSTEM_HANDLE_REF);
    assertTrue(list.get(2).getParent().isIExpression());
    assertEquals(list.get(2).getParent().getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertTrue(list.get(2).getParent().getParent().isIExpression());
    assertFalse(list.get(2).getParent().getParent().isStateHead());
  }

  @Test
  public void testThisObject01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test37.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    List<JPNode> list = unit.getTopNode().query(ABLNodeType.THISOBJECT);
    assertEquals(list.size(), 3);
    assertEquals(list.get(0).getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertTrue(list.get(0).getParent().isIExpression());
    assertTrue(list.get(0).getParent().getParent().isStateHead());
    assertEquals(list.get(1).getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertTrue(list.get(1).getParent().isIExpression());
    assertTrue(list.get(1).getParent().getParent().isStateHead());
    assertEquals(list.get(2).getParent().getNodeType(), ABLNodeType.SYSTEM_HANDLE_REF);
    assertTrue(list.get(2).getParent().isIExpression());
    assertEquals(list.get(2).getParent().getParent().getNodeType(), ABLNodeType.METHOD_REF);
    assertTrue(list.get(2).getParent().getParent().isIExpression());
    assertFalse(list.get(2).getParent().getParent().isStateHead());
  }

  @Test
  public void testTempTableNoUndo01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test38.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    ITable tta = unit.getRootScope().lookupTableDefinition("tta");
    ITable ttb = unit.getRootScope().lookupTableDefinition("ttb");
    ITable ttc = unit.getRootScope().lookupTableDefinition("ttc");
    ITable ttd = unit.getRootScope().lookupTableDefinition("ttd");
    ITable tte = unit.getRootScope().lookupTableDefinition("tte");
    ITable ttf = unit.getRootScope().lookupTableDefinition("ttf");
    ITable ttg = unit.getRootScope().lookupTableDefinition("ttg");
    ITable tth = unit.getRootScope().lookupTableDefinition("tth");
    assertNotNull(tta);
    assertNotNull(ttb);
    assertNotNull(ttc);
    assertNotNull(ttd);
    assertNotNull(tte);
    assertNotNull(ttf);
    assertNotNull(ttg);
    assertNotNull(tth);
    assertFalse(tta.isNoUndo());
    assertFalse(ttb.isNoUndo());
    assertTrue(ttc.isNoUndo());
    assertFalse(ttd.isNoUndo());
    assertFalse(tte.isNoUndo());
    assertTrue(ttf.isNoUndo());
    assertTrue(ttg.isNoUndo());
    assertFalse(tth.isNoUndo());
  }

  @Test
  public void testTTAsParameter() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test39.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    Optional<Routine> p1 = unit.getRootScope().getRoutines().stream().filter(r -> "p1".equals(r.getName())).findFirst();
    Optional<Routine> f1 = unit.getRootScope().getRoutines().stream().filter(r -> "f1".equals(r.getName())).findFirst();
    Optional<Routine> f2 = unit.getRootScope().getRoutines().stream().filter(r -> "f2".equals(r.getName())).findFirst();
    assertTrue(p1.isPresent());
    assertTrue(f1.isPresent());
    assertTrue(f2.isPresent());
    Parameter prm1 = p1.get().getParameters().get(0);
    assertNotNull(prm1);
    assertEquals(prm1.getDefinitionNode().getNodeType(), ABLNodeType.DEFINE);
    assertEquals(prm1.getDefinitionNode().getLine(), 4);
    assertEquals(prm1.getProgressType(), ABLNodeType.TEMPTABLE.getType());
    assertNotNull(prm1.getSymbol());
    assertEquals(prm1.getSymbol().getName(), "tt1");
    Parameter prm2 = f1.get().getParameters().get(0);
    assertNotNull(prm2);
    assertEquals(prm2.getDefinitionNode().getNodeType(), ABLNodeType.RECORD_NAME);
    assertEquals(prm2.getDefinitionNode().getLine(), 7);
    assertEquals(prm2.getProgressType(), ABLNodeType.TEMPTABLE.getType());
    assertNotNull(prm2.getSymbol());
    assertEquals(prm2.getSymbol().getName(), "tt1");
    // All parameters from f2
    Parameter f2prm1 = f2.get().getParameters().get(0);
    Parameter f2prm2 = f2.get().getParameters().get(1);
    Parameter f2prm3 = f2.get().getParameters().get(2);
    Parameter f2prm4 = f2.get().getParameters().get(3);
    Parameter f2prm5 = f2.get().getParameters().get(4);
    Parameter f2prm6 = f2.get().getParameters().get(5);
    assertNotNull(f2prm1);
    assertNotNull(f2prm2);
    assertNotNull(f2prm3);
    assertNotNull(f2prm4);
    assertNotNull(f2prm5);
    assertNotNull(f2prm6);
    assertNotNull(f2prm1.getDefinitionNode());
    assertNotNull(f2prm2.getDefinitionNode());
    assertNotNull(f2prm3.getDefinitionNode());
    assertNotNull(f2prm4.getDefinitionNode());
    assertNotNull(f2prm5.getDefinitionNode());
    assertNotNull(f2prm6.getDefinitionNode());
    assertEquals(f2prm1.getDefinitionNode().getNodeType(), ABLNodeType.ID);
    assertEquals(f2prm1.getDefinitionNode().getLine(), 13);
    assertEquals(f2prm1.getDefinitionNode().getColumn(), 4);
    assertEquals(f2prm2.getDefinitionNode().getNodeType(), ABLNodeType.ID);
    assertEquals(f2prm2.getDefinitionNode().getLine(), 14);
    assertEquals(f2prm2.getDefinitionNode().getColumn(), 4);
    assertEquals(f2prm3.getDefinitionNode().getNodeType(), ABLNodeType.RECORD_NAME);
    assertEquals(f2prm3.getDefinitionNode().getLine(), 15);
    assertEquals(f2prm3.getDefinitionNode().getColumn(), 10);
    assertEquals(f2prm4.getDefinitionNode().getNodeType(), ABLNodeType.ID);
    assertEquals(f2prm4.getDefinitionNode().getLine(), 16);
    assertEquals(f2prm4.getDefinitionNode().getColumn(), 17);
    assertEquals(f2prm5.getDefinitionNode().getNodeType(), ABLNodeType.ID);
    assertEquals(f2prm5.getDefinitionNode().getLine(), 17);
    assertEquals(f2prm5.getDefinitionNode().getColumn(), 12);
    assertEquals(f2prm6.getDefinitionNode().getNodeType(), ABLNodeType.ID);
    assertEquals(f2prm6.getDefinitionNode().getLine(), 18);
    assertEquals(f2prm6.getDefinitionNode().getColumn(), 19);

  }

  @Test
  public void test40() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test40.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable xx = unit.getRootScope().getVariable("xx");
    Variable yy = unit.getRootScope().getVariable("yy");
    Variable zz = unit.getRootScope().getVariable("zz");
    Variable zz2 = unit.getRootScope().getVariable("zz");
    assertNotNull(xx);
    assertNotNull(yy);
    assertNotNull(zz);
    assertNotNull(zz2);
    assertEquals(xx.getDataType(), DataType.DATETIME);
    assertEquals(yy.getDataType(), DataType.INTEGER);
    assertEquals(zz.getDataType(), DataType.DECIMAL);
    assertEquals(zz2.getDataType(), DataType.DECIMAL);
  }

  @Test
  public void test41() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test41.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    Variable numCustomers = unit.getRootScope().getVariable("numCustomers");
    assertNotNull(numCustomers);
    assertEquals(numCustomers.getDataType(), DataType.INTEGER);
    assertEquals(numCustomers.getNumReads(), 1);
    assertEquals(numCustomers.getNumWrites(), 3);
    assertEquals(unit.getRootScope().getUnnamedBuffers().size(), 1);

    TableBuffer buff = unit.getRootScope().getUnnamedBuffers().iterator().next();
    assertNotNull(buff);
    assertEquals(buff.getName(), "Customer");
    assertEquals(buff.getAllRefsCount(), 3);

    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.AGGREGATE_EXPRESSION);
    assertNotNull(nodes);
    assertEquals(nodes.size(), 3);
    assertEquals(nodes.get(0).asIExpression().getDataType(), DataType.INT64);
    assertEquals(nodes.get(1).asIExpression().getDataType(), DataType.INT64);
    assertEquals(nodes.get(2).asIExpression().getDataType(), DataType.DECIMAL);
  }

  @Test
  public void test42() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test42.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    assertNull(unit.getRootScope().lookupQuery("qry00"));
    Query qry = unit.getRootScope().lookupQuery("qry");
    assertNotNull(qry);
    List<JPNode> list = unit.getTopNode().query(ABLNodeType.ID);
    assertEquals(list.size(), 4);
    assertFalse(list.stream().anyMatch(it -> it.getSymbol() == null));
    assertFalse(list.stream().anyMatch(it -> it.getSymbol().getNodeType() != ABLNodeType.QUERY));
  }

  @Test
  public void testCanFindScope() {
    var code = """
        define temp-table tt1 field fld1 as character.
        define temp-table tt2 field fld1 as character.
        define buffer btt2 for tt2.
        can-find(tt1 where tt1.fld1 = "").
        can-find(tt2 where tt2.fld1 = "").
        can-find(btt2 where btt2.fld1 = "").
        can-find(customer where customer.name = "").
        find customer where customer.name = "".
        find item where item.itemname = "".
        """;
    var unit = getParseUnit(code, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var list = unit.getTopNode().query(ABLNodeType.RECORD_NAME);
    var recNode1 = list.get(1);
    var tblBuf1 = (TableBuffer) recNode1.getSymbol();
    assertEquals(recNode1.getSymbol().getDefineNode().getLine(), 4);
    assertFalse(tblBuf1.isDefault());
    var recNode2 = list.get(2);
    var tblBuf2 = (TableBuffer) recNode2.getSymbol();
    assertEquals(recNode2.getSymbol().getDefineNode().getLine(), 5);
    assertFalse(tblBuf2.isDefault());
    var recNode3 = list.get(3);
    var tblBuf3 = (TableBuffer) recNode3.getSymbol();
    assertEquals(recNode3.getSymbol().getDefineNode().getLine(), 6);
    assertFalse(tblBuf3.isDefault());
    var recNode4 = list.get(4);
    var tblBuf4 = (TableBuffer) recNode4.getSymbol();
    assertEquals(recNode4.getSymbol().getDefineNode().getLine(), 7);
    assertFalse(tblBuf4.isDefault());
    var recNode5 = list.get(5);
    var tblBuf5 = (TableBuffer) recNode5.getSymbol();
    // Check we still end up with default buffer on customer after the can-find function
    assertTrue(tblBuf5.isDefault());
    var recNode6 = list.get(6);
    var tblBuf6 = (TableBuffer) recNode6.getSymbol();
    // Check we still end up with default buffer on customer without can-find function
    assertTrue(tblBuf6.isDefault());
  }

  @Test
  public void testArrayRefInFrame() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/arrayRefInFrame.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());

    var a1 = unit.getRootScope().getVariable("a1");
    assertNotNull(a1);
    assertEquals(a1.getNumReads(), 0);
    assertEquals(a1.getNumReferenced(), 2);
    assertEquals(a1.getNumWrites(), 0);
    assertTrue(a1.isReferencedInFrame());

    var b1 = unit.getRootScope().getVariable("b1");
    assertNotNull(b1);
    assertEquals(b1.getNumReads(), 0);
    assertEquals(b1.getNumReferenced(), 2);
    assertEquals(b1.getNumWrites(), 0);
    assertTrue(b1.isReferencedInFrame());

    var a2 = unit.getRootScope().getVariable("a2");
    assertNotNull(a2);
    assertEquals(a2.getNumReads(), 1);
    assertEquals(a2.getNumReferenced(), 0);
    assertEquals(a2.getNumWrites(), 0);
    assertFalse(a2.isReferencedInFrame());

    var b2 = unit.getRootScope().getVariable("b2");
    assertNotNull(b2);
    assertEquals(b2.getNumReads(), 1);
    assertEquals(b2.getNumReferenced(), 0);
    assertEquals(b2.getNumWrites(), 0);
    assertFalse(b2.isReferencedInFrame());
  }

  @Test
  public void testGenerics01() {
    ParseUnit unit = getParseUnit("def var xx as Progress.Collections.IMap<Employee, Manager>.", session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);

    assertEquals(unit.getRootScope().getVariables().size(), 1);
    var xx = unit.getRootScope().getVariable("xx");
    assertNotNull(xx);
    assertEquals(xx.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(xx.getDataType().getClassName(), "Progress.Collections.IMap");
  }

  @Test
  public void testGenerics02() {
    ParseUnit unit = getParseUnit(
        "def var xx as Progress.Collections.IMap<Progress.Collections.Hashable<K,V>, Progress.Collections.Hashable<L,M>>.",
        session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
    assertEquals(unit.getRootScope().getVariables().size(), 1);

    var xx = unit.getRootScope().getVariable("xx");
    assertNotNull(xx);
    assertEquals(xx.getDataType().getPrimitive(), PrimitiveDataType.CLASS);
    assertEquals(xx.getDataType().getClassName(), "Progress.Collections.IMap");
  }

  @Test
  public void test43() {
    var unit = getParseUnit(new File("src/test/resources/treeparser03/test43.cls"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    var lst = unit.getRootScope().lookupRoutines("foo1");
    assertEquals(lst.size(), 1);
    var r1 = lst.get(0);
    assertNotNull(r1.getMethodElement());
    var lst2 = unit.getRootScope().lookupRoutines("foo2");
    assertEquals(lst2.size(), 3);
    assertNotNull(lst2.get(0).getMethodElement());
    assertNotNull(lst2.get(1).getMethodElement());
    assertNotNull(lst2.get(2).getMethodElement());
  }

  @Test
  public void test44() {
    var unit = getParseUnit(new File("src/test/resources/treeparser03/test44.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
    var lst = unit.getRootScope().lookupRoutines("foo1");
    assertEquals(lst.size(), 1);
    var r1 = lst.get(0);
    assertNull(r1.getMethodElement());
  }

  @Test
  public void testDataset01() {
    var code = """
        define temp-table tt1 field fld1 as character.
        define dataset ds1 for tt1.
        dataset ds1:fill().
        run proc1 (input dataset ds1).
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 4);
    List<Dataset> xx = (List<Dataset>) unit.getRootScope().getAllSymbols(Dataset.class);
    assertNotNull(xx);
    assertEquals(xx.size(), 1);
    assertEquals(xx.get(0).getName(), "ds1");
    assertEquals(xx.get(0).getAllRefsCount(), 2);
    assertEquals(xx.get(0).getDefineNode().getLine(), 2);
    assertEquals(xx.get(0).getBuffers().size(), 1);
    assertEquals(xx.get(0).getBuffers().get(0).getName(), "tt1");

    var lststatements = unit.getTopNode().queryStateHead();
    var methodCall = lststatements.get(2).getDirectChildren(ABLNodeType.METHOD_REF);

    assertNotNull(methodCall);
    assertEquals(methodCall.size(), 1);
    var dataset = methodCall.get(0).getDirectChildren(ABLNodeType.WIDGET_REF).get(0).getSymbol();
    assertNotNull(dataset);
    assertEquals(dataset.getName(), "ds1");
    assertEquals(dataset.getDefineNode().getLine(), 2);

    var procCall = lststatements.get(3);
    assertNotNull(procCall);
    var param = procCall.query(ABLNodeType.PARAMETER_ITEM);
    assertNotNull(param);
    assertEquals(param.size(), 1);
    var dataset2 = param.get(0).getSymbol();
    assertNotNull(dataset2);
    assertEquals(dataset2.getDefineNode().getLine(), 2);
  }

  @Test
  public void testDataset02() {
    var code = """
        define temp-table tt1 field fld1 as character.
        define dataset ds1 for tt1.
        define buffer ds1 for tt1.
        dataset ds1:fill().
        run proc1 (input dataset ds1).

        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 5);
    List<Dataset> xx = (List<Dataset>) unit.getRootScope().getAllSymbols(Dataset.class);
    assertNotNull(xx);
    assertEquals(xx.size(), 1);
    assertEquals(xx.get(0).getName(), "ds1");
    assertEquals(xx.get(0).getAllRefsCount(), 2);
    assertEquals(xx.get(0).getDefineNode().getLine(), 2);
    assertEquals(xx.get(0).getBuffers().size(), 1);
    assertEquals(xx.get(0).getBuffers().get(0).getName(), "tt1");

    var lststatements = unit.getTopNode().queryStateHead();
    var methodCall = lststatements.get(3).getDirectChildren(ABLNodeType.METHOD_REF);

    assertNotNull(methodCall);
    assertEquals(methodCall.size(), 1);
    var dataset = methodCall.get(0).getDirectChildren(ABLNodeType.WIDGET_REF).get(0).getSymbol();
    assertNotNull(dataset);
    assertEquals(dataset.getName(), "ds1");
    assertEquals(dataset.getDefineNode().getLine(), 2);

    var procCall = lststatements.get(4);
    assertNotNull(procCall);
    var param = procCall.query(ABLNodeType.PARAMETER_ITEM);
    assertNotNull(param);
    assertEquals(param.size(), 1);
    var dataset2 = param.get(0).getSymbol();
    assertNotNull(dataset2);
    assertEquals(dataset2.getDefineNode().getLine(), 2);

  }

  @Test
  public void testDataset03() {
    var code = """
        define temp-table tt1 field fld1 as character.
        define temp-table tt2 field fld2 as character.
        define dataset ds1 for tt1, tt2 data-relation rel1 for tt1,tt2.
        """;
    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
    List<Dataset> xx = (List<Dataset>) unit.getRootScope().getAllSymbols(Dataset.class);
    assertNotNull(xx);
    assertEquals(xx.size(), 1);
    assertEquals(xx.get(0).getName(), "ds1");
    assertEquals(xx.get(0).getDefineNode().getLine(), 3);
    assertEquals(xx.get(0).getBuffers().size(), 2);
    assertEquals(xx.get(0).getBuffers().get(0).getName(), "tt1");
    assertEquals(xx.get(0).getBuffers().get(1).getName(), "tt2");

    var rels = xx.get(0).getRelations();
    assertNotNull(rels);
    assertEquals(rels.size(), 1);
    assertEquals(rels.get(0).getName(), "rel1");
    assertEquals(rels.get(0).getParentBuffer().getName(), "tt1");
    assertEquals(rels.get(0).getChildBuffer().getName(), "tt2");

  }

  @Test
  public void testDataset05() {
    var code = """
        define temp-table tt1 field fld1 as character.
        define temp-table tt2 field fld2 as CHARACTER FIELD fld21 AS CHARACTER.
        define temp-table tt3 field fld3 as CHARACTER FIELD fld31 AS CHARACTER.
        define dataset ds1 for tt1, tt2, tt3
          data-relation rel1 for tt1,tt2
          RELATION-FIELDS(fld1, fld2)
           data-relation rel2 for tt2,tt3
          RELATION-FIELDS(fld2, fld3, fld21, fld31).
          """;

    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 4);
    List<Dataset> xx = (List<Dataset>) unit.getRootScope().getAllSymbols(Dataset.class);
    assertNotNull(xx);
    assertEquals(xx.size(), 1);
    assertEquals(xx.get(0).getName(), "ds1");
    assertEquals(xx.get(0).getDefineNode().getLine(), 4);
    assertEquals(xx.get(0).getBuffers().size(), 3);
    assertEquals(xx.get(0).getBuffers().get(0).getName(), "tt1");
    assertEquals(xx.get(0).getBuffers().get(1).getName(), "tt2");
    assertEquals(xx.get(0).getBuffers().get(2).getName(), "tt3");

    var rels = xx.get(0).getRelations();
    assertNotNull(rels);
    assertEquals(rels.size(), 2);
    assertEquals(rels.get(0).getName(), "rel1");
    assertEquals(rels.get(0).getParentBuffer().getName(), "tt1");
    assertEquals(rels.get(0).getChildBuffer().getName(), "tt2");
    assertEquals(rels.get(1).getName(), "rel2");
    assertEquals(rels.get(1).getParentBuffer().getName(), "tt2");
    assertEquals(rels.get(1).getChildBuffer().getName(), "tt3");

    var fieldrel1 = rels.get(0).getRelationFields();
    assertNotNull(fieldrel1);
    assertEquals(fieldrel1.size(), 1);
    assertEquals(fieldrel1.get(0).getO1().getName(), "fld1");
    assertEquals(fieldrel1.get(0).getO2().getName(), "fld2");

    var fieldrel2 = rels.get(1).getRelationFields();
    assertNotNull(fieldrel2);
    assertEquals(fieldrel2.size(), 2);
    assertEquals(fieldrel2.get(0).getO1().getName(), "fld2");
    assertEquals(fieldrel2.get(0).getO2().getName(), "fld3");
    assertEquals(fieldrel2.get(1).getO1().getName(), "fld21");
    assertEquals(fieldrel2.get(1).getO2().getName(), "fld31");
  }

  @Test
  public void testDataset06() {
    // without data-relation identifier
    var code = """
        define temp-table tt1 field fld1 as character.
        define temp-table tt2 field fld2 as character.
        define dataset ds1 for tt1, tt2, tt3
          data-relation for tt1,tt2
          RELATION-FIELDS(fld1, fld2).
        """;

    ParseUnit unit = getParseUnit(code, session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
    List<Dataset> xx = (List<Dataset>) unit.getRootScope().getAllSymbols(Dataset.class);
    assertNotNull(xx);
    assertEquals(xx.size(), 1);
    assertEquals(xx.get(0).getName(), "ds1");
    assertEquals(xx.get(0).getDefineNode().getLine(), 3);
    assertEquals(xx.get(0).getBuffers().size(), 2);
    assertEquals(xx.get(0).getBuffers().get(0).getName(), "tt1");
    assertEquals(xx.get(0).getBuffers().get(1).getName(), "tt2");

    var rels = xx.get(0).getRelations();
    assertNotNull(rels);
    assertEquals(rels.size(), 1);
    assertEquals(rels.get(0).getName(), "");
    assertEquals(rels.get(0).getParentBuffer().getName(), "tt1");
    assertEquals(rels.get(0).getChildBuffer().getName(), "tt2");

  }

  @Test
  public void test45() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/treeparser03/test45.p"), session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getRootScope());

    var lststate = unit.getTopNode().queryStateHead();
    assertEquals(lststate.size(), 12);

    var lstwidget = unit.getTopNode().query(ABLNodeType.WIDGET_REF);
    assertEquals(lstwidget.size(), 6);

    var browse = lstwidget.get(0).getSymbol();
    assertNotNull(browse);
    assertEquals(browse.getName(), "b1");
    assertEquals(browse.getDefineNode().getLine(), 3);
    assertEquals(browse.getAllRefsCount(), 2);
    var query = lstwidget.get(1).getSymbol();
    assertNotNull(query);
    assertEquals(query.getName(), "q1");
    assertEquals(query.getDefineNode().getLine(), 2);
    assertEquals(query.getAllRefsCount(), 1);
    var temptable = lstwidget.get(2).getSymbol();
    assertNotNull(temptable);
    assertEquals(temptable.getName(), "tt1");
    assertEquals(temptable.getDefineNode().getLine(), 1);
    assertEquals(temptable.getAllRefsCount(), 5);
    var frame = lstwidget.get(3).getSymbol();
    assertNotNull(frame);
    assertEquals(frame.getName(), "f1");
    assertEquals(frame.getDefineNode().getLine(), 5);
    assertEquals(frame.getAllRefsCount(), 1);
    var stream = lstwidget.get(4).getSymbol();
    assertNotNull(stream);
    assertEquals(stream.getName(), "sin");
    assertEquals(stream.getDefineNode().getLine(), 8);
    assertEquals(stream.getAllRefsCount(), 1);
    var buffer = lstwidget.get(5).getSymbol();
    assertNotNull(buffer);
    assertEquals(buffer.getName(), "buf1");
    assertEquals(buffer.getDefineNode().getLine(), 9);
    assertEquals(buffer.getAllRefsCount(), 1);

  }

  @Test
  public void test46() {
    var code = """
        define temp-table tt1 field fld1 as character.
        define temp-table tt2 field fld2 as character.
        define dataset ds1 for tt1, tt2 data-relation rel1 for tt1,tt2.
        APPLY "VALUE-CHANGED" TO DATASET ds1.
        """;

    ParseUnit unit = getParseUnit(code, session);
    assertNull(unit.getTopNode());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getRootScope());

    var lstwidget = unit.getTopNode().query(ABLNodeType.WIDGET_REF);
    assertEquals(lstwidget.size(), 1);

    var dataset = lstwidget.get(0).getSymbol();
    assertNotNull(dataset);
  }

}
