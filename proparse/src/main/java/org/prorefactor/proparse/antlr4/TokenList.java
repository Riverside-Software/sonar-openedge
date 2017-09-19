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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;
import org.prorefactor.core.NodeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenList.class);

  private final TokenSource tokenStream;
  private final List<ProToken> list = new ArrayList<>();

  private int currentPosition = 0;
  private ProToken currentToken;
  private boolean initialized = false;

  TokenList(TokenSource input) {
    this.tokenStream = input;
  }

  private void build()  {
    LOGGER.trace("Entering TokenList#build()");
    int zz = 0;
    for (;;) {
      ProToken nextToken = (ProToken) tokenStream.nextToken();
      nextToken.setTokenIndex(zz++);
      list.add(nextToken);
      if (nextToken.getType() == PreprocessorParser.OBJCOLON)
        reviewObjcolon();
      if (nextToken.getType() == PreprocessorParser.EOF)
        break;
    }
    LOGGER.trace("Exiting TokenList#build() - {} tokens", list.size());
  }

  private void reviewObjcolon() {
    int colonIndex = list.size() - 1;
    int lastIndex = colonIndex - 1;

    // Getting type of the token just before colon (excluding comments and whitespaces)
    int ttype = list.get(lastIndex).getType();
    while (ttype == PreprocessorParser.WS || ttype == PreprocessorParser.COMMENT) {
      ttype = list.get(--lastIndex).getType();
    }

    // Look for NAMEDOT pairs.
    // Actually, it's not that easy. Something like:
    // newsyntax.101b.deep.FindMe
    // is perfectly valid, and because of the digit following the '.',
    // one part of that name gets picked up as a token with text ".101b".
    int index = lastIndex;
    boolean foundNamedot = false;
    for (;;) {
      if (index == 0)
        break;
      int currType = list.get(index).getType();
      if (currType == PreprocessorParser.WS || currType == PreprocessorParser.COMMENT) {
        // There can be space in front of a NAMEDOT in a table or field name.
        // We don't want to fiddle with those here.
        return;
      }
      if (list.get(index - 1).getType() == PreprocessorParser.NAMEDOT) {
        index = index - 2;
      } else if (list.get(index).getText().charAt(0) == '.') {
        index = index - 1;
      } else {
        break;
      }
      foundNamedot = true;
    }
    if (foundNamedot) {
      // Now merge all the parts into one ID token.
      ProToken token = list.get(index);
      token.setType(PreprocessorParser.ID);
      StringWriter text = new StringWriter();
      text.append(token.getText());
      int drop = index + 1;
      for (int i = 0; i < lastIndex - index; i++) {
        text.append(list.get(drop).getText());
        list.remove(drop);
      }
      token.setText(text.toString());
      return;
    }

    // Not namedotted, so if it's reserved and not a system handle, convert to ID.
    ttype = list.get(index).getType();
    if (NodeTypes.isReserved(ttype) && (!NodeTypes.isSystemHandleName(ttype)))
      list.get(index).setType(PreprocessorParser.ID);
  }

  @Override
  public Token nextToken() {
    if (!initialized) {
      build();
      initialized = true;
    }
    if (currentPosition >= list.size())
      return currentToken;
    currentToken = list.get(currentPosition++);
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
    return tokenStream.getTokenFactory();
  }

}
