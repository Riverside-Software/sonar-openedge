package org.prorefactor.proparse.antlr4;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
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

public class NameDotTokenFilterTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException, InvalidRCodeException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testEmptyList() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    Token tok = filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getType(), Token.EOF);
    // Indefinitely returns EOF
    tok = filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getType(), Token.EOF);
  }

  @Test
  public void testNoNameDot() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("message 'Hello'.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());
    
    Token tok = filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getType(), ABLNodeType.MESSAGE.getType());
    tok = filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getType(), ABLNodeType.WS.getType());
    tok = filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getType(), ABLNodeType.QSTRING.getType());
    tok = filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getType(), ABLNodeType.PERIOD.getType());
  }

  @Test
  public void testNameDot00() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap(".Lang.Object.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    ProToken tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.NAMEDOT);
    assertEquals(tok.getText(), ".");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Lang.Object");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }
  
  @Test
  public void testNameDot01() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("using Riverside.Lang.Object.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    ProToken tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.USING);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Riverside.Lang.Object");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.EOF_ANTLR4);
  }

  @Test
  public void testNameDot02() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("using Progress.Lang.Object.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    ProToken tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.USING);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) filter.nextToken();
    // Changed from ABLNodeType.PROGRESS to ABLNodeType.ID
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Progress.Lang.Object");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.EOF_ANTLR4);
  }

  @Test
  public void testNameDot03() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("using Riverside.20190101.Object.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    ProToken tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.USING);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Riverside.20190101.Object");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.EOF_ANTLR4);
  }

  @Test
  public void testNameDot04() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("using Riverside./* Woot */ /* Woot woot */20190101.Object.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    ProToken tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.USING);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "Riverside.20190101.Object");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.EOF_ANTLR4);
  }

  @Test
  public void testAnnotation01() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("@Riverside.Lang.Object. MESSAGE 'foo'.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    ProToken tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(tok.getText(), "@Riverside.Lang.Object");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
  }

  @Test
  public void testAnnotation02() {
    ProgressLexer lexer = new ProgressLexer(session, ByteSource.wrap("@Riverside.20190101.Object. MESSAGE 'foo'.".getBytes()), "file.txt");
    TokenSource filter = new NameDotTokenFilter(lexer.getTokenSource());

    ProToken tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.ANNOTATION);
    assertEquals(tok.getText(), "@Riverside.20190101.Object");
    tok = (ProToken) filter.nextToken();
    assertNotNull(tok);
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

}
