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
package org.prorefactor.proparse.antlr4;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.macrolevel.MacroDef;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class Lexer  {
  private static final Logger LOGGER = LoggerFactory.getLogger(Lexer.class);

  private static final int EOF_CHAR = -1;

  /** Lowercase value of current character */
  private int currChar;

  /** Current character, before being lowercased */
  private int currInt;
  private int currFile, currLine, currCol;
  private boolean currMacro, prevMacro;
  private int prevFile, prevLine, prevCol;

  private int currStringType;
  private StringBuilder currText = new StringBuilder();

  private final ProgressLexer prepro;

  private boolean mergeNameDotInId = false;
  private boolean gettingAmpIfDefArg = false;
  private boolean preserve = false;
  private int preserveFile;
  private int preserveLine;
  private int preserveCol;
  private int preserveSource;
  private int preserveChar;

  private int textStartFile;
  private int textStartLine;
  private int textStartCol;
  private int textStartSource;

  private Set<Integer> comments = new HashSet<>();
  private Set<Integer> loc = new HashSet<>();

  Lexer(ProgressLexer prepro) {
    this.prepro = prepro;
    getChar(); // We always assume "currChar" is available.
  }


  //////////////// Lexical productions listed first, support functions follow.
  public ProToken nextToken() {
    LOGGER.trace("Entering nextToken()");
    for (;;) {

      if (preserve) {
        // The preserved character is the character prior to currChar.
        textStartFile = preserveFile;
        textStartLine = preserveLine;
        textStartCol = preserveCol;
        textStartSource = preserveSource;
        currText.setLength(1);
        currText.setCharAt(0, (char) preserveChar);
        preserveDrop(); // we are done with the preservation
        if (preserveChar == '.') {
          return periodStart();
        } else if (preserveChar == ':') {
          return colon();
        }
      }

      // Proparse Directive
      // Check this before setting currText...
      // we don't want BEGIN_PROPARSE_DIRECTIVE in the text
      if (currInt == ProgressLexer.PROPARSE_DIRECTIVE) {
        textStartFile = prepro.getTextStart().getFile();
        textStartLine = prepro.getTextStart().getLine();
        textStartCol = prepro.getTextStart().getCol();
        textStartSource = prepro.getTextStart().getSourceNum();
        getChar();
        return makeToken(ABLNodeType.PROPARSEDIRECTIVE, prepro.getProparseDirectiveText());
      } else if (currInt == ProgressLexer.INCLUDE_DIRECTIVE) {
        textStartFile = prepro.getTextStart().getFile();
        textStartLine = prepro.getTextStart().getLine();
        textStartCol = prepro.getTextStart().getCol();
        textStartSource = prepro.getTextStart().getSourceNum();
        getChar();
        return makeToken(ABLNodeType.INCLUDEDIRECTIVE, prepro.getIncludeDirectiveText());
      }

      textStartFile = prepro.getFileIndex();
      textStartLine = prepro.getLine2();
      textStartCol = prepro.getColumn();
      textStartSource = prepro.getSourceNum();
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
          if (prepro.isEscapeCurrent()) {
            getChar();
            // Escaped quote does not start a string
            return id(ABLNodeType.FILENAME);
          } else {
            currStringType = currInt;
            getChar();
            return quotedString();
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
          return digitStart();

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
          if (currInt == EOF_CHAR) {
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
          append('/');
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

    loop : for (;;) {
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
          break loop;
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
    prepro.setDoingComment(true);
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
      } else if (currInt == EOF_CHAR) {
        prepro.lexicalThrow("Missing end of comment");
      }
    }
    prepro.setDoingComment(false);
    getChar();
    return makeToken(ABLNodeType.COMMENT);
  }

  ProToken singleLineComment() {
    LOGGER.trace("Entering singleLineComment()");

    // Single line comments are treated just like regular comments,
    // everything till end of line is considered comment - no escape
    // character to look after
    prepro.setDoingComment(true);
    append(); // currChar=='/'

    while (true) {
      getChar();
      if ((currInt == EOF_CHAR) || (!prepro.isEscapeCurrent() && (currChar == '\r' || currChar == '\n' ))) {
        prepro.setDoingComment(false);
        return makeToken(ABLNodeType.COMMENT);
      } else {
        unEscapedAppend();
      }
    }
  }

  ProToken quotedString() {
    LOGGER.trace("Entering quotedString()");

    // Inside quoted strings (string constants) we preserve
    // the source code's original text - we don't discard
    // escape characters.
    // The preprocessor *does* expand macros inside strings.
    for (;;) {
      if (currInt == EOF_CHAR)
        prepro.lexicalThrow("Unmatched quote");
      unEscapedAppend();
      if (currInt == currStringType && !prepro.isEscapeCurrent()) {
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
        append(theText);
        preserveDrop();
      } else {
        // Fix current end position
        prevCol--;
        ProToken tok = makeToken(ABLNodeType.QSTRING);
        prevCol++;
        return tok;
      }
    } // currChar==':'

    return makeToken(ABLNodeType.QSTRING);
  }

  ProToken digitStart() {
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
          if (prepro.isNameDot()) {
            append();
            getChar();
            break;
          } else
            break for_loop;
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
        // We don't know here if the plus or minus is in the middle or at the end.
        // Don't change ttype.
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
          if (prepro.isNameDot()) {
            append();
            getChar();
            break;
          } else
            break for_loop;
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
      if (prepro.isNameDot())
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
          if (mergeNameDotInId && prepro.isNameDot()) {
            append();
            getChar();
            break;
          } else
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
      macroDefine(ProParserTokenTypes.AMPGLOBALDEFINE);
      getChar();
      return makeToken(ABLNodeType.AMPGLOBALDEFINE);
    }
    if ("&scoped-define".startsWith(macroType) && macroType.length() >= 4) {
      appendToEOL();
      // We have to do the define *before* getting next char.
      macroDefine(ProParserTokenTypes.AMPSCOPEDDEFINE);
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
      while ((!Character.isWhitespace(currChar)) && currInt != EOF_CHAR) {
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
      } else if (currInt != EOF_CHAR) {
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
        // Documentation says &analyze-suspend is always followed by an option, but better to never trust documentation...
        // Generates a clean comma-separated list of all entries
        analyzeSuspend = Joiner.on(',').join(Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(
            currText.toString().substring(currText.toString().indexOf(' ') + 1)));
      }
      getChar();
      prepro.analyzeSuspend(analyzeSuspend);
      prepro.getLstListener().analyzeSuspend(analyzeSuspend, textStartLine);
      return makeToken(ABLNodeType.AMPANALYZESUSPEND);
    }
    if ("&analyze-resume".equals(macroType)) {
      appendToEOL();
      getChar();
      prepro.analyzeResume();
      prepro.getLstListener().analyzeResume(textStartLine);
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

  void append() {
    currText.append((char) currInt);
  }

  void append(char c) {
    currText.append(c);
  }

  void append(String theText) {
    currText.append(theText);
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
      if (currInt == EOF_CHAR)
        break;
      append();
      // Unescaped newline character or escaped newline where previous char is not tilde
      if ((currChar == '\n') && (!prepro.wasEscape() || (prepro.wasEscape() && !currText.toString().endsWith("~\n")))) {
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
    return (currInt == EOF_CHAR || Character.isWhitespace(currChar));
  }

  void getChar() {
    currInt = prepro.getChar();
    currChar = Character.toLowerCase(currInt);
    prevFile = currFile;
    prevLine = currLine;
    prevCol = currCol;
    prevMacro = currMacro;
    currFile = prepro.getFileIndex();
    currLine = prepro.getLine2();
    currCol = prepro.getColumn();
    currMacro = prepro.getMacroExpansion();
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
    prepro.getLstListener().define(textStartLine, textStartCol, macroName.toLowerCase(Locale.ENGLISH), defText,
            defType == ProParserTokenTypes.AMPGLOBALDEFINE ? MacroDef.GLOBAL : MacroDef.SCOPED);
    if (defType == ProParserTokenTypes.AMPGLOBALDEFINE)
      prepro.defGlobal(macroName.toLowerCase(), defText);
    else
      prepro.defScoped(macroName.toLowerCase(), defText);
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
    prepro.getLstListener().undefine(textStartLine, textStartCol, macroName);
    prepro.undef(macroName.toLowerCase());
  }

  ProToken makeToken(ABLNodeType type) {
    return makeToken(type, currText.toString());
  }

  ProToken makeToken(ABLNodeType type, String text) {
    // Counting lines of code and commented lines only in the main file (textStartFile set to 0)
    if ((textStartFile == 0) && (type == ABLNodeType.COMMENT)) {
      int numLines = currText.toString().length() - currText.toString().replace("\n", "").length();
      for (int zz = textStartLine; zz <= textStartLine + numLines; zz++) {
        comments.add(zz);
      }
    } else if ((textStartFile == 0) && (type != ABLNodeType.WS) && (type != ABLNodeType.EOF_ANTLR4) && (textStartLine > 0)) {
      loc.add(textStartLine);
    }
    ProToken tok = new ProToken(type, text);
    tok.setText(text);
    tok.setFileIndex(textStartFile);
    tok.setLine(textStartLine);
    tok.setCharPositionInLine(textStartCol);
    tok.setEndFileIndex(prevFile);
    tok.setEndLine(prevLine);
    tok.setEndCharPositionInLine(prevCol);
    tok.setMacroExpansion(prevMacro);
    tok.setMacroSourceNum(textStartSource);
    tok.setAnalyzeSuspend(prepro.getCurrentAnalyzeSuspend());

    return tok;
  }

  /**
   * Returns number of lines of code in the main file (i.e. including any line where there's a non-comment and non-whitespace token
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
    preserveFile = prepro.getFileIndex();
    preserveLine = prepro.getLine2();
    preserveCol = prepro.getColumn();
    preserveSource = prepro.getSourceNum();
    preserveChar = currChar;
  }

  void preserveDrop() {
    preserve = false;
  }

  void unEscapedAppend() {
    if (prepro.wasEscape()) {
      append(prepro.getEscapeText());
      if (prepro.isEscapeAppend())
        append();
    } else
      append();
  }

  public ProgressLexer getPreprocessor() {
    return prepro;
  }

  /**
   * Only for ANTLR4, don't produce NAMEDOT tokens but merge them in ID tokens
   */
  // TEMP-ANTLR4
  public void setMergeNameDotInId(boolean merge) {
    this.mergeNameDotInId = merge;
  }

}
