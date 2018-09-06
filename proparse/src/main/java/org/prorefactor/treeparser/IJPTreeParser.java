/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
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
  public void program(AST ast) throws RecognitionException;

  /**
   * Get the (hopefully) last node where the tree parser left off before it died with an exception. See JPTreeParser for
   * an implementation of this (it needs to be copied and pasted into your own tree parser's .g grammar file).
   */
  public AST get_retTree();

}
