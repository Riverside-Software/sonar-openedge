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

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.unittest.util.RoutineHandler;
import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser01.TP01Support;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/** Tests for Calls and Routines in the tree parser. */
public class TP01ProcessActionTest {
  private TP01Support symbolAction;
  private RefactorSession session; // = RefactorSession.getInstance();

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestSports2000Module());
    session = injector.getInstance(RefactorSession.class);
    symbolAction = new TP01Support(session);
  }

  /**
   * Parse compile-file.p and verify that all calls are registered correctly, for each scope.
   * 
   * @throws RefactorException
   */
  @Test
  public void testCompileFileCalls() throws Exception {
    File file = new File("src/test/resources/data/tp01ProcessTests/compile-file.p");
    String externalName = file.getName();

    ParseUnit pu = new ParseUnit(file, session);
    pu.treeParser01(symbolAction);

    // Define routine handlers for expected routines.
    RoutineHandler enableUi = new RoutineHandler("enable-ui", symbolAction);
    RoutineHandler userAction = new RoutineHandler("user-action", symbolAction);
    RoutineHandler disableUi = new RoutineHandler("disable-ui", symbolAction);
    RoutineHandler setState = new RoutineHandler("setState", symbolAction);
    RoutineHandler getCompileList = new RoutineHandler("get-compile-list", symbolAction);

    // Define call objects for expected calls.
    Call enableUiCall = new Call(externalName, enableUi.getName());
    Call userActionCall = new Call(externalName, userAction.getName());
    Call disableUiCall = new Call(externalName, disableUi.getName());
    Call setStateCall = new Call(externalName, setState.getName());
    Call getCompileListCall = new Call(externalName, getCompileList.getName());

    // Create expected result set for root scope: enable-ui, user-action, disable-ui.
    ArrayList<Call> expectedRootCalls = new ArrayList<Call>();
    expectedRootCalls.add(disableUiCall);
    expectedRootCalls.add(enableUiCall);
    expectedRootCalls.add(userActionCall);

    // Get actual calls found in code and test against expected.
    List<Call> actualRootCalls = pu.getRootScope().getCallList();
    assertTrue(actualRootCalls.containsAll(expectedRootCalls));
    assertTrue(!actualRootCalls.contains(setStateCall));
    assertTrue(!actualRootCalls.contains(getCompileListCall));

    // Internal proc enable-ui calls: setState.
    List<Call> actualEnableUiCalls = enableUi.getRoutineScope().getCallList();
    assertTrue(actualEnableUiCalls.contains(setStateCall));

    // Internal proc user-action calls: get-compile-list.
    List<Call> actualUserActionCalls = userAction.getRoutineScope().getCallList();
    assertTrue(actualUserActionCalls.contains(getCompileListCall));

    // Internal proc get-compile-list calls: setState x 3.
    List<Call> actualGetCompileListCalls = getCompileList.getRoutineScope().getCallList();
    assertTrue(actualGetCompileListCalls.contains(setStateCall));

  }

  /**
   * Parse persistent-run.p and verify that: a) run <proc1> persistent set <h> results in the handle variable being
   * updated. b) run <proc2> in <h> is registered as a call to proc1.proc2.
   */
  @Test
  public void testPersistenProc() throws Exception {
    File file = new File("src/test/resources/data/tp01ProcessTests/persistent-run.p");
    String externalName = file.getName();

    ParseUnit pu = new ParseUnit(file, session);
    pu.treeParser01(symbolAction);

    // Define routines.
    RoutineHandler test01 = new RoutineHandler("test_01", symbolAction);
    RoutineHandler test02 = new RoutineHandler("test_02", symbolAction);

    // Define calls.
    String targetProc = "persistent-proc.p";
    Call persistentProcCall = new Call(targetProc, null);
    Call test01InHandleCall = new Call(targetProc, test01.getName());
    Call test02InHandleCall = new Call(targetProc, test02.getName());
    Call test01InternalCall = new Call(externalName, test01.getName());

    // Expected root procedure calls.
    ArrayList<Call> expectedRootCalls = new ArrayList<Call>();
    expectedRootCalls.add(persistentProcCall);
    expectedRootCalls.add(test01InHandleCall);
    expectedRootCalls.add(test01InternalCall);

    // Expected calls in procedure test_01
    List<Call> expectedTest01Calls = new ArrayList<Call>();
    expectedTest01Calls.add(test02InHandleCall);

    // Test actual root calls agains expected root calls.
    List<Call> actualRootCalls = pu.getRootScope().getCallList();
    assertTrue(actualRootCalls.containsAll(expectedRootCalls));
    assertTrue(!actualRootCalls.contains(test02InHandleCall));

    // Test actual calls in test_01 against expected calls.
    List<Call> actualTest01Calls = test01.getRoutineScope().getCallList();
    assertTrue(actualTest01Calls.containsAll(expectedTest01Calls));
  }

}
