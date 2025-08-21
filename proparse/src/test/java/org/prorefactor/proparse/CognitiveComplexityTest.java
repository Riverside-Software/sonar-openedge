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
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.prorefactor.core.Pair;
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
    var positions = visitor.getItems().stream().map(
        it -> Pair.of(it.getO1().getLine(), it.getO1().getColumn())).toList();
    var expectedPositions = List.of(Pair.of(6, 6), Pair.of(8, 0), Pair.of(11, 0), Pair.of(12, 8), Pair.of(14, 2));
    assertEqualsNoOrder(positions, expectedPositions);

    var p1Routine = unit.getRootScope().getRoutines().stream().filter(it -> "p1".equals(it.getName())).findAny().get();
    var p1Block = p1Routine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var p1Visitor = new CognitiveComplexityListener(p1Block);
    p1Visitor.walkStatementBlock(p1Block);
    assertEquals(p1Visitor.getComplexity(), 5);
    assertEquals(p1Visitor.getMainFileComplexity(), 5);
    var positions2 = p1Visitor.getItems().stream().map(
        it -> Pair.of(it.getO1().getLine(), it.getO1().getColumn())).toList();
    var expectedPositions2 = List.of(Pair.of(22, 8), Pair.of(24, 2), Pair.of(26, 2), Pair.of(26, 14), Pair.of(28, 2));
    assertEqualsNoOrder(positions2, expectedPositions2);
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
    var unit = getParseUnit(new File(SRC_DIR + "/test08.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 3);
    assertEquals(visitor.getMainFileComplexity(), 3);

    var items = visitor.getItems();
    var lines = items.stream().map(it -> it.getO1().getLine()).sorted().toList();
    assertEquals(lines, List.of(8, 11, 12));
  }

  @Test
  public void test09() {
    var unit = getParseUnit(new File(SRC_DIR + "/test09.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 8);
    assertEquals(visitor.getMainFileComplexity(), 8);

    var positions = visitor.getItems().stream().map(
        it -> Pair.of(it.getO1().getLine(), it.getO1().getColumn())).toList();
    var expectedPositions = List.of(Pair.of(11, 29), Pair.of(15, 52), Pair.of(15, 29));
    assertEqualsNoOrder(positions, expectedPositions);
  }

  @Test
  public void test10() {
    var unit = getParseUnit(new File(SRC_DIR + "/test10.p"), session);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    var mainRoutine = unit.getRootScope().getRoutine();
    var mainBlock = mainRoutine.getRoutineScope().getRootBlock().getNode().asIStatementBlock();
    var visitor = new CognitiveComplexityListener(mainBlock);
    visitor.walkStatementBlock(mainBlock);
    assertEquals(visitor.getComplexity(), 5);
    assertEquals(visitor.getMainFileComplexity(), 5);

    var positions = visitor.getItems().stream().map(
        it -> Pair.of(it.getO1().getLine(), it.getO1().getColumn())).toList();
    var expectedPositions = List.of(Pair.of(11, 0), Pair.of(11, 9), Pair.of(11, 16), Pair.of(11, 23), Pair.of(15, 0));
    assertEqualsNoOrder(positions, expectedPositions);
  }
}
