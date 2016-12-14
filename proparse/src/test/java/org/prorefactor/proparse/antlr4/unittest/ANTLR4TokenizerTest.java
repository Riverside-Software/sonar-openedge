/*******************************************************************************
 * Copyright (c) 2016 Gilles QUERRET
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles QUERRET - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4.unittest;

import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.antlr4.DoParse;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;
import antlr.TokenStream;

/**
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
public class ANTLR4TokenizerTest {
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";
  private final static String TEMP_DIR = "target/nodes-lister/data/bugsfixed";

  private RefactorSession session;
  private File tempDir = new File(TEMP_DIR);

  @BeforeTest
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().createAlias("foo", "sports2000");

    tempDir.mkdirs();
  }

  @Test
  public void test01() throws Exception {
    genericTest("bug01.p");
  }

  @Test
  public void test02() throws Exception {
    genericTest("bug02.p");
  }

  @Test
  public void test03() throws Exception {
    genericTest("bug03.p");
  }

  @Test
  public void test04() throws Exception {
    genericTest("bug04.p");
  }

  @Test
  public void test05() throws Exception {
    genericTest("bug05.p");
  }

  @Test
  public void test06() throws Exception {
    genericTest("bug06.p");
  }

  @Test
  public void test07() throws Exception {
    genericTest("interface07.cls");
  }

  @Test
  public void test08() throws Exception {
    genericTest("bug08.cls");
  }

  @Test
  public void test09() throws Exception {
    genericTest("bug09.p");
  }

  @Test
  public void test10() throws Exception {
    genericTest("bug10.p");
  }

  @Test
  public void test11() throws Exception {
    genericTest("bug11.p");
  }

  @Test
  public void test12() throws Exception {
    genericTest("bug12.p");
  }

  @Test
  public void test13() throws Exception {
    genericTest("bug13.p");
  }

  @Test
  public void test14() throws Exception {
    genericTest("bug14.p");
  }

  @Test
  public void test15() throws Exception {
    genericTest("bug15.p");
  }

  @Test
  public void test16() throws Exception {
    genericTest("bug16.p");
  }

  @Test
  public void test17() throws Exception {
    genericTest("bug17.p");
  }

  @Test
  public void test18() throws Exception {
    genericTest("bug18.p");
  }

  @Test
  public void test19() throws Exception {
    genericTest("bug19.p");
  }

  @Test
  public void test20() throws Exception {
    genericTest("bug20.p");
  }

  @Test
  public void test21() throws Exception {
    genericTest("bug21.cls");
  }

  @Test
  public void test22() throws Exception {
    genericTest("bug22.cls");
  }

  @Test
  public void test23() throws Exception {
    genericTest("bug23.cls");
  }

  @Test
  public void test24() throws Exception {
    genericTest("bug24.p");
  }

  @Test
  public void test25() throws Exception {
    genericTest("bug25.p");
  }

  @Test
  public void test26() throws Exception {
    genericTest("bug26.cls");
  }

  @Test
  public void test27() throws Exception {
    genericTest("bug27.cls");
  }

  @Test
  public void test28() throws Exception {
    genericTest("bug28.cls");
  }

  @Test
  public void test29() throws Exception {
    genericTest("bug29.p");
  }

  @Test
  public void test30() throws Exception {
    genericTest("bug30.p");
  }

  @Test
  public void test31() throws Exception {
    genericTest("bug31.cls");
  }

  @Test
  public void test32() throws Exception {
    genericTest("bug32.p");
  }

  @Test
  public void test33() throws Exception {
    genericTest("bug33.cls");
  }

  private void genericTest(String fileName) throws RefactorException, ANTLRException, IOException {
    executeTokenizerTest(new File(SRC_DIR, fileName));
  }

  private void executeTokenizerTest(File file) throws RefactorException, ANTLRException, IOException {
    // ANTLR2
    ParseUnit pu = new ParseUnit(file, session);
    TokenStream tokenStream = pu.lex();

    // ANTLR4
    DoParse dp = new DoParse(session, file.getAbsolutePath());
    dp.doParse(true, null);
    TokenSource tokenSource = dp.getLexerTokenStream();

    compareTokens(tokenStream, tokenSource);
  }

  /**
   * @return True if both flows of tokens are identical
   */
  private void compareTokens(TokenStream stream, TokenSource source) throws ANTLRException {
    // Compares ANTLR2 token stream to ANTLR4 token source
    antlr.Token tok2 = stream.nextToken();
    Token tok4 = source.nextToken();
    
    int zz = 0;
    while ((tok2 != null) && (tok2.getType() != antlr.Token.EOF_TYPE) && (tok4 != null) && (tok4.getType() != Token.EOF)) {
      if (tok2.getType() != tok4.getType()) {
        fail("Difference at position " + zz + " -- " + tok2.getType() + "/" + tok4.getType() + " -- " + tok2.getText() + "/" +tok4.getText());
      }
      zz++;
      tok2 = stream.nextToken();
      tok4 = source.nextToken();
    }
    if (tok2.getType() != antlr.Token.EOF_TYPE) {
      fail("Still some tokens in ANTLR2 stream - Pos " + zz + " - " + tok2.getType() + " - " + tok2.getText());
    }
    if (tok4.getType() != Token.EOF) {
      fail("Still some tokens in ANTLR4 stream");
    }
  }

}
