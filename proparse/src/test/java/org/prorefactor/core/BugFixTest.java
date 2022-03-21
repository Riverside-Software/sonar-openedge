/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.Variable.ReadWrite;
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
public class BugFixTest {
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";
  private final static String TEMP_DIR = "target/nodes-lister/data/bugsfixed";

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
    writer.println("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"https://dl.rssw.eu/d3-style.css\" />");
    writer.println("<script src=\"https://dl.rssw.eu/jquery-1.10.2.min.js\"></script><script src=\"https://dl.rssw.eu/d3.v3.min.js\"></script>");
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
    writer.println("<script src=\"https://dl.rssw.eu/dndTreeDebug.js\"></script></body></html>");
    writer.close();
  }

  private ParseUnit genericTest(String file) {
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, file), session);
    assertNull(pu.getTopNode());
    assertNull(pu.getRootScope());
    pu.parse();
    pu.treeParser01();
    assertFalse(pu.hasSyntaxError());
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

  private TokenSource genericLex(String file) {
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, file), session);
    assertNull(pu.getTopNode());
    assertNull(pu.getMetrics());
    assertNull(pu.getRootScope());
    pu.lexAndGenerateMetrics();
    assertNotNull(pu.getMetrics());
    return pu.lex();
  }

  @Test
  public void testVarUsage() {
    ParseUnit unit = genericTest("varusage.cls");
    assertEquals(unit.getRootScope().getVariable("x1").getNumWrites(), 2);
    assertEquals(unit.getRootScope().getVariable("x1").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("x2").getNumWrites(), 1);
    assertEquals(unit.getRootScope().getVariable("x2").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("x3").getNumWrites(), 1);
    assertEquals(unit.getRootScope().getVariable("x3").getNumReads(), 0);
    assertEquals(unit.getRootScope().getVariable("x4").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("x4").getNumWrites(), 0);

    assertEquals(unit.getRootScope().getVariable("lProcedure1").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("lProcedure1").getNumWrites(), 0);
    assertEquals(unit.getRootScope().getVariable("lProcedure2").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("lProcedure2").getNumWrites(), 0);
    assertEquals(unit.getRootScope().getVariable("lApsv").getNumReads(), 1);
    assertEquals(unit.getRootScope().getVariable("lApsv").getNumWrites(), 0);
    assertEquals(unit.getRootScope().getVariable("lRun").getNumReads(), 0);
    assertEquals(unit.getRootScope().getVariable("lRun").getNumWrites(), 1);
  }

  @Test
  public void testVarUsage2() {
    ParseUnit unit = genericTest("varusage2.cls");
    Variable x1 = unit.getRootScope().getVariable("x1");
    assertEquals(x1.getNumWrites(), 1);
    assertEquals(x1.getNumReads(), 0);
    assertEquals(x1.getReadWriteReferences().get(0).getNode().getStatement().firstNaturalChild().getLine(), 13);
    Variable x2 = unit.getRootScope().getVariable("x2");
    assertEquals(x2.getNumWrites(), 1);
    assertEquals(x2.getNumReads(), 2);
    assertEquals(x2.getReadWriteReferences().get(0).getNode().getStatement().firstNaturalChild().getLine(), 6);
    assertEquals(x2.getReadWriteReferences().get(0).getType(), ReadWrite.WRITE);
    assertEquals(x2.getReadWriteReferences().get(1).getNode().getStatement().firstNaturalChild().getLine(), 10);
    assertEquals(x2.getReadWriteReferences().get(1).getType(), ReadWrite.READ);
    assertEquals(x2.getReadWriteReferences().get(2).getNode().getStatement().firstNaturalChild().getLine(), 11);
    assertEquals(x2.getReadWriteReferences().get(2).getType(), ReadWrite.READ);
    Variable x3 = unit.getRootScope().getVariable("x3");
    assertEquals(x3.getNumWrites(), 2);
    assertEquals(x3.getNumReads(), 1);
    assertEquals(x3.getReadWriteReferences().get(0).getNode().getStatement().firstNaturalChild().getLine(), 6);
    assertEquals(x3.getReadWriteReferences().get(0).getType(), ReadWrite.READ);
    assertEquals(x3.getReadWriteReferences().get(1).getNode().getStatement().firstNaturalChild().getLine(), 7);
    assertEquals(x3.getReadWriteReferences().get(1).getType(), ReadWrite.WRITE);
    assertEquals(x3.getReadWriteReferences().get(2).getNode().getStatement().firstNaturalChild().getLine(), 11);
    assertEquals(x3.getReadWriteReferences().get(2).getType(), ReadWrite.WRITE);
    Variable x4 = unit.getRootScope().getVariable("x4");
    assertEquals(x4.getNumWrites(), 1);
    assertEquals(x4.getNumReads(), 2);
    assertEquals(x4.getReadWriteReferences().get(0).getNode().getStatement().firstNaturalChild().getLine(), 14);
    assertEquals(x4.getReadWriteReferences().get(0).getType(), ReadWrite.READ);
    assertEquals(x4.getReadWriteReferences().get(1).getNode().getStatement().firstNaturalChild().getLine(), 14);
    assertEquals(x4.getReadWriteReferences().get(1).getType(), ReadWrite.READ);
    assertEquals(x4.getReadWriteReferences().get(2).getNode().getStatement().firstNaturalChild().getLine(), 15);
    assertEquals(x4.getReadWriteReferences().get(2).getType(), ReadWrite.WRITE);
  }

  @Test
  public void test01() {
    genericTest("bug01.p");
  }

  @Test
  public void test02() {
    genericTest("bug02.p");
  }

  @Test
  public void test03() {
    genericTest("bug03.p");
  }

  @Test
  public void test04() {
    genericTest("bug04.p");
  }

  @Test
  public void test05() {
    genericTest("bug05.p");
  }

  @Test
  public void test06() {
    genericTest("bug06.p");
  }

  @Test
  public void test07() {
    genericTest("interface07.cls");
  }

  @Test
  public void test08() {
    genericTest("bug08.cls");
  }

  @Test
  public void test09() {
    genericTest("bug09.p");
  }

  @Test
  public void test10() {
    genericTest("bug10.p");
  }

  @Test
  public void test11() {
    genericTest("bug11.p");
  }

  @Test
  public void test12() {
    genericTest("bug12.p");
  }

  @Test
  public void test13() {
    genericTest("bug13.p");
  }

  @Test
  public void test14() {
    genericTest("bug14.p");
  }

  @Test
  public void test15() {
    genericTest("bug15.p");
  }

  @Test
  public void test16() {
    genericTest("bug16.p");
  }

  @Test
  public void test17() {
    genericTest("bug17.p");
  }

  @Test
  public void test18() {
    genericTest("bug18.p");
  }

  @Test
  public void test19() {
    ParseUnit unit = genericTest("bug19.p");
    assertEquals("MESSAGE \"Hello\".", unit.getTopNode().toStringFulltext().trim());
  }

  @Test
  public void test20() {
    genericTest("bug20.p");
  }

  @Test
  public void test21() {
    genericTest("bug21.cls");
  }

  @Test
  public void test22() {
    genericTest("bug22.cls");
  }

  @Test
  public void test23() {
    genericTest("bug23.cls");
  }

  @Test
  public void test24() {
    genericTest("bug24.p");
  }

  @Test
  public void test25() {
    genericTest("bug25.p");
  }

  @Test
  public void test26() {
    genericTest("bug26.cls");
  }

  @Test
  public void test27() {
    genericTest("bug27.cls");
  }

  @Test
  public void test28() {
    genericTest("bug28.cls");
  }

  @Test
  public void test29() {
    genericTest("bug29.p");
  }

  @Test
  public void test30() {
    genericTest("bug30.p");
  }

  @Test
  public void test31() {
    genericTest("bug31.cls");
  }

  @Test
  public void test32() {
    genericLex("bug32.i");
  }

  @Test
  public void test33() {
    genericTest("bug33.cls");
  }

  @Test
  public void test34() {
    genericTest("bug34.p");
  }

  @Test
  public void test35() {
    genericTest("bug35.p");
  }

  @Test
  public void test36() {
    genericTest("bug36.p");
  }

  @Test
  public void test41() {
    genericTest("bug41.cls");
  }

  @Test
  public void test43() {
    genericTest("bug43.p");
  }

  @Test
  public void test44() {
    ParseUnit unit = genericTest("bug44.cls");
    assertEquals(unit.getTopNode().queryStateHead().size(), 6);
  }

  @Test
  public void test45() {
    ParseUnit unit = genericTest("bug45.p");
    assertEquals(unit.getTopNode().queryStateHead().size(), 5);
  }

  @Test
  public void test46() {
    ParseUnit unit = genericTest("bug46.p");
    assertEquals(unit.getTopNode().queryStateHead().size(), 1);
  }

  @Test
  public void test47() {
    ParseUnit unit = genericTest("bug47.cls");
    assertEquals(unit.getTopNode().queryStateHead().size(), 2);
  }

  @Test
  public void test48() {
    ParseUnit unit = genericTest("bug48.p");
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
  }

  @Test
  public void test49() {
    ParseUnit unit = genericTest("bug49.p");
    assertEquals(unit.getTopNode().queryStateHead().size(), 3);
  }

  // Next two tests : same exception should be thrown in both cases
//  @Test(expectedExceptions = {ProparseRuntimeException.class})
//  public void testCache1() {
//    genericTest("CacheChild.cls");
//  }
//
//  @Test(expectedExceptions = {ProparseRuntimeException.class})
//  public void testCache2() {
//    genericTest("CacheChild.cls");
//  }

  @Test
  public void testSerializableKeyword() {
    genericTest("serialkw.cls");
  }

  @Test
  public void testXor() {
    genericTest("xor.p");
  }

  @Test
  public void testSaxWriter() {
    genericTest("sax-writer.p");
  }

  @Test
  public void testNoBox() {
    genericTest("nobox.p");
  }

  @Test
  public void testOnStatement() {
    genericTest("on_statement.p");
  }

  @Test
  public void testIncludeInComment() {
    genericTest("include_comment.p");
  }

  @Test
  public void testCreateComObject() {
    ParseUnit unit = genericTest("createComObject.p");
    List<JPNode> list = unit.getTopNode().query(ABLNodeType.CREATE);
    // COM automation
    assertEquals(list.get(0).getLine(), 3);
    assertEquals(list.get(0).asIStatement().getNodeType2(), ABLNodeType.AUTOMATION_OBJECT);
    assertEquals(list.get(1).getLine(), 4);
    assertEquals(list.get(1).asIStatement().getNodeType2(), ABLNodeType.AUTOMATION_OBJECT);
    // Widgets
    assertEquals(list.get(2).getLine(), 8);
    assertEquals(list.get(2).asIStatement().getNodeType2(), ABLNodeType.WIDGET);
    assertEquals(list.get(3).getLine(), 12);
    assertEquals(list.get(3).asIStatement().getNodeType2(), ABLNodeType.WIDGET);
    // Ambiguous
    assertEquals(list.get(4).getLine(), 15);
    assertEquals(list.get(4).asIStatement().getNodeType2(), ABLNodeType.WIDGET);
  }

  @Test
  public void testCopyLob() {
    genericTest("copylob.p");
  }

  @Test
  public void testOsCreate() {
    genericTest("oscreate.p");
  }

  @Test
  public void testOnEvent() {
    genericTest("onEvent.p");
  }

  @Test
  public void testGetDbClient() {
    genericTest("getdbclient.p");
  }

  @Test
  public void testDoubleColon() {
    genericTest("double-colon.p");
  }

  @Test
  public void testDynProp() {
    genericTest("dynprop.p");
  }

  @Test
  public void testTildeInComment() {
    TokenSource stream = genericLex("comment-tilde.p");
    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.COMMENT);
    assertEquals(tok.getText(), "// \"~n\"");
    assertEquals(((ProToken) stream.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) stream.nextToken()).getNodeType(), ABLNodeType.DEFINE);
  }

  @Test
  public void testTildeInComment2() {
    TokenSource stream = genericLex("comment-tilde2.p");
    assertEquals(((ProToken) stream.nextToken()).getNodeType(), ABLNodeType.DEFINE);
    assertEquals(((ProToken) stream.nextToken()).getNodeType(), ABLNodeType.WS);
    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.COMMENT);
    assertEquals(tok.getText(), "// \"~n\"");
  }

  @Test(enabled = false, description = "Issue #309, won't fix,")
  public void testAbstractKw() {
    genericTest("abstractkw.p");
  }

  @Test
  public void testNoArgFunc() {
    ParseUnit pu = genericTest("noargfunc.p");
    List<JPNode> nodes = pu.getTopNode().query(ABLNodeType.MESSAGE);
    assertEquals(nodes.get(0).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.BUILTIN_FUNCTION);
    assertEquals(nodes.get(1).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.FIELD_REF);
    assertEquals(nodes.get(2).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.BUILTIN_FUNCTION);
    assertEquals(nodes.get(3).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.FIELD_REF);
    assertEquals(nodes.get(4).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.BUILTIN_FUNCTION);
    assertEquals(nodes.get(5).getFirstChild().getFirstChild().getNodeType(), ABLNodeType.FIELD_REF);

    assertEquals(nodes.get(0).getFirstChild().getFirstChild().getFirstChild().getNodeType(), ABLNodeType.GUID);
    assertEquals(nodes.get(2).getFirstChild().getFirstChild().getFirstChild().getNodeType(), ABLNodeType.TIMEZONE);
    assertEquals(nodes.get(4).getFirstChild().getFirstChild().getFirstChild().getNodeType(), ABLNodeType.MTIME);
  }

  @Test
  public void testLexer01() {
    @SuppressWarnings("unused")
    TokenSource stream = genericLex("lex.p");
  }

  @Test
  public void testDataset() {
    genericTest("DatasetParentFields.p");
  }

  @Test
  public void testExtentFunction() {
    genericTest("testextent1.cls");
    genericTest("testextent2.p");
  }

  @Test
  public void testTTLikeDB01() {
    genericTest("ttlikedb01.p");
  }

  @Test
  public void testStopAfter() {
    genericTest("stopafter.p");
  }

  @Test(expectedExceptions = {ParseCancellationException.class})
  public void testDefined() {
    // https://github.com/Riverside-Software/sonar-openedge/issues/515
    genericTest("defined.p");
  }

  @Test
  public void testTTLikeDB02() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/bugsfixed/ttlikedb02.p"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
    assertNotNull(unit.getTopNode());

    // First FIND statement
    JPNode node = unit.getTopNode().queryStateHead(ABLNodeType.FIND).get(0);
    assertNotNull(node);
    assertEquals(node.query(ABLNodeType.RECORD_NAME).size(), 1);
    Object obj = node.query(ABLNodeType.RECORD_NAME).get(0).getSymbol();
    assertNotNull(obj);
    assertEquals(((TableBuffer) obj).getTable().getStoretype(), IConstants.ST_DBTABLE);

    // Second FIND statement
    node = unit.getTopNode().queryStateHead(ABLNodeType.FIND).get(1);
    assertNotNull(node);
    assertEquals(node.query(ABLNodeType.RECORD_NAME).size(), 1);
    obj = node.query(ABLNodeType.RECORD_NAME).get(0).getSymbol();
    assertNotNull(obj);
    assertEquals(((TableBuffer) obj).getTable().getStoretype(), IConstants.ST_TTABLE);

    // Third FIND statement
    node = unit.getTopNode().queryStateHead(ABLNodeType.FIND).get(2);
    assertNotNull(node);
    assertEquals(node.query(ABLNodeType.RECORD_NAME).size(), 1);
    obj = node.query(ABLNodeType.RECORD_NAME).get(0).getSymbol();
    assertNotNull(obj);
    assertEquals(((TableBuffer) obj).getTable().getStoretype(), IConstants.ST_DBTABLE);

    // Fourth FIND statement
    node = unit.getTopNode().queryStateHead(ABLNodeType.FIND).get(3);
    assertNotNull(node);
    assertEquals(node.query(ABLNodeType.RECORD_NAME).size(), 1);
    obj = node.query(ABLNodeType.RECORD_NAME).get(0).getSymbol();
    assertNotNull(obj);
    assertEquals(((TableBuffer) obj).getTable().getStoretype(), IConstants.ST_TTABLE);
  }

  @Test
  public void testRCodeStructure() {
     ParseUnit unit = new ParseUnit(new File("src/test/resources/data/rssw/pct/ChildClass.cls"), session);
     assertNull(unit.getTopNode());
     assertNull(unit.getRootScope());
     unit.treeParser01();
     assertFalse(unit.hasSyntaxError());
     assertNotNull(unit.getTopNode());
   }

  @Test
  public void testProtectedTTAndBuffers() {
     ParseUnit unit = new ParseUnit(new File("src/test/resources/data/ProtectedTT.cls"), session);
     assertNull(unit.getTopNode());
     assertNull(unit.getRootScope());
     unit.treeParser01();
     assertFalse(unit.hasSyntaxError());
     assertNotNull(unit.getTopNode());
   }

  @Test
  public void testAscendingFunction() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/bugsfixed/ascending.p"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());
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
  public void testDefineMenu() {
    genericTest("definemenu.p");
  }

  @Test
  public void testOptionsField() {
    genericTest("options_field.p");
  }

  @Test
  public void testTooManyStatements() {
    // Verifies that lots of statements (5000 here) don't raise a stack overflow exception
    genericTest("tooManyStatements.p");
  }

  @Test
  public void testCatchError() {
    genericTest("catchError.p");
  }

}
