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
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.ListTokenSource;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.io.IOException;

/**
 * This class deals with &amp;IF conditions by acting as a filter between the lexer and the parser
 */
public class PostLexer implements TokenSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(PostLexer.class);

  private final Lexer lexer;
  private final ProgressLexer prepro;
  private final PreproEval eval;

  private final LinkedList<PreproIfState> preproIfVec = new LinkedList<>();
  private ProToken currToken;

  public PostLexer(Lexer lexer) {
    this.lexer = lexer;
    this.prepro = lexer.getPreprocessor();
    this.eval = new PreproEval(prepro.getProparseSettings());
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
    return new ProToken(PreprocessorParser.NUMBER, prepro.defined(argToken.getText().trim().toLowerCase()));
  }

  private void getNextToken() throws IOException {
    currToken = lexer.nextToken();
  }

  // For consuming tokens that has been preprocessed out (&IF FALSE...)
  private void preproconsume() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproconsume()");

    int thisIfLevel = preproIfVec.size();
    prepro.incrementConsuming();
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
    prepro.decrementConsuming();
  }

  private void preproIf() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproIf()");

    // Preserve the currToken current position for listing, before evaluating the expression.
    // We can't just write to listing here, because the expression evaluation may
    // find macro references to list.
    int currLine = currToken.getLine();
    int currCol = currToken.getCharPositionInLine();
    PreproIfState preproIfState = new PreproIfState();
    preproIfVec.add(preproIfState);
    // Only evaluate if we aren't consuming from an outer &IF.
    boolean isTrue = preproIfCond(!prepro.isConsuming());
    if (isTrue) {
      prepro.getLstListener().preproIf(currLine, currCol, true);
      preproIfState.done = true;
    } else {
      prepro.getLstListener().preproIf(currLine, currCol, false);
      preproIfState.consuming = true;
      preproconsume();
    }
  }

  private void preproElse() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproElse()");

    PreproIfState preproIfState = preproIfVec.getLast();
    if (!preproIfState.done) {
      preproIfState.consuming = false;
      prepro.getLstListener().preproElse(currToken.getLine(), currToken.getCharPositionInLine());
    } else {
      if (!preproIfState.consuming) {
        prepro.getLstListener().preproElse(currToken.getLine(), currToken.getCharPositionInLine());
        preproIfState.consuming = true;
        preproconsume();
      }
      // else: already consuming. no change.
      prepro.getLstListener().preproElse(currToken.getLine(), currToken.getCharPositionInLine());
    }
  }

  private void preproElseif() throws IOException, TokenStreamException, RecognitionException {
    LOGGER.trace("Entering preproElseif()");
    // Preserve the current position for listing, before evaluating the expression.
    // We can't just write to listing here, because the expression evaluation may
    // find macro references to list.
    int currLine = currToken.getLine();
    int currCol = currToken.getCharPositionInLine();
    boolean evaluate = true;
    // Don't evaluate if we're consuming from an outer &IF
    if (prepro.getConsuming() - 1 > 0)
      evaluate = false;
    // Don't evaluate if we're already done with this &IF
    if (preproIfVec.getLast().done)
      evaluate = false;
    boolean isTrue = preproIfCond(evaluate);
    prepro.getLstListener().preproElseIf(currLine, currCol);
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
    prepro.getLstListener().preproEndIf(currToken.getLine(), currToken.getCharPositionInLine());
    // XXX Got a case where removeLast() fails with NoSuchElementException
    if (!preproIfVec.isEmpty())
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
      CommonTokenStream cts = new CommonTokenStream(new ListTokenSource(tokenVector));
      PreprocessorParser parser = new PreprocessorParser(cts);
      return eval.visitPreproIfEval(parser.preproIfEval());
    }
  }

  private void throwMessage(String theMessage) {
    // TODO Add file name in message
    throw new IllegalArgumentException("Line " + currToken.getLine() + " - " + theMessage);
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
    return null;
  }

}
