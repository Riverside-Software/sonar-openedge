package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;

public abstract class TokenSourceUtils {

  private TokenSourceUtils() {
    // No constructor
  }

  protected static ProToken firstToken(TokenSource lexer, ABLNodeType type) {
    ProToken tok = (ProToken) lexer.nextToken();
    while ((tok != null) && (tok.getNodeType() != type)) {
      tok = (ProToken) lexer.nextToken();
    }
    return tok;
  }

  protected static void nextMessageToken(TokenSource lexer, boolean suspend, boolean editable) {
    ProToken tok = (ProToken) lexer.nextToken();
    while (tok.getNodeType() != ABLNodeType.MESSAGE) {
      tok = (ProToken) lexer.nextToken();
    }
    assertNotNull(tok);
    if (suspend)
      assertNotNull(tok.getAnalyzeSuspend());
    assertEquals(tok.isEditableInAB(), editable);
  }

  protected static void assertNextTokenType(TokenSource lexer, ABLNodeType type) {
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), type);
  }

  protected static void assertNextTokenTypeWS(TokenSource lexer, ABLNodeType type) {
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), type);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
  }

  /**
   * Checks next token is of given type, and consume following token (just assuming it's whitespace)
   */
  protected static void assertNextTokenType(TokenSource lexer, ABLNodeType type, String text) {
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), type);
    assertEquals(tok.getText(), text);
  }

  /**
   * Checks next token is of given type, and consume following token (just assuming it's whitespace)
   */
  protected static void assertNextTokenTypeWS(TokenSource lexer, ABLNodeType type, String text) {
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), type);
    assertEquals(tok.getText(), text);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
  }

}
