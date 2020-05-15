package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.common.io.ByteSource;
import com.google.inject.Guice;
import com.google.inject.Injector;

import eu.rssw.pct.RCodeInfo.InvalidRCodeException;

public class LexerTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }
  
  @Test
  public void testTypeName() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("CLASS Riverside.20190101.Object".getBytes()), "file.txt");
    
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.CLASS);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Riverside");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), ".20190101");
  }

  @Test
  public void testAnnotation01() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("@Riverside.Lang.Object. MESSAGE 'foo'.".getBytes()), "file.txt");
    
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(tok.getText(), "@Riverside");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.NAMEDOT);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Lang");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.NAMEDOT);
    assertEquals(tok.getText(), ".");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.OBJECT);
    assertEquals(tok.getText(), "Object");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testAnnotation02() {
    ABLLexer lexer = new ABLLexer(session, ByteSource.wrap("@Riverside.20190101.Object. MESSAGE 'foo'.".getBytes()), "file.txt");
    
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(tok.getText(), "@Riverside");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), ".20190101");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.NAMEDOT);
    assertEquals(tok.getText(), ".");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.OBJECT);
    assertEquals(tok.getText(), "Object");
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testKeywords01() {
    ABLLexer lexer = new ABLLexer(session,
        ByteSource.wrap(
            "AUTO-ENDKEY AUTO-END-KEY CAPS UPPER COM-HANDLE COMPONENT-HANDLE EXCLUSIVE EXCLUSIVE-LOCK ".getBytes()),
        "file.txt");
    assertNextToken(lexer, ABLNodeType.AUTOENDKEY, "AUTO-ENDKEY");
    assertNextToken(lexer, ABLNodeType.AUTOENDKEY, "AUTO-END-KEY");
    assertNextToken(lexer, ABLNodeType.CAPS, "CAPS");
    assertNextToken(lexer, ABLNodeType.CAPS, "UPPER");
    assertNextToken(lexer, ABLNodeType.COMHANDLE, "COM-HANDLE");
    assertNextToken(lexer, ABLNodeType.COMHANDLE, "COMPONENT-HANDLE");
    assertNextToken(lexer, ABLNodeType.EXCLUSIVELOCK, "EXCLUSIVE");
    assertNextToken(lexer, ABLNodeType.EXCLUSIVELOCK, "EXCLUSIVE-LOCK");

    ABLLexer lexer2 = new ABLLexer(session,
        ByteSource.wrap(
            "INIT INITIAL LC LOWER NO-ATTR NO-ATTR-SPACE NO-ATTR-LIST TRANS TRANSAC TRANSACT TRANSACTION ".getBytes()),
        "file.txt");
    assertNextToken(lexer2, ABLNodeType.INITIAL, "INIT");
    assertNextToken(lexer2, ABLNodeType.INITIAL, "INITIAL");
    assertNextToken(lexer2, ABLNodeType.LC, "LC");
    assertNextToken(lexer2, ABLNodeType.LC, "LOWER");
    assertNextToken(lexer2, ABLNodeType.NOATTRSPACE, "NO-ATTR");
    assertNextToken(lexer2, ABLNodeType.NOATTRSPACE, "NO-ATTR-SPACE");
    assertNextToken(lexer2, ABLNodeType.NOATTRLIST, "NO-ATTR-LIST");
    assertNextToken(lexer2, ABLNodeType.TRANSACTION, "TRANS");
    assertNextToken(lexer2, ABLNodeType.ID, "TRANSAC");
    assertNextToken(lexer2, ABLNodeType.TRANSACTION, "TRANSACT");
    assertNextToken(lexer2, ABLNodeType.TRANSACTION, "TRANSACTION");

    ABLLexer lexer3 = new ABLLexer(session, ByteSource.wrap("USERID USER-ID ".getBytes()), "file.txt");
    assertNextToken(lexer3, ABLNodeType.USERID, "USERID");
    assertNextToken(lexer3, ABLNodeType.ID, "USER-ID");
  }

  private static void assertNextToken(ABLLexer lexer, ABLNodeType type, String text) {
    ProToken tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), type);
    assertEquals(tok.getText(), text);
    tok = (ProToken) lexer.nextToken();
    assertNotNull(tok);
  }
}
