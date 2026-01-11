/********************************************************************************
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.proparse.ABLLexer;
import org.prorefactor.proparse.ErrorDetectionListener;
import org.prorefactor.proparse.ProparseErrorStrategy;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.io.ByteSource;
import com.progress.xref.EmptyCrossReference;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

/**
 * Test how errors are detected with invalid source code. Useful in code completion context
 */
public class ErrorRangeTest extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test01() throws IOException {
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test01.p", 3, "dynamic-function ('foobar', ).");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    String t0 = listener.getErrCode().get(0);
    assertEquals(t0, "dynamic-function ('foobar', )");
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 11);
    assertEquals(i0.b, 17);
  }

  @Test
  public void test02() throws IOException {
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test02.cls", 8, "dynamic-function ('foobar', ).");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    String t0 = listener.getErrCode().get(0);
    assertEquals(t0, "dynamic-function ('foobar', )");
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 37);
    assertEquals(i0.b, 43);
  }

  @Test
  public void test03() throws IOException {
    // Error on last line
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test01.p", 4, "dynamic-function('foobar2', )");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    String t0 = listener.getErrCode().get(0);
    assertEquals(t0, "dynamic-function('foobar2', )");
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 14);
    assertEquals(i0.b, 19);
  }

  @Test
  public void test04() throws IOException {
    // Just a missing period at the end
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test01.p", 4, "message x1");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 18);
    assertEquals(i0.b, 18);
    // TODO Interval currently reported in only EOF, while it should be the last statement which is not complete
  }

  @Test
  public void test05() throws IOException {
    // Add m1() line, the end statement is also included in the error range... Shouldn't be there
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test02.cls", 9, "  m1 ( )");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    String t0 = listener.getErrCode().get(0);
    assertEquals(t0, "m1 ( )\n end");
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 40);
    assertEquals(i0.b, 46);
  }

  @Test
  public void test05bis() throws IOException {
    // Add m1() line and a valid statement, error range contains the first keyword of the next statement
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test02.cls", 9, "  m1 ( )", "dynamic-function('plop', 1, 2, 3).");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    String t0 = listener.getErrCode().get(0);
    assertEquals(t0, "m1 ( )\ndynamic-function");
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 40);
    assertEquals(i0.b, 46);
  }

  @Test
  public void test06() throws IOException {
    // Compared to test05, adding a parameter changes the error range. Weird 
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test02.cls", 9, "  m1 ( 'foobar', )");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    String t0 = listener.getErrCode().get(0);
    assertEquals(t0, "m1 ( 'foobar', )");
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 40);
    assertEquals(i0.b, 47);
  }

  @Test
  public void test07() throws IOException {
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test02.cls", 9, "  assign this-object:m1().");
    assertEquals(listener.getErrors().size(), 1);
    assertEquals(listener.getErrCode().size(), 1);
    String t0 = listener.getErrCode().get(0);
    assertEquals(t0, "this-object:m1()");
    Interval i0 = listener.getErrors().get(0);
    assertEquals(i0.a, 42);
    assertEquals(i0.b, 46);
  }

  private ErrorDetectionListener genericTest(String filename, int lineNumber, String... lines2) throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get(filename))) {
      String code = injectCode(filename, lineNumber, lines2);
      ABLLexer lexer = new ABLLexer(session, StandardCharsets.UTF_8, ByteSource.wrap(code.getBytes()), filename, false);
      CommonTokenStream tokStream = new CommonTokenStream(lexer);
      Proparse parser = new Proparse(tokStream);
      parser.initialize(session, new EmptyCrossReference(), true);
      parser.setErrorHandler(new ProparseErrorStrategy(false, false, false));
      parser.getInterpreter().setPredictionMode(PredictionMode.LL);

      ErrorDetectionListener errLsnr = new ErrorDetectionListener();
      parser.addErrorListener(errLsnr);
      try {
        parser.program();
        throw new RuntimeException("Code is supposed to fail...");
      } catch (ParseCancellationException uncaught) {
        // Source code is supposed to contain error
      }

      return errLsnr;
    }
  }

  /**
   * Return specified source file with additional lines
   */
  private static String injectCode(String filename, int lineNumber, String... lines2) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder();
    for (int zz = 0; zz < lines.size(); zz++) {
      if (zz == lineNumber - 1) {
        for (String str : lines2) {
          sb.append(str).append('\n');
        }
      }
      sb.append(lines.get(zz)).append('\n');
    }
    if (lines.size() == lineNumber - 1) {
      for (String str : lines2) {
        sb.append(str).append('\n');
      }
    }

    return sb.toString();
  }

}
