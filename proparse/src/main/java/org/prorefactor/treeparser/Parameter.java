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

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.symbols.Symbol;

public class Parameter {

  private boolean bind = false;
  private int progressType = ProParserTokenTypes.VARIABLE;
  private ABLNodeType directionNode;
  private Symbol symbol;

  /** For a TEMP-TABLE or DATASET, was the BIND keyword used? */
  public boolean isBind() {
    return bind;
  }

  /** The node of (BUFFER|INPUT|OUTPUT|INPUTOUTPUT|RETURN). */
  public ABLNodeType getDirectionNode() {
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
  public void setDirectionNode(ABLNodeType directionNode) {
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
