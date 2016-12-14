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

import antlr.TokenStreamException;
import antlr.RecognitionException;

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.proparse.IntegerIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.io.IOException;

/**
 * This class deals with &amp;IF conditions by acting as a filter between the lexer and the parser
 */
public class Postlexer implements TokenSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(Postlexer.class);

  private final DoParse doParse;
  private final IntegerIndex<String> filenameList;
  private final Lexer lexer;
  private final Preprocessor prepro;

  private final LinkedList<PreproIfState> preproIfVec = new LinkedList<>();
  private ProToken currToken;
  private final ProTokenFactory factory;

  Postlexer(Preprocessor prepro, Lexer lexer, DoParse doParse) {
    this.prepro = prepro;
    this.lexer = lexer;
    this.doParse = doParse;
    this.filenameList = doParse.getFilenameList();
    this.factory = new ProTokenFactory(this);
  }

  @Override
  public ProToken nextToken() {
    LOGGER.trace("Entering nextToken()");
    try {
      for (;;) {

        getNextToken();

        switch (currToken.getType()) {

          case PreprocessorParser.AMPIF:
            preproIf();
            break; // loop again

          case PreprocessorParser.AMPTHEN:
            // &then are consumed by preproIf()
            throwMessage("Unexpected &THEN");
            break;

          case PreprocessorParser.AMPELSEIF:
            preproElseif();
            break; // loop again

          case PreprocessorParser.AMPELSE:
            preproElse();
            break; // loop again

          case PreprocessorParser.AMPENDIF:
            preproEndif();
            break; // loop again

          default:
            return currToken;

        }
      }
    } catch (IOException | RecognitionException | TokenStreamException caught) {
      throw new RuntimeException(caught);
    }
  }

  private ProToken defined() throws IOException {
    LOGGER.trace("Entering defined()");
    // Progress DEFINED() returns a single digit: 0,1,2, or 3.
    // The text between the parens can be pretty arbitrary, and can
    // have embedded comments, so this calls a specific lexer function for it.
    getNextToken();
    if (currToken.getType() == PreprocessorParser.WS)
      getNextToken();
    if (currToken.getType() != PreprocessorParser.LEFTPAREN)
      throwMessage("Bad DEFINED function in &IF preprocessor condition");
    ProToken argToken = lexer.getAmpIfDefArg();
    getNextToken();
    if (currToken.getType() != PreprocessorParser.RIGHTPAREN)
      throwMessage("Bad DEFINED function in &IF preprocessor condition");
    return new ProToken(/*filenameList,*/ PreprocessorParser.NUMBER, prepro.defined(argToken.getText().trim().toLowerCase()));
  }

  private void getNextToken() throws IOException {
    currToken = lexer.nextToken();
  }

  private void listingLine(ProToken token, String text) throws IOException {
    if (!prepro.listing)
      return;
    StringBuilder bldr = listingToken(token);
    bldr.append(text);
    prepro.listingStream.write(bldr.toString());
    prepro.listingStream.newLine();
  }

  // For consuming tokens that has been preprocessed out (&IF FALSE...)
  private void preproconsume() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproconsume()");

    int thisIfLevel = preproIfVec.size();
    prepro.consuming++;
    while (thisIfLevel <= preproIfVec.size() && preproIfVec.get(thisIfLevel - 1).consuming) {
      getNextToken();
      switch (currToken.getType()) {
        case PreprocessorParser.AMPIF:
          preproIf();
          break;
        case PreprocessorParser.AMPELSEIF:
          preproElseif();
          break;
        case PreprocessorParser.AMPELSE:
          preproElse();
          break;
        case PreprocessorParser.AMPENDIF:
          preproEndif();
          break;
        case PreprocessorParser.EOF:
          throwMessage("Unexpected end of input when consuming discarded &IF/&ELSEIF/&ELSE text");
          break;
        default:
          break;
      }
    }
    prepro.consuming--;
  }

  private void preproIf() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproIf()");

    // Preserve the currToken current position for listing, before evaluating the expression.
    // We can't just write to listing here, because the expression evaluation may
    // find macro references to list.
    StringBuilder bldr = listingToken(currToken);
    bldr.append("ampif ");
    PreproIfState preproIfState = new PreproIfState();
    preproIfVec.add(preproIfState);
    // Only evaluate if we aren't consuming from an outer &IF.
    boolean isTrue = preproIfCond(prepro.consuming == 0);
    if (isTrue) {
      if (prepro.listing) {
        bldr.append("true");
        prepro.listingStream.write(bldr.toString());
        prepro.listingStream.newLine();
      }
      preproIfState.done = true;
    } else {
      if (prepro.listing) {
        bldr.append("false");
        prepro.listingStream.write(bldr.toString());
        prepro.listingStream.newLine();
      }
      preproIfState.consuming = true;
      preproconsume();
    }
  }

  private void preproElse() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproElse()");

    PreproIfState preproIfState = preproIfVec.getLast();
    if (!preproIfState.done) {
      preproIfState.consuming = false;
      listingLine(currToken, "ampelse ?");
    } else {
      if (!preproIfState.consuming) {
        listingLine(currToken, "ampelse true");
        preproIfState.consuming = true;
        preproconsume();
      }
      // else: already consuming. no change.
      listingLine(currToken, "ampelse ?");
    }
  }

  private void preproElseif() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproElseif()");
    // Preserve the current position for listing, before evaluating the expression.
    // We can't just write to listing here, because the expression evaluation may
    // find macro references to list.
    StringBuilder bldr = listingToken(currToken);
    bldr.append("ampelseif ");
    boolean evaluate = true;
    // Don't evaluate if we're consuming from an outer &IF
    if (prepro.consuming - 1 > 0)
      evaluate = false;
    // Don't evaluate if we're already done with this &IF
    if (preproIfVec.getLast().done)
      evaluate = false;
    boolean isTrue = preproIfCond(evaluate);
    if (prepro.listing) {
      if (!evaluate) {
        bldr.append("?");
      } else {
        if (isTrue)
          bldr.append("true");
        else
          bldr.append("false");
      }
      prepro.listingStream.write(bldr.toString());
      prepro.listingStream.newLine();
    }
    PreproIfState preproIfState = preproIfVec.getLast();
    if (isTrue && (!preproIfState.done)) {
      preproIfState.done = true;
      preproIfState.consuming = false;
    } else {
      if (!preproIfState.consuming) {
        preproIfState.consuming = true;
        preproconsume();
      }
      // else: already consuming. no change.
    }
  }

  private void preproEndif() throws IOException {
    LOGGER.trace("Entering preproEndif()");
    listingLine(currToken, "ampendif");
    // XXX Got a case where removeLast() fails with NoSuchElementException 
    preproIfVec.removeLast();
  }

  private boolean preproIfCond(boolean evaluate) throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproIfCond()");

    
    // Notes
    // An &IF here in this &IF condition is not legal. Progress would barf on it.
    // That allows us to simply use a global flag to watch for &THEN.

    List<ProToken> tokenVector = new ArrayList<>();
    boolean done = false;
    while (!done) {
      getNextToken();
      switch (currToken.getType()) {
        case PreprocessorParser.EOF:
          throwMessage("Unexpected end of input after &IF or &ELSEIF");
          break;
        case PreprocessorParser.AMPTHEN:
          done = true;
          break;
        case PreprocessorParser.DEFINED:
          if (evaluate)
            // If not evaluating, just discard
            tokenVector.add(defined());
          break;
        case PreprocessorParser.COMMENT:
        case PreprocessorParser.WS:
        case PreprocessorParser.PREPROCESSTOKEN:
          break;
        default:
          if (evaluate)
            // If not evaluating, just discard
            tokenVector.add(currToken);
      }
    }

    // If it's blank or the the evaluate argument is false, we don't evaluate
    if (tokenVector.isEmpty() || !evaluate)
      return false;
    else {
      DoParse evalDoParse = new DoParse(doParse.getRefactorSession(), null, doParse);
      evalDoParse.preProcessCondition = true;
      for (int i = 0; i < 4; i++) {
        tokenVector.add(new ProToken(/*filenameList,*/ PreprocessorParser.EOF, ""));
      }
      try {
        evalDoParse.doParse(tokenVector);
      } catch (ProEvalException e) {
        e.appendMessage(" Unable to evaluate &IF condition:");
        for (ProToken tok : tokenVector) {
          e.appendMessage(" " + tok.getText());
        }
        int theIndex = currToken.getFileIndex();
        if (doParse.isValidIndex(theIndex))
          e.filename = doParse.getFilename(theIndex);
        e.line = currToken.getLine();
        e.column = currToken.getCharPositionInLine();
        throw e;
      }
      return evalDoParse.preProcessConditionResult;
    }
  }

  private void throwMessage(String theMessage) {
    int theIndex = currToken.getFileIndex();
    if (doParse.isValidIndex(theIndex))
      throw new IllegalArgumentException(doParse.getFilename(theIndex) + ":" + currToken.getLine() + " " + theMessage);
    else
      throw new IllegalArgumentException(theMessage);
  }

  private static StringBuilder listingToken(ProToken token) {
    StringBuilder bldr = new StringBuilder();
    bldr.append(token.getFileIndex()).append(" ").append(token.getLine()).append(" ").append(token.getCharPositionInLine()).append(" ");

    return bldr;
  }

  private static class PreproIfState {
    private boolean consuming = false;
    private boolean done = false;
  }

  @Override
  public int getLine() {
    return currToken.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return currToken.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return currToken.getInputStream();
  }

  @Override
  public String getSourceName() {
    return IntStream.UNKNOWN_SOURCE_NAME;
  }

  @Override
  public void setTokenFactory(TokenFactory<?> factory) {
    throw new UnsupportedOperationException("Unable to override ProTokenFactory");
  }

  @Override
  public TokenFactory<?> getTokenFactory() {
    return factory;
  }

}
