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
package org.prorefactor.treeparser;

import antlr.RecognitionException;
import antlr.collections.AST;

/**
 * Common interface for our tree parsers.
 */
public interface IJPTreeParser {

  /**
   * The starting point for parsing a tree. You don't have to worry about this one - it is generated automatically if
   * your grammar "extends" JPTreeParser.g.
   */
  public void program(AST ast) throws RecognitionException, TreeParserException;

  /**
   * Get the (hopefully) last node where the tree parser left off before it died with an exception. See JPTreeParser for
   * an implementation of this (it needs to be copied and pasted into your own tree parser's .g grammar file).
   */
  public AST get_retTree();

}
