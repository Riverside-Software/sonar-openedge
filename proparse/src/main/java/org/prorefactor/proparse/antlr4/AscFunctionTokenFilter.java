package org.prorefactor.proparse.antlr4;

import java.util.LinkedList;
import java.util.Queue;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.NodeTypes;

public class AscFunctionTokenFilter implements TokenSource {
  private final TokenSource source;
  private final Queue<Token> heap = new LinkedList<>();

  public AscFunctionTokenFilter(TokenSource source) {
    this.source = source;
  }

  @Override
  public Token nextToken() {
    if (!heap.isEmpty()) {
      return heap.poll();
    }
    
    Token currToken = source.nextToken();
    if (currToken.getType() == NodeTypes.ASC) {
      Token nxt = source.nextToken();
      while ((nxt.getType() != Token.EOF) && (nxt.getChannel() != Token.DEFAULT_CHANNEL)) {
        heap.offer(nxt);
        nxt = source.nextToken();
      }
      heap.offer(nxt);
      if (nxt.getType() != NodeTypes.LEFTPAREN) {
        ((ProToken) currToken).setType(NodeTypes.ASCENDING);
      }
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
