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

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.core.nodetypes.ProparseDirectiveNode;
import org.prorefactor.core.nodetypes.RecordNameNode;

import antlr.ASTFactory;
import antlr.Token;
import antlr.collections.AST;

public class NodeFactory extends ASTFactory {

  @Override
  public AST create() {
    // Not used in ProParser
    return create(0); 
  }

  @Override
  public AST create(int type) {
    // Not used in ProParser
    return create(type, "");
  }

  @Override
  public AST create(int type, String text) {
    // Used for synthetic node creation by the ANTLR generated parser
    ProToken token = new ProToken(type, text);
    switch (type) {
      case NodeTypes.Field_ref:
        return new FieldRefNode(token);
      case NodeTypes.Program_root:
        return new ProgramRootNode(token);
      case NodeTypes.Property_getter:
      case NodeTypes.Property_setter:
        return new BlockNode(token);
      default:
        return new JPNode(token);
    }
  }

  /**
   * Override Antlr's default use of the class name and reflection. Antlrs's call to its createUsingCtor() looks a bit
   * expensive to me. Here, we're able to just use a switch on the token type, because that will always tell us which
   * subclass to use. We're unlikely to ever want multiple node subclasses for any one token type.
   * 
   * @param s Class name... is ignored here, but it is used in the generated parser code for casting the return.
   */
  @Override
  public AST create(Token token, String s) {
    switch (token.getType()) {
      case NodeTypes.RECORD_NAME:
        return new RecordNameNode((ProToken) token);
      case NodeTypes.PROPARSEDIRECTIVE:
        return new ProparseDirectiveNode((ProToken) token);
      case NodeTypes.DO:
      case NodeTypes.FOR:
      case NodeTypes.REPEAT:
      case NodeTypes.FUNCTION:
      case NodeTypes.PROCEDURE:
      case NodeTypes.CONSTRUCTOR:
      case NodeTypes.DESTRUCTOR:
      case NodeTypes.METHOD:
      case NodeTypes.CANFIND:
      case NodeTypes.CATCH:
      case NodeTypes.ON:
        return new BlockNode((ProToken) token);
      default:
        throw new IllegalArgumentException("Proparse error creating AST node " + token.toString() + ", " + s);
    }
  }

}
