/********************************************************************************
 * Copyright (c) 2015-2024 Riverside Software
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.ListTokenSource;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.macrolevel.PreprocessorExpressionVisitor;
import org.prorefactor.proparse.antlr4.PreprocessorParser;
import org.prorefactor.proparse.antlr4.PreprocessorParser.PreproIfEvalContext;
import org.prorefactor.proparse.antlr4.Proparse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class deals with &amp;IF conditions by acting as a filter between the lexer and the parser
 */
public class PostLexer implements TokenSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(PostLexer.class);

  private final Lexer lexer;
  private final ABLLexer prepro;
  private final PreproEval eval;

  private final LinkedList<PreproIfState> preproIfVec = new LinkedList<>();
  private ProToken currToken;

  private final Queue<Token> heap = new LinkedList<>();

  public PostLexer(ABLLexer parent, Lexer lexer) {
    this.lexer = lexer;
    this.prepro = parent;
    this.eval = new PreproEval(prepro.getProparseSettings());
  }

  @Override
  public Token nextToken() {
    LOGGER.trace("Entering nextToken()");

    if (!heap.isEmpty()) {
      return heap.poll();
    }

    while (true) {
      getNextToken();

      switch (currToken.getType()) {
        case PreprocessorParser.AMPIF:
          preproIf();
          return heap.poll();

        case PreprocessorParser.AMPTHEN:
          // &then are consumed by preproIf()
          throwMessage("Unexpected &THEN");
          break;

        case PreprocessorParser.AMPELSEIF:
          heap.offer(currToken);
          preproElseif();
          return heap.poll();

        case PreprocessorParser.AMPELSE:
          heap.offer(currToken);
          preproElse();
          return heap.poll();

        case PreprocessorParser.AMPENDIF:
          preproEndif();
          return currToken;

        default:
          return currToken;

      }
    }
  }

  private ProToken defined() {
    LOGGER.trace("Entering defined()");
    // Progress DEFINED() returns a single digit: 0, 1, 2, or 3.
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
    return new ProToken.Builder(ABLNodeType.NUMBER, prepro.defined(argToken.getText().trim().toLowerCase())).build();
  }

  private void getNextToken() {
    currToken = lexer.nextToken();
  }

  // For consuming tokens that has been preprocessed out (&IF FALSE...)
  private void preproconsume() {
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
          heap.offer(currToken);
          preproEndif();
          break;
        case Token.EOF:
          throwMessage("Unexpected end of input when consuming discarded &IF/&ELSEIF/&ELSE text");
          break;
        default:
          break;
      }
    }
    prepro.decrementConsuming();
  }

  private void preproIf() {
    LOGGER.trace("Entering preproIf()");

    // Preserve the currToken current position for listing, before evaluating the expression.
    // We can't just write to listing here, because the expression evaluation may
    // find macro references to list.
    int currLine = currToken.getLine();
    int currCol = currToken.getCharPositionInLine();
    PreproIfState preproIfState = new PreproIfState();
    preproIfVec.add(preproIfState);
    // Only evaluate if we aren't consuming from an outer &IF.
    if (preproIfCond(!prepro.isConsuming())) {
      prepro.getLstListener().preproIf(currLine, currCol, true);
      preproIfState.done = true;
    } else {
      prepro.getLstListener().preproIf(currLine, currCol, false);
      preproIfState.consuming = true;
      preproconsume();
    }
  }

  private void preproElse() {
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

  private void preproElseif() {
    LOGGER.trace("Entering preproElseif()");
    // Preserve the current position for listing, before evaluating the expression.
    // We can't just write to listing here, because the expression evaluation may
    // find macro references to list.
    int currLine = currToken.getLine();
    int currCol = currToken.getCharPositionInLine();
    boolean evaluate = true;
    // Don't evaluate if we're consuming from an outer &IF
    if (prepro.isConsumingFromOuterIf())
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

  private void preproEndif() {
    LOGGER.trace("Entering preproEndif()");
    prepro.getLstListener().preproEndIf(currToken.getLine(), currToken.getCharPositionInLine());
    if (!preproIfVec.isEmpty())
      preproIfVec.removeLast();
  }

  // Returns result of &IF expression evaluation
  private boolean preproIfCond(boolean evaluate) {
    LOGGER.trace("Entering preproIfCond()");

    if (evaluate)
      heap.offer(currToken);

    // An &IF here in this &IF condition is not legal. Progress would barf on it.
    // That allows us to simply use a global flag to watch for &THEN.
    List<ProToken> tokens = new ArrayList<>();
    boolean done = false;
    while (!done) {
      getNextToken();
      switch (currToken.getType()) {
        case Token.EOF:
          throwMessage("Unexpected end of input after &IF or &ELSEIF");
          break;
        case PreprocessorParser.AMPTHEN:
          done = true;
          break;
        case PreprocessorParser.DEFINED:
          if (evaluate)
            // If not evaluating, just discard
            tokens.add(defined());
          break;
        case PreprocessorParser.COMMENT:
        case PreprocessorParser.WS:
          break;
        default:
          if (evaluate)
            // If not evaluating, just discard
            tokens.add(currToken);
      }
    }

    // If it's blank or the the evaluate argument is false, we don't evaluate
    if (tokens.isEmpty() || !evaluate) {
      return false;
    } else {
      CommonTokenStream cts = new CommonTokenStream(new ListTokenSource(tokens));
      PreprocessorParser parser = new PreprocessorParser(cts);
      parser.setErrorHandler(new BailErrorStrategy());
      parser.removeErrorListeners();
      parser.addErrorListener(new PreprocessorErrorListener(prepro, tokens));
      try {
        PreproIfEvalContext tree = parser.preproIfEval();
        boolean expressionResult = eval.visitPreproIfEval(tree);
        PreprocessorExpressionVisitor vv = new PreprocessorExpressionVisitor();
        heap.offer(getTokenFactory().create(expressionResult ? Proparse.PREPROEXPRTRUE : Proparse.PREPROEXPRFALSE, vv.visitPreproIfEval(tree)));
        heap.offer(currToken); // &THEN token
        return eval.visitPreproIfEval(tree);
      } catch (ParseCancellationException caught) {
        return false;
      }
    }
  }

  private void throwMessage(String msg) {
    throw new ProparseRuntimeException("File '" + prepro.getFilename(0) + "' - Current position '"
        + prepro.getFilename(currToken.getFileIndex()) + "':" + currToken.getLine() + " - " + msg);
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
    return lexer.getTokenFactory();
  }

}
