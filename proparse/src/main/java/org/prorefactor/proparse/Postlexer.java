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
package org.prorefactor.proparse;

import antlr.TokenStreamException;
import antlr.RecognitionException;
import antlr.Token;
import antlr.TokenStream;

import java.util.LinkedList;
import java.util.List;

import org.prorefactor.core.ProToken;
import org.prorefactor.proparse.ProParserTokenTypes;

import java.util.ArrayList;
import java.io.IOException;

/**
 * This class deals with &amp;IF conditions by acting as a filter between the lexer and the parser
 */
public class Postlexer implements TokenStream {
  private final DoParse doParse;
  private final IntegerIndex<String> filenameList;
  private final Lexer lexer;
  private final Preprocessor prepro;

  private final LinkedList<PreproIfState> preproIfVec = new LinkedList<>();
  private ProToken currToken;

  Postlexer(Preprocessor prepro, Lexer lexer, DoParse doParse) {
    this.prepro = prepro;
    this.lexer = lexer;
    this.doParse = doParse;
    this.filenameList = doParse.getFilenameList();
  }

  @Override
  public Token nextToken() throws TokenStreamException {
    try {
      for (;;) {

        getNextToken();

        switch (currToken.getType()) {

          case ProParserTokenTypes.AMPIF:
            preproIf();
            break; // loop again

          case ProParserTokenTypes.AMPTHEN:
            // &then are consumed by preproIf()
            throwMessage("Unexpected &THEN");
            break;

          case ProParserTokenTypes.AMPELSEIF:
            preproElseif();
            break; // loop again

          case ProParserTokenTypes.AMPELSE:
            preproElse();
            break; // loop again

          case ProParserTokenTypes.AMPENDIF:
            preproEndif();
            break; // loop again

          default:
            return currToken;

        }
      }
    } catch (IOException | RecognitionException caught) {
      throw new TokenStreamException(caught);
    }
  }

  private ProToken defined() throws IOException {
    // Progress DEFINED() returns a single digit: 0,1,2, or 3.
    // The text between the parens can be pretty arbitrary, and can
    // have embedded comments, so this calls a specific lexer function for it.
    getNextToken();
    if (currToken.getType() == ProParserTokenTypes.WS)
      getNextToken();
    if (currToken.getType() != ProParserTokenTypes.LEFTPAREN)
      throwMessage("Bad DEFINED function in &IF preprocessor condition");
    ProToken argToken = lexer.getAmpIfDefArg();
    getNextToken();
    if (currToken.getType() != ProParserTokenTypes.RIGHTPAREN)
      throwMessage("Bad DEFINED function in &IF preprocessor condition");
    return new ProToken(filenameList, ProParserTokenTypes.NUMBER, prepro.defined(argToken.getText().trim().toLowerCase()));
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
    int thisIfLevel = preproIfVec.size();
    prepro.consuming++;
    while (thisIfLevel <= preproIfVec.size() && preproIfVec.get(thisIfLevel - 1).consuming) {
      getNextToken();
      switch (currToken.getType()) {
        case ProParserTokenTypes.AMPIF:
          preproIf();
          break;
        case ProParserTokenTypes.AMPELSEIF:
          preproElseif();
          break;
        case ProParserTokenTypes.AMPELSE:
          preproElse();
          break;
        case ProParserTokenTypes.AMPENDIF:
          preproEndif();
          break;
        case ProParserTokenTypes.EOF:
          throwMessage("Unexpected end of input when consuming discarded &IF/&ELSEIF/&ELSE text");
          break;
        default:
          break;
      }
    }
    prepro.consuming--;
  }

  private void preproIf() throws IOException, TokenStreamException, RecognitionException {
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
    listingLine(currToken, "ampendif");
    // XXX Got a case where removeLast() fails with NoSuchElementException
    if (!preproIfVec.isEmpty())
      preproIfVec.removeLast();
  }

  private boolean preproIfCond(boolean evaluate) throws IOException, TokenStreamException, RecognitionException {
    // Notes
    // An &IF here in this &IF condition is not legal. Progress would barf on it.
    // That allows us to simply use a global flag to watch for &THEN.

    List<ProToken> tokenVector = new ArrayList<>();
    boolean done = false;
    while (!done) {
      getNextToken();
      switch (currToken.getType()) {
        case ProParserTokenTypes.EOF:
          throwMessage("Unexpected end of input after &IF or &ELSEIF");
          break;
        case ProParserTokenTypes.AMPTHEN:
          done = true;
          break;
        case ProParserTokenTypes.DEFINED:
          if (evaluate)
            // If not evaluating, just discard
            tokenVector.add(defined());
          break;
        case ProParserTokenTypes.COMMENT:
        case ProParserTokenTypes.WS:
        case ProParserTokenTypes.PREPROCESSTOKEN:
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
        tokenVector.add(new ProToken(filenameList, ProParserTokenTypes.EOF, ""));
      }
      try {
        evalDoParse.doParse(tokenVector);
      } catch (ProEvalException e) {
        String str = "Unable to evaluate &IF condition:";
        for (ProToken tok : tokenVector) {
          str += " " + tok.getText();
        }
        String fileName = null;
        if (doParse.isValidIndex(currToken.getFileIndex()))
          fileName = doParse.getFilename(currToken.getFileIndex());
        throw new ProEvalException(str, e, fileName, currToken.getLine(), currToken.getColumn());
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
    bldr.append(token.getFileIndex()).append(" ").append(token.getLine()).append(" ").append(token.getColumn()).append(" ");

    return bldr;
  }

  private static class PreproIfState {
    private boolean consuming = false;
    private boolean done = false;
  }

}
