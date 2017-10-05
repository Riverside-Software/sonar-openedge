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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.JsonNodeLister;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.Token;
import antlr.TokenStream;
import eu.rssw.pct.RCodeInfo;

/**
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
public class BugFixTest {
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";
  private final static String TEMP_DIR = "target/nodes-lister/data/bugsfixed";

  private RefactorSession session;
  private File tempDir = new File(TEMP_DIR);

  private List<String> jsonOut = new ArrayList<>();
  private List<String> jsonNames = new ArrayList<>();

  @BeforeTest
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().createAlias("foo", "sports2000");
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/rssw/pct/ParentClass.r")).getTypeInfo());
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/rssw/pct/ChildClass.r")).getTypeInfo());

    tempDir.mkdirs();
  }

  @AfterTest
  public void tearDown() throws Exception {
    PrintWriter writer = new PrintWriter(new File(tempDir, "index.html"));
    writer.println("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"http://riverside-software.fr/d3-style.css\" />"); 
    writer.println("<script src=\"http://riverside-software.fr/jquery-1.10.2.min.js\"></script><script src=\"http://riverside-software.fr/d3.v3.min.js\"></script>");
    writer.println("<script>var data= { \"files\": [");
    int zz = 1;
    for (String str : jsonNames) {
      if (zz > 1) {
        writer.write(',');
      }
      writer.print("{ \"file\": \"" + str + "\", \"var\": \"json" + zz++ + "\" }");
    }
    writer.println("]};");
    zz = 1;
    for (String str : jsonOut) {
      writer.println("var json" + zz++ + " = " + str + ";");
    }
    writer.println("</script></head><body><div id=\"wrapper\"><div id=\"left\"></div><div id=\"tree-container\"></div></div>");
    writer.println("<script src=\"http://riverside-software.fr/dndTreeDebug.js\"></script></body></html>");
    writer.close();
  }

  private ParseUnit genericTest(String file) throws Exception {
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, file), session);
    assertNull(pu.getTopNode());
    assertNull(pu.getRootScope());
    pu.parse();
    pu.treeParser01();
    assertNotNull(pu.getTopNode());
    assertNotNull(pu.getRootScope());

    StringWriter writer = new StringWriter();
    JsonNodeLister nodeLister = new JsonNodeLister(pu.getTopNode(), writer, ABLNodeType.LEFTPAREN,
        ABLNodeType.RIGHTPAREN, ABLNodeType.COMMA, ABLNodeType.PERIOD, ABLNodeType.LEXCOLON, ABLNodeType.OBJCOLON,
        ABLNodeType.THEN, ABLNodeType.END);
    nodeLister.print();
    
    jsonNames.add(file);
    jsonOut.add(writer.toString());

    return pu;
  }

  private TokenStream genericLex(String file) throws Exception {
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, file), session);
    assertNull(pu.getTopNode());
    assertNull(pu.getRootScope());
    assertNull(pu.getMetrics());
    pu.lexAndGenerateMetrics();
    assertNotNull(pu.getMetrics());
    return pu.lex();
  }

  @Test
  public void testVarUsage() throws Exception {
    ParseUnit unit = genericTest("varusage.cls");
    assertEquals(unit.getRootScope().getVariable("x1").getNumWrites(), 2);
    assertEquals(unit.getRootScope().getVariable("x1").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("x2").getNumWrites(), 1);
    assertEquals(unit.getRootScope().getVariable("x2").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("x3").getNumWrites(), 1);
    assertEquals(unit.getRootScope().getVariable("x3").getNumReads(), 0);
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
    genericLex("bug32.i");
  }

  @Test
  public void test33() throws Exception {
    genericTest("bug33.cls");
  }

  // Next two tests : same exception should be thrown in both cases
//  @Test(expectedExceptions = {ProparseRuntimeException.class})
//  public void testCache1() throws Exception {
//    genericTest("CacheChild.cls");
//  }
//
//  @Test(expectedExceptions = {ProparseRuntimeException.class})
//  public void testCache2() throws Exception {
//    genericTest("CacheChild.cls");
//  }

  @Test
  public void testSaxWriter() throws Exception {
    genericTest("sax-writer.p");
  }

  @Test
  public void testNoBox() throws Exception {
    genericTest("nobox.p");
  }

  @Test
  public void testIncludeInComment() throws Exception {
    genericTest("include_comment.p");
  }

  @Test
  public void testCreateComObject() throws Exception {
    ParseUnit unit = genericTest("createComObject.p");
    List<JPNode> list = unit.getTopNode().query(ABLNodeType.CREATE);
    // COM automation
    assertEquals(list.get(0).getLine(), 3);
    assertEquals(list.get(0).getState2(), ProParserTokenTypes.Automationobject);
    assertEquals(list.get(1).getLine(), 4);
    assertEquals(list.get(1).getState2(), ProParserTokenTypes.Automationobject);
    // Widgets
    assertEquals(list.get(2).getLine(), 8);
    assertEquals(list.get(2).getState2(), ProParserTokenTypes.WIDGET);
    assertEquals(list.get(3).getLine(), 12);
    assertEquals(list.get(3).getState2(), ProParserTokenTypes.WIDGET);
    // Ambiguous
    assertEquals(list.get(4).getLine(), 15);
    assertEquals(list.get(4).getState2(), ProParserTokenTypes.WIDGET);
  }

  @Test
  public void testCopyLob() throws Exception {
    genericTest("copylob.p");
  }

  @Test
  public void testOsCreate() throws Exception {
    genericTest("oscreate.p");
  }

  @Test
  public void testGetDbClient() throws Exception {
    genericTest("getdbclient.p");
  }

  @Test
  public void testDoubleColon() throws Exception {
    genericTest("double-colon.p");
  }

  @Test
  public void testTildeInComment() throws Exception {
    TokenStream stream = genericLex("comment-tilde.p");
    Token tok = stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.COMMENT);
    assertEquals(tok.getText(), "// \"~n\"");
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.DEFINE);
  }

  @Test
  public void testTildeInComment2() throws Exception {
    TokenStream stream = genericLex("comment-tilde2.p");
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.DEFINE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    Token tok = stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.COMMENT);
    assertEquals(tok.getText(), "// \"~n\"");
  }

  @Test(enabled = false, description = "Issue #309, won't fix,")
  public void testAbstractKw() throws Exception {
    genericTest("abstractkw.p");
  }

  @Test
  public void testNoArgFunc() throws Exception {
    ParseUnit pu = genericTest("noargfunc.p");
    List<JPNode> nodes = pu.getTopNode().query(ABLNodeType.MESSAGE);
    assertEquals(nodes.get(0).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.GUID);
    assertEquals(nodes.get(1).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.FIELD_REF);
    assertEquals(nodes.get(2).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.TIMEZONE);
    assertEquals(nodes.get(3).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.FIELD_REF);
    assertEquals(nodes.get(4).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.MTIME);
    assertEquals(nodes.get(5).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.FIELD_REF);
  }

  @Test
  public void testLexer01() throws Exception {
    @SuppressWarnings("unused")
    TokenStream stream = genericLex("lex.p");
  }

  @Test
  public void testDataset() throws Exception {
    genericTest("DatasetParentFields.p");
  }

  @Test
  public void testRCodeStructure() throws Exception {
     ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/ChildClass.cls"), session);
     assertNull(unit.getTopNode());
     assertNull(unit.getRootScope());
     unit.treeParser01();
     assertNotNull(unit.getTopNode());
   }

  @Test
  public void testAscendingFunction() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/bugsfixed/ascending.p"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());

    // Message statement
    JPNode node = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE).get(0);
    assertNotNull(node);
    assertEquals(node.query(ABLNodeType.ASCENDING).size(), 0);
    assertEquals(node.query(ABLNodeType.ASC).size(), 1);

    // Define TT statement
    JPNode node2 = unit.getTopNode().queryStateHead(ABLNodeType.DEFINE).get(0);
    assertNotNull(node2);
    assertEquals(node2.query(ABLNodeType.ASCENDING).size(), 1);
    assertEquals(node2.query(ABLNodeType.ASC).size(), 0);
  }

  @Test(enabled = false, description = "Issue #356, won't fix,")
  public void testDefineMenu() throws Exception {
    genericTest("definemenu.p");
  }
}
