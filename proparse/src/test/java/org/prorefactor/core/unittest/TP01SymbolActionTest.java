/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.prorefactor.core.unittest.util.RoutineHandler;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser01.TP01Support;
import org.prorefactor.treeparser01.TreeParser01;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Tests for symbol parse action (TP01Support).
 *
 */
public class TP01SymbolActionTest {
  private TP01Support walkAction;
  private TreeParser01 walker;
  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);

    walkAction = new TP01Support(session);
    walker = new TreeParser01();
    walker.setActionObject(walkAction);
  }

  /**
   * Parse compile-file.p and verify that all symbols are extracted correctly.
   */
  @Test
  public void testCompileFileRoutines() throws Exception {
    ParseUnit pu = new ParseUnit(new File("src/test/resources/data/tp01ProcessTests/compile-file.p"), session);
    pu.treeParser(walker);

    // Create expected symbols.
    RoutineHandler enableUi = new RoutineHandler("enable-ui", walkAction);
    RoutineHandler userAction = new RoutineHandler("user-action", walkAction);
    RoutineHandler disableUi = new RoutineHandler("disable-ui", walkAction);
    RoutineHandler setState = new RoutineHandler("setState", walkAction);
    RoutineHandler getCompileList = new RoutineHandler("get-compile-list", walkAction);

    // Routines expected in root scope.
    assertTrue(rootScope().hasRoutine(enableUi.getName()));
    assertTrue(rootScope().hasRoutine(userAction.getName()));
    assertTrue(rootScope().hasRoutine(disableUi.getName()));
    assertTrue(rootScope().hasRoutine(setState.getName()));
    assertTrue(rootScope().hasRoutine(getCompileList.getName()));
  }

  /**
   * Parse compile-file.p and verify that all symbols are extracted correctly.
   */
  @Test
  public void testCompileFileVars() throws Exception {
    ParseUnit pu = new ParseUnit(new File("src/test/resources/data/tp01ProcessTests/compile-file.p"), session);
    pu.treeParser(walker);

    // Create expected symbols.
    String sourcePath = "sourcePath";
    String currentPropath = "currentPropath";
    String compileFile = "compileFile";
    String currentStatus = "currentStatus";
    String test = "test";
    String aFile = "aFile";
    String aNewFile = "aNewFile";
    String aNewSrcDir = "aNewSrcDir";
    RoutineHandler getCompileList = new RoutineHandler("get-compile-list", walkAction);

    // Variables expected in root scope.
    assertNotNull(rootScope().lookupVariable(sourcePath));
    assertFalse(rootScope().lookupVariable(sourcePath).isParameter());
    assertNotNull(rootScope().lookupVariable(currentPropath));
    assertFalse(rootScope().lookupVariable(currentPropath).isParameter());
    assertNotNull(rootScope().lookupVariable(compileFile));
    assertFalse(rootScope().lookupVariable(compileFile).isParameter());
    assertNotNull(rootScope().lookupVariable(currentStatus));
    assertFalse(rootScope().lookupVariable(currentStatus).isParameter());
    assertNotNull(rootScope().lookupVariable(test));
    assertFalse(rootScope().lookupVariable(test).isParameter());

    // Variables not expected in root scope.
    assertNull(rootScope().lookupVariable(aFile));
    assertNull(rootScope().lookupVariable(aNewFile));
    assertNull(rootScope().lookupVariable(aNewSrcDir));

    // Get get-compile-list scope.
    TreeParserSymbolScope routineScope = getCompileList.getRoutineScope();

    // Variables expected in get-compile-list scope.
    assertNotNull(routineScope.lookupVariable(aFile));
    assertFalse(routineScope.lookupVariable(sourcePath).isParameter());
    assertNotNull(routineScope.lookupVariable(aNewFile));
    assertFalse(routineScope.lookupVariable(sourcePath).isParameter());
    assertNotNull(routineScope.lookupVariable(aNewSrcDir));
    assertFalse(routineScope.lookupVariable(sourcePath).isParameter());
    // Parameters expected in get-compile-list scope
    assertNotNull(routineScope.lookupVariable("pSourcePath"));
    assertTrue(routineScope.lookupVariable("pSourcePath").isParameter());

    // Variables and parameters from the 'foo' function scope
    RoutineHandler fooList = new RoutineHandler("foo", walkAction);
    TreeParserSymbolScope fooScope = fooList.getRoutineScope();
    assertNotNull(fooScope.lookupVariable("yy"));
    assertTrue(fooScope.lookupVariable("yy").isParameter());
    assertNotNull(fooScope.lookupVariable("abc"));
    assertFalse(fooScope.lookupVariable("abc").isParameter());

    // Variables visible from the open scope.
    assertNotNull(routineScope.lookupVariable(sourcePath));
    assertNotNull(routineScope.lookupVariable(currentPropath));
    assertNotNull(routineScope.lookupVariable(compileFile));
    assertNotNull(routineScope.lookupVariable(currentStatus));
    assertNotNull(routineScope.lookupVariable(test));
  }

  private TreeParserSymbolScope rootScope() {
    return walkAction.getRootScope();
  }

}
