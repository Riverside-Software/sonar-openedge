/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.JPNodeMetrics;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.ListingListener;
import org.prorefactor.macrolevel.ListingParser;
import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.IProparseSettings;
import org.prorefactor.refactor.settings.ProparseSettings.OperatingSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.TokenStream;
import antlr.TokenStreamException;
import antlr.TokenStreamHiddenTokenFilter;

/**
 * A preprocessor contains one or more IncludeFiles.
 * 
 * There is a special IncludeFile object created for the top-level program (ex: .p or .w).
 * 
 * Every time the lexer has to scan an include file, we create an IncludeFile object, for managing include file
 * arguments and pre-processor scopes.
 * 
 * We keep an InputSource object, which has an input stream.
 * 
 * Each IncludeFile object will have one or more InputSource objects.
 * 
 * The bottom InputSource object for an IncludeFile is the input for the include file itself.
 * 
 * Each upper (potentially stacked) InputSource object is for an argument reference: - include file argument reference
 * or reference to scoped or global preprocessor definition
 * 
 * We keep a reference to the input stream "A" in the InputSource object so that if "A" spawns a new input stream "B",
 * we can return to "A" when we are done with "B".
 */
public class ProgressLexer implements TokenSource, IPreprocessor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ProgressLexer.class);

  private static final Pattern regexNumberedArg = Pattern.compile("\\{\\d+\\}");
  private static final Pattern regexEmptyCurlies = Pattern.compile("\\{\\s*\\}");
  private static final int EOF_CHAR = -1;
  private static final int SKIP_CHAR = -100;
  public static final int PROPARSE_DIRECTIVE = -101;

  private final IProparseSettings ppSettings;

  // How many levels of &IF FALSE are we currently into?
  private int consuming = 0;

  private int currChar;
  private int currFile;
  private int currSourceNum;
  private int currLine;
  private int currCol;

  // Are we in the middle of a comment?
  private boolean doingComment;
  // Would you append the currently returned character to escapeText in order to see what the original code looked like
  // before escape processing? (See escape().)
  private boolean escapeAppend;
  // Is the currently returned character escaped?
  private boolean escapeCurrent;
  // Was there escaped text before current character?
  private boolean wasEscape;
  private String escapeText;
  // Is the current '.' a name dot? (i.e. not followed by whitespace) */
  private boolean nameDot;
  private String proparseDirectiveText;
  private FilePos textStart;

  private ListingListener lstListener;

  private IncludeFile currentInclude;
  private InputSource currentInput;
  private Map<String, String> globalDefdNames = new HashMap<>();
  private boolean gotLookahead = false;
  private LinkedList<IncludeFile> includeVector = new LinkedList<>();
  private int laFile;
  private int laLine;
  private int laCol;
  private int laSourceNum;
  private int laChar;
  private int safetyNet = 0;
  private int sequence = 0;
  private int sourceCounter = -1;

  // From ProgressLexer
  private Lexer lexer;
  private IntegerIndex<String> filenameList = new IntegerIndex<>();
  private final RefactorSession session;
  private TokenSource wrapper;

  /**
   * An existing reference to the input stream is required for construction. The caller is responsible for closing that
   * stream once parsing is complete.
   */
  public ProgressLexer(RefactorSession session, String fileName) throws IOException {
    LOGGER.trace("New ProgressLexer instance {}", fileName);
    this.ppSettings = session.getProparseSettings();
    this.session = session;

    // Create input source with flag isPrimaryInput=true
    currFile = addFilename(fileName);
    currentInput = new InputSource(++sourceCounter, new File(fileName), session.getCharset(), currFile, true);
    currentInclude = new IncludeFile(fileName, currentInput);
    includeVector.add(currentInclude);
    currSourceNum = currentInput.getSourceNum();
    lstListener = new ListingParser();
    
    lexer = new Lexer(this);
    PostLexer postlexer = new PostLexer(lexer);
    TokenSource filter1 = new TokenList(postlexer);
    wrapper = new MultiChannelTokenSource(filter1);
  }

  public String getMainFileName() {
    return filenameList.getValue(0);
  }

  public IntegerIndex<String> getFilenameList() {
    return filenameList;
  }

  public String getFilename(int fileIndex) {
    return filenameList.getValue(fileIndex);
  }

  protected int addFilename(String filename) {
    return filenameList.add(filename);
  }

  // **********************
  // TokenSource interface
  // **********************

  @Override
  public Token nextToken() {
    return wrapper.nextToken();
  }

  @Override
  public int getLine() {
    return wrapper.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return wrapper.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return wrapper.getInputStream();
  }

  @Override
  public String getSourceName() {
    return wrapper.getSourceName();
  }

  @Override
  public void setTokenFactory(TokenFactory<?> factory) {
    wrapper.setTokenFactory(factory);
  }

  @Override
  public TokenFactory<?> getTokenFactory() {
    return wrapper.getTokenFactory();
  }

  // ****************************
  // End of TokenSource interface
  // ****************************

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
      return Boolean.toString(ppSettings.getBatchMode());
    if ("opsys".equals(argName))
      return ppSettings.getOpSys().getName();
    if ("window-system".equals(argName))
      return ppSettings.getWindowSystem();
    if ("file-name".equals(argName)) {
      // {&FILE-NAME}, unlike {0}, returns the filename as found on the PROPATH.
      ret = session.findFile(currentInclude.getNumberedArgument(0));
      // Progress seems to be converting the slashes for the appropriate OS.
      // I don't convert the slashes when I store the filename - instead I do it here.
      // (Saves us from converting the slashes for each and every include reference.)
      if (ppSettings.getOpSys() == OperatingSystem.UNIX)
        ret = ret.replace('\\', '/');
      else
        ret = ret.replace('/', '\\');
      return ret;
    }
    if ("line-number".equals(argName))
      return Integer.toString(getLine2());
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

  private boolean undefHelper(String argName, Map<String, String> names) {
    if (names.containsKey(argName)) {
      names.remove(argName);
      return true;
    }
    return false;
  }

  int getChar() throws IOException {
    wasEscape = false;
    for (;;) {
      escapeCurrent = false;
      if (gotLookahead)
        laUse();
      else
        getRawChar();
      switch (currChar) {
        case '\\':
        case '~': {
          // Escapes are *always* processed, even inside strings and comments.
          if ((currChar == '\\') && (ppSettings.getOpSys() == OperatingSystem.WINDOWS) && !ppSettings.useBackslashAsEscape()) {
            return currChar;
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
            return currChar;
          else {
            macroReference();
            if (currChar == PROPARSE_DIRECTIVE)
              return currChar;
            // else do another loop
          }
          break;
        case '.':
          checkForNameDot();
          return currChar;
        default:
          return currChar;
      }
    }
  }

  // ****************************
  // IPreprocessor implementation
  // ****************************

  int getColumn() {
    return currCol;
  }

  int getFileIndex() {
    return currFile;
  }

  int getLine2() {
    return currLine;
  }

  int getSourceNum() {
    return currSourceNum;
  }

  /**
   * We keep a record of discarded escape characters. This is in case the client wants to fetch those and use them. (Ex:
   * Our lexer preserves original text inside string constants, including escape sequences).
   */
  private int escape() throws IOException {
    // We may have multiple contiguous discarded characters
    // or a new escape sequence.
    if (wasEscape)
      escapeText += (char) currChar;
    else {
      wasEscape = true;
      escapeText = Character.toString((char) currChar);
      escapeAppend = true;
    }
    // Discard current character ('~' or '\\'), get next.
    getRawChar();
    int retChar = currChar;
    escapeCurrent = true;
    switch (currChar) {
      case '\n':
        // An escaped newline can be pretty much anywhere: mid-keyword, mid-identifier, between '*' and '/', etc.
        // It is discarded.
        escapeText += (char) currChar;
        retChar = SKIP_CHAR;
        break;
      case '\r':
        // Lookahead to the next character.
        // If it's anything other than '\n', we need to do normal processing on it. Progress does not strip '\r' the way
        // it strips '\n'. There is one issue here - Progress treats "\r\r\n" the same as "\r\n". I'm not sure how I
        // could implement it.
        if (!gotLookahead)
          laGet();
        if (laChar == '\n') {
          escapeText += "\r\n";
          laUse();
          retChar = SKIP_CHAR;
        } else {
          retChar = '\r';
        }
        break;
      case 'r':
        // An escaped 'r' or an escaped 'n' gets *converted* to a different character. We don't just drop chars.
        escapeText += (char) currChar;
        escapeAppend = false;
        retChar = '\r';
        break;
      case 'n':
        // An escaped 'r' or an escaped 'n' gets *converted* to a different character. We don't just drop chars.
        escapeText += (char) currChar;
        escapeAppend = false;
        retChar = '\n';
        break;
      default:
        escapeAppend = true;
        break; // No change to retChar.
    }
    return retChar;
  }

  private String getFilename() {
    return getFilename(currentInput.getFileIndex());
  }

  /**
   * Deal with end of input stream, and switch to previous. Because Progress allows you to switch streams in the middle
   * of a token, we have to completely override EOF handling, right at the point where we get() a new character from the
   * input stream. If we are at an EOF other than the topmost program file, then we don't want the EOF to get into our
   * character stream at all. If we've popped an include file off the stack (not just argument or preprocessor text),
   * then we have to insert a space into the character stream, because that's what Progress's compiler does.
   */
  private void getRawChar() throws IOException {
    currLine = currentInput.getNextLine();
    currCol = currentInput.getNextCol();
    currChar = currentInput.get();
    if (currChar == 65533) {
      // This is the 'replacement' character in Unicode, used by Java as a
      // placeholder for a character which could not be converted.
      // We replace those characters at runtime with a space, and log an error
      LOGGER.error("Character conversion error in {} at line {} column {} from encoding {}", getFilename(), currLine, currCol, session.getCharset().name());
      currChar = ' ';
    }
    while (currChar == EOF_CHAR) {
      switch (popInput()) {
        case 0: // nothing left to pop
          safetyNet++;
          if (safetyNet > 100)
            throw new ProparseRuntimeException("Proparse error. Infinite loop caught by preprocessor.");
          return;
        case 1: // popped an include file
          currFile = currentInput.getFileIndex();
          currLine = currentInput.getNextLine();
          currCol = currentInput.getNextCol();
          currSourceNum = currentInput.getSourceNum();
          currChar = ' ';
          return;
        case 2: // popped a macro ref or include arg ref
          currFile = currentInput.getFileIndex();
          currLine = currentInput.getNextLine();
          currCol = currentInput.getNextCol();
          currChar = currentInput.get(); // might be another EOF
          currSourceNum = currentInput.getSourceNum();
          break;
        default:
          throw new IOException("Proparse error. popInput() returned unexpected value.");
      }
    }
  }

  /*
   * Get the next include reference arg, reposition the charpos. A doublequote will start a string - all this means is
   * that we'll collect whitespace. A singlequote does not have this effect.
   */
  private String includeRefArg(CharPos cp) {
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

  /**
   * Get the lookahead character. The caller is responsible for knowing that the lookahead isn't already there, ex: if
   * (!gotLookahead) laGet();
   */
  private void laGet() throws IOException {
    int saveFile = currFile;
    int saveLine = currLine;
    int saveCol = currCol;
    int saveSourceNum = currSourceNum;
    int saveChar = currChar;
    getRawChar();
    gotLookahead = true;
    laFile = currFile;
    laLine = currLine;
    laCol = currCol;
    laChar = currChar;
    laSourceNum = currSourceNum;
    currFile = saveFile;
    currLine = saveLine;
    currCol = saveCol;
    currSourceNum = saveSourceNum;
    currChar = saveChar;
  }

  private void laUse() {
    gotLookahead = false;
    currFile = laFile;
    currLine = laLine;
    currCol = laCol;
    currSourceNum = laSourceNum;
    currChar = laChar;
  }

  void lexicalThrow(String theMessage) {
    throw new ProparseRuntimeException(getFilename() + ":" + Integer.toString(getLine2()) + " " + theMessage);
  }

  private void macroReference() throws IOException {
    ArrayList<IncludeArg> incArgs = new ArrayList<>();

    this.textStart = new FilePos(currFile, currLine, currCol, currSourceNum);
    // Preserve the macro reference start point, because textStart get messed with if this macro reference itself contains any macro references.
    FilePos refPos = new FilePos(textStart.file, textStart.line, textStart.col, textStart.sourceNum);

    // Gather the macro reference text
    // Do not stop on escaped '}'
    StringBuilder refTextBldr = new StringBuilder("{");
    char macroChar = (char) getChar();
    while ((macroChar != '}' || wasEscape) && macroChar != EOF_CHAR) {
      refTextBldr.append(macroChar);
      macroChar = (char) getChar();
    }
    if (macroChar == EOF_CHAR)
      lexicalThrow("Unmatched curly brace");
    refTextBldr.append(macroChar); // should be '}'
    String refText = refTextBldr.toString();
    CharPos cp = new CharPos(refText.toCharArray(), 0);
    int refTextEnd = refText.length();
    int closingCurly = refTextEnd - 1;

    // Proparse Directive
    if (refText.toLowerCase().startsWith("{&_proparse_")
        && ppSettings.getProparseDirectives()) {
      currChar = PROPARSE_DIRECTIVE;
      // We strip "{&_proparse_", trailing '}', and leading/trailing whitespace
      proparseDirectiveText = refText.substring(12, closingCurly).trim();
      // This will be counted as a source whether picked up here or picked
      // up as a normal macro ref.
      ++sourceCounter;
      lstListener.macroRef(refPos.line, refPos.col, "_proparse");
      return;
    }

    // {*} -- all arguments
    else if ("{*}".equals(refText)) {
      newMacroRef("*", refPos);
      return;
    }

    // {&* -- all named arguments
    else if (refText.startsWith("{&*")) {
      newMacroRef("&*", refPos);
      return;
    }

    // {(0..9)+} -- a numbered argument
    else if (regexNumberedArg.matcher(refText).matches()) {
      String theText = refText.substring(1, closingCurly);
      int argNum = Integer.parseInt(theText);
      newMacroRef(argNum, refPos);
      return;
    }

    // { } -- empty curlies - ignored
    else if (regexEmptyCurlies.matcher(refText).matches()) {
      return;
    }

    // {& -- named argument or macro expansion
    // Note that you can reference "{&}" to get an
    // undefined named include argument.
    // In that case, argName remains blank.
    // Trailing whitespace is trimmed.
    else if (refText.startsWith("{&")) {
      String argName = refText.substring(2, closingCurly).trim().toLowerCase();
      newMacroRef(argName, refPos);
      return;
    }

    else { // If we got here, it's an include file reference
      boolean usingNamed = false;
      String argName;
      String argVal;

      // '{'
      cp.pos = 1; // skip '{'

      // whitespace?
      while (Character.isWhitespace(cp.chars[cp.pos]))
        ++cp.pos;

      // filename
      String includeFilename = includeRefArg(cp);

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
            if (cp.pos == closingCurly || cp.chars[cp.pos] == '=')
              break;
            if (!(Character.isWhitespace(cp.chars[cp.pos])))
              argName += cp.chars[cp.pos];
            ++cp.pos;
          }

          argVal = "";
          if (cp.chars[cp.pos] == '=') {
            // '=' with optional WS
            ++cp.pos;
            while (cp.pos != closingCurly && Character.isWhitespace(cp.chars[cp.pos]))
              ++cp.pos;
            // Arg val
            if (cp.pos != closingCurly)
              argVal = includeRefArg(cp);
          }

          // Add the argument name/val pair
          incArgs.add(new IncludeArg(argName, argVal));

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
          incArgs.add(new IncludeArg("", includeRefArg(cp)));
        }
      } // numbered args

      // newInclude() returns false if filename is blank or currently
      // "consuming" due to &IF FALSE.
      // newInclude() will throw() if file not found or cannot be opened.
      if (newInclude(includeFilename)) {
        // Unlike currline and currcol,
        // currfile is only updated with a push/pop of the input stack.
        currFile = currentInput.getFileIndex();
        currSourceNum = currentInput.getSourceNum();
        lstListener.include(refPos.line, refPos.col, currFile, includeFilename);
        // Add the arguments to the new include object.
        int argNum = 1;
        for (IncludeArg incarg : incArgs) {
          if (usingNamed)
            currentInclude.addNamedArgument(incarg.argName, incarg.argVal);
          else
            currentInclude.addArgument(incarg.argVal);
          lstListener.includeArgument(usingNamed ? incarg.argName : Integer.toString(argNum), incarg.argVal);
          argNum++;
        }
      }

    } // include file reference

  } // macroReference()

  private boolean newInclude(String referencedWithName) throws IOException {
    // Progress doesn't enter include files if &IF FALSE
    // It *is* possible to get here with a blank include file
    // name. See bug#034. Don't enter if the includefilename is blank.
    String fName = referencedWithName.trim();
    if (consuming != 0 || fName.length() == 0)
      return false;
    File ff = session.findFile3(fName);
    if (ff == null) {
      throw new IOException(getFilename() + ": " + "Could not find include file: " + referencedWithName);
    }
    currentInput = new InputSource(++sourceCounter, ff, session.getCharset(), addFilename(fName));
    currentInclude = new IncludeFile(referencedWithName, currentInput);
    includeVector.add(currentInclude);
    LOGGER.trace("Entering file: {}", getFilename());

    return true;
  }

  /**
   * New macro or named/numbered argument reference. Input either macro/argument name or the argument number, as well as
   * fileIndex, line, and column where the '{' appeared. Returns false if there's nothing to expand.
   */
  private void newMacroRef(String macroName, FilePos refPos) throws IOException {
    // Using this trick: {{&undefined-argument}{&*}}
    // it is possible to get line breaks into what we
    // get here as the macroName. See test data bug15.p and bug15.i.
    lstListener.macroRef(refPos.line, refPos.col, macroName);
    newMacroRef2(getArgText(macroName), refPos);
  }

  private void newMacroRef(int argNum, FilePos refPos) throws IOException {
    lstListener.macroRef(refPos.line, refPos.col, Integer.toString(argNum));
    newMacroRef2(getArgText(argNum), refPos);
  }

  private void newMacroRef2(String theText, FilePos refPos) throws IOException {
    if (theText.length() == 0) {
      ++sourceCounter;
      lstListener.macroRefEnd();
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
   * Cleanup work, once the parse is complete. Gets run by doParse even if there's an exception thrown.
   */
  public void parseComplete() throws IOException {
    // Things to do once the parse is complete
    // Release input streams, so that files can be written to by other processes.
    // Otherwise, these hang around until the next parse or until the Progress
    // session closes, and nothing else can write to the include files.

    // List all file indexes.
    // TODO Probably a much cleaner way to write that
    int i = 0;
    while (filenameList.hasIndex(i)) {
      getLstListener().fileIndex(i, getFilename(i));
      ++i;
    }

    while (popInput() != 0) {
      // No-op
    }
  }

  /**
   * Pop the current input source off the stack. Returns true if we've popped off the end of an include file, false if
   * we've just popped off an argument or preprocessor text. The calling program has to know this, to add the space ' '
   * at the end of the include reference.
   */
  private int popInput() throws IOException {
    // Returns 2 if we popped a macro or arg ref off the input stack.
    // Returns 1 if we popped an include file off the input stack.
    // Returns 0 if there's nothing left to pop.
    // There's no need to pop the primary input source, so we leave it
    // around. There's a good chance that something will want to refer
    // to currentInclude or currentInput anyway, even though it's done.
    InputSource tmp;
    if ((tmp = currentInclude.pop()) != null) {
      currentInput = tmp;
      lstListener.macroRefEnd();
      return 2;
    } else if (includeVector.size() > 1) {
      includeVector.removeLast();
      currentInclude = includeVector.getLast();
      currentInput = currentInclude.getLastSource();
      lstListener.includeEnd();
      LOGGER.trace("Back to file: {}", getFilename());
      return 1;
    } else {
      return 0;
    }
  }

  private void checkForNameDot() throws IOException {
    // Have to check for nameDot in the preprocessor because nameDot is true
    // even if the next character is a '{' which eventually expands
    // out to be a space character.
    if (!gotLookahead)
      laGet();
    nameDot = (laChar != EOF_CHAR && (!Character.isWhitespace(laChar)));
  }

  public void setDoingComment(boolean doingComment) {
    this.doingComment = doingComment;
  }

  public boolean isEscapeAppend() {
    return escapeAppend;
  }

  public boolean isEscapeCurrent() {
    return escapeCurrent;
  }

  public String getEscapeText() {
    return escapeText;
  }

  public boolean wasEscape() {
    return wasEscape;
  }

  public boolean isNameDot() {
    return nameDot;
  }

  public String getProparseDirectiveText() {
    return proparseDirectiveText;
  }

  public boolean isConsuming() {
    return consuming != 0;
  }

  public int getConsuming() {
    return consuming;
  }

  public void incrementConsuming() {
    consuming++;
  }

  public void decrementConsuming() {
    consuming--;
  }

  public FilePos getTextStart() {
    return textStart;
  }

  public ListingListener getLstListener() {
    return lstListener;
  }

  public JPNodeMetrics getMetrics() {
    return new JPNodeMetrics(lexer.getLoc(), lexer.getCommentedLines());
  }

  public IncludeRef getMacroGraph() {
    return ((ListingParser) lstListener).getMacroGraph();
  }

  public IProparseSettings getProparseSettings(){
    return ppSettings;
  }

  private static class CharPos {
    private final char[] chars;
    private int pos;

    CharPos(char[] c, int p) {
      chars = c;
      pos = p;
    }
  }
  
  public static class FilePos {
    private final int file;
    private final int line;
    private final int col;
    private final int sourceNum;
    
    public FilePos(int file, int line, int col, int sourceNum) {
      this.file = file;
      this.line = line;
      this.col = col;
      this.sourceNum = sourceNum;
    }

    public int getFile() {
      return file;
    }

    public int getLine() {
      return line;
    }

    public int getCol() {
      return col;
    }

    public int getSourceNum() {
      return sourceNum;
    }
  }

  private static class IncludeArg {
    private final String argName;
    private final String argVal;
    
    IncludeArg(String argName, String argVal) {
      this.argName = argName;
      this.argVal = argVal;
    }
  }

  public TokenStream getANTLR2TokenStream(boolean hideNonDefaultChannel) {
    TokenStream stream = new ANTRL2TokenStreamWrapper();
    if (hideNonDefaultChannel) {
      TokenStreamHiddenTokenFilter filter = new TokenStreamHiddenTokenFilter(stream); 
      filter.hide(NodeTypes.WS);
      filter.hide(NodeTypes.COMMENT);
      filter.hide(NodeTypes.AMPMESSAGE);
      filter.hide(NodeTypes.AMPANALYZESUSPEND);
      filter.hide(NodeTypes.AMPANALYZERESUME);
      filter.hide(NodeTypes.AMPGLOBALDEFINE);
      filter.hide(NodeTypes.AMPSCOPEDDEFINE);
      filter.hide(NodeTypes.AMPUNDEFINE);
      stream = filter;
    }

    return stream;
  }

  private class ANTRL2TokenStreamWrapper implements TokenStream {

    @Override
    public antlr.Token nextToken() throws TokenStreamException {
      return convertToken((org.prorefactor.proparse.antlr4.ProToken) wrapper.nextToken());
    }

    private antlr.Token convertToken(org.prorefactor.proparse.antlr4.ProToken tok) {
      // Value of EOF is different in ANTLR2 and ANTLR4
      return new ProToken(filenameList, tok.getType() == Token.EOF ? antlr.Token.EOF_TYPE : tok.getType(),
          tok.getText(), tok.getFileIndex(), tok.getLine(), tok.getCharPositionInLine(), tok.getEndFileIndex(),
          tok.getEndLine(), tok.getEndCharPositionInLine(), tok.getMacroSourceNum());
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
