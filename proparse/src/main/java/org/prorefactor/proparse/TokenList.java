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
import java.util.NoSuchElementException;

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
    Deque<ProToken> clsName = new LinkedList<>();

    boolean foundNamedot = false;
    ProToken tok = null;
    try {
      // Store comments and whitespaces before the colon
      tok = queue.removeLast();
      while ((tok.getNodeType() == ABLNodeType.WS) || (tok.getNodeType() == ABLNodeType.COMMENT)) {
        comments.addFirst(tok);
        tok = queue.removeLast();
      }
  
      foundNamedot = false;
      while (true) {
        // There can be space in front of a NAMEDOT in a table or field name. We don't want to fiddle with those here.
        if ((tok.getNodeType() == ABLNodeType.WS) || (tok.getNodeType() == ABLNodeType.COMMENT)) {
          break;
        }
  
        // If previous is NAMEDOT, then we add both tokens
        if ((queue.peekLast() != null) && (queue.peekLast().getNodeType() == ABLNodeType.NAMEDOT)) {
          clsName.addFirst(tok);
          clsName.addFirst(queue.removeLast());
          tok = queue.removeLast();
        } else if (tok.getText().startsWith(".")) {
          clsName.addFirst(tok);
          tok = queue.removeLast();
        } else {
          break;
        }
        foundNamedot = true;
      }
    } catch (NoSuchElementException caught) {
      queue.addAll(clsName);
      queue.addAll(comments);
      queue.add(objColonToken);
      return;
    }

    if (foundNamedot) {
      // Now merge all the parts into one ID token.
      ProToken.Builder newTok = new ProToken.Builder(tok).setType(ABLNodeType.ID);
      for (ProToken zz : clsName) {
        newTok.mergeWith(zz);
      }
      queue.addLast(newTok.build());
      queue.addAll(comments);
    } else {
      // Not namedotted, so if it's reserved and not a system handle, convert to ID.
      if (tok.getNodeType().isReservedKeyword() && !tok.getNodeType().isSystemHandle())
        queue.addLast(new ProToken.Builder(tok).setType(ABLNodeType.ID).build());
      else
        queue.addLast(tok);
      queue.addAll(comments);
    }
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

}
