/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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
package org.prorefactor.proparse;

import static org.prorefactor.proparse.TokenSourceUtils.assertNextTokenType;
import static org.prorefactor.proparse.TokenSourceUtils.assertNextTokenTypeWS;
import static org.prorefactor.proparse.TokenSourceUtils.firstToken;
import static org.prorefactor.proparse.TokenSourceUtils.nextMessageToken;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.WritableProToken;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.macrolevel.PreprocessorEventListener;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

public class ABLLexerTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void testDecimalNumber01() {
    final String source = "rct1:height = 0.1\n rct1:row ";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.HEIGHT, "height");
    assertNextTokenTypeWS(lexer, ABLNodeType.EQUAL, "=");
    assertNextTokenTypeWS(lexer, ABLNodeType.NUMBER, "0.1");
    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.ROW, "row");
  }

  @Test
  public void testDecimalNumber02() {
    final String source = "rct1:height = .1\n rct1:row ";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.HEIGHT, "height");
    assertNextTokenTypeWS(lexer, ABLNodeType.EQUAL, "=");
    assertNextTokenTypeWS(lexer, ABLNodeType.NUMBER, ".1"); // White space is unfortunately gobbled
    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.ROW, "row");
  }

  @Test
  public void testDecimalNumber03() {
    final String source = "rct1:height = var1 * .1\n rct1:row ";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.HEIGHT, "height");
    assertNextTokenTypeWS(lexer, ABLNodeType.EQUAL, "=");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "var1");
    assertNextTokenTypeWS(lexer, ABLNodeType.STAR, "*");
    assertNextTokenTypeWS(lexer, ABLNodeType.NUMBER, ".1"); // White space is unfortunately gobbled
    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.ROW, "row");
  }

  @Test
  public void testDecimalNumber04() {
    final String source = "rct1:height = .1 * var1\nrct1:row ";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.HEIGHT, "height");
    assertNextTokenTypeWS(lexer, ABLNodeType.EQUAL, "=");
    assertNextTokenTypeWS(lexer, ABLNodeType.NUMBER, ".1");
    assertNextTokenTypeWS(lexer, ABLNodeType.STAR, "*");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "var1");
    assertNextTokenType(lexer, ABLNodeType.ID, "rct1");
    assertNextTokenType(lexer, ABLNodeType.OBJCOLON, ":");
    assertNextTokenTypeWS(lexer, ABLNodeType.ROW, "row");
  }

  @Test
  public void testSymbols() {
    final String source = "+ - +123 -123 += -= * *= / /= ";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.PLUS);
    assertNextTokenTypeWS(lexer, ABLNodeType.MINUS);
    assertNextTokenTypeWS(lexer, ABLNodeType.NUMBER);
    assertNextTokenTypeWS(lexer, ABLNodeType.NUMBER);
    assertNextTokenTypeWS(lexer, ABLNodeType.PLUSEQUAL);
    assertNextTokenTypeWS(lexer, ABLNodeType.MINUSEQUAL);
    assertNextTokenTypeWS(lexer, ABLNodeType.STAR);
    assertNextTokenTypeWS(lexer, ABLNodeType.STAREQUAL);
    assertNextTokenTypeWS(lexer, ABLNodeType.SLASH);
    assertNextTokenTypeWS(lexer, ABLNodeType.SLASHEQUAL);
  }

  @Test
  public void testAmpersand01() {
    final String source = "MESSAGE &giélles &ab-cd &ab#$%&&-_cd & && &ab/*foo*/ &ab/def message";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.MESSAGE);
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "&giélles");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "&ab-cd");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "&ab#$%&&-_cd");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "&");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "&&");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "&ab");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "&ab/def");
    assertNextTokenType(lexer, ABLNodeType.MESSAGE, "message");
  }

  @Test
  public void testSlash01() {
    final String source = "MESSAGE / xyz /(2) /10";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.MESSAGE);
    assertNextTokenTypeWS(lexer, ABLNodeType.SLASH, "/");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "xyz");
    assertNextTokenType(lexer, ABLNodeType.SLASH, "/");
    assertNextTokenType(lexer, ABLNodeType.LEFTPAREN, "(");
    assertNextTokenType(lexer, ABLNodeType.NUMBER, "2");
    assertNextTokenTypeWS(lexer, ABLNodeType.RIGHTPAREN, ")");
    assertNextTokenType(lexer, ABLNodeType.SLASH, "/");
    assertNextTokenType(lexer, ABLNodeType.NUMBER, "10");
  }

  @Test
  public void testSlash02() throws IOException {
    RefactorSession session2 = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ((ProparseSettings) session2.getProparseSettings()).setTokenStartChars(new char[] {'/'});

    final String source = "MESSAGE / xyz /(2) /10";
    ABLLexer lexer = new ABLLexer(session2, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.MESSAGE);
    assertNextTokenTypeWS(lexer, ABLNodeType.SLASH, "/");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "xyz");
    assertNextTokenType(lexer, ABLNodeType.SLASH, "/");
    assertNextTokenType(lexer, ABLNodeType.LEFTPAREN, "(");
    assertNextTokenType(lexer, ABLNodeType.NUMBER, "2");
    assertNextTokenTypeWS(lexer, ABLNodeType.RIGHTPAREN, ")");
    assertNextTokenType(lexer, ABLNodeType.FILENAME, "/10");
  }

  @Test
  public void testSlash03() {
    final String source = "MESSAGE //Test\nMESSAGE";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.MESSAGE);
    assertNextTokenTypeWS(lexer, ABLNodeType.COMMENT, "//Test");
    assertNextTokenType(lexer, ABLNodeType.MESSAGE);
  }

  @Test
  public void testSlash04() {
    final String source = "MESSAGE /*Test*/ MESSAGE";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.MESSAGE);
    assertNextTokenTypeWS(lexer, ABLNodeType.COMMENT, "/*Test*/");
    assertNextTokenType(lexer, ABLNodeType.MESSAGE);
  }

  @Test
  public void testAt01() {
    final String source = "message h@ello @hello @ RETURNS";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.MESSAGE);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h@ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.ANNOTATION, "@hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.LEXAT, "@");
    assertNextTokenType(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testCaret01() {
    final String source = "FUNCTION h^ello ^hello ^ RETURNS VOID";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h^ello");
    assertNextTokenType(lexer, ABLNodeType.CARET, "^");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.CARET, "^");
    assertNextTokenTypeWS(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testCaret02() throws IOException {
    RefactorSession session2 = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ((ProparseSettings) session2.getProparseSettings()).setTokenStartChars(new char[] {'^'});

    final String source = "FUNCTION h^ello ^hello ^ RETURNS VOID";
    ABLLexer lexer = new ABLLexer(session2, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h^ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "^hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.CARET, "^");
    assertNextTokenTypeWS(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testSemiColon01() {
    final String source = "FUNCTION h;ello ;hello ; RETURNS VOID";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h;ello");
    assertNextTokenType(lexer, ABLNodeType.SEMI, ";");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.SEMI, ";");
    assertNextTokenTypeWS(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testSemiColon02() throws IOException {
    RefactorSession session2 = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ((ProparseSettings) session2.getProparseSettings()).setTokenStartChars(new char[] {';'});

    final String source = "FUNCTION h;ello ;hello ; RETURNS VOID";
    ABLLexer lexer = new ABLLexer(session2, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h;ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, ";hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.SEMI, ";");
    assertNextTokenTypeWS(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testStar01() {
    final String source = "FUNCTION h*ello *hello * *= RETURNS";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h*ello");
    assertNextTokenType(lexer, ABLNodeType.STAR, "*");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.STAR, "*");
    assertNextTokenTypeWS(lexer, ABLNodeType.STAREQUAL, "*=");
    assertNextTokenType(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testStar02() throws IOException {
    RefactorSession session2 = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ((ProparseSettings) session2.getProparseSettings()).setTokenStartChars(new char[] {'*'});

    final String source = "FUNCTION h*ello *hello * *= VOID";
    ABLLexer lexer = new ABLLexer(session2, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h*ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "*hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.STAR, "*");
    assertNextTokenTypeWS(lexer, ABLNodeType.STAREQUAL, "*=");
    assertNextTokenType(lexer, ABLNodeType.VOID);
  }

  @Test
  public void testExclamationHashPercent01() {
    final String source = "FUNCTION h#ello h%ello #hello %hello # % RETURNS ";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h#ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h%ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "#hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "%hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "#");
    assertNextTokenTypeWS(lexer, ABLNodeType.FILENAME, "%");
    assertNextTokenTypeWS(lexer, ABLNodeType.RETURNS, "RETURNS");
  }

  @Test
  public void testExclamationMarkBacktick01() {
    final String source = "FUNCTION h!ello !hello ! h`ello `hello ` RETURNS";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h!ello");
    assertNextTokenType(lexer, ABLNodeType.EXCLAMATION, "!");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.EXCLAMATION, "!");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h`ello");
    assertNextTokenType(lexer, ABLNodeType.BACKTICK, "`");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.BACKTICK, "`");
    assertNextTokenType(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testExclamationMarkBacktick02() throws IOException {
    RefactorSession session2 = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    ((ProparseSettings) session2.getProparseSettings()).setTokenStartChars(new char[] {'!', '`'});

    final String source = "FUNCTION h!ello !hello ! h`ello `hello ` RETURNS";
    ABLLexer lexer = new ABLLexer(session2, ByteSource.wrap(source.getBytes()), true);

    assertNextTokenTypeWS(lexer, ABLNodeType.FUNCTION);
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h!ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "!hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.EXCLAMATION, "!");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "h`ello");
    assertNextTokenTypeWS(lexer, ABLNodeType.ID, "`hello");
    assertNextTokenTypeWS(lexer, ABLNodeType.BACKTICK, "`");
    assertNextTokenType(lexer, ABLNodeType.RETURNS);
  }

  @Test
  public void testEndOfFile() {
    // Could be anything...
    final String source = "CURRENT-WINDOW:HANDLE. SESSION:FIRST-SERVER-SOCKET:HANDLE. TEMP-TABLE tt1::fld1. DATASET ds1::tt1. DATASET ds1::tt1:set-callback().";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), true);

    while (lexer.nextToken().getType() != Token.EOF) {
      // Consume until the end of the file
    }
    for (int zz = 0; zz < 1000; zz++) {
      // Verify safety net is not triggered
      lexer.nextToken();
    }
    // Make sure nextToken() always return EOF (and no null element or any exception)
    assertEquals(lexer.nextToken().getType(), Token.EOF);
    assertEquals(lexer.nextToken().getType(), Token.EOF);
  }

  @Test
  public void testSomeKeywords01() {
    final String source = "CURRENT-WINDOW:HANDLE. SESSION:FIRST-SERVER-SOCKET:HANDLE. TEMP-TABLE tt1::fld1. DATASET ds1::tt1. DATASET ds1::tt1:set-callback().";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), "file.txt");

    // CURRENT-WINDOW:HANDLE.
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.CURRENTWINDOW);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.HANDLE);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);

    // SESSION:FIRST-SERVER-SOCKET:HANDLE.
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.SESSION);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "FIRST-SERVER-SOCKET");
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.HANDLE);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);

    // TEMP-TABLE tt1::fld1.
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.TEMPTABLE);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.DOUBLECOLON);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);

    // DATASET ds1::tt1.
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.DATASET);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.DOUBLECOLON);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);

    // DATASET ds1::tt1:set-callback().
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.DATASET);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "ds1");
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.DOUBLECOLON);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "tt1");
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "set-callback");
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.LEFTPAREN);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.RIGHTPAREN);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testAnalyzeSuspend() {
    try (InputStream input = new FileInputStream(new File("src/test/resources/data/lexer/lexer05.p"))) {
      ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(ByteStreams.toByteArray(input)), "file.txt");
      nextMessageToken(lexer, false, true);
      nextMessageToken(lexer, true, false);
      nextMessageToken(lexer, true, true);
      nextMessageToken(lexer, true, false);
      nextMessageToken(lexer, true, true);
      nextMessageToken(lexer, true, true);
      nextMessageToken(lexer, true, false);
      nextMessageToken(lexer, true, true);
      nextMessageToken(lexer, true, true);
      nextMessageToken(lexer, true, true);
      nextMessageToken(lexer, true, true);
    } catch (IOException uncaught) {
      fail("Unable to open file", uncaught);
    }
  }

  @Test
  public void testQuotedStringPosition01() {
    // Same as previous test, but with a space before the colon
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("do while xx > '': end.".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.DO);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 0);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 2);

    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WHILE);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.RIGHTANGLE);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    // Quoted string
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 14);
    assertEquals(tok.getEndLine(), 1);
    // The important test here, end column has to be 16 even when followed by ':'
    assertEquals(tok.getEndCharPositionInLine(), 16);

    // Colon
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.LEXCOLON);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 16);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 17);
  }

  @Test
  public void testQuotedStringPosition02() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("do while xx > '' : end.".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.DO);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 0);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 2);

    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WHILE);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.RIGHTANGLE);
    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);

    // Quoted string
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 14);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 16);

    assertEquals(((ProToken) lexer.nextToken()).getNodeType(), ABLNodeType.WS);

    // Colon
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.LEXCOLON);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 17);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 18);
  }

  @Test
  public void testQuotedStringPosition03() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("\"Test\":L10.".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 0);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 10);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 10);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 11);
  }

  @Test
  public void testQuotedStringPosition04() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("\"Test\".".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 0);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 6);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 6);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 7);
  }

  @Test
  public void testQuotedStringPosition05() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("\"Test\":U.".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 0);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 8);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 8);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 9);
  }

  @Test
  public void testTypeName() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("CLASS Riverside.20190101.Object".getBytes()), "file.txt");

    assertNextTokenTypeWS(lexer, ABLNodeType.CLASS, "CLASS");
    assertNextTokenType(lexer, ABLNodeType.ID, "Riverside");
    assertNextTokenType(lexer, ABLNodeType.NUMBER, ".20190101");
  }

  @Test
  public void testAnnotation01() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("@Riverside.Lang.Object. MESSAGE 'foo'.".getBytes()), "file.txt");

    assertNextTokenType(lexer, ABLNodeType.ANNOTATION, "@Riverside");
    assertNextTokenType(lexer, ABLNodeType.NAMEDOT, ".");
    assertNextTokenType(lexer, ABLNodeType.ID, "Lang");
    assertNextTokenType(lexer, ABLNodeType.NAMEDOT, ".");
    assertNextTokenType(lexer, ABLNodeType.OBJECT, "Object");
    assertNextTokenType(lexer, ABLNodeType.PERIOD, ".");
  }

  @Test
  public void testAnnotation02() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("@Riverside.20190101.Object. MESSAGE 'foo'.".getBytes()), "file.txt");

    assertNextTokenType(lexer, ABLNodeType.ANNOTATION, "@Riverside");
    assertNextTokenType(lexer, ABLNodeType.NUMBER, ".20190101");
    assertNextTokenType(lexer, ABLNodeType.NAMEDOT, ".");
    assertNextTokenType(lexer, ABLNodeType.OBJECT, "Object");
    assertNextTokenType(lexer, ABLNodeType.PERIOD, ".");
  }

  @Test
  public void testKeywords01() {
    ABLLexer lexer = new ABLLexer(session,
        ByteSource.wrap(
            "AUTO-ENDKEY AUTO-END-KEY CAPS UPPER COM-HANDLE COMPONENT-HANDLE EXCLUSIVE EXCLUSIVE-LOCK".getBytes()),
        "file.txt");
    assertNextTokenTypeWS(lexer, ABLNodeType.AUTOENDKEY, "AUTO-ENDKEY");
    assertNextTokenTypeWS(lexer, ABLNodeType.AUTOENDKEY, "AUTO-END-KEY");
    assertNextTokenTypeWS(lexer, ABLNodeType.CAPS, "CAPS");
    assertNextTokenTypeWS(lexer, ABLNodeType.CAPS, "UPPER");
    assertNextTokenTypeWS(lexer, ABLNodeType.COMHANDLE, "COM-HANDLE");
    assertNextTokenTypeWS(lexer, ABLNodeType.COMHANDLE, "COMPONENT-HANDLE");
    assertNextTokenTypeWS(lexer, ABLNodeType.EXCLUSIVELOCK, "EXCLUSIVE");
    assertNextTokenType(lexer, ABLNodeType.EXCLUSIVELOCK, "EXCLUSIVE-LOCK");

    ABLLexer lexer2 = new ABLLexer(session,
        ByteSource.wrap(
            "INIT INITIAL LC LOWER NO-ATTR NO-ATTR-SPACE NO-ATTR-LIST TRANS TRANSAC TRANSACT TRANSACTION VAR VARI VARIABLE".getBytes()),
        "file.txt");
    assertNextTokenTypeWS(lexer2, ABLNodeType.INITIAL, "INIT");
    assertNextTokenTypeWS(lexer2, ABLNodeType.INITIAL, "INITIAL");
    assertNextTokenTypeWS(lexer2, ABLNodeType.LC, "LC");
    assertNextTokenTypeWS(lexer2, ABLNodeType.LC, "LOWER");
    assertNextTokenTypeWS(lexer2, ABLNodeType.NOATTRSPACE, "NO-ATTR");
    assertNextTokenTypeWS(lexer2, ABLNodeType.NOATTRSPACE, "NO-ATTR-SPACE");
    assertNextTokenTypeWS(lexer2, ABLNodeType.NOATTRLIST, "NO-ATTR-LIST");
    assertNextTokenTypeWS(lexer2, ABLNodeType.TRANSACTION, "TRANS");
    assertNextTokenTypeWS(lexer2, ABLNodeType.ID, "TRANSAC");
    assertNextTokenTypeWS(lexer2, ABLNodeType.TRANSACTION, "TRANSACT");
    assertNextTokenTypeWS(lexer2, ABLNodeType.TRANSACTION, "TRANSACTION");
    assertNextTokenTypeWS(lexer2, ABLNodeType.VAR, "VAR");
    assertNextTokenTypeWS(lexer2, ABLNodeType.VARIABLE, "VARI");
    assertNextTokenType(lexer2, ABLNodeType.VARIABLE, "VARIABLE");

    ABLLexer lexer3 = new ABLLexer(session, ByteSource.wrap("USERID USER-ID ".getBytes()), "file.txt");
    assertNextTokenTypeWS(lexer3, ABLNodeType.USERID, "USERID");
    assertNextTokenTypeWS(lexer3, ABLNodeType.USERID2, "USER-ID");
  }

  @Test
  public void testAbbrevKeywords() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("LC('HELLO').".getBytes()), "file.txt");
    ProToken tok = (ProToken) lexer.nextToken();
    assertFalse(tok.isAbbreviated());

    lexer = new ABLLexer(session, ByteSource.wrap("LOWER('WORLD').".getBytes()), "file.txt");
    tok = (ProToken) lexer.nextToken();
    assertFalse(tok.isAbbreviated());

    lexer = new ABLLexer(session, ByteSource.wrap("FILE-INFO.".getBytes()), "file.txt");
    tok = (ProToken) lexer.nextToken();
    assertTrue(tok.isAbbreviated());

    lexer = new ABLLexer(session, ByteSource.wrap("FILE-INFORMATION.".getBytes()), "file.txt");
    tok = (ProToken) lexer.nextToken();
    assertFalse(tok.isAbbreviated());

    lexer = new ABLLexer(session, ByteSource.wrap("SUBST('').".getBytes()), "file.txt");
    tok = (ProToken) lexer.nextToken();
    assertTrue(tok.isAbbreviated());

    lexer = new ABLLexer(session, ByteSource.wrap("SUBSTITUTE('').".getBytes()), "file.txt");
    tok = (ProToken) lexer.nextToken();
    assertFalse(tok.isAbbreviated());
  }

  @Test
  public void testWritableTokens() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("  MESSAGE 'Hello'".getBytes()), "file.txt");
    lexer.enableWritableTokens();
    lexer.nextToken(); // Skip first WS
    Token tok = lexer.nextToken();
    assertNotNull(tok);
    assertTrue(tok instanceof WritableProToken);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 2);
    WritableProToken tok2 = (WritableProToken) tok;
    tok2.setLine(5);
    tok2.setCharPositionInLine(4);
    assertEquals(tok.getLine(), 5);
    assertEquals(tok.getCharPositionInLine(), 4);
  }

  @Test
  public void testNestedComments() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("/* Gilles */ /* Gilles /* Querret */ Test */".getBytes()), "file.txt");
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.COMMENT);
    assertFalse(tok.hasNestedComments());
    lexer.nextToken();
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.COMMENT);
    assertTrue(tok.hasNestedComments());
  }

  @Test
  public void testTilde() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("IF TRUE // W1\\rW2 ~\n THEN MESSAGE \"XXX\".\n ELSE MESSAGE \"YYY\".".getBytes()), "file.txt");
    ProToken tok = firstToken(lexer, ABLNodeType.COMMENT);
    assertNotNull(tok);
    // Backslash and tildes are not escaped
    assertEquals(tok.getText(), "// W1\\rW2 ~");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.THEN);
  }

  @Test
  public void testCommentInPrepro() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(
        "&global-define /* foobar /*foobar2*/ foobar1 */ foo /* foobar */ bar /* foobar */ /* foobar */".getBytes()),
        "file.txt");
    assertNextTokenType(lexer, ABLNodeType.AMPGLOBALDEFINE, "&global-define  foo  bar  ");
  }

  @Test
  public void testEscapeInPrepro() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("&GLOBAL-DEFINE machin truc ~\nchouette".getBytes()),
        "file.txt");
    assertNextTokenType(lexer, ABLNodeType.AMPGLOBALDEFINE, "&GLOBAL-DEFINE machin truc chouette");
  }

  @Test(enabled = false)
  public void testQuotesInPrepro01() {
    String code = "&SCOPED-DEFINE EMPTY1\n&SCOPED-DEFINE EMPTY2\"\"\n\n&MESSAGE X{&EMPTY1}X{&EMPTY2}X\n";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(code.getBytes()), true);
    Token tok = lexer.nextToken();
    while (tok.getType() != Token.EOF) {
      tok = lexer.nextToken();
    }
    List<String> msgs = ((PreprocessorEventListener) lexer.getLstListener()).getMessages();
    assertNotNull(msgs);
    assertEquals(msgs.size(), 1);
    // The &MESSAGE should be the full value. Current implementation removes quotes (during variable definition).
    assertEquals(msgs.get(0), "XX\"\"X");
  }

  @Test(enabled = false)
  public void testQuotesInPrepro02() {
    String code = "&SCOPED-DEFINE EMPTY1\n&SCOPED-DEFINE EMPTY2\"\"\n\n{ lexer/lexer24.i &P1 = \"{&EMPTY1}\" &P2 = \"{&EMPTY2}\" }\n";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(code.getBytes()), false);
    Token tok = lexer.nextToken();
    while (tok.getType() != Token.EOF) {
      tok = lexer.nextToken();
    }
    List<String> msgs = ((PreprocessorEventListener) lexer.getLstListener()).getMessages();
    assertNotNull(msgs);
    assertEquals(msgs.size(), 1);
    // The &MESSAGE should be the full value. Current implementation removes quotes (during variable definition).
    assertEquals(msgs.get(0), "X\"X\"\"X");
  }

}
