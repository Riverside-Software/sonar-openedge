/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
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
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
public class C3Test {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() throws IOException {
    genericTest("src/test/resources/data/c3/TestClass01.cls", false);
  }

  @Test(expectedExceptions = {ParseCancellationException.class})
  public void test02() throws IOException {
    genericTest("src/test/resources/data/c3/TestClass01.cls", true);
  }

  @Test
  public void test03() throws IOException {
    genericTest("src/test/resources/data/c3/TestClass02.cls", false);
  }

  @Test
  public void test04() throws IOException {
    genericTest("src/test/resources/data/c3/TestClass02.cls", true);
  }

  private void genericTest(String filename, boolean c3) throws IOException {
    try (InputStream input = Files.newInputStream(Paths.get(filename))) {
      ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(ByteStreams.toByteArray(input)), filename, false);
      CommonTokenStream tokStream = new CommonTokenStream(lexer);
      Proparse parser = new Proparse(tokStream);
      if (c3)
        parser.initialize(session, new EmptyCrossReference(), true);
      else
        parser.initAntlr4(session, new EmptyCrossReference());
      parser.setErrorHandler(new ProparseErrorStrategy(false, false, false));
      parser.getInterpreter().setPredictionMode(PredictionMode.LL);
      parser.program();
    }
  }
}
