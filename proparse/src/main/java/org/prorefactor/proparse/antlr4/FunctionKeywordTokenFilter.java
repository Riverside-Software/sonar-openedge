/*******************************************************************************
 * Copyright (c) 2017 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import java.util.LinkedList;
import java.util.Queue;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.NodeTypes;

/**
 * Convert some tokens to another type when not followed by LEFTPAREN:
 * <ul>
 * <li>ASC to ASCENDING</li>
 * <li>LOG to LOGICAL</li>
 * <li>GET-CODEPAGE to GET-CODEPAGES</li>
 * </ul>
 */
public class FunctionKeywordTokenFilter implements TokenSource {
  private final TokenSource source;
  private final Queue<Token> heap = new LinkedList<>();

  public FunctionKeywordTokenFilter(TokenSource source) {
    this.source = source;
  }

  @Override
  public Token nextToken() {
    if (!heap.isEmpty()) {
      return heap.poll();
    }

    Token currToken = source.nextToken();
    if ((currToken.getType() == NodeTypes.ASC) || (currToken.getType() == NodeTypes.LOG)
        || (currToken.getType() == NodeTypes.GETCODEPAGE) || (currToken.getType() == NodeTypes.GETCODEPAGES)) {
      Token nxt = source.nextToken();
      while ((nxt.getType() != Token.EOF) && (nxt.getChannel() != Token.DEFAULT_CHANNEL)) {
        heap.offer(nxt);
        nxt = source.nextToken();
      }
      heap.offer(nxt);
      if (nxt.getType() != NodeTypes.LEFTPAREN) {
        if (currToken.getType() == NodeTypes.ASC)
          ((ProToken) currToken).setType(NodeTypes.ASCENDING);
        else if (currToken.getType() == NodeTypes.LOG)
          ((ProToken) currToken).setType(NodeTypes.LOGICAL);
        else if (currToken.getType() == NodeTypes.GETCODEPAGE)
          ((ProToken) currToken).setType(NodeTypes.GETCODEPAGES);
      } else if (currToken.getType() == NodeTypes.GETCODEPAGES)
        ((ProToken) currToken).setType(NodeTypes.GETCODEPAGE);
    }
    return currToken;
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
