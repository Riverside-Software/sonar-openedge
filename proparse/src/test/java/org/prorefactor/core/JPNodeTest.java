/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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
package org.prorefactor.core;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

/**
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
public class JPNodeTest {
  private final static String SRC_DIR = "src/test/resources/jpnode";
  private final static String TEMP_DIR = "target/nodes-lister/jpnode";

  private RefactorSession session;
  private File tempDir = new File(TEMP_DIR);

  private List<String> jsonOut = new ArrayList<>();
  private List<String> jsonNames = new ArrayList<>();

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().createAlias("foo", "sports2000");
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/rssw/pct/ParentClass.r")).getTypeInfo());
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/rssw/pct/ChildClass.r")).getTypeInfo());
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/ttClass.r")).getTypeInfo());
    session.injectTypeInfo(
        new RCodeInfo(new FileInputStream("src/test/resources/data/ProtectedTT.r")).getTypeInfo());

    tempDir.mkdirs();
  }

  @AfterTest
  public void tearDown() throws IOException {
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

  private ParseUnit genericTest(String file) {
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, file), session);
    assertNull(pu.getTopNode());
    pu.parse();
    assertNotNull(pu.getTopNode());

    StringWriter writer = new StringWriter();
    JsonNodeLister nodeLister = new JsonNodeLister(pu.getTopNode(), writer, ABLNodeType.LEFTPAREN,
        ABLNodeType.RIGHTPAREN, ABLNodeType.COMMA, ABLNodeType.PERIOD, ABLNodeType.LEXCOLON, ABLNodeType.OBJCOLON,
        ABLNodeType.THEN, ABLNodeType.END);
    nodeLister.print();
    
    jsonNames.add(file);
    jsonOut.add(writer.toString());

    return pu;
  }

  @Test
  public void testDotComment01() {
    ParseUnit unit = genericTest("dotcomment01.p");
    JPNode node = unit.getTopNode().firstNaturalChild();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.DOT_COMMENT);
    assertEquals(node.getText(), ".");

    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.UNQUOTEDSTRING);
    assertEquals(node.getFirstChild().getLine(), 1);
    assertEquals(node.getFirstChild().getEndLine(), 3);
    assertEquals(node.getFirstChild().getColumn(), 2);
    assertEquals(node.getFirstChild().getEndColumn(), 27);

    assertNotNull(node.getFirstChild().getNextSibling());
    assertEquals(node.getFirstChild().getNextSibling().getNodeType(), ABLNodeType.PERIOD);
    assertEquals(node.getFirstChild().getNextSibling().getLine(), 3);
    assertEquals(node.getFirstChild().getNextSibling().getEndLine(), 3);
    assertEquals(node.getFirstChild().getNextSibling().getColumn(), 29);
    assertEquals(node.getFirstChild().getNextSibling().getEndColumn(), 29);
  }

  @Test
  public void testDotComment02() {
    ParseUnit unit = genericTest("dotcomment02.p");
    JPNode node = unit.getTopNode().firstNaturalChild();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.DOT_COMMENT);
    assertEquals(node.getText(), ".");

    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.UNQUOTEDSTRING);
    assertEquals(node.getFirstChild().getLine(), 1);
    assertEquals(node.getFirstChild().getEndLine(), 1);
    assertEquals(node.getFirstChild().getColumn(), 2);
    assertEquals(node.getFirstChild().getEndColumn(), 8);

    assertNotNull(node.getFirstChild().getNextSibling());
    assertEquals(node.getFirstChild().getNextSibling().getNodeType(), ABLNodeType.PERIOD);
    assertEquals(node.getFirstChild().getNextSibling().getLine(), 1);
    assertEquals(node.getFirstChild().getNextSibling().getEndLine(), 1);
    assertEquals(node.getFirstChild().getNextSibling().getColumn(), 9);
    assertEquals(node.getFirstChild().getNextSibling().getEndColumn(), 9);
  }
  
  @Test
  public void testComparison01() {
    ParseUnit unit = genericTest("comparison01.p");
    JPNode node = unit.getTopNode().getFirstChild();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.EXPR_STATEMENT);
    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.EQ);
    assertNotNull(node.getFirstChild().getFirstChild());
    assertEquals(node.getFirstChild().getFirstChild().getNodeType(), ABLNodeType.NUMBER);
    assertNotNull(node.getFirstChild().getFirstChild().getNextSibling());
    assertEquals(node.getFirstChild().getFirstChild().getNextSibling().getNodeType(), ABLNodeType.NUMBER);
   
    node = node.getNextSibling();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.EXPR_STATEMENT);
    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.GTHAN);

    node = node.getNextSibling();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.EXPR_STATEMENT);
    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.LTHAN);

    node = node.getNextSibling();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.EXPR_STATEMENT);
    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.GE);

    node = node.getNextSibling();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.EXPR_STATEMENT);
    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.LE);

    node = node.getNextSibling();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.EXPR_STATEMENT);
    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.NE);
  }

  @Test
  public void testFileName01() {
    ParseUnit unit = genericTest("filename01.p");
    JPNode node = unit.getTopNode().getFirstChild();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.COMPILE);

    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.FILENAME);
    assertEquals(node.getFirstChild().getText(), "foo/bar/something.p");
    assertEquals(node.getFirstChild().getLine(), 1);
    assertEquals(node.getFirstChild().getEndLine(), 1);
    assertEquals(node.getFirstChild().getColumn(), 9);
    assertEquals(node.getFirstChild().getEndColumn(), 27);

    assertNotNull(node.getFirstChild().getNextSibling());
    assertEquals(node.getFirstChild().getNextSibling().getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testAnnotation01() {
    ParseUnit unit = genericTest("annotation01.p");
    JPNode node = unit.getTopNode().getFirstChild();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(node.getText(), "@MyAnnotation");

    node = node.getNextSibling();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(node.getText(), "@My.Super.Annotation");

    node = node.getNextSibling();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(node.getText(), "@MyAnnotation");
    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.UNQUOTEDSTRING);
    assertEquals(node.getFirstChild().getText(), "(xxx=\"yyy\",zz=\"abc\")");
  }
}
