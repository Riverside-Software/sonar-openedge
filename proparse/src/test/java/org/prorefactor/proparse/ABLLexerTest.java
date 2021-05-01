package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.WritableProToken;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

public class ABLLexerTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testSymbols() {
    final String source = "+ - +123 -123 += -= * *= / /= ";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), "file.txt", true);

    assertEquals(lexer.nextToken().getType(), Proparse.PLUS);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.MINUS);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.NUMBER);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.NUMBER);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.PLUSEQUAL);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.MINUSEQUAL);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.STAR);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.STAREQUAL);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.SLASH);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.SLASHEQUAL);
  }

  @Test
  public void testEndOfFile() {
    // Could be anything...
    final String source = "CURRENT-WINDOW:HANDLE. SESSION:FIRST-SERVER-SOCKET:HANDLE. TEMP-TABLE tt1::fld1. DATASET ds1::tt1. DATASET ds1::tt1:set-callback().";
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap(source.getBytes()), "file.txt", true);

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
    assertEquals(lexer.nextToken().getType(), Proparse.CURRENTWINDOW);
    assertEquals(lexer.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(lexer.nextToken().getType(), Proparse.HANDLE);
    assertEquals(lexer.nextToken().getType(), Proparse.PERIOD);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);

    // SESSION:FIRST-SERVER-SOCKET:HANDLE.
    assertEquals(lexer.nextToken().getType(), Proparse.SESSION);
    assertEquals(lexer.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(lexer.nextToken().getType(), Proparse.HANDLE);
    assertEquals(lexer.nextToken().getType(), Proparse.PERIOD);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);

    // TEMP-TABLE tt1::fld1.
    assertEquals(lexer.nextToken().getType(), Proparse.TEMPTABLE);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.DOUBLECOLON);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.PERIOD);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);

    // DATASET ds1::tt1.
    assertEquals(lexer.nextToken().getType(), Proparse.DATASET);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.DOUBLECOLON);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.PERIOD);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);

    // DATASET ds1::tt1:set-callback().
    assertEquals(lexer.nextToken().getType(), Proparse.DATASET);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.DOUBLECOLON);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.OBJCOLON);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.LEFTPAREN);
    assertEquals(lexer.nextToken().getType(), Proparse.RIGHTPAREN);
    assertEquals(lexer.nextToken().getType(), Proparse.PERIOD);
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
    assertEquals(tok.getType(), Proparse.DO);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 2);

    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.WHILE);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.RIGHTANGLE);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    // Quoted string
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 15);
    assertEquals(tok.getEndLine(), 1);
    // The important test here, end column has to be 16 even when followed by ':'
    assertEquals(tok.getEndCharPositionInLine(), 16);

    // Colon
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.LEXCOLON);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 17);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 17);
  }

  @Test
  public void testQuotedStringPosition02() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("do while xx > '' : end.".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.DO);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 2);

    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.WHILE);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.ID);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);
    assertEquals(lexer.nextToken().getType(), Proparse.RIGHTANGLE);
    assertEquals(lexer.nextToken().getType(), Proparse.WS);

    // Quoted string
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 15);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 16);

    assertEquals(lexer.nextToken().getType(), Proparse.WS);

    // Colon
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.LEXCOLON);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 18);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 18);
  }

  @Test
  public void testQuotedStringPosition03() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("\"Test\":L10.".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 10);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 11);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 11);
  }

  @Test
  public void testQuotedStringPosition04() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("\"Test\".".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 6);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 7);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 7);
  }

  @Test
  public void testQuotedStringPosition05() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("\"Test\":U.".getBytes()), "file.txt");

    ProToken tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 1);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 8);
    tok = (ProToken) lexer.nextToken();
    assertEquals(tok.getType(), Proparse.PERIOD);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 9);
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
    assertNextTokenTypeWS(lexer, ABLNodeType.EXCLUSIVELOCK, "EXCLUSIVE-LOCK");

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
    assertNextTokenTypeWS(lexer2, ABLNodeType.VARIABLE, "VARIABLE");

    ABLLexer lexer3 = new ABLLexer(session, ByteSource.wrap("USERID USER-ID ".getBytes()), "file.txt");
    assertNextTokenTypeWS(lexer3, ABLNodeType.USERID, "USERID");
    assertNextTokenTypeWS(lexer3, ABLNodeType.ID, "USER-ID");
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
    Token tok = lexer.nextToken();
    tok = lexer.nextToken();
    assertNotNull(tok);
    assertTrue(tok instanceof WritableProToken);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 3);
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
    tok = (ProToken) lexer.nextToken();
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

  // *********
  // Utilities
  // *********

  private ProToken firstToken(TokenSource lexer, ABLNodeType type) {
    ProToken tok = (ProToken) lexer.nextToken();
    while ((tok != null) && (tok.getNodeType() != type)) {
      tok = (ProToken) lexer.nextToken();
    }
    return tok;
  }

  private void nextMessageToken(TokenSource lexer, boolean suspend, boolean editable) {
    ProToken tok = (ProToken) lexer.nextToken();
    while (tok.getNodeType() != ABLNodeType.MESSAGE) {
      tok = (ProToken) lexer.nextToken();
    }
    assertNotNull(tok);
    if (suspend)
      assertNotNull(tok.getAnalyzeSuspend());
    assertEquals(tok.isEditableInAB(), editable);
  }

  /**
   * Checks next token is of given type, and consume following token (just assuming it's whitespace)
   */
  private static void assertNextTokenType(ABLLexer lexer, ABLNodeType type, String text) {
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), type);
    assertEquals(tok.getText(), text);
  }

  /**
   * Checks next token is of given type, and consume following token (just assuming it's whitespace)
   */
  private static void assertNextTokenTypeWS(ABLLexer lexer, ABLNodeType type, String text) {
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), type);
    assertEquals(tok.getText(), text);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
  }

}
