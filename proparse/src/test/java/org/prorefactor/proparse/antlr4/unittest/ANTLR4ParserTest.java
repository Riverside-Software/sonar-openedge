/*******************************************************************************
 * Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4.unittest;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ParseInfo;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.TreeNodeLister;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.ParserSupport;
import org.prorefactor.proparse.ProParser;
import org.prorefactor.proparse.antlr4.DescriptiveErrorListener;
import org.prorefactor.proparse.antlr4.JPNode;
import org.prorefactor.proparse.antlr4.JPNodeVisitor;
import org.prorefactor.proparse.antlr4.ProgressLexer;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.ParseUnit;
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
  private final static String SRC_DIR = "src/test/resources";
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

  static {
    long start = System.currentTimeMillis();
    System.out.println("Loading class Proparse - " + Proparse._ATN.getNumberOfDecisions() + " decisions in ATN");
    System.out.println("  ==> " + (System.currentTimeMillis() - start) + " ms");
    start = System.currentTimeMillis();

    // Sample schema
    RefactorSession session = new RefactorSession(new ProparseSettings(""), new Schema());
    String sampleClass = "class SampleClass inherits Progress.Lang.Object: method public void foo(): end method. end class.";
    ProgressLexer lexer = new ProgressLexer(session, new StringBufferInputStream(sampleClass), "SampleClass.cls", false);
    lexer.setMergeNameDotInId(true);
    Proparse parser = new Proparse(new CommonTokenStream(lexer));
    parser.initAntlr4(session, lexer.getFilenameList());
    ParseTree tree = parser.program();
    System.out.println("Sample class parsed - " + tree.getChildCount() + " children");
    System.out.println("  ==> " + (System.currentTimeMillis() - start) + " ms");

  }
  
  @Test(enabled=true)
  public void test00() throws Exception {
    // Only in order to initialize Proparse class
    try {
      ParseUnit unit = new ParseUnit(new File(SRC_DIR, "data/bugsfixed/bug01.p"), session);
      unit.lex4().nextToken();
    } catch (Throwable uncaught) {
      
    }
  }

  @Test(enabled=false)
  public void testOpenEdgeClasses() throws Exception {
    Files.walk(new File("src/test/resources/OpenEdge").toPath()).filter(
        p -> p.toFile().isFile() && p.getFileName().toString().endsWith(".cls")).map(
            p -> new File("src\\test\\resources").toPath().relativize(p)).forEach(path -> genericTest(path.toString()));
  }

  @Test(enabled=false)
  public void testRiversideClasses() throws Exception {
    Files.walk(new File("src/test/resources/rssw").toPath()).filter(
        p -> p.toFile().isFile() && p.getFileName().toString().endsWith(".cls")).map(
            p -> new File("src\\test\\resources").toPath().relativize(p)).forEach(path -> genericTest(path.toString()));
  }

  @Test(enabled=true)
  public void test01() throws Exception {
    genericTest("data/bugsfixed/bug01.p");
  }

  @Test(enabled=true)
  public void test02() throws Exception {
    genericTest("data/bugsfixed/bug02.p");
  }

  @Test(enabled=true)
  public void test03() throws Exception {
    genericTest("data/bugsfixed/bug03.p");
  }

  @Test(enabled=true)
  public void test04() throws Exception {
    genericTest("data/bugsfixed/bug04.p");
  }

  @Test(enabled=true)
  public void test05() throws Exception {
    genericTest("data/bugsfixed/bug05.p");
  }

  @Test(enabled=true)
  public void test06() throws Exception {
    genericTest("data/bugsfixed/bug06.p");
  }

  @Test(enabled=true)
  public void test07() throws Exception {
    genericTest("data/bugsfixed/interface07.cls");
  }

  @Test(enabled=true)
  public void test08() throws Exception {
    genericTest("data/bugsfixed/bug08.cls");
  }

  @Test(enabled=true)
  public void test09() throws Exception {
    genericTest("data/bugsfixed/bug09.p");
  }

  @Test(enabled=true)
  public void test10() throws Exception {
    genericTest("data/bugsfixed/bug10.p");
  }

  @Test(enabled=true)
  public void test11() throws Exception {
    genericTest("data/bugsfixed/bug11.p");
  }

  @Test(enabled=true)
  public void test12() throws Exception {
    genericTest("data/bugsfixed/bug12.p");
  }

  @Test(enabled=true)
  public void test13() throws Exception {
    genericTest("data/bugsfixed/bug13.p");
  }

  @Test(enabled=true)
  public void test14() throws Exception {
    genericTest("data/bugsfixed/bug14.p");
  }

  @Test(enabled=true)
  public void test15() throws Exception {
    genericTest("data/bugsfixed/bug15.p");
  }

  @Test(enabled=true)
  public void test16() throws Exception {
    genericTest("data/bugsfixed/bug16.p");
  }

  @Test(enabled=true)
  public void test17() throws Exception {
    genericTest("data/bugsfixed/bug17.p");
  }

  @Test(enabled=true)
  public void test18() throws Exception {
    genericTest("data/bugsfixed/bug18.p");
  }

  @Test(enabled=true)
  public void test19() throws Exception {
    genericTest("data/bugsfixed/bug19.p");
  }

  @Test(enabled=true)
  public void test20() throws Exception {
    genericTest("data/bugsfixed/bug20.p");
  }

  @Test(enabled=true)
  public void test21() throws Exception {
    genericTest("data/bugsfixed/bug21.cls");
  }

  @Test(enabled=true)
  public void test22() throws Exception {
    genericTest("data/bugsfixed/bug22.cls");
  }

  @Test(enabled=true)
  public void test23() throws Exception {
    genericTest("data/bugsfixed/bug23.cls");
  }

  @Test(enabled=true)
  public void test24() throws Exception {
    genericTest("data/bugsfixed/bug24.p");
  }

  @Test(enabled=true)
  public void test25() throws Exception {
    genericTest("data/bugsfixed/bug25.p");
  }

  @Test(enabled=true)
  public void test26() throws Exception {
    genericTest("data/bugsfixed/bug26.cls");
  }

  @Test(enabled=true)
  public void test27() throws Exception {
    genericTest("data/bugsfixed/bug27.cls");
  }

  @Test(enabled=true)
  public void test28() throws Exception {
    genericTest("data/bugsfixed/bug28.cls");
  }

  @Test(enabled=true)
  public void test29() throws Exception {
    genericTest("data/bugsfixed/bug29.p");
  }

  @Test(enabled=true)
  public void test30() throws Exception {
    genericTest("data/bugsfixed/bug30.p");
  }

  @Test(enabled=true)
  public void test31() throws Exception {
    genericTest("data/bugsfixed/bug31.cls");
  }

  @Test(enabled=true)
  public void test33() throws Exception {
    genericTest("data/bugsfixed/bug33.cls");
  }

  // Next two tests : same exception should be thrown in both cases
  @Test(enabled=false, expectedExceptions = {ProparseRuntimeException.class})
  public void testCache1() throws Exception {
    genericTest("data/bugsfixed/CacheChild.cls");
  }

  @Test(enabled=false, expectedExceptions = {ProparseRuntimeException.class})
  public void testCache2() throws Exception {
    genericTest("data/bugsfixed/CacheChild.cls");
  }

  @Test(enabled=true)
  public void testSaxWriter() throws Exception {
    genericTest("data/bugsfixed/sax-writer.p");
  }

  private void genericTest(String fileName) /* throws ANTLRException, IOException */ {
    File file = new File(SRC_DIR, fileName);
    System.out.println("Generic test: " + fileName);

    try {
      ProgressLexer lexer = new ProgressLexer(session, new FileInputStream(file), file.getAbsolutePath(), false);
      lexer.setMergeNameDotInId(true);
      
      // lexer.addTypeNameTokenFilter();
      Proparse parser = new Proparse(new CommonTokenStream(lexer));
      parser.initAntlr4(session, lexer.getFilenameList());
      parser.setProfile(true);
      parser.removeErrorListeners();
      parser.addErrorListener(new DescriptiveErrorListener());
      // parser.setTrace(true);
      // parser.addErrorListener(new DiagnosticErrorListener(false));
      ParseTree tree = parser.program();
      JPNode root4 = new JPNodeVisitor(parser.getParserSupport(), (BufferedTokenStream) parser.getInputStream()).visit(
          tree).build(parser.getParserSupport());
      displayParseInfo(parser.getParseInfo());
      displayRootNode4(root4, parser.getParserSupport(), "target/antlr4.txt");

      ProgressLexer lexer2 = new ProgressLexer(session, new FileInputStream(file), file.getAbsolutePath(), false);
      ProParser parser2 = new ProParser(lexer2.getANTLR2TokenStream(true));
      parser2.initAntlr4(session, lexer2.getFilenameList());
      parser2.program();
      ProgramRootNode root2 = (ProgramRootNode) parser2.getAST();
      root2.backLinkAndFinalize();
      lexer2.parseComplete();
      displayRootNode(root2, parser2.support, "target/antlr2.txt");

      assertEquals(root2.compareTo(root4, 0), 0);
      assertEquals(parser2.support.compareTo(parser.getParserSupport()), 0);
    } catch (ANTLRException | IOException uncaught) {
      System.err.println(uncaught);
    }
  }

  @SuppressWarnings("unused")
  private void displayParseInfo(ParseInfo info) {
    System.out.println("Rules longer than 100ms");
    Arrays.stream(info.getDecisionInfo()).filter(decision -> decision.timeInPrediction > 100000000).sorted(
        (d1, d2) -> Long.compare(d2.timeInPrediction, d1.timeInPrediction)).forEach(
            decision -> System.out.println(
                String.format("Time: %d in %d calls - LL_Lookaheads: %d Max k: %d Ambiguities: %d Errors: %d Rule: %s",
                    decision.timeInPrediction / 1000000, decision.invocations, decision.SLL_TotalLook,
                    decision.SLL_MaxLook, decision.ambiguities.size(), decision.errors.size(),
                    Proparse.ruleNames[Proparse._ATN.getDecisionState(decision.decision).ruleIndex])));

    System.out.println("Rules with max-k greater than 50");
    Arrays.stream(info.getDecisionInfo()).filter(decision -> decision.SLL_MaxLook > 50).sorted(
        (d1, d2) -> Long.compare(d2.SLL_MaxLook, d1.SLL_MaxLook)).forEach(
            decision -> System.out.println(
                String.format("Time: %d in %d calls - LL_Lookaheads: %d Max k: %d Ambiguities: %d Errors: %d Rule: %s",
                    decision.timeInPrediction / 1000000, decision.invocations, decision.SLL_TotalLook,
                    decision.SLL_MaxLook, decision.ambiguities.size(), decision.errors.size(),
                    Proparse.ruleNames[Proparse._ATN.getDecisionState(decision.decision).ruleIndex])));
  }

  @SuppressWarnings("unused")
  private void displayRootNode(ProgramRootNode rootNode, ParserSupport support, String s) {
    try (FileWriter writer = new FileWriter(s)) {
      new TreeNodeLister(rootNode, support, writer, ABLNodeType.INVALID_NODE).print();
    } catch (IOException uncaught) {
      
    }
  }

  private void displayRootNode4(JPNode rootNode, ParserSupport support, String s) {
    try (FileWriter writer = new FileWriter(s)) {
      new org.prorefactor.proparse.antlr4.TreeNodeLister(rootNode, support, writer, ABLNodeType.INVALID_NODE).print();
    } catch (IOException uncaught) {
      
    }
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
