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
package org.prorefactor.core.session;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestBackslashProparseSettings;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpSysTest extends AbstractProparseTest {
  private final static boolean IS_WINDOWS = System.getenv("windir") != null;
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";

  private ParseUnit createParseUnit(RefactorSession session, String fileName) {
    return getParseUnit(new File(SRC_DIR, fileName), session);
  }

  @Test
  public void testBackslashNoEscape() throws IOException {
    // Backslash not considered an escape character on Windows, so it has to fail on Windows
    // UNIX test not executed
    if (!IS_WINDOWS)
      return;
    RefactorSession session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ParseUnit pu = createParseUnit(session, "escape_char.p");
    try {
      pu.parse();
      Assert.fail("Should have failed");
    } catch (ProparseRuntimeException caught) {

    }
  }

  @Test
  public void testBackslashEscape() throws IOException {
    // Backslash considered an escape character on Windows, so it shouldn't fail on both Windows and Unix
    RefactorSession session = new RefactorSession(new UnitTestBackslashProparseSettings(), new SportsSchema());
    ParseUnit pu = createParseUnit(session, "escape_char.p");
    pu.parse();
  }

  @Test
  public void testBackslashInIncludeWindows() throws IOException {
    // Backslash considered an escape character on Windows, so include file will fail
    if (!IS_WINDOWS)
      return;

    RefactorSession session = new RefactorSession(new UnitTestBackslashProparseSettings(), new SportsSchema());
    ParseUnit pu = createParseUnit(session, "escape_char2.p");
    try {
      pu.parse();
      Assert.fail("Should have failed");
    } catch (UncheckedIOException caught) {

    }
  }

  @Test
  public void test2BackslashInIncludeWindows() throws IOException {
    // Backslash not considered an escape character on Windows, so include file is OK (standard behavior)
    if (!IS_WINDOWS)
      return;

    RefactorSession session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ParseUnit pu = createParseUnit(session, "escape_char2.p");
    pu.parse();
  }

  @Test
  public void testBackslashInIncludeLinux() throws IOException {
    // Always fail on Linux
    if (IS_WINDOWS)
      return;

    RefactorSession session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ParseUnit pu = createParseUnit(session, "escape_char2.p");
    try {
      pu.parse();
      Assert.fail("Should have failed");
    } catch (UncheckedIOException caught) {

    }
  }

}
