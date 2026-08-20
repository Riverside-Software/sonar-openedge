/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.core.util.UnitTestWindowsSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class PostLexerTest extends AbstractProparseTest {
  private static final String SRC_DIR = "src/test/resources/data/lexer";

  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void testPostLexer01Init() {
    // First time verifying the channel locations
    String code = """
        &if true or int(00) = 0 and (2 + 3) = 5 &then &scoped-define xx zz ~n &endif
        "{&xx}"
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();
    assertEquals(unit.getCodeSections().keySet().size(), 0);
    // &IF
    ProToken tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPIF);
    assertEquals(tok.getChannel(), ProToken.PREPROCESSOR_CHANNEL);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PREPROEXPR_TRUE);
    assertEquals(tok.getChannel(), ProToken.PREPROCESSOR_CHANNEL);
    assertEquals(tok.getText(), "TRUE || INTEGER(00) == 0 && (2 + 3) == 5");
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPTHEN);
    assertEquals(tok.getChannel(), ProToken.PREPROCESSOR_CHANNEL);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), 1);
    // Then scoped-define on a different channel again
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPSCOPEDDEFINE);
    assertEquals(tok.getChannel(), 2);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), 1);
    // &ENDIF
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.AMPENDIF);
    assertEquals(tok.getChannel(), ProToken.PREPROCESSOR_CHANNEL);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), 1);
    // Then the string
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
  }

  @Test
  public void testPostLexer01() {
    // First time verifying the channel locations
    String code = """
        &if true or int(00) = 0 and (2 + 3) = 5 &then &scoped-define xx zz ~n &endif
        "{&xx}"
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();
    // Whitespaces on hidden channel
    ProToken tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
  }

  @Test
  public void testPostLexer02() {
    String code = """
        &if false &then &scoped-define xx zz ~n &else &scoped-define xx yy ~n&endif
        "{&xx}"
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();
    ProToken tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"yy\"");
  }

  @Test
  public void testPostLexer03() {
    String code = """
        &if false &then &elseif true &then &scoped-define xx zz ~n &else &scoped-define xx yy ~n &endif
        "{&xx}"
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();
    ProToken tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getText(), "\"zz\"");
  }

  @Test
  public void testPostLexer04() {
    String code = "&GLOBAL-DEFINE AAA a'aa~n&GLOBAL-DEFINE BBB bb'b~n&GLOBAL-DEFINE EMPTY~n&GLOBAL-DEFINE NEWLINE xxx~~~nyyy~n\n\"{&AAA}{&BBB}{&EMPTY}{&NEWLINE}\"\n";
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();
    ProToken tok = nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    // The best we can do right now... This is to cover edge cases in preprocessing...
    assertEquals(tok.getText(), "\"a'aabb'bxxx~\nyyy\"");
  }

  @Test
  public void testPreprocessorLevel01() {
    String code = """
        define variable x as logical no-undo. // 0

        &if true
        &then

        if x then message "test1". // 1
        &if true
        &then
        if x then message "test2". // 2
        &endif

        &endif
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();

    ProToken tok = nextToken(src, ABLNodeType.IF);
    assertEquals(tok.getPreprocessorLevel(), 1);
    assertEquals(tok.getLine(), 6);
    tok = nextToken(src, ABLNodeType.IF);
    assertEquals(tok.getPreprocessorLevel(), 2);
    assertEquals(tok.getLine(), 9);
  }

  @Test
  public void testPreprocessorLevel02() {
    String code = """
        define variable x as logical no-undo. // 0

        &if true
        &then
          message "test1". // 1
          &if false
          &then
            message "test2". // Not visible
          &else
            message "test3". // 2
          &endif
          message "test4". // 1
        &endif

        define variable x2 as logical no-undo. // 0
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();

    ProToken tok = nextToken(src, ABLNodeType.DEFINE);
    assertEquals(tok.getPreprocessorLevel(), 0);

    tok = nextToken(src, ABLNodeType.MESSAGE);
    ProToken tokmsg = nextVisibleToken(src);
    assertEquals(tokmsg.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tokmsg.getText(), "\"test1\"");
    assertEquals(tok.getPreprocessorLevel(), 1);

    tok = nextToken(src, ABLNodeType.MESSAGE);
    tokmsg = nextVisibleToken(src);
    assertEquals(tokmsg.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tokmsg.getText(), "\"test3\"");
    assertEquals(tok.getPreprocessorLevel(), 2);

    tok = nextToken(src, ABLNodeType.MESSAGE);
    tokmsg = nextVisibleToken(src);
    assertEquals(tokmsg.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tokmsg.getText(), "\"test4\"");
    assertEquals(tok.getPreprocessorLevel(), 1);

    tok = nextToken(src, ABLNodeType.DEFINE);
    assertEquals(tok.getPreprocessorLevel(), 0);
  }

  @Test
  public void testPreprocessorLevel03() {
    String code = """
        define variable x as logical no-undo. // 0

        &if true
        &then
          message "test1". // 1
          &if false
          &then
            message "test2".
          &elseif true &then
            &if true &then
              message "test3". // 3
            &endif
            message "test4". // 2
          &endif
          message "test5". // 1
        &endif
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource src = unit.preprocess();

    ProToken tok = nextToken(src, ABLNodeType.MESSAGE);
    ProToken tokmsg = nextVisibleToken(src);
    assertEquals(tokmsg.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tokmsg.getText(), "\"test1\"");
    assertEquals(tok.getPreprocessorLevel(), 1);

    tok = nextToken(src, ABLNodeType.MESSAGE);
    tokmsg = nextVisibleToken(src);
    assertEquals(tokmsg.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tokmsg.getText(), "\"test3\"");
    assertEquals(tok.getPreprocessorLevel(), 3);

    tok = nextToken(src, ABLNodeType.MESSAGE);
    tokmsg = nextVisibleToken(src);
    assertEquals(tokmsg.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tokmsg.getText(), "\"test4\"");
    assertEquals(tok.getPreprocessorLevel(), 2);

    tok = nextToken(src, ABLNodeType.MESSAGE);
    tokmsg = nextVisibleToken(src);
    assertEquals(tokmsg.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tokmsg.getText(), "\"test5\"");
    assertEquals(tok.getPreprocessorLevel(), 1);
  }

  @Test
  public void testAnalyzeSuspend() {
    String code = """
        // Not yet in AB Code
        MESSAGE "".

        &ANALYZE-SUSPEND _VERSION-NUMBER AB_v10r12 GUI
        &ANALYZE-RESUME

        &SCOPED-DEFINE WINDOW-NAME wSettings
        // Read-only
        MESSAGE "".

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _CUSTOM _DEFINITIONS wSettings\s
        // Editable
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-PREPROCESSOR-BLOCK
        // Read-only
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _CUSTOM _MAIN-BLOCK wSettings \s
        // Read-only
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _CONTROL wSettings wSettings
        // Editable
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE disable_UI wSettings  _DEFAULT-DISABLE
        // Read-only
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE initializeObject wSettings\s
        // Editable
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE xxx wSettings  _FREEFORM
        // Editable
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _FUNCTION yyy getCurrent wSettings
        // Editable\s
        MESSAGE "".
        &ANALYZE-RESUME

        &ANALYZE-SUSPEND _UIB-CODE-BLOCK _PROCEDURE zzz DataLogicProcedure  _DB-REQUIRED
        // Editable\s
        MESSAGE "".
        &ANALYZE-RESUME
        """;
    ParseUnit unit2 = getParseUnit(code, session);
    unit2.parse();
    assertEquals(unit2.getCodeSections().keySet().size(), 1);
    assertFalse(unit2.isInEditableSection(-1, 10));
    assertFalse(unit2.isInEditableSection(0, -1));
    assertFalse(unit2.isInEditableSection(0, 9));
    assertFalse(unit2.isInEditableSection(0, 18));
    assertTrue(unit2.isInEditableSection(0, 28));
    assertFalse(unit2.isInEditableSection(0, 1000));
    // Code sections only apply to main file
    assertTrue(unit2.isInEditableSection(1, 1));
  }

  @Test
  public void testPreproErrorMessages01() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer06.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

      }
    } catch (ProparseRuntimeException caught) {
      assertTrue(caught.getMessage().replace('\\', '/').startsWith("File '" + SRC_DIR + "/lexer06.p'"));
      assertTrue(caught.getMessage().endsWith("Unexpected &THEN"));
      return;
    } catch (Exception caught) {
      fail("Unwanted exception...");
    }
    fail("No exception found");
  }

  @Test
  public void testPreproErrorMessages02() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer07.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

      }
    } catch (ProparseRuntimeException caught) {
      assertTrue(caught.getMessage().replace('\\', '/').startsWith("File '" + SRC_DIR + "/lexer07.p'"));
      assertTrue(caught.getMessage().endsWith("Unexpected end of input after &IF or &ELSEIF"));
      return;
    } catch (Exception caught) {
      fail("Unwanted exception...");
    }
    fail("No exception found");
  }

  @Test
  public void testPreproErrorMessages03() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer08.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

      }
    } catch (ProparseRuntimeException caught) {
      assertTrue(caught.getMessage().replace('\\', '/').startsWith("File '" + SRC_DIR + "/lexer08.p'"));
      assertTrue(
          caught.getMessage().endsWith("Unexpected end of input when consuming discarded &IF/&ELSEIF/&ELSE text"));
      return;
    } catch (Exception caught) {
      fail("Unwanted exception...");
    }
    fail("No exception found");
  }

  @Test
  public void testPreproErrorMessages04() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer09.p"), session);
    try {
      TokenSource src = unit.preprocess();
      while (src.nextToken().getType() != Token.EOF) {

      }
    } catch (ProparseRuntimeException caught) {
      String msg = caught.getMessage().replace('\\', '/');
      assertTrue(msg.startsWith("File '" + SRC_DIR + "/lexer09.p' - Current position '"), msg);
      assertTrue(msg.endsWith("src/test/resources/data/lexer/lexer09.i':2 - Unexpected &THEN"), msg);
      return;
    } catch (Exception caught) {
      fail("Unwanted exception...");
    }
    fail("No exception found");
  }

  @Test
  public void testAnalyzeSuspendIncludeFile() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer10.p"), session);
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

  @Test(enabled = false)
  public void testMacroExpansion() {
    String code = """
        &Scoped-define XXXX MESSAGE 'Hello' .
        {&XXXX}
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource stream = unit.preprocess();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertTrue(tok.isMacroExpansion());

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertTrue(tok.isMacroExpansion());

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
    // Bug lies here in the lexer
    assertTrue(tok.isMacroExpansion());
  }

  @Test
  public void testUnicodeBom() {
    RefactorSession session2 = new RefactorSession(new ProparseSettings("src/test/resources/data"), new Schema(), StandardCharsets.UTF_8);
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer13.p"), session2);
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
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer14.p"), session);
    TokenSource src = unit.preprocess();

    // lexer14.i contains 'message "xcode".'
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getCharPositionInLine(), 0);
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
    RefactorSession session2 = new RefactorSession(settings, new Schema(), StandardCharsets.UTF_8);
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer14.p"), session2);
    TokenSource src = unit.preprocess();

    // lexer14.i contains 'message "xcode".'
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getLine(), 2);
    assertEquals(tok.getCharPositionInLine(), 0);
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
    RefactorSession session2 = new RefactorSession(settings, new Schema(), StandardCharsets.UTF_8);
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer14.p"), session2);
    // Has to fail here
    unit.preprocess();
  }

  @Test
  public void testXCode4() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer14-2.p"), session);
    TokenSource src = unit.preprocess();

    // lexer14.i contains 'message "xcode".'
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);

    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), Token.HIDDEN_CHANNEL);

    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 26);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 33);

    // Two xcoded include files are replaced by a two whitespaces leading to one token
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    assertEquals(tok.getChannel(), Token.HIDDEN_CHANNEL);

    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.QSTRING);
    assertEquals(tok.getLine(), 1);
    assertEquals(tok.getCharPositionInLine(), 71);
    assertEquals(tok.getEndLine(), 1);
    assertEquals(tok.getEndCharPositionInLine(), 84);
  }

  @Test
  public void testProparseDirectiveLexPhase() {
    String code = """
        {&_proparse_ xxx}
        custnum = 1.

        {&_proparse_ xxx}
        customer.custnum = 1.

        {&_proparse_ xxx}
        sp2k.customer.custnum = 1.
        """;
    ParseUnit unit = getParseUnit(code, session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getChannel(), ProToken.PROPARSE_CHANNEL);
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
    assertEquals(tok.getChannel(), ProToken.PROPARSE_CHANNEL);
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
    assertEquals(tok.getChannel(), ProToken.PROPARSE_CHANNEL);
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
    String code = """
        {&_proparse_ xxx}
        custnum = 1.

        {&_proparse_ xxx}
        customer.custnum = 1.

        {&_proparse_ xxx}
        sp2k.customer.custnum = 1.
        """;
    ParseUnit unit = getParseUnit(code, session);
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
    ParseUnit unit = getParseUnit("MESSAGE 125 0x65 0X66 0xfb 0xab -0x01.\n", session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "125");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "0x65");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "0X66");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "0xfb");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "0xab");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "-0x01");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void testHexNumbers2() {
    ParseUnit unit = getParseUnit("MESSAGE 125 0x2g8.\n", session);
    TokenSource stream = unit.lex();

    ProToken tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "125");

    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) stream.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    assertEquals(tok.getText(), "0x2g8");
  }

  @Test
  public void testFileNumName() throws IOException {
    // Use Windows settings here in order to use backlash directory separator
    RefactorSession session = new RefactorSession(new UnitTestWindowsSettings(), new SportsSchema());
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer18.p"), session);
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
    assertTrue(tok.getFileName().replace('\\', '/').endsWith("src/test/resources/data/lexer/lexer18.i"));

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getTokenIndex(), 4);
    assertEquals(tok.getFileIndex(), 1);
    assertTrue(tok.getFileName().replace('\\', '/').endsWith("src/test/resources/data/lexer/lexer18.i"));

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.QUIT);
    assertEquals(tok.getTokenIndex(), 6);
    assertEquals(tok.getFileIndex(), 2);
    assertTrue(tok.getFileName().replace('\\', '/').endsWith("src/test/resources/data/lexer/lexer18-2.i"));

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getTokenIndex(), 8);
    assertEquals(tok.getFileIndex(), 1);
    assertTrue(tok.getFileName().replace('\\', '/').endsWith("src/test/resources/data/lexer/lexer18.i"));

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.MESSAGE);
    assertEquals(tok.getTokenIndex(), 10);
    assertEquals(tok.getFileIndex(), 1);
    assertTrue(tok.getFileName().replace('\\', '/').endsWith("src/test/resources/data/lexer/lexer18.i"));

    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.STOP);
    assertEquals(tok.getTokenIndex(), 12);
    assertEquals(tok.getFileIndex(), 0);
    assertTrue(tok.getFileName().replace('\\', '/').endsWith("src/test/resources/data/lexer/lexer18.p"));
  }

  @Test
  public void testDirectiveEOF() throws IOException {
    RefactorSession session = new RefactorSession(new UnitTestWindowsSettings(), new SportsSchema());
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer19.p"), session);
    TokenSource src = unit.preprocess();

    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.IF);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.YES);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.THEN);
  }

  @Test
  public void testUndefine() throws IOException {
    RefactorSession session = new RefactorSession(new UnitTestWindowsSettings(), new SportsSchema());
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer20.p"), session);
    TokenSource src = unit.preprocess();

    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "123123");
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "123123");
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "123456");
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.NUMBER);
    assertEquals(tok.getText(), "123123");
  }

  @Test
  public void testEndOfIncInIncludeParameter() throws IOException {
    RefactorSession session = new RefactorSession(new UnitTestWindowsSettings(), new SportsSchema());
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer21.p"), session);
    TokenSource src = unit.preprocess();

    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.FIND);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.ID);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.COMMENT);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PERIOD);
  }

  @Test
  public void test22() throws IOException {
    RefactorSession session = new RefactorSession(new UnitTestWindowsSettings(), new SportsSchema());
    ParseUnit unit = getParseUnit("&GLOBAL-DEFINE DEF1 IF TRUE THEN {&_proparse_ prolint-nowarn(use-index)} RUN Foo{1}Bar.\n{&DEF1}\n", session);
    TokenSource src = unit.preprocess();
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.IF);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.TRUE);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.THEN);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getText(), "prolint-nowarn(use-index)");
  }

  @Test
  public void test23() throws IOException {
    RefactorSession session = new RefactorSession(new UnitTestWindowsSettings(), new SportsSchema());
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "lexer23.p"), session);
    TokenSource src = unit.preprocess();
    ProToken tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.IF);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.TRUE);
    tok = (ProToken) nextVisibleToken(src);
    assertEquals(tok.getNodeType(), ABLNodeType.THEN);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.WS);
    tok = (ProToken) src.nextToken();
    assertEquals(tok.getNodeType(), ABLNodeType.PROPARSEDIRECTIVE);
    assertEquals(tok.getText(), "prolint-nowarn(use-index)");
  }

  /**
   * Utility method for tests, returns next node of given type
   */
  private ProToken nextToken(TokenSource stream, ABLNodeType type) {
    ProToken tok = (ProToken) stream.nextToken();
    while (tok.getNodeType() != type) {
      tok = (ProToken) stream.nextToken();
    }
    return tok;
  }

  /**
   * Utility method for preprocess(), removes all tokens from hidden channels
   */
  protected static ProToken nextVisibleToken(TokenSource src) {
    ProToken tok = (ProToken) src.nextToken();
    while ((tok.getType() != Token.EOF) && (tok.getChannel() != Token.DEFAULT_CHANNEL))
      tok = (ProToken) src.nextToken();
    return tok;
  }
}
