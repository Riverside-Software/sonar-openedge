/********************************************************************************
 * Copyright (c) 2015-2020 Riverside Software
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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.antlr.v4.runtime.Token;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.macrolevel.MacroDefinitionType;
import org.prorefactor.proparse.antlr4.Proparse;
import org.prorefactor.proparse.support.StringFuncs;
import org.prorefactor.refactor.settings.ProparseSettings.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.ByteSource;

/**
 * Voodoo implementation of the ABL lexer (i.e. include file management, preprocessor variables, and lexer) in this
 * class. Preprocessor expressions are executed in PostLexer.
 * 
 * Do not change this class unless you know what you're doing, and are ready to spend countless hours debugging what
 * could go wrong. There are some unit tests, but they don't cover every corner case of the lexer (and I swear there are
 * loads of corner cases, all of them being used at least by one Progress vendor).
 * It's still possible to do some refactor on the code... But running out of sleepless nights...
 */
public class Lexer implements IPreprocessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(Lexer.class);

  private static final int SKIP_CHAR = -100;
  private static final int PROPARSE_DIRECTIVE = -101;
  private static final int INCLUDE_DIRECTIVE = -102;

  // Cached include files for current lexer
  private final Map<String, Integer> includeCache = new HashMap<>();
  private final Map<Integer, String> includeCache2 = new HashMap<>();
  private final LinkedList<IncludeFile> includeVector = new LinkedList<>();
  private IncludeFile currentInclude;
  private InputSource currentInput;
  private int sourceCounter;

  private final ABLLexer prepro;

  private int ppCurrChar;
  // Current character, before being lowercased
  private int currInt;
  // Lowercase value of current character
  private int currChar;
  private int currFile;
  private int currLine;
  private int currCol;
  private int currSourceNum;
  private boolean currMacroExpansion;

  private StringBuilder currText = new StringBuilder();
  private FilePos prevChar = new FilePos(0, 1, 1, 0);
  private boolean prevMacroExpansion;

  private FilePos macroStartPos;
  private FilePos tokenStartPos;

  private Set<Integer> comments = new HashSet<>();
  private Set<Integer> loc = new HashSet<>();

  private boolean preserve;
  private CharPos preservedChar;
  private CharPos la = null;

  // Was there escaped text before current character?
  private boolean wasEscape;
  // Would you append the currently returned character to escapeText in order to see what the original code looked like
  // before escape processing? (See escape().)
  private boolean escapeAppend;
  // Is the currently returned character escaped?
  private boolean escapeCurrent;
  private String escapeText;

  // Are we in the middle of a comment?
  private boolean doingComment;
  // Is the current '.' a name dot? (i.e. not followed by whitespace) */
  private boolean nameDot;

  private String proparseDirectiveText;
  private String includeDirectiveText;
  private int safetyNet = 0;

  private boolean gettingAmpIfDefArg = false;
  private Map<String, String> globalDefdNames = new HashMap<>();
  private int sequence = 0;

  protected Lexer(ABLLexer prepro, ByteSource src, String fileName) {
    this.prepro = prepro;
    try {
      currentInput = new InputSource(0, fileName, src, prepro.getRefactorSession().getCharset(), 0, true, true);
    } catch (IOException caught) {
      throw new UncheckedIOException(caught);
    }
    currentInclude = new IncludeFile(fileName, currentInput);
    currFile = addFilename(fileName);
    includeVector.add(currentInclude);
    currSourceNum = currentInput.getSourceNum();

    getChar(); // We always assume "currChar" is available.
  }

  //////////////// Lexical productions listed first, support functions follow.
  public ProToken nextToken() {
    LOGGER.trace("Entering nextToken()");
    for (;;) {

      if (preserve) {
        // The preserved character is the character prior to currChar.
        tokenStartPos = new FilePos(preservedChar);
        currText.setLength(1);
        currText.setCharAt(0, (char) preservedChar.ch);
        preserveDrop(); // we are done with the preservation
        if (preservedChar.ch == '.') {
          return periodStart();
        } else if (preservedChar.ch == ':') {
          return colon();
        }
      }

      // Proparse Directive
      // Check this before setting currText...
      // we don't want BEGIN_PROPARSE_DIRECTIVE in the text
      if (currInt == PROPARSE_DIRECTIVE) {
        tokenStartPos = new FilePos(macroStartPos);
        getChar();
        return makeToken(ABLNodeType.PROPARSEDIRECTIVE, proparseDirectiveText);
      } else if (currInt == INCLUDE_DIRECTIVE) {
        tokenStartPos = new FilePos(macroStartPos);
        getChar();
        return makeToken(ABLNodeType.INCLUDEDIRECTIVE, includeDirectiveText);
      }
      tokenStartPos = new FilePos(currFile, currLine, currCol, currSourceNum);
      currText.setLength(1);
      currText.setCharAt(0, (char) currInt);

      if (gettingAmpIfDefArg) {
        getChar();
        gettingAmpIfDefArg = false;
        return ampIfDefArg();
      }

      switch (currChar) {

        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
          getChar();
          return whitespace();

        case '"':
        case '\'':
          if (escapeCurrent) {
            getChar();
            // Escaped quote does not start a string
            return id(ABLNodeType.FILENAME);
          } else {
            int currStringType = currInt;
            getChar();
            return quotedString(currStringType);
          }

        case '/':
          getChar();
          if (currChar == '*') {
            return comment();
          } else if (currChar == '/') {
            return singleLineComment();
          } else if (currChar == '(' || currIsSpace()) {
            // slash (division) can only be followed by whitespace or '('
            // ...that's what I found empirically, anyway. (jag 2003/05/09)
            return makeToken(ABLNodeType.SLASH);
          } else {
            append();
            getChar();
            return id(ABLNodeType.FILENAME);
          }

        case ':':
          getChar();
          return colon();

        case '&':
          getChar();
          return ampText();
        case '@':
          getChar();
          if (currIsSpace())
            return makeToken(ABLNodeType.LEXAT);
          else
            append();
          getChar();
          return id(ABLNodeType.ANNOTATION);
        case '[':
          getChar();
          return makeToken(ABLNodeType.LEFTBRACE);
        case ']':
          getChar();
          return makeToken(ABLNodeType.RIGHTBRACE);
        case '^':
          getChar();
          return makeToken(ABLNodeType.CARET);
        case ',':
          getChar();
          return makeToken(ABLNodeType.COMMA);
        case '!':
          getChar();
          return makeToken(ABLNodeType.EXCLAMATION);
        case '=':
          getChar();
          return makeToken(ABLNodeType.EQUAL);
        case '(':
          getChar();
          return makeToken(ABLNodeType.LEFTPAREN);
        case ')':
          getChar();
          return makeToken(ABLNodeType.RIGHTPAREN);
        case ';':
          getChar();
          return makeToken(ABLNodeType.SEMI);
        case '*':
          getChar();
          return makeToken(ABLNodeType.STAR);
        case '?':
          getChar();
          return makeToken(ABLNodeType.UNKNOWNVALUE);
        case '`':
          getChar();
          return makeToken(ABLNodeType.BACKTICK);

        case '0':
          getChar();
          if ((currChar == 'x') || (currChar == 'X')) {
            append();
            getChar();
            return digitStart(true);
          } else {
            return digitStart(false);
          }

        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          getChar();
          return digitStart(false);

        case '.':
          getChar();
          return periodStart();

        case '>':
          getChar();
          if (currChar == '=') {
            append();
            getChar();
            return makeToken(ABLNodeType.GTOREQUAL);
          } else {
            return makeToken(ABLNodeType.RIGHTANGLE);
          }

        case '<':
          getChar();
          if (currChar == '>') {
            append();
            getChar();
            return makeToken(ABLNodeType.GTORLT);
          } else if (currChar == '=') {
            append();
            getChar();
            return makeToken(ABLNodeType.LTOREQUAL);
          } else {
            return makeToken(ABLNodeType.LEFTANGLE);
          }

        case '+':
          getChar();
          return plusMinusStart(ABLNodeType.PLUS);
        case '-':
          getChar();
          return plusMinusStart(ABLNodeType.MINUS);

        case '#':
        case '|':
        case '%':
          getChar();
          return id(ABLNodeType.FILENAME);

        default:
          if (currInt == Token.EOF) {
            getChar(); // preprocessor will catch any infinite loop on this.
            return makeToken(ABLNodeType.EOF_ANTLR4, "");
          } else {
            getChar();
            return id(ABLNodeType.ID);
          }

      }
    }
  }

  /**
   * Get argument for &IF DEFINED(...). The nextToken function is necessarily the main entry point. This is just a
   * wrapper around that.
   */
  ProToken getAmpIfDefArg() {
    LOGGER.trace("Entering getAmpIfDefArg()");

    gettingAmpIfDefArg = true;
    return nextToken();
  }

  /**
   * Get the text between the parens for &IF DEFINED(...). The compiler seems to allow any number of tokens between the
   * parens, and like with an &Name reference, it allows embedded comments. Here, I'm allowing for the embedded comments
   * and just gathering all the text up to the closing paren. Hopefully that will do it.
   *
   * The compiler doesn't seem to ignore extra tokens. For example, &if defined(ab cd) does not match a macro named
   * "ab". It doesn't match "abcd" either, so all I can guess is that they are combining the text of all the tokens
   * between the parens. I haven't found any macro name that matches &if defined(ab"cd").
   *
   * The compiler works different here than it does for a typical ID token. An ID token (like a procedure name) may
   * contain arbitrary quotation marks. Within an &if defined() function, the quotation marks must match. I don't know
   * if that really makes a difference, because the quoted string can't contain a paren ')' anyway, so as far as I can
   * tell we can ignore quotation marks and just watch for the closing paren. A macro name can't contain any quotation
   * marks anyway, so for all I know the compiler's handling of quotes within defined() may just be an artifact of its
   * lexer. I don't think there's any way to get whitespace into a macro name either.
   */
  private ProToken ampIfDefArg() {
    LOGGER.trace("Entering ampIfDefArg()");

    loop : for (;;) {
      if (currChar == ')') {
        break loop;
      }
      // Watch for comments.
      if (currChar == '/') {
        getChar();
        if (currChar != '*') {
          currText.append('/');
          continue loop;
        } else {
          String s = currText.toString();
          comment();
          currText.replace(0, currText.length(), s);
          continue loop;
        }
      }
      append();
      getChar();
    }
    return makeToken(ABLNodeType.ID);
  }

  ProToken colon() {
    LOGGER.trace("Entering colon()");

    if (currChar == ':') {
      append();
      getChar();
      return makeToken(ABLNodeType.DOUBLECOLON);
    }
    if (currIsSpace())
      return makeToken(ABLNodeType.LEXCOLON);
    return makeToken(ABLNodeType.OBJCOLON);
  }

  ProToken whitespace() {
    LOGGER.trace("Entering whitespace()");

    boolean consume = true;
    while (consume) {
      switch (currChar) {
        case ' ':
        case '\t':
        case '\f':
        case '\n':
        case '\r':
          append();
          getChar();
          break;
        default:
          consume = false;
      }
    }
    return makeToken(ABLNodeType.WS);
  }

  ProToken comment() {
    LOGGER.trace("Entering comment()");

    // Escapes in comments are processed because you can end a comment
    // with something dumb like: ~*~/
    // We preserve that text.
    // Note that macros are *not* expanded inside comments.
    // (See the preprocessor source)
    doingComment = true;
    append(); // currChar=='*'
    int commentLevel = 1;
    while (commentLevel > 0) {
      getChar();
      unEscapedAppend();
      if (currChar == '/') {
        getChar();
        unEscapedAppend();
        if (currChar == '*')
          commentLevel++;
      } else if (currChar == '*') {
        while (currChar == '*') {
          getChar();
          unEscapedAppend();
          if (currChar == '/')
            commentLevel--;
        }
      } else if (currInt == Token.EOF) {
        lexicalThrow("Missing end of comment");
      }
    }
    doingComment = false;
    getChar();
    return makeToken(ABLNodeType.COMMENT);
  }

  ProToken singleLineComment() {
    LOGGER.trace("Entering singleLineComment()");

    // Single line comments are treated just like regular comments,
    // everything till end of line is considered comment - no escape
    // character to look after
    doingComment = true;
    append(); // currChar=='/'

    while (true) {
      getChar();
      if ((currInt == Token.EOF) || (!escapeCurrent && (currChar == '\r' || currChar == '\n'))) {
        doingComment = false;
        return makeToken(ABLNodeType.COMMENT);
      } else {
        unEscapedAppend();
      }
    }
  }

  ProToken quotedString(int currStringType) {
    LOGGER.trace("Entering quotedString()");

    // Inside quoted strings (string constants) we preserve
    // the source code's original text - we don't discard
    // escape characters.
    // The preprocessor *does* expand macros inside strings.
    for (;;) {
      if (currInt == Token.EOF)
        lexicalThrow("Unmatched quote");
      unEscapedAppend();
      if (currInt == currStringType && !escapeCurrent) {
        getChar();
        if (currInt == currStringType) { // quoted quote
          unEscapedAppend();
        } else {
          break; // close quote
        }
      }
      getChar();
    }

    if (currChar == ':') {
      boolean isStringAttributes = false;
      // Preserve the colon before calling getChar,
      // in case it belongs in the next token.
      preserveCurrent();
      String theText = ":";
      for_loop : for (;;) {
        getChar();
        switch (currChar) {
          case 'r':
          case 'l':
          case 'c':
          case 't':
          case 'u':
          case 'x':
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            theText += (char) currInt;
            isStringAttributes = true;
            break;
          default:
            break for_loop;
        }
      }
      // either string attributes, or the preserved colon
      // goes into the next token.
      if (isStringAttributes) {
        currText.append(theText);
        preserveDrop();
      } else {
        // Fix current end position
        prevChar = new FilePos(prevChar.file, prevChar.line, prevChar.col - 1, prevChar.sourceNum);
        ProToken tok = makeToken(ABLNodeType.QSTRING);
        prevChar = new FilePos(prevChar.file, prevChar.line, prevChar.col + 1, prevChar.sourceNum);
        return tok;
      }
    } // currChar==':'

    return makeToken(ABLNodeType.QSTRING);
  }

  ProToken digitStart(boolean hex) {
    LOGGER.trace("Entering digitStart()");

    ABLNodeType ttype = ABLNodeType.NUMBER;
    for_loop : for (;;) {
      switch (currChar) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          append();
          getChar();
          break;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
          if (hex) {
            append();
            getChar();
            break;
          } else {
            append();
            getChar();
            if (ttype != ABLNodeType.FILENAME)
              ttype = ABLNodeType.ID;
            break;
          }
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
        case '#':
        case '$':
        case '%':
        case '&':
        case '_':
          append();
          getChar();
          if (ttype != ABLNodeType.FILENAME)
            ttype = ABLNodeType.ID;
          break;
        // We don't know here if the plus or minus is in the middle or at the end.
        // Don't change ttype.
        case '+':
        case '-':
          append();
          getChar();
          break;
        case '/':
          append();
          getChar();
          if (ttype == ABLNodeType.NUMBER)
            ttype = ABLNodeType.LEXDATE;
          break;
        case '\\':
          append();
          getChar();
          ttype = ABLNodeType.FILENAME;
          break;
        case '.':
          if (nameDot) {
            append();
            getChar();
            break;
          } else {
            break for_loop;
          }
        default:
          break for_loop;
      }
    }
    return makeToken(ttype);
  }

  ProToken plusMinusStart(ABLNodeType inputType) {
    LOGGER.trace("Entering plusMinusStart()");
    ABLNodeType ttype = ABLNodeType.NUMBER;
    for_loop : for (;;) {
      switch (currChar) {
        case '0':
          append();
          getChar();
          if ((currChar == 'x') || (currChar == 'X')) {
            append();
            getChar();
            return digitStart(true);
          } else {
            return digitStart(false);
          }

        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          append();
          getChar();
          return digitStart(false);

        // Leave comma out of this. -1, might be part of an expression list.
        case '#':
        case '$':
        case '%':
        case '&':
        case '/':
        case '\\':
        case '_':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
          append();
          getChar();
          ttype = ABLNodeType.FILENAME;
          break;
        case '.':
          if (nameDot) {
            append();
            getChar();
            break;
          } else {
            break for_loop;
          }
        default:
          break for_loop;
      }
    }
    if (currText.length() == 1)
      return makeToken(inputType);
    else
      return makeToken(ttype);
  }

  ProToken periodStart() {
    LOGGER.trace("Entering periodStart()");

    if (!Character.isDigit(currChar)) {
      if (nameDot)
        return makeToken(ABLNodeType.NAMEDOT);
      else
        return makeToken(ABLNodeType.PERIOD);
    }
    ABLNodeType ttype = ABLNodeType.NUMBER;
    for_loop : for (;;) {
      switch (currChar) {
        // We don't know here if the plus or minus is in the middle or at the end.
        // Don't change _ttype.
        case '+':
        case '-':
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
          append();
          getChar();
          break;
        case '#':
        case '$':
        case '%':
        case '&':
        case '/':
        case '\\':
        case '_':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
          append();
          getChar();
          ttype = ABLNodeType.FILENAME;
          break;
        default:
          break for_loop;
      }
    }
    return makeToken(ttype);
  }

  ProToken id(ABLNodeType inputTokenType) {
    LOGGER.trace("Entering id()");

    // Tokens that start with a-z or underscore
    // - ID
    // - FILENAME
    // - keyword (testLiterals = true)
    // Also inputTokenType can be ANNOTATION for a token that starts with '@'.
    // Based on the PROGRESS online help, the following are the valid name characters.
    // Undocumented: you can use a slash in an index name! Arg!
    // Undocumented: the compiler allows you to start a block label with $
    // If we find a back slash, we know we're into a filename.
    // Extended characters (octal 200-377) can be used in identifiers, even at the beginning.
    ABLNodeType ttype = inputTokenType;
    for_loop : for (;;) {
      switch (currChar) {
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
        case '_':
        case '-':
        case '$':
        case '#':
        case '%':
        case '&':
        case '/':
          // For tokens like ALT-* and CTRL-` :
          // Emperically, I found that the following are the only other
          // characters that can be put into a key label. Things
          // like ALT-, must be put into quotes "ALT-,", because
          // the comma has special meaning in 4gl code.
          // Note also that extended characters can come after CTRL- or ALT-.
          // ('!'|'@'|'^'|'*'|'+'|';'|'"'|'`')
        case '!':
        case '"':
        case '*':
        case '+':
        case ';':
        case '@':
        case '^':
        case '`':
          append();
          getChar();
          break;
        case '\\':
        case '\'':
          append();
          getChar();
          if (ttype == ABLNodeType.ID)
            ttype = ABLNodeType.FILENAME;
          break;
        case '.':
          break for_loop;
        default:
          if (currInt >= 128 && currInt <= 255) {
            append();
            getChar();
            break;
          } else {
            break for_loop;
          }
      }
    }
    // See if it's a keyword
    if (ttype == ABLNodeType.ID)
      ttype = ABLNodeType.getLiteral(currText.toString(), ttype);
    return makeToken(ttype);
  }

  ProToken ampText() {
    LOGGER.trace("Entering ampText()");
    for (;;) {
      if (Character.isLetterOrDigit(currInt) || (currInt >= 128 && currInt <= 255)) {
        append();
        getChar();
        continue;
      }
      switch (currChar) {
        case '#':
        case '$':
        case '%':
        case '&':
        case '-':
        case '_':
          append();
          getChar();
          continue;
      }
      if (currChar == '/') {
        // You can embed comments in (or at the end of) an &token.
        // I've no idea why. See the regression test for bug#083.
        preserveCurrent();
        getChar();
        if (currChar == '*') {
          String s = currText.toString();
          comment();
          currText.replace(0, currText.length(), s);
          preserveDrop();
          continue;
        }
      }
      break;
    }
    ProToken t = directive();
    if (t != null)
      return t;
    return makeToken(ABLNodeType.FILENAME);
  }

  ProToken directive() {
    LOGGER.trace("Entering directive()");

    // Called by ampText, which has already gather the text for
    // the *potential* directive.

    String macroType = currText.toString().toLowerCase();

    if ("&global-define".startsWith(macroType) && macroType.length() >= 4) {
      appendToEOL();
      // We have to do the define *before* getting next char.
      macroDefine(Proparse.AMPGLOBALDEFINE);
      getChar();
      return makeToken(ABLNodeType.AMPGLOBALDEFINE);
    }
    if ("&scoped-define".startsWith(macroType) && macroType.length() >= 4) {
      appendToEOL();
      // We have to do the define *before* getting next char.
      macroDefine(Proparse.AMPSCOPEDDEFINE);
      getChar();
      return makeToken(ABLNodeType.AMPSCOPEDDEFINE);
    }

    if ("&undefine".startsWith(macroType) && macroType.length() >= 5) {
      // Append whitespace between &UNDEFINE and the target token
      while (Character.isWhitespace(currChar)) {
        append();
        getChar();
      }
      // Append the target token
      while ((!Character.isWhitespace(currChar)) && currInt != Token.EOF) {
        append();
        getChar();
      }
      // &UNDEFINE consumes up to *and including* the first whitespace
      // after the token it undefines.
      // At least that seems to be what Progress is doing.
      if (currChar == '\r') {
        append();
        getChar();
        if (currChar == '\n') {
          append();
          getChar();
        }
      } else if (currInt != Token.EOF) {
        append();
        getChar();
      }
      macroUndefine();
      return makeToken(ABLNodeType.AMPUNDEFINE);
    }

    if ("&analyze-suspend".equals(macroType)) {
      appendToEOL();
      String analyzeSuspend = "";
      if (currText.toString().indexOf(' ') != -1) {
        // Documentation says &analyze-suspend is always followed by an option, but better to never trust
        // documentation...
        // Generates a clean comma-separated list of all entries
        analyzeSuspend = Joiner.on(',').join(Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(
            currText.toString().substring(currText.toString().indexOf(' ') + 1)));
      }
      getChar();
      analyzeSuspend(analyzeSuspend);
      prepro.getLstListener().analyzeSuspend(analyzeSuspend, tokenStartPos.line);
      return makeToken(ABLNodeType.AMPANALYZESUSPEND);
    }
    if ("&analyze-resume".equals(macroType)) {
      appendToEOL();
      getChar();
      analyzeResume();
      prepro.getLstListener().analyzeResume(tokenStartPos.line);
      return makeToken(ABLNodeType.AMPANALYZERESUME);
    }
    if ("&message".equals(macroType)) {
      appendToEOL();
      getChar();
      return makeToken(ABLNodeType.AMPMESSAGE);
    }

    if ("&if".equals(macroType)) {
      return makeToken(ABLNodeType.AMPIF);
    }
    if ("&then".equals(macroType)) {
      return makeToken(ABLNodeType.AMPTHEN);
    }
    if ("&elseif".equals(macroType)) {
      return makeToken(ABLNodeType.AMPELSEIF);
    }
    if ("&else".equals(macroType)) {
      return makeToken(ABLNodeType.AMPELSE);
    }
    if ("&endif".equals(macroType)) {
      return makeToken(ABLNodeType.AMPENDIF);
    }

    // If we got here, it wasn't a preprocessor directive,
    // and the caller is responsible for building the token.
    return null;

  }

  //////////////// End lexical productions, begin support functions

  private void append() {
    currText.append((char) currInt);
  }

  private void appendToEOL() {
    // As with the other "append" functions, the caller is responsible for calling getChar() after this.
    for (;;) {
      if (currChar == '/') {
        append();
        getChar();
        if (currChar == '*') {
          // comment() expects to start at '*',
          // finishes on char after closing slash
          comment();
          continue;
        }
        continue;
      }
      if (currInt == Token.EOF)
        break;
      append();
      // Unescaped newline character or escaped newline where previous char is not tilde
      if ((currChar == '\n') && (!wasEscape || (wasEscape && !currText.toString().endsWith("~\n")))) {
        // We do not call getChar() here. That is because we cannot
        // get the next character until after any &glob, &scoped, or &undefine
        // have been dealt with. The next character might be a '{' which in
        // turn leads to a reference to what is just now being defined or
        // undefined.
        break;
      }
      getChar();
    }
  }

  boolean currIsSpace() {
    return (currInt == Token.EOF || Character.isWhitespace(currChar));
  }

  private void getChar() {
    prevChar = new FilePos(currFile, currLine, currCol, currSourceNum);
    prevMacroExpansion = currMacroExpansion;
    currInt = ppGetChar();
    currChar = Character.toLowerCase(currInt);
  }

  ProToken makeToken(ABLNodeType type) {
    return makeToken(type, currText.toString());
  }

  ProToken makeToken(ABLNodeType type, String text) {
    // Counting lines of code and commented lines only in the main file (textStartFile set to 0)
    if ((tokenStartPos.file == 0) && (type == ABLNodeType.COMMENT)) {
      int numLines = currText.toString().length() - currText.toString().replace("\n", "").length();
      for (int zz = tokenStartPos.line; zz <= tokenStartPos.line + numLines; zz++) {
        comments.add(zz);
      }
    } else if ((tokenStartPos.file == 0) && (type != ABLNodeType.WS) && (type != ABLNodeType.EOF_ANTLR4)
        && (tokenStartPos.line > 0)) {
      loc.add(tokenStartPos.line);
    }
    return new ProToken.Builder(type, text) //
      .setFileIndex(tokenStartPos.file) //
      .setFileName(prepro.getFilename(tokenStartPos.file)) //
      .setLine(tokenStartPos.line) //
      .setCharPositionInLine(tokenStartPos.col) //
      .setEndFileIndex(prevChar.file) //
      .setEndLine(prevChar.line) //
      .setEndCharPositionInLine(prevChar.col) //
      .setMacroExpansion(prevMacroExpansion) //
      .setMacroSourceNum(tokenStartPos.sourceNum) //
      .setAnalyzeSuspend(getCurrentAnalyzeSuspend()) //
      .build();
  }

  void macroDefine(int defType) {
    LOGGER.trace("Entering macroDefine({})", defType);

    if (prepro.isConsuming() || prepro.isLexOnly())
      return;
    int it = 0;
    int end = currText.length();
    while (!Character.isWhitespace(currText.charAt(it)))
      ++it; // "&glob..." or "&scoped..."
    while (Character.isWhitespace(currText.charAt(it)))
      ++it; // whitespace
    int start = it;
    while (!Character.isWhitespace(currText.charAt(it)))
      ++it; // macro name
    String macroName = currText.substring(start, it);
    while (it != end && Character.isWhitespace(currText.charAt(it)))
      ++it; // whitespace
    String defText = StringFuncs.stripComments(currText.substring(it));
    defText = defText.trim();
    // Do listing before lowercasing the name
    prepro.getLstListener().define(tokenStartPos.line, tokenStartPos.col, macroName.toLowerCase(Locale.ENGLISH),
        defText, defType == Proparse.AMPGLOBALDEFINE ? MacroDefinitionType.GLOBAL : MacroDefinitionType.SCOPED);
    if (defType == Proparse.AMPGLOBALDEFINE)
      defGlobal(macroName.toLowerCase(), defText);
    else
      defScoped(macroName.toLowerCase(), defText);
  }

  void macroUndefine() {
    LOGGER.trace("Entering macroUndefine()");

    if (prepro.isConsuming())
      return;
    int it = 0;
    int end = currText.length();
    while (!Character.isWhitespace(currText.charAt(it)))
      ++it; // "&undef..."
    while (Character.isWhitespace(currText.charAt(it)))
      ++it; // whitespace
    int start = it;
    while (it != end && (!Character.isWhitespace(currText.charAt(it))))
      ++it; // macro name
    String macroName = currText.substring(start, it);
    // List the name as in the code - not lowercased
    prepro.getLstListener().undefine(tokenStartPos.line, tokenStartPos.col, macroName);
    undef(macroName.toLowerCase());
  }

  /**
   * Get the lookahead character. The caller is responsible for knowing that the lookahead isn't already there, ex: if
   * (!gotLookahead) laGet();
   */
  private void laGet() {
    CharPos saveChar = new CharPos(ppCurrChar, currFile, currLine, currCol, currSourceNum);
    ppGetRawChar();
    la = new CharPos(ppCurrChar, currFile, currLine, currCol, currSourceNum);
    currFile = saveChar.file;
    currLine = saveChar.line;
    currCol = saveChar.col;
    currSourceNum = saveChar.sourceNum;
    ppCurrChar = saveChar.ch;
  }

  private void laUse() {
    currFile = la.file;
    currLine = la.line;
    currCol = la.col;
    currSourceNum = la.sourceNum;
    ppCurrChar = la.ch;
    la = null;
  }

  /**
   * We keep a record of discarded escape characters. This is in case the client wants to fetch those and use them.
   */
  private int escape() {
    // We may have multiple contiguous discarded characters
    // or a new escape sequence.
    if (wasEscape)
      escapeText += (char) ppCurrChar;
    else {
      wasEscape = true;
      escapeText = Character.toString((char) ppCurrChar);
      escapeAppend = true;
    }
    // Discard current character ('~' or '\\'), get next.
    ppGetRawChar();
    int retChar = ppCurrChar;
    escapeCurrent = true;
    switch (ppCurrChar) {
      case '\n':
        // An escaped newline can be pretty much anywhere: mid-keyword, mid-identifier, between '*' and '/', etc.
        // It is discarded.
        escapeText += (char) ppCurrChar;
        retChar = SKIP_CHAR;
        break;
      case '\r':
        // Lookahead to the next character.
        // If it's anything other than '\n', we need to do normal processing on it. Progress does not strip '\r' the way
        // it strips '\n'. There is one issue here - Progress treats "\r\r\n" the same as "\r\n". I'm not sure how I
        // could implement it.
        if (la == null)
          laGet();
        if (la.ch == '\n') {
          escapeText += "\r\n";
          laUse();
          retChar = SKIP_CHAR;
        } else {
          retChar = '\r';
        }
        break;
      case 'r':
        // An escaped 'r' or an escaped 'n' gets *converted* to a different character. We don't just drop chars.
        escapeText += (char) ppCurrChar;
        escapeAppend = false;
        retChar = '\r';
        break;
      case 'n':
        // An escaped 'r' or an escaped 'n' gets *converted* to a different character. We don't just drop chars.
        escapeText += (char) ppCurrChar;
        escapeAppend = false;
        retChar = '\n';
        break;
      default:
        escapeAppend = true;
        break; // No change to retChar.
    }
    return retChar;
  }

  /**
   * Returns number of lines of code in the main file (i.e. including any line where there's a non-comment and
   * non-whitespace token
   */
  public int getLoc() {
    return loc.size();
  }

  public int getCommentedLines() {
    return comments.size();
  }

  void preserveCurrent() {
    // Preserve the current character/file/line/col before looking
    // ahead to the next character. Need this because current char
    // might be appended to current token, or it might be the start
    // of the next token, depending on what character follows... but
    // as soon as we look ahead to the following character, we lose
    // our file/line/col, and that's why we need to preserve.
    preserve = true;
    preservedChar = new CharPos(currChar, currFile, currLine, currCol, currSourceNum);
  }

  void preserveDrop() {
    preserve = false;
  }

  void unEscapedAppend() {
    if (wasEscape) {
      currText.append(escapeText);
      if (escapeAppend)
        append();
    } else {
      append();
    }
  }

  void lexicalThrow(String theMessage) {
    throw new ProparseRuntimeException(getFilename() + ":" + Integer.toString(currLine) + " " + theMessage);
  }

  /**
   * Cleanup work, once the parse is complete.
   */
   void parseComplete() {
    while (ppPopInput() != 0) {
      // No-op
    }
    // Clean up the temporary junk
    currentInclude = null;
    currentInput = null;
    includeCache.clear();
    includeCache2.clear();
  }

  @CheckForNull
  String getCurrentAnalyzeSuspend() {
    return currentInput.getAnalyzeSuspend();
  }

  private String getFilename() {
    return prepro.getFilename(currentInput.getFileIndex());
  }

  private void checkForNameDot() {
    // Have to check for nameDot in the preprocessor because nameDot is true
    // even if the next character is a '{' which eventually expands
    // out to be a space character.
    if (la == null)
      laGet();
    nameDot = (la.ch != Token.EOF) && !Character.isWhitespace(la.ch) && (la.ch != '.');
  }

  int addFilename(String filename) {
    if (prepro.getFilenameList().hasValue(filename))
      return prepro.getFilenameList().getIndex(filename);

    return prepro.getFilenameList().add(filename);
  }

  private int ppGetChar() {
    wasEscape = false;
    for (;;) {
      escapeCurrent = false;
      if (la != null)
        laUse();
      else
        ppGetRawChar();
      switch (ppCurrChar) {
        case '\\':
        case '~': {
          // Escapes are *always* processed, even inside strings and comments.
          if ((ppCurrChar == '\\') && (prepro.getProparseSettings().getOpSys() == OperatingSystem.WINDOWS)
              && !prepro.getProparseSettings().useBackslashAsEscape()) {
            return ppCurrChar;
          }
          int retChar = escape();
          if (retChar == '.')
            checkForNameDot();
          if (retChar != SKIP_CHAR)
            return retChar;
          // else do another loop
          break;
        }
        case '{':
          // Macros are processed inside strings, but not inside comments.
          if (doingComment)
            return ppCurrChar;
          else {
            ppMacroReference();
            if ((ppCurrChar == PROPARSE_DIRECTIVE) || (ppCurrChar == INCLUDE_DIRECTIVE))
              return ppCurrChar;
            // else do another loop
          }
          break;
        case '.':
          checkForNameDot();
          return ppCurrChar;
        default:
          return ppCurrChar;
      }
    }
  }

  /**
   * Deal with end of input stream, and switch to previous. Because Progress allows you to switch streams in the middle
   * of a token, we have to completely override EOF handling, right at the point where we get() a new character from the
   * input stream. If we are at an EOF other than the topmost program file, then we don't want the EOF to get into our
   * character stream at all. If we've popped an include file off the stack (not just argument or preprocessor text),
   * then we have to insert a space into the character stream, because that's what Progress's compiler does.
   */
  private void ppGetRawChar() {
    currLine = currentInput.getNextLine();
    currCol = currentInput.getNextCol();
    ppCurrChar = currentInput.get();
    currMacroExpansion = currentInput.isMacroExpansion();

    if (ppCurrChar == 0xFFFD) {
      // This is the 'replacement' character in Unicode, used by Java as a placeholder for a character which could not
      // be converted. We replace those characters at runtime with a space, and log an error
      LOGGER.error("Character conversion error in {} at line {} column {} from encoding {}", getFilename(), currLine,
          currCol, prepro.getRefactorSession().getCharset());
      ppCurrChar = ' ';
    }
    while (ppCurrChar == Token.EOF) {
      switch (ppPopInput()) {
        case 0: // nothing left to pop
          if (++safetyNet > 100)
            throw new ProparseRuntimeException("Proparse error. Infinite loop caught by preprocessor.");
          return;
        case 1: // popped an include file
          currFile = currentInput.getFileIndex();
          currLine = currentInput.getNextLine();
          currCol = currentInput.getNextCol();
          currSourceNum = currentInput.getSourceNum();
          currMacroExpansion = currentInput.isMacroExpansion();
          ppCurrChar = ' ';
          return;
        case 2: // popped a macro ref or include arg ref
          currFile = currentInput.getFileIndex();
          currLine = currentInput.getNextLine();
          currCol = currentInput.getNextCol();
          ppCurrChar = currentInput.get(); // might be another EOF
          currSourceNum = currentInput.getSourceNum();
          currMacroExpansion = currentInput.isMacroExpansion();
          break;
        default:
          throw new IllegalStateException("Proparse error. popInput() returned unexpected value.");
      }
    }
  }

  private void ppMacroReference() {
    ArrayList<IncludeArg> incArgs = new ArrayList<>();

    macroStartPos = new FilePos(currFile, currLine, currCol, currSourceNum);
    // Preserve the macro reference start point, because textStart get messed with if this macro reference itself
    // contains any macro references.
    FilePos refPos = new FilePos(macroStartPos);

    // Gather the macro reference text
    // Do not stop on escaped '}'
    StringBuilder refTextBldr = new StringBuilder("{");
    char macroChar = (char) ppGetChar();
    while ((macroChar != '}' || wasEscape) && macroChar != Token.EOF) {
      refTextBldr.append(macroChar);
      macroChar = (char) ppGetChar();
    }
    if (macroChar == Token.EOF)
      lexicalThrow("Unmatched curly brace");
    refTextBldr.append(macroChar); // should be '}'
    String refText = refTextBldr.toString();
    MacroCharPos cp = new MacroCharPos(refText.toCharArray(), 0);
    int refTextEnd = refText.length();
    int closingCurly = refTextEnd - 1;

    if (refText.toLowerCase().startsWith("{&_proparse_") && prepro.getProparseSettings().getProparseDirectives()) {
      // Proparse Directive
      ppCurrChar = PROPARSE_DIRECTIVE;
      // We strip "{&_proparse_", trailing '}', and leading/trailing whitespace
      proparseDirectiveText = refText.substring(12, closingCurly).trim();
      // This will be counted as a source whether picked up here or picked
      // up as a normal macro ref.
      ++sourceCounter;
      prepro.getLstListener().macroRef(refPos.line, refPos.col, "_proparse");
    } else if ("{*}".equals(refText)) {
      // {*} -- all arguments
      ppNewMacroRef("*", refPos);
    } else if (refText.startsWith("{&*")) {
      // {&* -- all named arguments
      ppNewMacroRef("&*", refPos);
    } else if (isNumber(refText.substring(1, closingCurly))) {
      // {(0..9)+} -- a numbered argument
      String theText = refText.substring(1, closingCurly);
      int argNum = Integer.parseInt(theText);
      ppNewMacroRef(argNum, refPos);
    } else if (isWhiteSpace(refText.substring(1, closingCurly))) {
      // { } -- empty curlies - ignored
    } else if (refText.startsWith("{&")) {
      // {& -- named argument or macro expansion
      // Note that you can reference "{&}" to get an
      // undefined named include argument.
      // In that case, argName remains blank.
      // Trailing whitespace is trimmed.
      String argName = refText.substring(2, closingCurly).trim().toLowerCase();
      ppNewMacroRef(argName, refPos);
    } else {
      // If we got here, it's an include file reference
      boolean usingNamed = false;
      String argName;
      String argVal;

      // '{'
      cp.pos = 1; // skip '{'

      // whitespace?
      while (Character.isWhitespace(cp.chars[cp.pos]))
        ++cp.pos;

      // filename
      String includeFilename = ppIncludeRefArg(cp);

      // whitespace?
      while (Character.isWhitespace(cp.chars[cp.pos]))
        ++cp.pos;

      // no include args?
      if (cp.pos == closingCurly) {
        // do nothing
      }

      else if (cp.chars[cp.pos] == '&') { // include '&' named args
        usingNamed = true;
        while (cp.pos != refTextEnd && cp.chars[cp.pos] == '&') {
          ++cp.pos; // skip '&'

          // Arg name
          // Consume to '=' or closing '}'
          // discard all WS
          argName = "";
          while (cp.pos != refTextEnd) {
            if (cp.pos == closingCurly || cp.chars[cp.pos] == '=' || cp.chars[cp.pos] == '&')
              break;
            if (!(Character.isWhitespace(cp.chars[cp.pos])))
              argName += cp.chars[cp.pos];
            ++cp.pos;
          }

          argVal = "";
          boolean undefined = true;
          if (cp.chars[cp.pos] == '=') {
            undefined = false;
            // '=' with optional WS
            ++cp.pos;
            while (cp.pos != closingCurly && Character.isWhitespace(cp.chars[cp.pos]))
              ++cp.pos;
            // Arg val
            if (cp.pos != closingCurly)
              argVal = ppIncludeRefArg(cp);
          }

          // Add the argument name/val pair
          incArgs.add(new IncludeArg(argName, argVal, undefined));

          // Anything not beginning with & is discarded
          while (cp.pos != refTextEnd && cp.chars[cp.pos] != '&')
            ++cp.pos;

        } // while loop
      } // include '&' named args

      else { // include numbered args
        usingNamed = false;
        while (cp.pos != refTextEnd) {
          while (Character.isWhitespace(cp.chars[cp.pos]))
            ++cp.pos;
          // Are we at closing curly?
          if (cp.pos == closingCurly)
            break;
          incArgs.add(new IncludeArg("", ppIncludeRefArg(cp)));
        }
      } // numbered args

      // If lex only, we generate a token
      if (prepro.isLexOnly()) {
        ppCurrChar = INCLUDE_DIRECTIVE;
        includeDirectiveText = refText.trim();
      } else
      // newInclude() returns false if filename is blank or currently
      // "consuming" due to &IF FALSE.
      // newInclude() will throw() if file not found or cannot be opened.
      if (ppNewInclude(includeFilename)) {
        // Unlike currline and currcol,
        // currfile is only updated with a push/pop of the input stack.
        currFile = currentInput.getFileIndex();
        currSourceNum = currentInput.getSourceNum();
        prepro.getLstListener().include(refPos.line, refPos.col, currFile, includeFilename);
        // Add the arguments to the new include object.
        int argNum = 1;
        for (IncludeArg incarg : incArgs) {
          if (usingNamed)
            currentInclude.addNamedArgument(incarg.argName, incarg.argVal);
          else
            currentInclude.addArgument(incarg.argVal);
          prepro.getLstListener().includeArgument(usingNamed ? incarg.argName : Integer.toString(argNum), incarg.argVal,
              incarg.undefined);
          argNum++;
        }
      }
    } // include file reference
  } // macroReference()

  /**
   * New macro or named/numbered argument reference. Input either macro/argument name or the argument number, as well as
   * fileIndex, line, and column where the '{' appeared. Returns false if there's nothing to expand.
   */
  private void ppNewMacroRef(String macroName, FilePos refPos) {
    // Using this trick: {{&undefined-argument}{&*}}
    // it is possible to get line breaks into what we
    // get here as the macroName. See test data bug15.p and bug15.i.
    prepro.getLstListener().macroRef(refPos.line, refPos.col, macroName);
    ppNewMacroRef2(getArgText(macroName), refPos);
  }

  private void ppNewMacroRef(int argNum, FilePos refPos) {
    prepro.getLstListener().macroRef(refPos.line, refPos.col, Integer.toString(argNum));
    ppNewMacroRef2(getArgText(argNum), refPos);
  }

  private void ppNewMacroRef2(String theText, FilePos refPos) {
    if (theText.length() == 0) {
      ++sourceCounter;
      prepro.getLstListener().macroRefEnd();
      return;
    }
    // We must expand macros even if consuming,
    // because we can have &ENDIF inside a preprocesstoken
    // For a macro/argument expansion, we use the file/line/col of
    // the opening curly '{' of the ref file, for all characters/tokens.
    currentInput = new InputSource(++sourceCounter, theText, refPos.file, refPos.line, refPos.col);
    currentInclude.addInputSource(currentInput);
    currentInput.setNextLine(refPos.line);
    currentInput.setNextCol(refPos.col);
  }

  /**
   * Pop the current input source off the stack. Returns true if we've popped off the end of an include file, false if
   * we've just popped off an argument or preprocessor text. The calling program has to know this, to add the space ' '
   * at the end of the include reference.
   */
  private int ppPopInput() {
    // Returns 2 if we popped a macro or arg ref off the input stack.
    // Returns 1 if we popped an include file off the input stack.
    // Returns 0 if there's nothing left to pop.
    // There's no need to pop the primary input source, so we leave it
    // around. There's a good chance that something will want to refer
    // to currentInclude or currentInput anyway, even though it's done.
    InputSource tmp;
    if ((tmp = currentInclude.pop()) != null) {
      currentInput = tmp;
      prepro.getLstListener().macroRefEnd();
      return 2;
    } else if (includeVector.size() > 1) {
      includeVector.removeLast();
      currentInclude = includeVector.getLast();
      currentInput = currentInclude.getLastSource();
      prepro.getLstListener().includeEnd();
      LOGGER.trace("Back to file: {}", getFilename());
      return 1;
    } else {
      return 0;
    }
  }

  /*
   * Get the next include reference arg, reposition the charpos. A doublequote will start a string - all this means is
   * that we'll collect whitespace. A singlequote does not have this effect.
   */
  private String ppIncludeRefArg(MacroCharPos cp) {
    boolean gobbleWS = false;
    StringBuilder theRet = new StringBuilder();
    // Iterate up to, but not including, closing curly.
    while (cp.pos < cp.chars.length - 1) {
      char c = cp.chars[cp.pos];
      switch (c) {
        case '"':
          if (cp.chars[cp.pos + 1] == '"') {
            // quoted quote - does not open/close a string
            theRet.append('"');
            ++cp.pos;
            ++cp.pos;
          } else {
            gobbleWS = !gobbleWS;
            ++cp.pos;
          }
          break;
        case ' ':
        case '\t':
        case '\f':
        case '\n':
        case '\r':
          if (gobbleWS) {
            theRet.append(c);
            ++cp.pos;
          } else {
            return theRet.toString();
          }
          break;
        default:
          theRet.append(c);
          ++cp.pos;
          break;
      }
    }
    return theRet.toString();
  }

  private boolean ppNewInclude(String referencedWithName) {
    // Progress doesn't enter include files if &IF FALSE
    // It *is* possible to get here with a blank include file
    // name. See bug#034. Don't enter if the includefilename is blank.
    String fName = referencedWithName.trim().replace('\\', '/');
    if (prepro.isConsuming() || prepro.isLexOnly() || fName.length() == 0)
      return false;

    File incFile = null;
    // Did we ever read file with same referenced name ?
    Integer idx = includeCache.get(fName);
    if (idx == null) {
      // No, then we have to read file
      incFile = prepro.getRefactorSession().findFile3(fName);
      if (incFile == null) {
        throw new UncheckedIOException(new IncludeFileNotFoundException(getFilename(), referencedWithName));
      }
      try {
        idx = addFilename(incFile.getCanonicalPath());
      } catch (IOException caught) {
        throw new UncheckedIOException(caught);
      }
      includeCache.put(fName, idx);
    }

    if (includeCache2.get(idx) != null) {
      try {
        currentInput = new InputSource(++sourceCounter, fName,
            ByteSource.wrap(includeCache2.get(idx).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8, idx,
            prepro.getProparseSettings().getSkipXCode(), false);
      } catch (IOException caught) {
        throw new UncheckedIOException(caught);
      }
    } else {
      try {
        currentInput = new InputSource(++sourceCounter, incFile, prepro.getRefactorSession().getCharset(), idx,
            prepro.getProparseSettings().getSkipXCode(), false);
        includeCache2.put(idx, currentInput.getContent());
      } catch (IOException caught) {
        throw new UncheckedIOException(caught);
      }
    }
    currentInclude = new IncludeFile(referencedWithName, currentInput);
    includeVector.add(currentInclude);
    LOGGER.trace("Entering file: {}", getFilename());

    return true;
  }

  // ****************************
  // IPreprocessor implementation
  // ****************************

  @Override
  public String defined(String argName) {
    // Yes, the precedence for defined() really is 3,2,3,1,0.
    // First look for local SCOPED define
    if (currentInclude.isNameDefined(argName))
      return "3";
    // Second look for named include arg
    if (currentInclude.getNamedArg(argName) != null)
      return "2";
    // Third look for a non-local SCOPED define
    for (IncludeFile incl : includeVector) {
      if (incl.isNameDefined(argName))
        return "3";
    }
    // Finally, check for global define
    if (globalDefdNames.containsKey(argName))
      return "1";
    // Not defined
    return "0";
  }

  @Override
  public void defGlobal(String argName, String argVal) {
    LOGGER.trace("Global define '{}': '{}'", argName, argVal);
    globalDefdNames.put(argName, argVal);
  }

  @Override
  public void defScoped(String argName, String argVal) {
    LOGGER.trace("Scoped define '{}': '{}'", argName, argVal);
    currentInclude.scopeDefine(argName, argVal);
  }

  @Override
  public String getArgText(int argNum) {
    return currentInclude.getNumberedArgument(argNum);
  }

  @Override
  public String getArgText(String argName) {
    LOGGER.trace("getArgText('{}')", argName);
    String ret;
    // First look for local &SCOPE define
    ret = currentInclude.getValue(argName);
    if (ret != null) {
      LOGGER.trace("Found scope-defined variable: '{}'", ret);
      return ret;
    }
    // Second look for a named include file argument
    ret = currentInclude.getNamedArg(argName);
    if (ret != null) {
      LOGGER.trace("Found named argument: '{}'", ret);
      return ret;
    }
    // Third look for a non-local SCOPED define
    for (int i = includeVector.size() - 1; i >= 0; --i) {
      ret = includeVector.get(i).getValue(argName);
      if (ret != null) {
        LOGGER.trace("Found non-local scope-defined variable: '{}'", ret);
        return ret;
      }
    }
    // Fourth look for a global define
    ret = globalDefdNames.get(argName);
    if (ret != null) {
      LOGGER.trace("Found global-defined variable: '{}'", ret);
      return ret;
    }
    // * to return all include arguments, space delimited.
    if ("*".equals(argName)) {
      LOGGER.trace("Return all include arugments");
      return currentInclude.getAllArguments();
    }
    // &* to return all named include argument definitions
    if ("&*".equals(argName)) {
      LOGGER.trace("Return all named include arugments");
      return currentInclude.getAllNamedArgs();
    }
    // Built-ins
    if ("batch-mode".equals(argName))
      return Boolean.toString(prepro.getProparseSettings().getBatchMode());
    if ("opsys".equals(argName))
      return prepro.getProparseSettings().getOpSys().getName();
    if ("process-architecture".equals(argName))
      return prepro.getProparseSettings().getProcessArchitecture().toString();
    if ("window-system".equals(argName))
      return prepro.getProparseSettings().getWindowSystem();
    if ("file-name".equals(argName)) {
      // {&FILE-NAME}, unlike {0}, returns the filename as found on the PROPATH.
      ret = prepro.getRefactorSession().findFile(currentInclude.getNumberedArgument(0));
      // Progress seems to be converting the slashes for the appropriate OS.
      // I don't convert the slashes when I store the filename - instead I do it here.
      // (Saves us from converting the slashes for each and every include reference.)
      if (prepro.getProparseSettings().getOpSys() == OperatingSystem.UNIX)
        ret = ret.replace('\\', '/');
      else
        ret = ret.replace('/', '\\');
      return ret;
    }
    if ("line-number".equals(argName))
      return Integer.toString(currLine);
    if ("sequence".equals(argName))
      return Integer.toString(sequence++);

    // Not defined
    LOGGER.trace("Nothing found...");
    return "";
  }

  @Override
  public void undef(String argName) {
    // First look for a local file scoped defined name to undef
    if (currentInclude.isNameDefined(argName)) {
      currentInclude.removeVariable(argName);
      return;
    }
    // Second look for a named include arg to undef
    if (currentInclude.undefNamedArg(argName))
      return;
    // Third look for a non-local file scoped defined name to undef
    ListIterator<IncludeFile> it = includeVector.listIterator(includeVector.size());
    while (it.hasPrevious()) {
      IncludeFile incfile = it.previous();
      if (incfile.isNameDefined(argName)) {
        incfile.removeVariable(argName);
        return;
      }
    }
    // Last, look for a global arg to undef
    undefHelper(argName, globalDefdNames);
  }

  @Override
  public void analyzeSuspend(@Nonnull String analyzeSuspend) {
    // Notify current include
    currentInput.setAnalyzeSuspend(analyzeSuspend);
  }

  @Override
  public void analyzeResume() {
    // Notify current include
    currentInput.setAnalyzeSuspend("");
  }

  private boolean undefHelper(String argName, Map<String, String> names) {
    if (names.containsKey(argName)) {
      names.remove(argName);
      return true;
    }
    return false;
  }

  // ***********************************
  // End of IPreprocessor implementation
  // ***********************************

  private static boolean isNumber(String str) {
    if ((str == null) || (str.length() == 0))
      return false;
    for (int zz = 0; zz < str.length(); zz++) {
      if (!Character.isDigit(str.charAt(zz)))
        return false;
    }
    return true;
  }

  private static boolean isWhiteSpace(String str) {
    if ((str == null))
      return false;
    for (int zz = 0; zz < str.length(); zz++) {
      if (!Character.isWhitespace(str.charAt(zz)))
        return false;
    }
    return true;
  }

  private static class IncludeArg {
    private final String argName;
    private final String argVal;
    private final boolean undefined;

    IncludeArg(String argName, String argVal) {
      this(argName, argVal, false);
    }

    IncludeArg(String argName, String argVal, boolean undefined) {
      this.argName = argName;
      this.argVal = argVal;
      this.undefined = undefined;
    }
  }

  private static class MacroCharPos {
    private final char[] chars;
    private int pos;

    MacroCharPos(char[] c, int p) {
      chars = c;
      pos = p;
    }
  }

}

/*
 * EOF Notes
 * 
 * Note[1] Cannot track file/line/col of include ref arguments. Why? Because it gathers the {...} into a string, and
 * preprocessing takes place on that text as it is gathered into the string. (Escape sequences, especially.) Once that
 * is complete, *then* it begins to evaluate the string for include arguments. The only option is to try to synch the
 * source with the listing.
 * 
 */
