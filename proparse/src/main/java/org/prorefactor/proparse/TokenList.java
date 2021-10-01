/********************************************************************************
 * Copyright (c) 2015-2021 Riverside Software
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

import java.util.Deque;
import java.util.LinkedList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;

/**
 * More or less similar to NameDotTokenFilter: this filter tries to concatenate the tokens before an
 * <code>OBJCOLON</code> token (i.e. a colon immediately followed by something). ABL allows comments and whitespaces to
 * be nested in references to class names so this filters out the comments so the parser has an easier job. And this
 * time I can also complain against Java as it allows the same stupid syntax.
 */
public class TokenList implements TokenSource {
  private final TokenSource source;
  private final Deque<ProToken> queue = new LinkedList<>();

  private int currentPosition;
  private ProToken currentToken;

  public TokenList(TokenSource input) {
    this.source = input;
  }

  private void fillHeap() {
    boolean loop = true;
    while (loop) {
      ProToken nxt = (ProToken) source.nextToken();
      queue.offer(nxt);
      ABLNodeType type = nxt.getNodeType();
      if (type == ABLNodeType.OBJCOLON) {
        reviewObjcolon();
      }
      loop = (type != ABLNodeType.EOF_ANTLR4) && (type != ABLNodeType.PERIOD);
    }
  }

  private void reviewObjcolon() {
    ProToken objColonToken = queue.removeLast();
    Deque<ProToken> comments = new LinkedList<>();
    Deque<ProToken> leftPart = new LinkedList<>();

    // Save comments and whitespaces in list, and try to find first token on default channel
    Deque<ProToken> prevTokens = getBackwardsFirstVisibleToken(queue);
    if (prevTokens.isEmpty()) {
      queue.add(objColonToken);
      return;
    }
    boolean lookBackwards = false;
    ProToken tok = prevTokens.pollFirst();
    if (tok.getChannel() == Token.DEFAULT_CHANNEL) {
      leftPart.add(tok);
      lookBackwards = (tok.getNodeType() == ABLNodeType.ID) || tok.getNodeType().isKeyword();
    } else {
      comments.add(tok);
    }
    comments.addAll(prevTokens);

    // Look backwards as far as possible...
    while (lookBackwards) {
      prevTokens = getBackwardsFirstVisibleToken(queue);
      ProToken firstToken = prevTokens.peekFirst();
      if (firstToken == null) {
        // No more tokens, just stop
        lookBackwards = false;
      } else if ((firstToken.getNodeType() == ABLNodeType.NAMEDOT)
          || ((firstToken.getNodeType() != ABLNodeType.PERIOD) && firstToken.getText().startsWith("."))) {
        // NAMEDOT or something which starts with a dot but not just an end of statement ?
        // Then add previous tokens
        prevTokens.descendingIterator().forEachRemaining(leftPart::addFirst);
        getBackwardsFirstVisibleToken(queue).descendingIterator().forEachRemaining(leftPart::addFirst);
      } else if ((prevTokens.size() == 1)
          && ((firstToken.getNodeType() == ABLNodeType.ID) || firstToken.getNodeType().isKeyword())) {
        // No space or comment ? Then this is attached to the next token
        leftPart.addFirst(firstToken);
      } else {
        // Keep it as part of previous token
        lookBackwards = false;
        queue.addAll(prevTokens);
      }
    }

    if (leftPart.size() > 1) {
      if ((leftPart.peekFirst().getNodeType() == ABLNodeType.NAMEDOT)
          || leftPart.peekFirst().getNodeType().isSymbol()) {
        // NAMEDOT as the beginning of stream, kept as is
        leftPart.iterator().forEachRemaining(queue::addLast);
      } else {
        // Now merge all the parts into one ID token.
        StringBuilder origText = new StringBuilder(leftPart.peekFirst().getText());
        ProToken.Builder newTok = new ProToken.Builder(leftPart.pollFirst()).setType(ABLNodeType.ID);
        for (ProToken zz : leftPart) {
          origText.append(zz.getText());
          if (zz.getChannel() == Token.DEFAULT_CHANNEL)
            newTok.mergeWith(zz);
        }
        newTok.setRawText(origText.toString());
        queue.addLast(newTok.build());
      }
    } else if (leftPart.size() == 1) {
      ProToken tmp = leftPart.pollFirst();
      // Not namedotted, so if it's reserved and not a system handle, convert to ID.
      if (tmp.getNodeType().isReservedKeyword() && !tmp.getNodeType().isSystemHandle())
        queue.addLast(new ProToken.Builder(tmp).setType(ABLNodeType.ID).build());
      else
        queue.addLast(tmp);
    }
    queue.addAll(comments);
    queue.add(objColonToken);
  }

  @Override
  public Token nextToken() {
    if ((currentToken != null) && (currentToken.getType() == Token.EOF)) {
      return currentToken;
    }

    if (queue.isEmpty()) {
      fillHeap();
    }

    ProToken tok = queue.poll();
    if (tok != null) {
      currentToken = tok;
      currentToken.setTokenIndex(currentPosition++);
    }

    return currentToken;
  }

  @Override
  public int getLine() {
    return currentToken.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return currentToken.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return currentToken.getInputStream();
  }

  @Override
  public String getSourceName() {
    return IntStream.UNKNOWN_SOURCE_NAME;
  }

  @Override
  public void setTokenFactory(TokenFactory<?> factory) {
    throw new UnsupportedOperationException("Unable to change TokenFactory object");
  }

  @Override
  public TokenFactory<?> getTokenFactory() {
    return source.getTokenFactory();
  }

  private static Deque<ProToken> getBackwardsFirstVisibleToken(Deque<ProToken> queue) {
    Deque<ProToken> retVal = new LinkedList<>();
    ProToken tok = queue.pollLast();
    while ((tok != null) && (tok.getChannel() != Token.DEFAULT_CHANNEL)) {
      retVal.addFirst(tok);
      tok = queue.pollLast();
    }
    if (tok != null)
      retVal.addFirst(tok);

    return retVal;
  }

}
