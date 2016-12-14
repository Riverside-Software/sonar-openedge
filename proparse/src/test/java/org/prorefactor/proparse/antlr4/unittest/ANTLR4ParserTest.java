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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.ProParser;
import org.prorefactor.proparse.antlr4.ProgressLexer;
import org.prorefactor.refactor.RefactorSession;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

/**
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
public class ANTLR4ParserTest {
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";
  private final static String TEMP_DIR = "target/nodes-lister/data/bugsfixed";

  private RefactorSession session;
  private File tempDir = new File(TEMP_DIR);

  @BeforeSuite
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().createAlias("foo", "sports2000");

    tempDir.mkdirs();
  }

  @Test(enabled=false)
  public void test00() throws Exception {
    // Only in order to initialize Proparse class
    try {
      ProgressLexer lex = new ProgressLexer(session, new File(SRC_DIR, "bug01.p").getAbsolutePath());
      // Proparse parser = new Proparse(new CommonTokenStream(dp));
      // parser.program();
    } catch (Throwable uncaught) {
      
    }
  }

  @Test(enabled=false)
  public void test01() throws Exception {
    genericTest("bug01.p");
  }

  @Test(enabled=false)
  public void test02() throws Exception {
    genericTest("bug02.p");
  }

  @Test(enabled=false)
  public void test03() throws Exception {
    genericTest("bug03.p");
  }

  @Test(enabled=false)
  public void test04() throws Exception {
    genericTest("bug04.p");
  }

  @Test(enabled=false)
  public void test05() throws Exception {
    genericTest("bug05.p");
  }

  @Test(enabled=false)
  public void test06() throws Exception {
    genericTest("bug06.p");
  }

  @Test(enabled=false)
  public void test07() throws Exception {
    genericTest("interface07.cls");
  }

  @Test(enabled=false)
  public void test08() throws Exception {
    genericTest("bug08.cls");
  }

  @Test(enabled=false)
  public void test09() throws Exception {
    genericTest("bug09.p");
  }

  @Test(enabled=false)
  public void test10() throws Exception {
    genericTest("bug10.p");
  }

  @Test(enabled=false)
  public void test11() throws Exception {
    genericTest("bug11.p");
  }

  @Test(enabled=false)
  public void test12() throws Exception {
    genericTest("bug12.p");
  }

  @Test(enabled=false)
  public void test13() throws Exception {
    genericTest("bug13.p");
  }

  @Test(enabled=false)
  public void test14() throws Exception {
    genericTest("bug14.p");
  }

  @Test(enabled=false)
  public void test15() throws Exception {
    genericTest("bug15.p");
  }

  @Test(enabled=false)
  public void test16() throws Exception {
    genericTest("bug16.p");
  }

  @Test(enabled=false)
  public void test17() throws Exception {
    genericTest("bug17.p");
  }

  @Test(enabled=false)
  public void test18() throws Exception {
    genericTest("bug18.p");
  }

  @Test(enabled=false)
  public void test19() throws Exception {
    genericTest("bug19.p");
  }

  @Test(enabled=false)
  public void test20() throws Exception {
    genericTest("bug20.p");
  }

  @Test(enabled=false)
  public void test21() throws Exception {
    genericTest("bug21.cls");
  }

  @Test(enabled=false)
  public void test22() throws Exception {
    genericTest("bug22.cls");
  }

  @Test(enabled=false)
  public void test23() throws Exception {
    genericTest("bug23.cls");
  }

  @Test(enabled=false)
  public void test24() throws Exception {
    genericTest("bug24.p");
  }

  @Test(enabled=false)
  public void test25() throws Exception {
    genericTest("bug25.p");
  }

  @Test(enabled=false)
  public void test26() throws Exception {
    genericTest("bug26.cls");
  }

  @Test(enabled=false)
  public void test27() throws Exception {
    genericTest("bug27.cls");
  }

  @Test(enabled=false)
  public void test28() throws Exception {
    genericTest("bug28.cls");
  }

  @Test(enabled=false)
  public void test29() throws Exception {
    genericTest("bug29.p");
  }

  @Test(enabled=false)
  public void test30() throws Exception {
    genericTest("bug30.p");
  }

  @Test(enabled=false)
  public void test31() throws Exception {
    genericTest("bug31.cls");
  }

  @Test(enabled=false)
  public void test32() throws Exception {
    genericTest("bug32.p");
  }

  @Test(enabled=false)
  public void test33() throws Exception {
    genericTest("bug33.cls");
  }

  // Next two tests : same exception should be thrown in both cases
  @Test(enabled=false, expectedExceptions = {ProparseRuntimeException.class})
  public void testCache1() throws Exception {
    genericTest("CacheChild.cls");
  }

  @Test(enabled=false, expectedExceptions = {ProparseRuntimeException.class})
  public void testCache2() throws Exception {
    genericTest("CacheChild.cls");
  }

  @Test(enabled=false)
  public void testSaxWriter() throws Exception {
    genericTest("sax-writer.p");
  }

  private void genericTest(String fileName) throws ANTLRException, IOException {
//    executeTokenizerTest(new File(SRC_DIR, fileName));
    executeAntlr2Test(new File(SRC_DIR, fileName));
  }

  private void executeTokenizerTest(File file) throws ANTLRException, IOException {
    // ProgressLexer dp = new ProgressLexer(session, file.getAbsolutePath());
    // Proparse parser = new Proparse(new CommonTokenStream(dp));
    // ParseTree tree = parser.program();
    // Assert.assertNotNull(tree);
  }

  private void executeAntlr2Test(File file) throws ANTLRException, IOException {
    ProgressLexer dp = new ProgressLexer(session, file.getAbsolutePath());
    ProParser parser = new ProParser(dp.getANTLR2TokenStream(true));
    parser.initAntlr4(session, dp.getFilenameList());
    parser.program();
    Assert.assertNotNull(parser.getAST());
  }

  /** Print out a whole tree in LISP form. {@link #getNodeText} is used on the
   *  node payloads to get the text for the nodes.  Detect
   *  parse trees and extract data appropriately.
   */
  public static String toStringTree(Tree t, Parser recog) {
    String[] ruleNames = recog != null ? recog.getRuleNames() : null;
    List<String> ruleNamesList = ruleNames != null ? Arrays.asList(ruleNames) : null;
    return toStringTree(t, ruleNamesList);
  }

  /** Print out a whole tree in LISP form. {@link #getNodeText} is used on the
   *  node payloads to get the text for the nodes.
   */
  public static String toStringTree(final Tree t, final List<String> ruleNames) {
    String s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false);
    if ( t.getChildCount()==0 ) return s;
    StringBuilder buf = new StringBuilder();
    buf.append("(");
    s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false);
    buf.append(s);
    buf.append(' ');
    for (int i = 0; i<t.getChildCount(); i++) {
      if ( i>0 ) buf.append('\n');
      buf.append(toStringTree(t.getChild(i), ruleNames));
    }
    buf.append(")");
    return buf.toString();
  }
  
  public static String getNodeText(Tree t, List<String> ruleNames) {
    if ( ruleNames!=null ) {
      if ( t instanceof RuleNode ) {
        int ruleIndex = ((RuleNode)t).getRuleContext().getRuleIndex();
        String ruleName = ruleNames.get(ruleIndex);
        return ruleName;
      }
      else if ( t instanceof ErrorNode) {
        return t.toString();
      }
      else if ( t instanceof TerminalNode) {
        Token symbol = ((TerminalNode)t).getSymbol();
        if (symbol != null) {
          String s = symbol.getText();
          return s;
        }
      }
    }
    // no recog for rule names
    Object payload = t.getPayload();
    if ( payload instanceof Token ) {
      return ((Token)payload).getText();
    }
    return t.getPayload().toString();
  }

}
