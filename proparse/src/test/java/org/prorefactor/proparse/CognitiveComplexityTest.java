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
package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.fixed.TypeInfo;

public class CognitiveComplexityTest extends AbstractProparseTest {
  private static final String SRC_DIR = "src/test/resources/complexity";

  private RefactorSession session;

  @BeforeMethod
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test01() {
    var unit = getParseUnit(new File(SRC_DIR + "/test01.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 6);
    assertEquals(visitor.getMainFileComplexity(), 6);

    var p1Routine = unit.getRootScope().getRoutines().stream().filter(it -> "p1".equals(it.getName())).findAny().get();
    var p1Block = p1Routine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var p1Visitor = new CognitiveComplexityListener(p1Block);
    p1Visitor.walkStatementBlock(p1Block);
    assertEquals(p1Visitor.getComplexity(), 5);
    assertEquals(p1Visitor.getMainFileComplexity(), 5);
  }

  @Test
  public void test02() {
    var unit = getParseUnit(new File(SRC_DIR + "/test02.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 5);

    var items = visitor.getItems();
    assertEquals(items.size(), 4);

    var i1 = items.stream().filter(it -> it.getO1().getLine() == 6).findFirst();
    assertTrue(i1.isPresent());
    assertEquals(i1.get().getO2(), 0);

    var i2 = items.stream().filter(it -> it.getO1().getLine() == 8).findFirst();
    assertTrue(i2.isPresent());
    assertEquals(i2.get().getO2(), 0);

    var i3 = items.stream().filter(it -> it.getO1().getLine() == 9).findFirst();
    assertTrue(i3.isPresent());
    assertEquals(i3.get().getO2(), 1);

    var i4 = items.stream().filter(it -> it.getO1().getLine() == 11).findFirst();
    assertTrue(i4.isPresent());
    assertEquals(i4.get().getO2(), 0);
  }

  @Test
  public void test03() {
    var unit = getParseUnit(new File(SRC_DIR + "/test03.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 9);

    var items = visitor.getItems();
    assertEquals(items.size(), 6);

    var i1 = items.stream().filter(it -> it.getO1().getLine() == 6).findFirst();
    assertTrue(i1.isPresent());
    assertEquals(i1.get().getO2(), 0);

    var i2 = items.stream().filter(it -> it.getO1().getLine() == 7).findFirst();
    assertTrue(i2.isPresent());
    assertEquals(i2.get().getO2(), 1);

    var i3 = items.stream().filter(it -> it.getO1().getLine() == 8).findFirst();
    assertTrue(i3.isPresent());
    assertEquals(i3.get().getO2(), 0);

    var i4 = items.stream().filter(it -> it.getO1().getLine() == 10).findFirst();
    assertTrue(i4.isPresent());
    assertEquals(i4.get().getO2(), 0);

    var i5 = items.stream().filter(it -> it.getO1().getLine() == 11).findFirst();
    assertTrue(i5.isPresent());
    assertEquals(i5.get().getO2(), 0);

    var i6 = items.stream().filter(it -> it.getO1().getLine() == 12).findFirst();
    assertTrue(i6.isPresent());
    assertEquals(i6.get().getO2(), 2);
  }

  @BeforeMethod(dependsOnMethods = {"setUp"})
  public void beforeTest04() {
    TypeInfo typeInfo = new TypeInfo("foobar.FooClass", false, false, "Progress.Lang.Object", "");
    typeInfo.addMethod(new MethodElement("method01", false, DataType.VOID, //
        new Parameter(1, "prm1", 0, ParameterMode.INPUT, DataType.CHARACTER)));
    typeInfo.addMethod(new MethodElement("method02", false, DataType.VOID, //
        new Parameter(1, "prm1", 0, ParameterMode.OUTPUT, DataType.CHARACTER)));
    session.injectTypeInfo(typeInfo);
  }

  @Test
  public void test04() {
    var unit = getParseUnit(new File(SRC_DIR + "/test04.cls"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var m1Routine = unit.getRootScope().getRoutines().stream().filter(
        it -> "method01".equals(it.getName())).findAny().get();
    var elem = unit.getTypeInfo().getMethods().stream().filter(it -> m1Routine.getSignature().equals(
        it.getSignature().substring(0, it.getSignature().lastIndexOf(')') + 1))).findFirst().get();
    var m1Block = m1Routine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var m1Visitor = new CognitiveComplexityListener(m1Block, unit.getTypeInfo(), elem);
    m1Visitor.walkStatementBlock(m1Block);
    assertEquals(m1Visitor.getComplexity(), 3);

    var m2Routine = unit.getRootScope().getRoutines().stream().filter(
        it -> "method02".equals(it.getName())).findAny().get();
    var elem2 = unit.getTypeInfo().getMethods().stream().filter(it -> m2Routine.getSignature().equals(
        it.getSignature().substring(0, it.getSignature().lastIndexOf(')') + 1))).findFirst().get();
    var m2Block = m2Routine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var m2Visitor = new CognitiveComplexityListener(m2Block, unit.getTypeInfo(), elem2);
    m2Visitor.walkStatementBlock(m2Block);
    assertEquals(m2Visitor.getComplexity(), 6);
  }

  @Test
  public void test05() {
    var unit = getParseUnit(new File(SRC_DIR + "/test05.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 3);

    var items = visitor.getItems();
    assertEquals(items.size(), 3);

    var i1 = items.stream().filter(it -> it.getO1().getLine() == 6).findFirst();
    assertTrue(i1.isPresent());
    assertEquals(i1.get().getO2(), 0);

    var i2 = items.stream().filter(it -> it.getO1().getLine() == 7).findFirst();
    assertTrue(i2.isPresent());
    assertEquals(i2.get().getO2(), 0);

    var i3 = items.stream().filter(it -> it.getO1().getLine() == 8).findFirst();
    assertTrue(i3.isPresent());
    assertEquals(i3.get().getO2(), 0);
  }

  @Test
  public void test06() {
    var unit = getParseUnit(new File(SRC_DIR + "/test06.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 3);
    assertEquals(visitor.getMainFileComplexity(), 0);
  }

  @Test
  public void test07() {
    var unit = getParseUnit(new File(SRC_DIR + "/test07.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 6);

    var items = visitor.getItems();
    assertEquals(items.size(), 6);

    var i1 = items.stream().filter(it -> it.getO1().getLine() == 3).findFirst();
    assertTrue(i1.isPresent());
    assertEquals(i1.get().getO2(), 0);

    var i2 = items.stream().filter(it -> it.getO1().getLine() == 5).findFirst();
    assertTrue(i2.isPresent());
    assertEquals(i2.get().getO2(), 0);

    var i3 = items.stream().filter(it -> it.getO1().getLine() == 7).findFirst();
    assertTrue(i3.isPresent());
    assertEquals(i3.get().getO2(), 0);

    var i4 = items.stream().filter(it -> it.getO1().getLine() == 10).findFirst();
    assertTrue(i4.isPresent());
    assertEquals(i4.get().getO2(), 0);

    var i5 = items.stream().filter(it -> it.getO1().getLine() == 12).findFirst();
    assertTrue(i5.isPresent());
    assertEquals(i5.get().getO2(), 0);

    var i6 = items.stream().filter(it -> it.getO1().getLine() == 14).findFirst();
    assertTrue(i6.isPresent());
    assertEquals(i6.get().getO2(), 0);
  }

  @Test
  public void test08() {
    var lines = genericTest("test08.p", 3);
    assertEquals(lines, List.of(3, 4, 5));

  }

  @Test
  public void test09() {
    var lines = genericTest("test09.p", 3);
    assertEquals(lines, List.of(1, 2, 3));

  }

  @Test
  public void test10() {
    var lines = genericTest("test10.p", 3);
    assertEquals(lines, List.of(2, 3, 4));

  }

  @Test
  public void test11() {
    var lines = genericTest("test11.p", 5);
    assertEquals(lines, List.of(1, 2, 3, 5, 6));

  }

  @Test
  public void test12() {
    var lines = genericTest("test12.p", 6);
    assertEquals(lines, List.of(5, 6, 6, 7, 8, 10));

  }

  @Test
  public void test13() {
    var lines = genericTest("test13.p", 0);
    assertEquals(lines, List.of());
  }

  @Test
  public void test14() {
    var lines = genericTest("test14.p", 1);
    assertEquals(lines, List.of(1));

  }

  @Test
  public void test15() {
    var lines = genericTest("test15.p", 1);
    assertEquals(lines, List.of(1));
  }

  @Test
  public void test16() {
    var lines = genericTest("test16.p", 5);
    assertEquals(lines, List.of(1, 3, 4, 5, 6));
  }

  private List<Integer> genericTest(String fileName, int complexityToCheck) {
    var unit = getParseUnit(new File(SRC_DIR + "/" + fileName), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), complexityToCheck);
    assertEquals(visitor.getMainFileComplexity(), complexityToCheck);

    return visitor.getItems().stream().map(it -> it.getO1().getLine()).sorted().toList();
  }

}
