/*******************************************************************************
 * Copyright (c) 2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret
 *******************************************************************************/ 
package org.prorefactor.proparse.antlr4;

import org.prorefactor.core.ABLNodeType;

/**
 * ANTLR4 version of antlr.AST, where only the interesting methods are kept. 
 */
public interface AST {

  /**
   * @return First child of this node; null if no children
   */
  public AST getFirstChild();

  /**
   * @return Next sibling in line after this one
   */
  public AST getNextSibling();

  /**
   * @return Token text for this node
   */
  public String getText();

  /**
   * @return Get the token type for this node
   */
  public ABLNodeType getNodeType();

  /**
   * @return Get the token type for this node
   */
  public int getType();

  /**
   * @return Line number of this node
   */
  public int getLine();

  /**
   * @return Column number of this node
   */
  public int getColumn();

  /**
   * @return Number of children of this node; if leaf, returns 0
   */
  public int getNumberOfChildren();

}
