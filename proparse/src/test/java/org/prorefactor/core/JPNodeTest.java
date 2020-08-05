/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
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
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.progress.xref.CrossReference;
import com.progress.xref.EmptyCrossReference;

import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

/**
 * Tests for JPNodeVisitor
 */
public class JPNodeTest {
  private final static String SRC_DIR = "src/test/resources/jpnode";
  private final static String TEMP_DIR = "target/nodes-lister/jpnode";

  private JAXBContext context;
  private Unmarshaller unmarshaller;
  private XMLReader reader;

  private RefactorSession session;
  private File tempDir = new File(TEMP_DIR);

  private List<String> jsonOut = new ArrayList<>();
  private List<String> jsonNames = new ArrayList<>();

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    try {
      context = JAXBContext.newInstance("com.progress.xref", this.getClass().getClassLoader());
      unmarshaller = context.createUnmarshaller();

      SAXParserFactory sax = SAXParserFactory.newInstance();
      sax.setNamespaceAware(false);
      reader = sax.newSAXParser().getXMLReader();
    } catch (JAXBException | SAXException | ParserConfigurationException caught) {
      throw new IllegalStateException(caught);
    }

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
    return genericTest(file, new EmptyCrossReference());
  }

  private ParseUnit genericTest(String file, CrossReference xref) {
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, file), session);
    pu.attachXref(xref);
    assertNull(pu.getTopNode());
    pu.parse();
    assertFalse(pu.hasSyntaxError());
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
  public void testStatements() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "query01.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<JPNode> doStmts = unit.getTopNode().queryStateHead(ABLNodeType.DO);
    List<JPNode> msgStmts = unit.getTopNode().queryStateHead(ABLNodeType.MESSAGE);
    assertEquals(doStmts.size(), 2);
    assertEquals(msgStmts.size(), 3);

    assertEquals(doStmts.get(0).query(ABLNodeType.VIEWAS).size(), 3);
    assertEquals(doStmts.get(0).queryCurrentStatement(ABLNodeType.VIEWAS).size(), 0);
    assertEquals(doStmts.get(1).query(ABLNodeType.VIEWAS).size(), 1);
    assertEquals(doStmts.get(1).queryCurrentStatement(ABLNodeType.VIEWAS).size(), 0);

    assertEquals(msgStmts.get(0).query(ABLNodeType.VIEWAS).size(), 1);
    assertEquals(msgStmts.get(1).query(ABLNodeType.VIEWAS).size(), 1);
    assertEquals(msgStmts.get(2).query(ABLNodeType.VIEWAS).size(), 1);
  }

  @Test
  public void testDotComment01() {
    ParseUnit unit = genericTest("dotcomment01.p");
    JPNode node = unit.getTopNode().firstNaturalChild();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.DOT_COMMENT);
    // TODO Whitespaces should be kept...
    assertTrue(node.getText().startsWith(".message"));
    assertEquals(node.getLine(), 1);
    assertEquals(node.getEndLine(), 3);
    assertEquals(node.getColumn(), 1);
    assertEquals(node.getEndColumn(), 27);

    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.PERIOD);
    assertEquals(node.getFirstChild().getLine(), 3);
    assertEquals(node.getFirstChild().getEndLine(), 3);
    assertEquals(node.getFirstChild().getColumn(), 29);
    assertEquals(node.getFirstChild().getEndColumn(), 29);
  }

  @Test
  public void testDotComment02() {
    ParseUnit unit = genericTest("dotcomment02.p");
    JPNode node = unit.getTopNode().firstNaturalChild();
    assertNotNull(node);
    assertEquals(node.getNodeType(), ABLNodeType.DOT_COMMENT);
    assertEquals(node.getText(), ".message");

    assertEquals(node.getLine(), 1);
    assertEquals(node.getEndLine(), 1);
    assertEquals(node.getColumn(), 1);
    assertEquals(node.getEndColumn(), 8);

    assertNotNull(node.getFirstChild());
    assertEquals(node.getFirstChild().getNodeType(), ABLNodeType.PERIOD);
    assertEquals(node.getFirstChild().getLine(), 1);
    assertEquals(node.getFirstChild().getEndLine(), 1);
    assertEquals(node.getFirstChild().getColumn(), 9);
    assertEquals(node.getFirstChild().getEndColumn(), 9);
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
    assertEquals(node.getFirstChild().getText(), "c:/foo/bar/something.p");
    assertEquals(node.getFirstChild().getLine(), 1);
    assertEquals(node.getFirstChild().getEndLine(), 1);
    assertEquals(node.getFirstChild().getColumn(), 9);
    assertEquals(node.getFirstChild().getEndColumn(), 30);

    assertNotNull(node.getFirstChild().getNextSibling());
    assertEquals(node.getFirstChild().getNextSibling().getNodeType(), ABLNodeType.PERIOD);

    JPNode node2 = node.getNextSibling();
    assertEquals(node2.getNodeType(), ABLNodeType.INPUT);
    assertEquals(node2.getFirstChild().getNodeType(), ABLNodeType.THROUGH);
    assertEquals(node2.getFirstChild().getNextSibling().getNodeType(), ABLNodeType.FILENAME);
    assertEquals(node2.getFirstChild().getNextSibling().getText(), "echo $$ $PATH c:/foobar/something.p");
    assertEquals(node2.getFirstChild().getNextSibling().getNextSibling().getNodeType(), ABLNodeType.NOECHO);
    assertEquals(node2.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNodeType(), ABLNodeType.APPEND);
    assertEquals(node2.getFirstChild().getNextSibling().getNextSibling().getNextSibling().getNextSibling().getNodeType(), ABLNodeType.KEEPMESSAGES);
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
    assertEquals(node.getFirstChild().getText(), "( xxx = \"yyy\", zz = \"abc\" )");
  }

  @Test
  public void testDataType() {
    ParseUnit unit = genericTest("datatype01.p");
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.RETURNS);
    assertEquals(nodes.size(), 12);
    assertEquals(nodes.get(0).nextNode().getNodeType(), ABLNodeType.INTEGER);
    assertEquals(nodes.get(1).nextNode().getNodeType(), ABLNodeType.LOGICAL);
    assertEquals(nodes.get(2).nextNode().getNodeType(), ABLNodeType.ROWID);
    assertEquals(nodes.get(3).nextNode().getNodeType(), ABLNodeType.WIDGETHANDLE);
    assertEquals(nodes.get(4).nextNode().getNodeType(), ABLNodeType.CHARACTER);
    assertEquals(nodes.get(5).nextNode().getNodeType(), ABLNodeType.DATE);
    assertEquals(nodes.get(6).nextNode().getNodeType(), ABLNodeType.DECIMAL);
    assertEquals(nodes.get(7).nextNode().getNodeType(), ABLNodeType.INTEGER);
    assertEquals(nodes.get(8).nextNode().getNodeType(), ABLNodeType.INTEGER);
    assertEquals(nodes.get(9).nextNode().getNodeType(), ABLNodeType.RECID);
    assertEquals(nodes.get(10).nextNode().getNodeType(), ABLNodeType.ROWID);
    assertEquals(nodes.get(11).nextNode().getNodeType(), ABLNodeType.WIDGETHANDLE);
    

    List<JPNode> nodes2 = unit.getTopNode().query(ABLNodeType.TO);
    assertEquals(nodes2.size(), 3);
    assertEquals(nodes2.get(0).nextNode().getNodeType(), ABLNodeType.CHARACTER);
    assertEquals(nodes2.get(1).nextNode().getNodeType(), ABLNodeType.INT64);
    assertEquals(nodes2.get(2).nextNode().getNodeType(), ABLNodeType.DOUBLE);
  }

  @Test
  public void testEditing() {
    ParseUnit unit = genericTest("editing01.p");
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.EDITING_PHRASE);
    assertEquals(nodes.size(), 2);
    assertNotNull(nodes.get(0).getFirstChild());
    assertEquals(nodes.get(0).getFirstChild().getNodeType(), ABLNodeType.EDITING);
    assertNotNull(nodes.get(1).getFirstChild());
    assertEquals(nodes.get(1).getFirstChild().getNodeType(), ABLNodeType.ID);
    assertEquals(nodes.get(1).getFirstChild().getText(), "foobar");
  }

  @Test
  public void testChoose() {
    ParseUnit unit = genericTest("choose01.p");
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.CHOOSE);
    assertEquals(nodes.size(), 1);
    assertNotNull(nodes.get(0).getFirstChild());
    assertEquals(nodes.get(0).getFirstChild().getNodeType(), ABLNodeType.FIELD);
  }

  @Test
  public void testFormatPhrase() {
    ParseUnit unit = genericTest("formatphrase01.p");
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.MESSAGE);
    assertEquals(nodes.size(), 1);
    assertNotNull(nodes.get(0).getDirectChildren());
    assertEquals(nodes.get(0).getDirectChildren().size(), 4);
    assertEquals(nodes.get(0).getDirectChildren().get(0).getNodeType(), ABLNodeType.FORM_ITEM);
    assertEquals(nodes.get(0).getDirectChildren().get(1).getNodeType(), ABLNodeType.UPDATE);
    assertEquals(nodes.get(0).getDirectChildren().get(2).getNodeType(), ABLNodeType.VIEWAS);
    assertEquals(nodes.get(0).getDirectChildren().get(3).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testDefineVarAsHandle() {
    ParseUnit unit = genericTest("defvar01.p");
    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.DEFINE);
    assertEquals(nodes.size(), 1);
    assertFalse(nodes.get(0).query(ABLNodeType.NOUNDO).isEmpty());
  }

  @Test
  public void testXref01() throws JAXBException, IOException {
    InputSource is = new InputSource(new FileInputStream(SRC_DIR + "/xref01.p.xref"));
    SAXSource source = new SAXSource(reader, is);
    CrossReference xref = (CrossReference) unmarshaller.unmarshal(source);

    ParseUnit unit = genericTest("xref01.p", xref);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    List<JPNode> nodes = unit.getTopNode().query(ABLNodeType.RECORD_NAME);
    assertEquals(nodes.size(), 5);
    RecordNameNode warehouse = (RecordNameNode) nodes.get(0);
    RecordNameNode customer = (RecordNameNode) nodes.get(1);
    RecordNameNode item = (RecordNameNode) nodes.get(2);

    assertTrue(warehouse.isWholeIndex());
    assertEquals(warehouse.getSearchIndexName(), "Warehouse.warehousenum");

    assertFalse(customer.isWholeIndex());
    assertEquals(customer.getSearchIndexName(), "Customer.CountryPost");
    assertEquals(customer.getSortAccess(), "Address");

    assertTrue(item.isWholeIndex());
    assertEquals(item.getSearchIndexName(), "Item.ItemNum");
  }

  @Test
  public void testXref02() throws JAXBException, IOException {
    InputSource is = new InputSource(new FileInputStream(SRC_DIR + "/xref02.cls.xref"));
    SAXSource source = new SAXSource(reader, is);
    CrossReference xref = (CrossReference) unmarshaller.unmarshal(source);

    ParseUnit unit = genericTest("xref02.cls", xref);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    assertEquals(unit.getTopNode().query(ABLNodeType.RECORD_NAME).size(), 3);
    for (JPNode node : unit.getTopNode().query(ABLNodeType.RECORD_NAME)) {
      RecordNameNode rec = (RecordNameNode) node;
      assertEquals(rec.getTableBuffer().getTable().getName(), "ttFoo");
      assertTrue(rec.isWholeIndex());
    }
  }

  @Test
  public void testXref03() throws JAXBException, IOException {
    InputSource is = new InputSource(new FileInputStream(SRC_DIR + "/xref03.p.xref"));
    SAXSource source = new SAXSource(reader, is);
    CrossReference xref = (CrossReference) unmarshaller.unmarshal(source);

    ParseUnit unit = genericTest("xref03.p", xref);
    unit.treeParser01();
    assertFalse(unit.hasSyntaxError());

    List<JPNode> recNodes = unit.getTopNode().query(ABLNodeType.RECORD_NAME);
    assertEquals(recNodes.size(), 10);
    // One can-find, search index should be set
    assertEquals(((RecordNameNode) recNodes.get(3)).getSearchIndexName(), "Customer.Name");
    // Two can-find on different tables, should be ok
    assertEquals(((RecordNameNode) recNodes.get(4)).getSearchIndexName(), "Customer.Name");
    assertEquals(((RecordNameNode) recNodes.get(5)).getSearchIndexName(), "Item.ItemNum");
    // Two can-find on same buffer, not ok
    assertEquals(((RecordNameNode) recNodes.get(6)).getSearchIndexName(), "");
    assertEquals(((RecordNameNode) recNodes.get(7)).getSearchIndexName(), "");
    // Two can-find on different buffer, but same table, not ok
    assertEquals(((RecordNameNode) recNodes.get(8)).getSearchIndexName(), "");
    assertEquals(((RecordNameNode) recNodes.get(9)).getSearchIndexName(), "");
  }
}
