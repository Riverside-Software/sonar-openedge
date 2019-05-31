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
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.MacroDef;
import org.prorefactor.macrolevel.NamedMacroRef;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * For testing API and Backwards API access to the parser.
 */
public class ApiTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() {
    File f = new File("src/test/resources/data/hello.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.treeParser01();
    assertEquals(pu.getTopNode().query(ABLNodeType.DISPLAY).size(), 1);
  }

  @Test
  public void test02() {
    File f = new File("src/test/resources/data/no-undo.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.treeParser01();
    JPNode node = pu.getTopNode().findDirectChild(ABLNodeType.DEFINE);
    assertEquals(ABLNodeType.VARIABLE.getType(), node.attrGet(IConstants.STATE2));
  }

  @Test
  public void test03() {
    File f = new File("src/test/resources/data/include.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.treeParser01();
    // Three include file (including main file)
    assertEquals(3, pu.getMacroSourceArray().length);
    // First is inc.i, at line 3
    assertEquals("inc.i", ((IncludeRef) pu.getMacroSourceArray()[1]).getFileRefName());
    assertEquals(4, ((IncludeRef) pu.getMacroSourceArray()[1]).getPosition().getLine());
    // Second is inc2.i, at line 2 (in inc.i)
    assertEquals("inc2.i", ((IncludeRef) pu.getMacroSourceArray()[2]).getFileRefName());
    assertEquals(2, ((IncludeRef) pu.getMacroSourceArray()[2]).getPosition().getLine());
  }

  @Test
  public void test04() {
    File f = new File("src/test/resources/data/nowarn.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.parse();

    // Looking for the DEFINE node
    JPNode node1 = (JPNode) pu.getTopNode().findDirectChild(ABLNodeType.DEFINE);
    assertNotNull(node1);
    assertTrue(node1.isStateHead());

    // Looking for the NO-UNDO node, and trying to get the state-head node
    JPNode node2 = (JPNode) pu.getTopNode().query(ABLNodeType.NOUNDO).get(0);
    JPNode parent = node2;
    while (!parent.isStateHead()) {
      parent = parent.getPreviousNode();
    }
    assertEquals(node1, parent);

    // No proparse directive as nodes anymore
    JPNode left = node1.getPreviousSibling();
    assertNull(left);
    
    // But as ProToken
    ProToken tok = node1.getHiddenBefore();
    assertNotNull(tok);
    // First WS, then proparse directive
    tok = (ProToken) tok.getHiddenBefore();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getText(), "prolint-nowarn(shared)");

    // First WS
    tok = (ProToken) tok.getHiddenBefore();
    assertNotNull(tok);
    // Then previous directive
    tok = (ProToken) tok.getHiddenBefore();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getText(), "prolint-nowarn(something)");
  }

  @Test
  public void test05() {
    File f = new File("src/test/resources/data/bugsfixed/bug19.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.parse();
    assertEquals("MESSAGE \"Hello\".", pu.getTopNode().toStringFulltext().trim());
  }

  @Test
  public void test06() {
    File f = new File("src/test/resources/data/abbrev.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.parse();
    assertFalse(pu.getTopNode().query(ABLNodeType.LC).get(0).isAbbreviated());
    assertFalse(pu.getTopNode().query(ABLNodeType.LC).get(0).isAbbreviated());
    assertTrue(pu.getTopNode().query(ABLNodeType.FILEINFORMATION).get(0).isAbbreviated());
    assertFalse(pu.getTopNode().query(ABLNodeType.FILEINFORMATION).get(1).isAbbreviated());
    assertTrue(pu.getTopNode().query(ABLNodeType.SUBSTITUTE).get(0).isAbbreviated());
    assertFalse(pu.getTopNode().query(ABLNodeType.SUBSTITUTE).get(1).isAbbreviated());
  }

  @Test
  public void test07() {
    File f = new File("src/test/resources/data/prepro.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.parse();
    IncludeRef incRef = pu.getMacroGraph();
    assertEquals(incRef.macroEventList.size(), 2);
    assertTrue(incRef.macroEventList.get(0) instanceof MacroDef);
    assertTrue(incRef.macroEventList.get(1) instanceof NamedMacroRef);
    NamedMacroRef nmr = (NamedMacroRef) incRef.macroEventList.get(1);
    assertEquals(nmr.getMacroDef(), incRef.macroEventList.get(0));
  }

  @Test
  public void test08() {
    File f = new File("src/test/resources/data/prepro2.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.parse();
    IncludeRef incRef = pu.getMacroGraph();
    assertEquals(incRef.macroEventList.size(), 3);
    assertTrue(incRef.macroEventList.get(0) instanceof MacroDef);
    assertTrue(incRef.macroEventList.get(1) instanceof NamedMacroRef);
    NamedMacroRef nmr = (NamedMacroRef) incRef.macroEventList.get(1);
    assertEquals(nmr.getMacroDef(), incRef.macroEventList.get(0));
    List<JPNode> nodes = pu.getTopNode().query(ABLNodeType.DEFINE);
    assertEquals(nodes.size(), 1);
    // Preprocessor magic... Keywords can start in main file, and end in include file...
    assertEquals(nodes.get(0).getFileIndex(), 0);
    assertEquals(nodes.get(0).getEndFileIndex(), 1);
    assertEquals(nodes.get(0).getLine(), 6);
    assertEquals(nodes.get(0).getEndLine(), 1);
    assertEquals(nodes.get(0).getColumn(), 1);
    assertEquals(nodes.get(0).getEndColumn(), 3);
  }

  @Test
  public void test09() {
    File f = new File("src/test/resources/data/prepro3.p");
    ParseUnit pu = new ParseUnit(f, session);
    pu.parse();
    List<JPNode> nodes = pu.getTopNode().query(ABLNodeType.SUBSTITUTE);
    assertEquals(nodes.size(), 2);
    JPNode substNode = nodes.get(0);
    JPNode leftParen = substNode.nextNode();
    JPNode str = leftParen.nextNode();
    assertEquals(leftParen.getLine(), 2);
    assertEquals(leftParen.getColumn(), 19);
    assertEquals(leftParen.getEndLine(), 2);
    assertEquals(leftParen.getEndColumn(), 19);
    assertEquals(str.getLine(), 2);
    assertEquals(str.getColumn(), 20);
    assertEquals(str.getEndLine(), 2);
    assertEquals(str.getEndColumn(), 24);

    JPNode substNode2 = nodes.get(1);
    JPNode leftParen2 = substNode2.nextNode();
    JPNode str2 = leftParen2.nextNode();
    assertEquals(leftParen2.getLine(), 3);
    assertEquals(leftParen2.getColumn(), 19);
    assertEquals(leftParen2.getEndLine(), 3);
    assertEquals(leftParen2.getEndColumn(), 19);
    assertEquals(str2.getLine(), 3);
    assertEquals(str2.getColumn(), 20);
    assertEquals(str2.getEndLine(), 3);
    // FIXME Wrong value, should be 25
    assertEquals(str2.getEndColumn(), 20);

    List<JPNode> dispNodes = pu.getTopNode().query(ABLNodeType.DISPLAY);
    assertEquals(dispNodes.size(), 1);
    JPNode dispNode = dispNodes.get(0);
    JPNode str3 = dispNode.nextNode().nextNode();
    assertEquals(str3.getLine(), 4);
    assertEquals(str3.getEndLine(), 4);
    assertEquals(str3.getColumn(), 9);
    // FIXME Wrong value, should be 14
    assertEquals(str3.getEndColumn(), 9);
  }

}
