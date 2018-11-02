/********************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.core.unittest;

import java.io.File;
import java.io.UncheckedIOException;

import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.unittest.util.UnitTestBackslashModule;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class OpSysTest {
  private final static boolean IS_WINDOWS = (System.getenv("windir") != null);
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";

  @Test
  public void testBackslashNoEscape() {
    // Backslash not considered an escape character on Windows, so it has to fail on Windows
    // UNIX test not executed
    if (!IS_WINDOWS)
      return;
    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char.p"), session);
    try {
      pu.parse();
      Assert.fail("Should have failed");
    } catch (ProparseRuntimeException caught) {

    }
  }

  @Test
  public void testBackslashEscape() {
    // Backslash considered an escape character on Windows, so it shouldn't fail on both Windows and Unix
    Injector injector = Guice.createInjector(new UnitTestBackslashModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char.p"), session);
    pu.parse();
  }

  @Test
  public void testBackslashInIncludeWindows() {
    // Backslash considered an escape character on Windows, so include file will fail
    if (!IS_WINDOWS)
      return;

    Injector injector = Guice.createInjector(new UnitTestBackslashModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char2.p"), session);
    try {
      pu.parse();
      Assert.fail("Should have failed");
    } catch (UncheckedIOException caught) {

    }
  }

  @Test
  public void test2BackslashInIncludeWindows() {
    // Backslash not considered an escape character on Windows, so include file is OK (standard behavior)
    if (!IS_WINDOWS)
      return;

    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char2.p"), session);
    pu.parse();
  }

  @Test
  public void testBackslashInIncludeLinux() {
    // Always fail on Linux
    if (IS_WINDOWS)
      return;

    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char2.p"), session);
    try {
      pu.parse();
      Assert.fail("Should have failed");
    } catch (UncheckedIOException caught) {

    }
  }

}
