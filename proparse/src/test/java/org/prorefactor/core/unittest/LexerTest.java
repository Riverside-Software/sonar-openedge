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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;

public class LexerTest {
  private final static String SRC_DIR = "src/test/resources/data/lexer";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testTokenList01() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist01.p"), session);
    TokenStream stream = unit.lex();

    // CURRENT-WINDOW:HANDLE.
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.CURRENTWINDOW);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.HANDLE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // SESSION:FIRST-SERVER-SOCKET:HANDLE.
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.SESSION);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.HANDLE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // TEMP-TABLE tt1::fld1.
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.TEMPTABLE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.DOUBLECOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // DATASET ds1::tt1.
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.DATASET);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.DOUBLECOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // DATASET ds1::tt1:set-callback().
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.DATASET);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.DOUBLECOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.LEFTPAREN);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.RIGHTPAREN);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
  }

  @Test
  public void testTokenList02() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist02.p"), session);
    TokenStream stream = unit.lex();

    // Progress.Security.PAMStatus:AccessDenied.
    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndColumn(), 27);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // Progress.Security.PAMStatus :AccessDenied.
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 2);
    assertEquals(tok.getEndColumn(), 27);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // Progress.Security.PAMStatus <bazinga> :AccessDenied.
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 3);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 3);
    assertEquals(tok.getEndColumn(), 27);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.COMMENT);
    assertEquals(tok.getText(), "//Test");
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.COMMENT);
    assertEquals(tok.getText(), "//Test2");
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // Progress.117x.clsName:StaticProperty.
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.ID);
    assertEquals(tok.getText(), "Progress.117x.clsName");
    assertEquals(tok.getLine(), 7);
    assertEquals(tok.getColumn(), 1);
    assertEquals(tok.getEndLine(), 7);
    assertEquals(tok.getEndColumn(), 21);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
  }

  @Test(enabled = false)
  public void testTokenList03() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist03.p"), session);
    TokenStream stream = unit.lex();

    // MESSAGE Progress./* Holy shit */   Security.PAMStatus:AccessDenied.
    // The compiler accepts that...
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.MESSAGE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    Token tok = stream.nextToken();
    assertEquals(tok.getType(), ProParserTokenTypes.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
  }

  @Test
  public void testTokenList04() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist04.p"), session);
    TokenStream stream = unit.lex();

    // .Security.PAMStatus:AccessDenied.
    // Nothing recognized here, so we don't change the stream 
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.NAMEDOT);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.NAMEDOT);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
  }

  @Test
  public void testTokenList05() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist05.p"), session);
    TokenStream stream = unit.lex();

    // MESSAGE customer.custnum Progress.Security.PAMStatus:AccessDenied.
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.MESSAGE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.NAMEDOT);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);

    // MESSAGE customer.custnum. Progress.Security.PAMStatus:AccessDenied.
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.MESSAGE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.NAMEDOT);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
  }

  @Test
  public void testTokenList06() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist06.p"), session);
    TokenStream stream = unit.lex();

    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
  }

  @Test
  public void testTokenList07() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist07.p"), session);
    TokenStream stream = unit.lex();

    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.ID);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PERIOD);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
  }

  @Test
  public void testTokenList08() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist08.p"), session);
    TokenStream stream = unit.lex();
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.COMMENT);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.OBJCOLON);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.FILE);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.WS);
    assertEquals(stream.nextToken().getType(), ProParserTokenTypes.PLUS);
  }

  @Test
  public void testEndOfFile() throws TokenStreamException {
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

  @Test
  public void testAnalyzeSuspend() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer05.p"), session);
    TokenStream stream = unit.lex();

    ProToken tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.getAnalyzeSuspend().isEmpty());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
  }

  @Test
  public void testPreproErrorMessages01() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer06.p"), session);
    try {
      TokenStream stream = unit.preprocess();
      while (stream.nextToken().getType() != Token.EOF_TYPE) {

      }
    } catch (ProparseRuntimeException caught) {
      Assert.assertTrue(caught.getMessage().replace('\\', '/').startsWith("File '" + SRC_DIR + "/lexer06.p'"));
      Assert.assertTrue(caught.getMessage().endsWith("Unexpected &THEN"));
      return;
    } catch (Exception caught) {
      Assert.fail("Unwanted exception...");
    }
    Assert.fail("No exception found");
  }

  @Test
  public void testPreproErrorMessages02() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer07.p"), session);
    try {
      TokenStream stream = unit.preprocess();
      while (stream.nextToken().getType() != Token.EOF_TYPE) {

      }
    } catch (ProparseRuntimeException caught) {
      Assert.assertTrue(caught.getMessage().replace('\\', '/').startsWith("File '" + SRC_DIR + "/lexer07.p'"));
      Assert.assertTrue(caught.getMessage().endsWith("Unexpected end of input after &IF or &ELSEIF"));
      return;
    } catch (Exception caught) {
      Assert.fail("Unwanted exception...");
    }
    Assert.fail("No exception found");
  }

  @Test
  public void testPreproErrorMessages03() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer08.p"), session);
    try {
      TokenStream stream = unit.preprocess();
      while (stream.nextToken().getType() != Token.EOF_TYPE) {

      }
    } catch (ProparseRuntimeException caught) {
      Assert.assertTrue(caught.getMessage().replace('\\', '/').startsWith("File '" + SRC_DIR + "/lexer08.p'"));
      Assert.assertTrue(
          caught.getMessage().endsWith("Unexpected end of input when consuming discarded &IF/&ELSEIF/&ELSE text"));
      return;
    } catch (Exception caught) {
      Assert.fail("Unwanted exception...");
    }
    Assert.fail("No exception found");
  }

  @Test
  public void testPreproErrorMessages04() throws TokenStreamException {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer09.p"), session);
    try {
      TokenStream stream = unit.preprocess();
      while (stream.nextToken().getType() != Token.EOF_TYPE) {

      }
    } catch (ProparseRuntimeException caught) {
      Assert.assertTrue(caught.getMessage().replace('\\', '/').startsWith(
          "File '" + SRC_DIR + "/lexer09.p' - Current position 'data/lexer/lexer09.i':2"));
      Assert.assertTrue(caught.getMessage().endsWith("Unexpected &THEN"));
      return;
    } catch (Exception caught) {
      Assert.fail("Unwanted exception...");
    }
    Assert.fail("No exception found");
  }

  private ProToken nextToken(TokenStream stream, ABLNodeType type) throws TokenStreamException {
    ProToken tok = (ProToken) stream.nextToken();
    while (tok.getNodeType() != ABLNodeType.MESSAGE) {
      tok = (ProToken) stream.nextToken();
    }
    return tok;
  }

}
