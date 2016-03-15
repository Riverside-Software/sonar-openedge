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

import org.prorefactor.core.unittest.util.RoutineHandler;
import org.prorefactor.core.unittest.util.UnitTestSportsModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.SymbolScope;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser01.TP01Support;
import org.prorefactor.treeparser01.TreeParser01;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

import java.io.File;

/**
 * Tests for symbol parse action (TP01Support).
 *
 */
public class TP01SymbolActionTest extends TestCase {
  private TP01Support walkAction;
  private TreeParser01 walker;
  private RefactorSession session;

  @Override
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestSportsModule());
    session = injector.getInstance(RefactorSession.class);

    walkAction = new TP01Support(session);
    walker = new TreeParser01();
    walker.setActionObject(walkAction);
  }

  /**
   * Parse compile-file.p and verify that all symbols are extracted correctly.
   */
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
    assertTrue(rootScope().lookupVariable(sourcePath) != null);
    assertTrue(rootScope().lookupVariable(currentPropath) != null);
    assertTrue(rootScope().lookupVariable(compileFile) != null);
    assertTrue(rootScope().lookupVariable(currentStatus) != null);
    assertTrue(rootScope().lookupVariable(test) != null);

    // Variables not expected in root scope.
    assertTrue(rootScope().lookupVariable(aFile) == null);
    assertTrue(rootScope().lookupVariable(aNewFile) == null);
    assertTrue(rootScope().lookupVariable(aNewSrcDir) == null);

    // Get get-compile-list scope.
    SymbolScope routineScope = getCompileList.getRoutineScope();

    // Variables expected in get-compile-list scope.
    assertTrue(routineScope.lookupVariable(aFile) != null);
    assertTrue(routineScope.lookupVariable(aNewFile) != null);
    assertTrue(routineScope.lookupVariable(aNewSrcDir) != null);

    // Variables visible from the open scope.
    assertTrue(routineScope.lookupVariable(sourcePath) != null);
    assertTrue(routineScope.lookupVariable(currentPropath) != null);
    assertTrue(routineScope.lookupVariable(compileFile) != null);
    assertTrue(routineScope.lookupVariable(currentStatus) != null);
    assertTrue(routineScope.lookupVariable(test) != null);

  }

  private SymbolScope rootScope() {
    return walkAction.getRootScope();
  }

}
