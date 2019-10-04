package org.prorefactor.proparse.antlr4;

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
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("CLASS Riverside.20190101.Object".getBytes()), "file.txt");
    
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
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("@Riverside.Lang.Object. MESSAGE 'foo'.".getBytes()), "file.txt");
    
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
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("@Riverside.20190101.Object. MESSAGE 'foo'.".getBytes()), "file.txt");
    
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

}
