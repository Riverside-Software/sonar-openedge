/*******************************************************************************
 * Copyright (c) 2017 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.NoSuchElementException;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.Token;
import antlr.TokenStream;

public class LexerTest {
  private final static String SRC_DIR = "src/test/resources/data/lexer";

  private RefactorSession session;

  @BeforeTest
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testTokenList01() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist01.p"), session);
    TokenStream stream = unit.lex();

    // CURRENT-WINDOW:HANDLE.
    assertEquals(stream.nextToken().getType(), NodeTypes.CURRENTWINDOW);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.HANDLE);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // SESSION:FIRST-SERVER-SOCKET:HANDLE.
    assertEquals(stream.nextToken().getType(), NodeTypes.SESSION);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.HANDLE);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // TEMP-TABLE tt1::fld1.
    assertEquals(stream.nextToken().getType(), NodeTypes.TEMPTABLE);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.DOUBLECOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // DATASET ds1::tt1.
    assertEquals(stream.nextToken().getType(), NodeTypes.DATASET);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.DOUBLECOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // DATASET ds1::tt1:set-callback().
    assertEquals(stream.nextToken().getType(), NodeTypes.DATASET);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.DOUBLECOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.LEFTPAREN);
    assertEquals(stream.nextToken().getType(), NodeTypes.RIGHTPAREN);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
  }

  @Test
  public void testTokenList02() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist02.p"), session);
    TokenStream stream = unit.lex();

    // Progress.Security.PAMStatus:AccessDenied.
    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), NodeTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndColumn(), 27);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // Progress.Security.PAMStatus :AccessDenied.
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), NodeTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 2);
    assertEquals(tok.getEndColumn(), 27);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // Progress.Security.PAMStatus <bazinga> :AccessDenied.
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), NodeTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 3);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 3);
    assertEquals(tok.getEndColumn(), 27);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), NodeTypes.COMMENT);
    assertEquals(tok.getText(), "//Test");
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), NodeTypes.COMMENT);
    assertEquals(tok.getText(), "//Test2");
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // Progress.117x.clsName:StaticProperty.
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), NodeTypes.ID);
    assertEquals(tok.getText(), "Progress.117x.clsName");
    assertEquals(tok.getLine(), 7);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 7);
    assertEquals(tok.getEndColumn(), 21);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
  }

  @Test(enabled = false)
  public void testTokenList03() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist03.p"), session);
    TokenStream stream = unit.lex();

    // MESSAGE Progress./* Holy shit */   Security.PAMStatus:AccessDenied.
    // The compiler accepts that...
    assertEquals(stream.nextToken().getType(), NodeTypes.MESSAGE);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    Token tok = stream.nextToken();
    assertEquals(tok.getType(), NodeTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
  }

  @Test(expectedExceptions = NoSuchElementException.class)
  public void testTokenList04() throws Exception {
    // TokenList throws an exception, but not really bad, as the syntax is by the way invalid
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist04.p"), session);
    TokenStream stream = unit.lex();
    stream.nextToken();
  }

  @Test
  public void testTokenList05() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist05.p"), session);
    TokenStream stream = unit.lex();

    // MESSAGE customer.custnum Progress.Security.PAMStatus:AccessDenied.
    assertEquals(stream.nextToken().getType(), NodeTypes.MESSAGE);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.NAMEDOT);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);

    // MESSAGE customer.custnum. Progress.Security.PAMStatus:AccessDenied.
    assertEquals(stream.nextToken().getType(), NodeTypes.MESSAGE);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.NAMEDOT);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), NodeTypes.ID);
    assertEquals(stream.nextToken().getType(), NodeTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), NodeTypes.WS);
  }

  @Test
  public void testEndOfFile() throws Exception {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist01.p"), session);
    TokenStream stream = unit.lex();

    while (stream.nextToken().getType() != Token.EOF_TYPE) {

    }
    for (int zz = 0; zz < 1000; zz++) {
      // Verify safety net is not triggered
      stream.nextToken();
    }
    // Make sure nextToken() always return EOF (and no null element or any exception)
    assertEquals(stream.nextToken().getType(), Token.EOF_TYPE);
    assertEquals(stream.nextToken().getType(), Token.EOF_TYPE);
  }

}
