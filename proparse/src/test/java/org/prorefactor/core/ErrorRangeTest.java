/********************************************************************************
 * Copyright (c) 2015-2023 Riverside Software
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.proparse.ABLLexer;
import org.prorefactor.proparse.ProparseErrorStrategy;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.progress.xref.EmptyCrossReference;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

/**
 * Test how errors are detected with invalid source code. Useful in code completion context
 */
public class ErrorRangeTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() throws IOException {
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test01.p");
    assertEquals(listener.errors.size(), 1);
    assertEquals(listener.errCode.size(), 1);
    Interval i0 = listener.errors.get(0);
    assertEquals(i0.a, 11);
    assertEquals(i0.b, 17);
    String t0 = listener.errCode.get(0);
    assertEquals(t0, "dynamic-function ('foobar', )");
  }

  @Test
  public void test02() throws IOException {
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test02.cls");
    assertEquals(listener.errors.size(), 1);
    assertEquals(listener.errCode.size(), 1);
    Interval i0 = listener.errors.get(0);
    assertEquals(i0.a, 37);
    assertEquals(i0.b, 43);
    String t0 = listener.errCode.get(0);
    assertEquals(t0, "dynamic-function ('foobar', )");
  }

  @Test
  public void test03() throws IOException {
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test03.p");
    assertEquals(listener.errors.size(), 1);
    assertEquals(listener.errCode.size(), 1);
    Interval i0 = listener.errors.get(0);
    assertEquals(i0.a, 21);
    assertEquals(i0.b, 26);
    String t0 = listener.errCode.get(0);
    assertEquals(t0, "dynamic-function('foobar2', )");
  }

  @Test
  public void test04() throws IOException {
    ErrorDetectionListener listener = genericTest("src/test/resources/data/errors/test04.p");
    assertEquals(listener.errors.size(), 1);
    assertEquals(listener.errCode.size(), 1);
    Interval i0 = listener.errors.get(0);
    assertEquals(i0.a, 25);
    assertEquals(i0.b, 25);
    // TODO Interval currently reported in only EOF, while it should be the last statement which is not complete
  }

  private ErrorDetectionListener genericTest(String filename) throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get(filename))) {
      ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(ByteStreams.toByteArray(input)), filename, false);
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

  private static class ErrorDetectionListener extends BaseErrorListener {
    private final List<Interval> errors = new ArrayList<>();
    private final List<String> errCode = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
        String msg, RecognitionException caught) {
      Proparse proparse = (Proparse) recognizer;

      if (caught instanceof NoViableAltException) {
        NoViableAltException nvae = (NoViableAltException) caught;
        int startIndex = nvae.getStartToken().getTokenIndex();
        int endIndex = nvae.getOffendingToken().getTokenIndex();
        errors.add(new Interval(startIndex, endIndex));
        errCode.add(proparse.getTokenStream().getText(new Interval(startIndex, endIndex)));
      }
    }
  }

}
