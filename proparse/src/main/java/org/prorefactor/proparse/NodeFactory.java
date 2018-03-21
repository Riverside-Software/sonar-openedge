/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
package org.prorefactor.proparse;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
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

  public NodeFactory() {
    setASTNodeClass(JPNode.class);
  }

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
    ABLNodeType nodeType = ABLNodeType.getNodeType(type);
    if (nodeType == null)
      throw new IllegalArgumentException("Invalid type number " + type);
    // Used for synthetic node creation by the ANTLR generated parser
    ProToken token = new ProToken(nodeType, text);
    switch (nodeType) {
      case FIELD_REF:
        return new FieldRefNode(token);
      case PROGRAM_ROOT:
        return new ProgramRootNode(token);
      case PROPERTY_GETTER:
      case PROPERTY_SETTER:
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
    switch (ABLNodeType.getNodeType(token.getType())) {
      case RECORD_NAME:
        return new RecordNameNode((ProToken) token);
      case PROPARSEDIRECTIVE:
        return new ProparseDirectiveNode((ProToken) token);
      case DO:
      case FOR:
      case REPEAT:
      case FUNCTION:
      case PROCEDURE:
      case CONSTRUCTOR:
      case DESTRUCTOR:
      case METHOD:
      case CANFIND:
      case CATCH:
      case ON:
        return new BlockNode((ProToken) token);
      default:
        throw new IllegalArgumentException("Proparse error creating AST node " + token.toString() + ", " + s);
    }
  }

}
