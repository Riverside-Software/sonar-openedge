/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - 
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import java.util.Deque;
import java.util.LinkedList;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.NodeTypes;

/**
 * Review the token list at an OBJCOLON token.
 * 
 * This is the reason this class was created in the first place. If we have an OBJCOLON, what comes before it has to
 * be one of a few things:
 * <ul>
 * <li>a system handle,
 * <li>a handle expression,
 * <li>an Object reference expression, or
 * <li>a type name (class name) for a static member reference
 * </ul>
 * <p>
 * A type name can be pretty much anything, even a reserved keyword. It can also be a qualified class name, such as
 * com.joanju.Foo.
 * <p>
 * This method attempts to resolve the following problem: Because of static class member references, a class name can
 * be the first token in an expression. Class names can be composed of reserved keywords. This means that a reserved
 * keyword could be the first piece of an expression, and this completely breaks the lookahead in the ANTLR generated
 * parser. So, here, we look for OBJCOLON, and make sure that what comes before it is a system handle, an ID, or a
 * non-reserved keyword.
 * <p>
 * A NAMEDOT token is a '.' followed by anything other than whitespace. If the OBJCOLON is proceeded by a NAMEDOT pair
 * (NAMEDOT followed by anything), then we convert all of the NAMEDOT pairs to NAMEDOT-ID pairs. Otherwise, if the
 * OBJCOLON is proceeded by any reserved keyword other than a systemhandlename, then we change that token's type to
 * ID.
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
    ProToken nxt = (ProToken) source.nextToken();
    while (true) {
      queue.offer(nxt);
      if (nxt.getType() == NodeTypes.OBJCOLON) {
        reviewObjcolon();
      }
      if ((nxt.getType() == NodeTypes.OBJCOLON) || (nxt.getType() == Token.EOF))
        break;
      nxt = (ProToken) source.nextToken();
    }
  }

  private void reviewObjcolon() {
    ProToken objColonToken = queue.removeLast();

    // Store comments and whitespaces before the colon
    Deque<ProToken> comments = new LinkedList<>();
    ProToken tok = queue.removeLast();
    while ((tok.getType() == NodeTypes.WS) || (tok.getType() == NodeTypes.COMMENT)) {
      comments.addFirst(tok);
      tok = queue.pollLast();
    }

    Deque<ProToken> clsName = new LinkedList<>();
    boolean foundNamedot = false;
    while (true) {
      if (tok == null)
        break;
      
      // There can be space in front of a NAMEDOT in a table or field name. We don't want to fiddle with those here.
      if ((tok.getType() == PreprocessorParser.WS) || (tok.getType() == PreprocessorParser.COMMENT)) {
        break;
      }

      // If previous is NAMEDOT, then we add both tokens
      if ((queue.peekLast() != null) && (queue.peekLast().getType() == NodeTypes.NAMEDOT)) {
        clsName.addFirst(tok);
        clsName.addFirst(queue.pollLast());
        tok = queue.removeLast();
      } else if (tok.getText().startsWith(".")) {
        clsName.addFirst(tok);
        tok = queue.removeLast();
      } else {
        break;
      }
      foundNamedot = true;
    }

    if (foundNamedot) {
      // Now merge all the parts into one ID token.
      StringBuilder text = new StringBuilder(tok.getText());
      tok.setType(PreprocessorParser.ID);
      for (ProToken zz : clsName) {
        text.append(zz.getText());
        tok.setEndFileIndex(zz.getEndFileIndex());
        tok.setEndLine(zz.getEndLine());
        tok.setEndCharPositionInLine(zz.getEndCharPositionInLine());
      }
      tok.setText(text.toString());
      queue.addLast(tok);
      queue.addAll(comments);
    } else {
      // Not namedotted, so if it's reserved and not a system handle, convert to ID.
      int ttype = tok.getType();
      if (NodeTypes.isReserved(ttype) && (!NodeTypes.isSystemHandleName(ttype)))
        tok.setType(PreprocessorParser.ID);
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
