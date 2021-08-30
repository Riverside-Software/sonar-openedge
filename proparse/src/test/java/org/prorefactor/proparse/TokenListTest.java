/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TokenListTest {
  private final static String SRC_DIR = "src/test/resources/data/lexer";

  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testTokenList02() {
    try (InputStream input = new FileInputStream(new File(SRC_DIR, "tokenlist02.p"))) {
      ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(ByteStreams.toByteArray(input)), "file.txt");
      TokenSource filter0 = new NameDotTokenFilter(lexer);
      TokenSource src = new TokenList(filter0);

      // Progress.Security.PAMStatus:AccessDenied.
      ProToken tok = (ProToken) ((ProToken) src.nextToken());
      assertEquals(tok.getNodeType(), ABLNodeType.ID);
      assertEquals(tok.getText(), "Progress.Security.PAMStatus");
      assertEquals(tok.getLine(), 1);
      assertEquals(tok.getCharPositionInLine(), 1);
      assertEquals(tok.getEndLine(), 1);
      assertEquals(tok.getEndCharPositionInLine(), 27);
      assertEquals(tok.getChannel(), 0);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);

      // Progress.Security.PAMStatus :AccessDenied.
      tok = (ProToken) ((ProToken) src.nextToken());
      assertEquals(tok.getNodeType(), ABLNodeType.ID);
      assertEquals(tok.getText(), "Progress.Security.PAMStatus");
      assertEquals(tok.getLine(), 2);
      assertEquals(tok.getCharPositionInLine(), 1);
      assertEquals(tok.getEndLine(), 2);
      assertEquals(tok.getEndCharPositionInLine(), 27);
      assertEquals(tok.getChannel(), 0);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);

      // Progress.Security.PAMStatus <bazinga> :AccessDenied.
      tok = (ProToken) ((ProToken) src.nextToken());
      assertEquals(tok.getNodeType(), ABLNodeType.ID);
      assertEquals(tok.getText(), "Progress.Security.PAMStatus");
      assertEquals(tok.getLine(), 3);
      assertEquals(tok.getCharPositionInLine(), 1);
      assertEquals(tok.getEndLine(), 3);
      assertEquals(tok.getEndCharPositionInLine(), 27);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      tok = (ProToken) ((ProToken) src.nextToken());
      assertEquals(tok.getNodeType(), ABLNodeType.COMMENT);
      assertEquals(tok.getText(), "//Test");
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      tok = (ProToken) ((ProToken) src.nextToken());
      assertEquals(tok.getNodeType(), ABLNodeType.COMMENT);
      assertEquals(tok.getText(), "//Test2");
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);

      // Progress.117x.clsName:StaticProperty.
      tok = (ProToken) ((ProToken) src.nextToken());
      assertEquals(tok.getNodeType(), ABLNodeType.ID);
      assertEquals(tok.getText(), "Progress.117x.clsName");
      assertEquals(tok.getLine(), 7);
      assertEquals(tok.getCharPositionInLine(), 1);
      assertEquals(tok.getEndLine(), 7);
      assertEquals(tok.getEndCharPositionInLine(), 21);
      assertEquals(tok.getChannel(), 0);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    } catch (IOException caught) {
      fail("Unable to open file", caught);
    }
  }

  @Test
  public void testTokenList03() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("MESSAGE Progress./* Holy shit */   Security.PAMStatus:AccessDenied.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    // The compiler accepts that...
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    ProToken tok = ((ProToken) src.nextToken());
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getRawText(), "Progress./* Holy shit */   Security.PAMStatus");
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList03bis() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("MESSAGE Progress. /* Holy shit */   Security.PAMStatus:AccessDenied.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    // Recognized differently from the previous one (because of additional space between Progress. and comment ) */
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PROGRESS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.COMMENT);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    ProToken tok = ((ProToken) src.nextToken());
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Security.PAMStatus");
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList03ter() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("MESSAGE Progress /* Holy shit */   .Security.PAMStatus:AccessDenied.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    // Recognized differently from the previous one
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    ProToken tok = ((ProToken) src.nextToken());
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Progress.Security.PAMStatus");
    assertEquals(tok.getRawText(), "Progress /* Holy shit */   .Security.PAMStatus");
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList03quater() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("MESSAGE Riverside /* Holy shit */   .Security.PAMStatus:AccessDenied.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    // Recognized differently from the previous one
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    ProToken tok = ((ProToken) src.nextToken());
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Riverside.Security.PAMStatus");
    assertEquals(tok.getRawText(), "Riverside /* Holy shit */   .Security.PAMStatus");
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList04() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(".Security.PAMStatus.FooBar.Machin:AccessDenied.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    // Nothing recognized here, so we don't change the stream
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.NAMEDOT);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList05() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("MESSAGE customer.custnum Progress.Security.PAMStatus:AccessDenied.\nMESSAGE customer.custnum. Progress.Security.PAMStatus:AccessDenied.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    // MESSAGE customer.custnum Progress.Security.PAMStatus:AccessDenied.
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);

    // MESSAGE customer.custnum. Progress.Security.PAMStatus:AccessDenied.
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList06() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(":AccessDenied.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList07() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(".. :PAMStatus.".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testTokenList08() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("/* Comment */\n{&procedure-handle}:file-name + \"_\":U + string({&procedure-handle})".getBytes()), "file.txt");
    TokenSource filter0 = new NameDotTokenFilter(lexer);
    TokenSource src = new TokenList(filter0);

    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.COMMENT);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.OBJCOLON);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.FILE);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PLUS);
  }

  @Test
  public void testTokenList09() {
    try (InputStream input = new FileInputStream(new File(SRC_DIR, "tokenlist09.p"))) {
      ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(ByteStreams.toByteArray(input)), "file.txt");
      TokenSource filter0 = new NameDotTokenFilter(lexer);
      TokenSource src = new TokenList(filter0);

      // First line
      ProToken tok1 = ((ProToken) src.nextToken());
      assertEquals(tok1.getNodeType(), ABLNodeType.ID);
      assertEquals(tok1.getText(), "customer.name");
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      // Second line
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.ID);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      // Third line: comment after period results in NAMEDOT
      ProToken tok2 = ((ProToken) src.nextToken());
      assertEquals(tok2.getNodeType(), ABLNodeType.ID);
      assertEquals(tok2.getText(), "customer.name");
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      // Fourth line: same behaviour even if there's a space after the comment
      ProToken tok3 = ((ProToken) src.nextToken());
      assertEquals(tok3.getNodeType(), ABLNodeType.ID);
      assertEquals(tok3.getText(), "customer.name");
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      // Fifth line: this line doesn't compile...
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.QSTRING);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.NAMEDOT);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.COMMENT);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.MESSAGE);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.QSTRING);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
      // Sixth line: same behaviour even if there's a space after the comment
      ProToken tok4 = ((ProToken) src.nextToken());
      assertEquals(tok4.getNodeType(), ABLNodeType.ID);
      assertEquals(tok4.getText(), "customer.name");
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.PERIOD);
      assertEquals(((ProToken) src.nextToken()).getNodeType(), ABLNodeType.WS);
    } catch (IOException caught) {
      fail("Unable to open file", caught);
    }
  }

  /**
   * Utility method for preprocess(), removes all tokens from hidden channels
   */
  protected static ProToken nextVisibleToken(TokenSource src) {
    ProToken tok = (ProToken) ((ProToken) src.nextToken());
    while ((tok.getType() != Token.EOF) && (tok.getChannel() != Token.DEFAULT_CHANNEL))
      tok = (ProToken) ((ProToken) src.nextToken());
    return tok;
  }
}
