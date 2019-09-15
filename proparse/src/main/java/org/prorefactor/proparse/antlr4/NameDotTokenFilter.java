/********************************************************************************
 * Copyright (c) 2015-2019 Riverside Software
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

import java.util.Deque;
import java.util.LinkedList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.ProToken;

/**
 * Merge NAMEDOT with previous and next tokens with the following rules:<ul>
 *  <li> ( ID | keyword ) NAMEDOT ( ID | keyword ) </li>
 *  <li> A comment can follow immediately NAMEDOT. If so, then an unlimited number of WS and COMMENT can follow NAMEDOT before the ID</li></ul>
 */
public class NameDotTokenFilter implements TokenSource {
  private final TokenSource source;
  private final Deque<ProToken> queue = new LinkedList<>();

  private ProToken currentToken;

  public NameDotTokenFilter(TokenSource source) {
    this.source = source;
  }

  private void fillHeap() {
    boolean loop = true;
    while (loop) {
      ProToken nxt = (ProToken) source.nextToken();
      queue.offer(nxt);
      ABLNodeType type = nxt.getNodeType();
      if (nxt.getNodeType() == ABLNodeType.FILENAME) {
        type = reviewFileName();
      } else if (nxt.getNodeType() == ABLNodeType.NAMEDOT) {
        type = reviewNameDot();
      } else if ((nxt.getNodeType() == ABLNodeType.NUMBER) && nxt.getText().startsWith(".")) {
        type = reviewNumber();
      }
      // NAMEDOTs can be chained, so we stay in the loop until an expected end of statement
      loop = (type != ABLNodeType.EOF_ANTLR4) && (type != ABLNodeType.PERIOD);
    }
  }

  private ABLNodeType reviewNumber() {
    ProToken nameDot = queue.removeLast();
    ProToken prev = queue.pollLast();
    if (prev == null) {
      // In case NUMBER is the only one in queue (e.g. first token in procedure), we just exit safely by putting
      // it back in the queue
      queue.offer(nameDot);
    } else if ((prev.getNodeType() == ABLNodeType.ID) || prev.getNodeType().isKeyword() || (prev.getNodeType() == ABLNodeType.ANNOTATION)) {
      // Merge both tokens in first one
      ProToken.Builder builder = new ProToken.Builder(prev).mergeWith(nameDot);
      if (prev.getNodeType() != ABLNodeType.ANNOTATION)
        builder.setType(ABLNodeType.ID);
      queue.offer(builder.build());
    } else {
      // Anything else, we just put tokens back in the queue
      queue.offer(prev);
      queue.offer(nameDot);
    }

    return queue.peekLast().getNodeType();
  }

  private ABLNodeType reviewNameDot() {
    ProToken nameDot = queue.removeLast();
    ProToken prev = queue.pollLast();
    if (prev == null) {
      // In case NAMEDOT is the only one in queue (e.g. first token in procedure), we just exit safely by putting
      // it back in the queue
      queue.offer(nameDot);
    } else if ((prev.getNodeType() == ABLNodeType.ID) || prev.getNodeType().isKeyword() || (prev.getNodeType() == ABLNodeType.ANNOTATION)) {
      ProToken nxt = (ProToken) source.nextToken();
      if (nxt.getNodeType() == ABLNodeType.COMMENT) {
        // We can consume as much WS and COMMENT
        while ((nxt.getNodeType() == ABLNodeType.COMMENT) || (nxt.getNodeType() == ABLNodeType.WS)) {
          nxt = (ProToken) source.nextToken();
        }
        // Then we merge everything in first token
        ProToken.Builder builder = new ProToken.Builder(prev).mergeWith(nameDot).mergeWith(nxt);
        if (prev.getNodeType() != ABLNodeType.ANNOTATION)
          builder.setType(ABLNodeType.ID);
        queue.offer(builder.build());
      } else {
        // Merge everything in first token
        ProToken.Builder builder = new ProToken.Builder(prev).mergeWith(nameDot).mergeWith(nxt);
        if (prev.getNodeType() != ABLNodeType.ANNOTATION)
          builder.setType(ABLNodeType.ID);
        queue.offer(builder.build());
      }
    } else {
      // Anything else, we just put tokens back in the queue
      queue.offer(prev);
      queue.offer(nameDot);
    }

    return queue.peekLast().getNodeType();
  }

  private ABLNodeType reviewFileName() {
    ProToken fName = queue.removeLast();
    ProToken prev = queue.pollLast();
    if (prev == null) {
      queue.offer(fName);
    } else if ((prev.getNodeType() == ABLNodeType.ID) || (prev.getNodeType() == ABLNodeType.ANNOTATION)) {
      ProToken.Builder builder = new ProToken.Builder(prev).mergeWith(fName);
      queue.offer(builder.build());
    } else {
      queue.offer(prev);
      queue.offer(fName);
    }

    return queue.peekLast().getNodeType();
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
    }

    return currentToken;
  }

  @Override
  public int getLine() {
    return source.getLine();
  }

  @Override
  public int getCharPositionInLine() {
    return source.getCharPositionInLine();
  }

  @Override
  public CharStream getInputStream() {
    return source.getInputStream();
  }

  @Override
  public String getSourceName() {
    return source.getSourceName();
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
