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

import org.prorefactor.core.JPNode;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.symbols.Symbol;

public class Parameter {

  private boolean bind = false;
  private int progressType = ProParserTokenTypes.VARIABLE;
  private JPNode directionNode;
  private Symbol symbol;

  /** For a TEMP-TABLE or DATASET, was the BIND keyword used? */
  public boolean isBind() {
    return bind;
  }

  /** The node of (BUFFER|INPUT|OUTPUT|INPUTOUTPUT|RETURN). */
  public JPNode getDirectionNode() {
    return directionNode;
  }

  /**
   * Integer corresponding to TokenType for (BUFFER|VARIABLE|TEMPTABLE|DATASET|PARAMETER). The syntax
   * <code>PARAMETER field = expression</code> is for RUN STORED PROCEDURE, and for those there is no symbol.
   */
  public int getProgressType() {
    return progressType;
  }

  /**
   * For call arguments that are expressions, there might be no symbol (null). For Routines, the symbol should always be
   * non-null.
   */
  public Symbol getSymbol() {
    return symbol;
  }

  /** Set by TreeParser01. */
  public void setBind(boolean bind) {
    this.bind = bind;
  }

  /** Set by TreeParser01. */
  public void setDirectionNode(JPNode directionNode) {
    this.directionNode = directionNode;
  }

  /** Set by TreeParser01. */
  public void setProgressType(int progressType) {
    this.progressType = progressType;
  }

  /** Set by TreeParser01. */
  public void setSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

}
