/********************************************************************************
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
import java.io.UncheckedIOException;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.proparse.antlr4.MultiChannelTokenSource;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.base.Charsets;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class LexerTest {
  private final static String SRC_DIR = "src/test/resources/data/lexer";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testTokenList01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist01.p"), session);
    TokenSource src = unit.lex();

    // CURRENT-WINDOW:HANDLE.
    assertEquals(src.nextToken().getType(), Proparse.CURRENTWINDOW);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.HANDLE);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // SESSION:FIRST-SERVER-SOCKET:HANDLE.
    assertEquals(src.nextToken().getType(), Proparse.SESSION);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.HANDLE);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // TEMP-TABLE tt1::fld1.
    assertEquals(src.nextToken().getType(), Proparse.TEMPTABLE);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.DOUBLECOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // DATASET ds1::tt1.
    assertEquals(src.nextToken().getType(), Proparse.DATASET);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.DOUBLECOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // DATASET ds1::tt1:set-callback().
    assertEquals(src.nextToken().getType(), Proparse.DATASET);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.DOUBLECOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.LEFTPAREN);
    assertEquals(src.nextToken().getType(), Proparse.RIGHTPAREN);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
  }

  @Test
  public void testTokenList02() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist02.p"), session);
    TokenSource src = unit.lex();

    // Progress.Security.PAMStatus:AccessDenied.
    ProToken tok = (ProToken) src.nextToken();
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 27);
    assertEquals(tok.getChannel(), 0);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // Progress.Security.PAMStatus :AccessDenied.
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 2);
    assertEquals(tok.getEndCharPositionInLine(), 27);
    assertEquals(tok.getChannel(), 0);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // Progress.Security.PAMStatus <bazinga> :AccessDenied.
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getLine(), 3);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 3);
    assertEquals(tok.getEndCharPositionInLine(), 27);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getType(), Proparse.COMMENT);
    assertEquals(tok.getText(), "//Test");
    assertEquals(src.nextToken().getType(), Proparse.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getType(), Proparse.COMMENT);
    assertEquals(tok.getText(), "//Test2");
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // Progress.117x.clsName:StaticProperty.
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "Progress.117x.clsName");
    assertEquals(tok.getLine(), 7);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 7);
    assertEquals(tok.getEndCharPositionInLine(), 21);
    assertEquals(tok.getChannel(), 0);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
  }

  @Test(enabled = false)
  public void testTokenList03() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist03.p"), session);
    TokenSource src = unit.lex();

    // MESSAGE Progress./* Holy shit */   Security.PAMStatus:AccessDenied.
    // The compiler accepts that...
    assertEquals(src.nextToken().getType(), Proparse.MESSAGE);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    Token tok = src.nextToken();
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
  }

  @Test
  public void testTokenList04() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist04.p"), session);
    TokenSource src = unit.lex();

    // .Security.PAMStatus:AccessDenied.
    // Nothing recognized here, so we don't change the stream 
    assertEquals(src.nextToken().getType(), Proparse.NAMEDOT);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
  }

  @Test
  public void testTokenList05() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist05.p"), session);
    TokenSource src = unit.lex();

    // MESSAGE customer.custnum Progress.Security.PAMStatus:AccessDenied.
    assertEquals(src.nextToken().getType(), Proparse.MESSAGE);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);

    // MESSAGE customer.custnum. Progress.Security.PAMStatus:AccessDenied.
    assertEquals(src.nextToken().getType(), Proparse.MESSAGE);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
  }

  @Test
  public void testTokenList06() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist06.p"), session);
    TokenSource src = unit.lex();

    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
  }

  @Test
  public void testTokenList07() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist07.p"), session);
    TokenSource src = unit.lex();

    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.ID);
    assertEquals(src.nextToken().getType(), Proparse.PERIOD);
    assertEquals(src.nextToken().getType(), Proparse.WS);
  }

  @Test
  public void testTokenList08() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist08.p"), session);
    TokenSource src = unit.lex();
    assertEquals(src.nextToken().getType(), Proparse.COMMENT);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(src.nextToken().getType(), Proparse.FILE);
    assertEquals(src.nextToken().getType(), Proparse.WS);
    assertEquals(src.nextToken().getType(), Proparse.PLUS);
  }

  @Test
  public void testTokenList09() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist09.p"), session);
    TokenSource stream = unit.lex();
    // First line
    Token tok1 = stream.nextToken();
    assertEquals(tok1.getType(), Proparse.ID);
    assertEquals(tok1.getText(), "customer.name");
    assertEquals(stream.nextToken().getType(), Proparse.PERIOD);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    // Second line
    assertEquals(stream.nextToken().getType(), Proparse.ID);
    assertEquals(stream.nextToken().getType(), Proparse.PERIOD);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.ID);
    assertEquals(stream.nextToken().getType(), Proparse.PERIOD);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    // Third line: comment after period results in NAMEDOT
    Token tok2 = stream.nextToken();
    assertEquals(tok2.getType(), Proparse.ID);
    assertEquals(tok2.getText(), "customer.name");
    assertEquals(stream.nextToken().getType(), Proparse.PERIOD);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    // Fourth line: same behaviour even if there's a space after the comment
    Token tok3 = stream.nextToken();
    assertEquals(tok3.getType(), Proparse.ID);
    assertEquals(tok3.getText(), "customer.name");
    assertEquals(stream.nextToken().getType(), Proparse.PERIOD);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    // Fifth line: this line doesn't compile...
    assertEquals(stream.nextToken().getType(), Proparse.MESSAGE);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.QSTRING);
    assertEquals(stream.nextToken().getType(), Proparse.NAMEDOT);
    assertEquals(stream.nextToken().getType(), Proparse.COMMENT);
    assertEquals(stream.nextToken().getType(), Proparse.MESSAGE);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.QSTRING);
    assertEquals(stream.nextToken().getType(), Proparse.PERIOD);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    // Sixth line: same behaviour even if there's a space after the comment
    Token tok4 = stream.nextToken();
    assertEquals(tok4.getType(), Proparse.ID);
    assertEquals(tok4.getText(), "customer.name");
    assertEquals(stream.nextToken().getType(), Proparse.PERIOD);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
  }

  @Test
  public void testPostLexer01Init() {
    // First time verifying the channel locations
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "postlexer01.p"), session);
    TokenSource src = unit.preprocess();
    // Whitespaces on hidden channel
    Token tok = src.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    assertEquals(tok.getChannel(), 1);
    // Then scoped-define on a different channel again
    tok = src.nextToken();
    assertEquals(tok.getType(), Proparse.AMPSCOPEDDEFINE);
    assertEquals(tok.getChannel(), 2);
    // Whitespace again
    tok = src.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    assertEquals(tok.getChannel(), 1);
    // And again...
    tok = src.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    assertEquals(tok.getChannel(), 1);
    // Then the string
    tok = src.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
  }

  @Test
  public void testPostLexer01() {
    // First time verifying the channel locations
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "postlexer01.p"), session);
    TokenSource src = unit.preprocess();
    // Whitespaces on hidden channel
    Token tok = nextVisibleToken(src);
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
  }

  @Test
  public void testPostLexer02() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "postlexer02.p"), session);
    TokenSource src = unit.preprocess();
    Token tok = nextVisibleToken(src);
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getText(), "\"yy\"");
  }

  @Test
  public void testPostLexer03() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "postlexer03.p"), session);
    TokenSource src = unit.preprocess();
    Token tok = nextVisibleToken(src);
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
  }

  @Test
  public void testPostLexer04() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "postlexer04.p"), session);
    TokenSource src = unit.preprocess();
    Token tok = nextVisibleToken(src);
    assertEquals(tok.getType(), Proparse.QSTRING);
    // The best we can do right now... This is to cover edge cases in preprocessing...
    assertEquals(tok.getText(), "\"a'aabb'bxxx~\nyyy\"");
  }

  @Test
  public void testEndOfFile() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "tokenlist01.p"), session);
    TokenSource src = unit.lex();

    while (src.nextToken().getType() != Token.EOF) {

    }
    for (int zz = 0; zz < 1000; zz++) {
      // Verify safety net is not triggered
      src.nextToken();
    }
    // Make sure nextToken() always return EOF (and no null element or any exception)
    assertEquals(src.nextToken().getType(), Token.EOF);
    assertEquals(src.nextToken().getType(), Token.EOF);
  }

  @Test
  public void testAnalyzeSuspend() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer05.p"), session);
    TokenSource src = unit.lex();

    ProToken tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
    tok = nextToken(src, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());

    ParseUnit unit2 = new ParseUnit(new File(SRC_DIR, "lexer05.p"), session);
    unit2.parse();
    assertFalse(unit2.isInEditableSection(0, 9));
    assertFalse(unit2.isInEditableSection(0, 18));
    assertTrue(unit2.isInEditableSection(0, 28));
  }

  @Test
  public void testPreproErrorMessages01() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer06.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

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
  public void testPreproErrorMessages02() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer07.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

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
  public void testPreproErrorMessages03() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer08.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

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
  public void testPreproErrorMessages04() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer09.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

      }
    } catch (ProparseRuntimeException caught) {
      Assert.assertTrue(caught.getMessage().replace('\\', '/').startsWith(
          "File '" + SRC_DIR + "/lexer09.p' - Current position 'src/test/resources/data/lexer/lexer09.i':2"), caught.getMessage());
      Assert.assertTrue(caught.getMessage().endsWith("Unexpected &THEN"), caught.getMessage());
      return;
    } catch (Exception caught) {
      Assert.fail("Unwanted exception...");
    }
    Assert.fail("No exception found");
  }

  @Test
  public void testAnalyzeSuspendIncludeFile() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer10.p"), session);
    TokenSource stream = unit.preprocess();

    // First MESSAGE in main file
    ProToken tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());

    // First MESSAGE in first include file
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.getAnalyzeSuspend().isEmpty());
    assertTrue(tok.isEditableInAB());

    // Second MESSAGE in first include file
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertFalse(tok.getAnalyzeSuspend().isEmpty());
    assertFalse(tok.isEditableInAB());

    // MESSAGE in second include file
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());

    // Back to first include file
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNotNull(tok.getAnalyzeSuspend());
    assertTrue(tok.getAnalyzeSuspend().isEmpty());
    assertFalse(tok.isEditableInAB());

    // Back to main file
    tok = nextToken(stream, ABLNodeType.MESSAGE);
    assertNull(tok.getAnalyzeSuspend());
    assertTrue(tok.isEditableInAB());
  }

  @Test
  public void testQuotedStringPosition() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer11.p"), session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.DO);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 2);

    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.WHILE);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.ID);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.RIGHTANGLE);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    // Quoted string
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 15);
    assertEquals(tok.getEndLine(), 1);
    // The important test here, end column has to be 16 even when followed by ':'
    assertEquals(tok.getEndCharPositionInLine(), 16);

    // Colon
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.LEXCOLON);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 17);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 17);
  }

  @Test
  public void testQuotedStringPosition2() {
    // Same as previous test, but with a space before the colon
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer11-2.p"), session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.DO);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 2);

    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.WHILE);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.ID);
    assertEquals(stream.nextToken().getType(), Proparse.WS);
    assertEquals(stream.nextToken().getType(), Proparse.RIGHTANGLE);
    assertEquals(stream.nextToken().getType(), Proparse.WS);

    // Quoted string
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 15);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 16);

    assertEquals(stream.nextToken().getType(), Proparse.WS);

    // Colon
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.LEXCOLON);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 18);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 18);
  }

  @Test
  public void testQuotedStringPosition3() {
    // Same as previous test, but with a space before the colon
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer11-3.p"), session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 10);

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 11);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 11);

    assertEquals(stream.nextToken().getType(), Proparse.WS);

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 2);
    assertEquals(tok.getEndCharPositionInLine(), 6);
    
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getCharPositionInLine(), 7);
    assertEquals(tok.getEndLine(), 2);
    assertEquals(tok.getEndCharPositionInLine(), 7);

    assertEquals(stream.nextToken().getType(), Proparse.WS);

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 3);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 3);
    assertEquals(tok.getEndCharPositionInLine(), 8);

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
    assertEquals(tok.getLine(), 3);
    assertEquals(tok.getCharPositionInLine(), 9);
    assertEquals(tok.getEndLine(), 3);
    assertEquals(tok.getEndCharPositionInLine(), 9);
  }

  @Test(enabled = false)
  public void testMacroExpansion() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer12.p"), session);
    TokenSource stream = unit.preprocess();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.MESSAGE);
    assertTrue(tok.isMacroExpansion());

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertTrue(tok.isMacroExpansion());

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
    // Bug lies here in the lexer
    assertTrue(tok.isMacroExpansion());
  }

  @Test
  public void testUnicodeBom() {
    RefactorSession session2 = new RefactorSession(new ProparseSettings("src/test/resources/data"), new Schema(), Charsets.UTF_8);
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer13.p"), session2);
    TokenSource src = unit.preprocess();

    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testXCode1() {
    // Default behavior is that it shouldn't fail
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer14.p"), session);
    TokenSource src = unit.preprocess();

    // lexer14.i contains 'message "xcode".'
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getType(), Proparse.MESSAGE);
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 2);
    assertEquals(tok.getEndCharPositionInLine(), 7);
    
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"hello world\"");
  }

  @Test
  public void testXCode2() {
    // Test with customSkipXCode set to true
    ProparseSettings settings = new ProparseSettings("src/test/resources/data");
    settings.setCustomSkipXCode(true);
    RefactorSession session2 = new RefactorSession(settings, new Schema(), Charsets.UTF_8);
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer14.p"), session2);
    TokenSource src = unit.preprocess();

    // lexer14.i contains 'message "xcode".'
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getType(), Proparse.MESSAGE);
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 2);
    assertEquals(tok.getEndCharPositionInLine(), 7);
    
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"hello world\"");
  }

  @Test(expectedExceptions = UncheckedIOException.class)
  public void testXCode3() {
    // Test with customSkipXCode set to false
    ProparseSettings settings = new ProparseSettings("src/test/resources/data");
    settings.setCustomSkipXCode(false);
    RefactorSession session2 = new RefactorSession(settings, new Schema(), Charsets.UTF_8);
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer14.p"), session2);
    // Has to fail here
    unit.preprocess();
  }

  @Test
  public void testXCode4() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer14-2.p"), session);
    TokenSource src = unit.preprocess();

    // lexer14.i contains 'message "xcode".'
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getType(), Proparse.MESSAGE);

    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), Token.HIDDEN_CHANNEL);

    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 27);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 33);

    // Two xcoded include files are replaced by a two whitespaces leading to one token
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), Token.HIDDEN_CHANNEL);

    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 72);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 84);
  }

  @Test
  public void testProparseDirectiveLexPhase() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer15.p"), session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getChannel(), MultiChannelTokenSource.PROPARSE_CHANNEL);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), Token.HIDDEN_CHANNEL);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.EQUAL);
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);

    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getChannel(), MultiChannelTokenSource.PROPARSE_CHANNEL);
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "customer.custnum");
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.EQUAL);
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);

    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getChannel(), MultiChannelTokenSource.PROPARSE_CHANNEL);
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "sp2k.customer.custnum");
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.EQUAL);
    tok = (ProToken) stream.nextToken();
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testProparseDirectivePreprocessPhase() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer15.p"), session);
    TokenSource src = unit.preprocess();

    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "custnum");
    // FIXME Hidden tokens are attached in JPNodeVisitor, so this has to be tested in a later stage
    // assertNotNull(tok.getHiddenBefore());
    // assertEquals(((ProToken) tok.getHiddenBefore()).getNodeType(), ABLNodeType.WS);
    // assertNotNull(tok.getHiddenBefore().getHiddenBefore());
    // assertEquals(((ProToken) tok.getHiddenBefore().getHiddenBefore()).getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.EQUAL);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "customer.custnum");
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.EQUAL);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testHexNumbers() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer16.p"), session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.MESSAGE);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.NUMBER);
    assertEquals(tok.getText(), "125");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.NUMBER);
    assertEquals(tok.getText(), "0x65");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.NUMBER);
    assertEquals(tok.getText(), "0X66");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.NUMBER);
    assertEquals(tok.getText(), "0xfb");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.NUMBER);
    assertEquals(tok.getText(), "0xab");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.NUMBER);
    assertEquals(tok.getText(), "-0x01");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
  }

  @Test
  public void testHexNumbers2() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer17.p"), session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.MESSAGE);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.NUMBER);
    assertEquals(tok.getText(), "125");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getType(), Proparse.ID);
    assertEquals(tok.getText(), "0x2g8");
  }

  @Test
  public void testFileNumName() {
    ParseUnit unit = new ParseUnit(new File(SRC_DIR, "lexer18.p"), session);
    TokenSource src = unit.preprocess();

    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.STOP);
    assertEquals(tok.getTokenIndex(), 0);
    assertEquals(tok.getFileIndex(), 0);
    assertEquals(tok.getFileName().replace('\\', '/'), "src/test/resources/data/lexer/lexer18.p");

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getTokenIndex(), 2);
    assertEquals(tok.getFileIndex(), 1);
    assertEquals(tok.getFileName().replace('\\', '/'), "src/test/resources/data/lexer/lexer18.i");

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getTokenIndex(), 4);
    assertEquals(tok.getFileIndex(), 1);
    assertEquals(tok.getFileName().replace('\\', '/'), "src/test/resources/data/lexer/lexer18.i");

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QUIT);
    assertEquals(tok.getTokenIndex(), 6);
    assertEquals(tok.getFileIndex(), 2);
    assertEquals(tok.getFileName().replace('\\', '/'), "src/test/resources/data/lexer/lexer18-2.i");

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getTokenIndex(), 8);
    assertEquals(tok.getFileIndex(), 1);
    assertEquals(tok.getFileName().replace('\\', '/'), "src/test/resources/data/lexer/lexer18.i");

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getTokenIndex(), 10);
    assertEquals(tok.getFileIndex(), 1);
    assertEquals(tok.getFileName().replace('\\', '/'), "src/test/resources/data/lexer/lexer18.i");

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.STOP);
    assertEquals(tok.getTokenIndex(), 12);
    assertEquals(tok.getFileIndex(), 0);
    assertEquals(tok.getFileName().replace('\\', '/'), "src/test/resources/data/lexer/lexer18.p");
  }

  /**
   * Utility method for tests, returns next node of given type
   */
  private ProToken nextToken(TokenSource stream, ABLNodeType type) {
    ProToken tok = (ProToken) stream.nextToken();
    while (tok.getNodeType() != ABLNodeType.MESSAGE) {
      tok = (ProToken) stream.nextToken();
    }
    return tok;
  }

  /**
   * Utility method for preprocess(), removes all tokens from hidden channels
   */
  protected static Token nextVisibleToken(TokenSource src) {
    Token tok = src.nextToken();
    while ((tok.getType() != Token.EOF) && (tok.getChannel() != Token.DEFAULT_CHANNEL))
      tok = src.nextToken();
    return tok;
  }
}
