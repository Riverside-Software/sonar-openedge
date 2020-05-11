/********************************************************************************
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

import static org.prorefactor.core.LexerTest.nextVisibleToken;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class PreprocessorDirectiveTest {
  private final static String SRC_DIR = "src/test/resources/data/preprocessor";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor05.p"), session);
    unit.parse();
    assertEquals(unit.getTopNode().query(ABLNodeType.PROPARSEDIRECTIVE).size(), 0);
    JPNode node1 = unit.getTopNode().query(ABLNodeType.MESSAGE).get(0);
    JPNode node2 = unit.getTopNode().query(ABLNodeType.MESSAGE).get(1);

    ProToken h1 = node1.getHiddenBefore();
    int numDirectives = 0;
    while (h1 != null) {
      if (h1.getType() == Proparse.PROPARSEDIRECTIVE) {
        numDirectives += 1;
      }
      h1 = (ProToken) h1.getHiddenBefore();
    }
    assertEquals(numDirectives, 1);
    assertTrue(node1.hasProparseDirective("xyz"));
    assertFalse(node1.hasProparseDirective("abc"));

    numDirectives = 0;
    ProToken h2 = node2.getHiddenBefore();
    while (h2 != null) {
      if (h2.getType() == Proparse.PROPARSEDIRECTIVE) {
        numDirectives += 1;
      }
      h2 = (ProToken) h2.getHiddenBefore();
    }
    assertEquals(numDirectives, 2);
    assertTrue(node2.hasProparseDirective("abc"));
    assertTrue(node2.hasProparseDirective("def"));
    assertTrue(node2.hasProparseDirective("hij"));
    assertFalse(node2.hasProparseDirective("klm"));
  }

  @Test
  public void test02() {
    // See issue #341 - Won't fix
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor07.p"), session);
    unit.parse();
  }

  @Test
  public void test03() throws IOException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor09.p"), session);
    TokenSource stream = unit.preprocess();

    assertEquals(nextVisibleToken(stream).getType(), Proparse.DEFINE);
    assertEquals(nextVisibleToken(stream).getType(), Proparse.VARIABLE);
    Token tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "aaa");
    assertEquals(nextVisibleToken(stream).getType(), Proparse.AS);
    assertEquals(nextVisibleToken(stream).getType(), Proparse.CHARACTER);
    assertEquals(nextVisibleToken(stream).getType(), Proparse.PERIOD);

    assertEquals(nextVisibleToken(stream).getType(), Proparse.MESSAGE);
    tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getText(), "\"text1 text2\"");
    assertEquals(nextVisibleToken(stream).getType(), Proparse.PERIOD);

    assertEquals(nextVisibleToken(stream).getType(), Proparse.MESSAGE);
    tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "aaa");
    tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getText(), "\"text3\"");
    tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "aaa");
    assertEquals(nextVisibleToken(stream).getType(), Proparse.PERIOD);

    assertEquals(nextVisibleToken(stream).getType(), Proparse.MESSAGE);
    tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "bbb");
    tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getText(), "'text4'");
    tok = nextVisibleToken(stream);
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "bbb");
    assertEquals(nextVisibleToken(stream).getType(), Proparse.PERIOD);
  }

  @Test
  public void test04() throws IOException {
    ParseUnit unit01 = new ParseUnit(new ByteArrayInputStream("{ preprocessor/preprocessor10.i &myParam=1 }".getBytes()), "<unnamed>", session);
    TokenSource stream01 = unit01.preprocess();
    assertEquals(nextVisibleToken(stream01).getType(), Proparse.TRUE);

    ParseUnit unit02 = new ParseUnit(new ByteArrayInputStream("{ preprocessor/preprocessor10.i &abc=1 &myParam }".getBytes()), "<unnamed>", session);
    TokenSource stream02 = unit02.preprocess();
    assertEquals(nextVisibleToken(stream02).getType(), Proparse.TRUE);
    IncludeRef events02 = (IncludeRef) unit02.getMacroSourceArray()[1];
    assertEquals(events02.numArgs(), 2);
    assertEquals(events02.getArgNumber(1).getName(), "abc");
    assertEquals(events02.getArgNumber(1).getValue(), "1");
    assertFalse(events02.getArgNumber(1).isUndefined());
    assertEquals(events02.getArgNumber(2).getName(), "myParam");
    assertTrue(events02.getArgNumber(2).isUndefined());

    ParseUnit unit03 = new ParseUnit(new ByteArrayInputStream("{ preprocessor/preprocessor10.i &abc &myParam }".getBytes()), "<unnamed>", session);
    TokenSource stream03 = unit03.preprocess();
    assertEquals(nextVisibleToken(stream03).getType(), Proparse.TRUE);

    ParseUnit unit04 = new ParseUnit(new ByteArrayInputStream("{ preprocessor/preprocessor10.i &myParam &abc }".getBytes()), "<unnamed>", session);
    TokenSource stream04 = unit04.preprocess();
    // Different behavior in ABL
    assertEquals(nextVisibleToken(stream04).getType(), Proparse.TRUE);
    IncludeRef events04 = (IncludeRef) unit04.getMacroSourceArray()[1];
    assertEquals(events04.numArgs(), 2);
    assertEquals(events04.getArgNumber(1).getName(), "myParam");
    assertTrue(events04.getArgNumber(1).isUndefined());
    assertEquals(events04.getArgNumber(2).getName(), "abc");
    assertTrue(events04.getArgNumber(2).isUndefined());

    ParseUnit unit05 = new ParseUnit(new ByteArrayInputStream("{ preprocessor/preprocessor10.i &abc &myParam=1 }".getBytes()), "<unnamed>", session);
    TokenSource stream05 = unit05.preprocess();
    assertEquals(nextVisibleToken(stream05).getType(), Proparse.TRUE);
    IncludeRef events05 = (IncludeRef) unit05.getMacroSourceArray()[1];
    assertEquals(events05.numArgs(), 2);
    assertEquals(events05.getArgNumber(1).getName(), "abc");
    assertTrue(events05.getArgNumber(1).isUndefined());
    assertEquals(events05.getArgNumber(2).getName(), "myParam");
    assertEquals(events05.getArgNumber(2).getValue(), "1");
    assertFalse(events05.getArgNumber(2).isUndefined());
  }

  @Test
  public void test05() throws IOException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor11.p"), session);
    TokenSource src = unit.preprocess();
    ProToken tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.DISPLAY);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPIF);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PREPROEXPR_TRUE);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPTHEN);
    tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"xx\"");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPIF);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PREPROEXPR_FALSE);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPTHEN);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPENDIF);
    tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPENDIF);
  }

  @Test
  public void test06() throws IOException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor12.p"), session);
    TokenSource src = unit.preprocess();
    ProToken tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.DISPLAY);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPIF);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PREPROEXPR_FALSE);
    assertEquals(tok.getText(), "FALSE");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPTHEN);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPENDIF);
    tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"zz2\"");
  }

  @Test
  public void test07() throws IOException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "preprocessor13.p"), session);
    TokenSource src = unit.preprocess();
    ProToken tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.DISPLAY);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPIF);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PREPROEXPR_FALSE);
    assertEquals(tok.getText(), "FALSE");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPTHEN);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPELSEIF);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PREPROEXPR_FALSE);
    assertEquals(tok.getText(), "FALSE");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPTHEN);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPELSEIF);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PREPROEXPR_TRUE);
    assertEquals(tok.getText(), "TRUE");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPTHEN);

    tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPENDIF);
  }
}
