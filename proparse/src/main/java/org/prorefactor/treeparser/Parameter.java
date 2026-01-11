/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2026 Riverside Software
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
import org.prorefactor.core.JPNode;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.Variable;

import eu.rssw.pct.elements.PrimitiveDataType;

public class Parameter {
  private final JPNode definitionNode;
  private ABLNodeType progressType = ABLNodeType.VARIABLE;
  private ABLNodeType directionNode = ABLNodeType.INPUT;
  private Symbol symbol;

  public Parameter(JPNode definitionNode) {
    this.definitionNode = definitionNode;
  }

  public JPNode getDefinitionNode() {
    return definitionNode;
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
    return progressType.getType();
  }

  /**
   * For call arguments that are expressions, there might be no symbol (null). For Routines, the symbol should always be
   * non-null.
   */
  public Symbol getSymbol() {
    return symbol;
  }

  /** Set by TreeParserVariableDefinition */
  public void setDirectionNode(ABLNodeType directionNode) {
    this.directionNode = directionNode;
  }

  /** Set by TreeParserVariableDefinition */
  public void setProgressType(ABLNodeType type) {
    this.progressType = type;
  }

  /** Set by TreeParserVariableDefinition */
  public void setSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

  public String getSignatureString() {
    StringBuilder sb = new StringBuilder();
    switch (directionNode) {
      case INPUTOUTPUT: sb.append('M'); break;
      case OUTPUT: sb.append('O'); break;
      case RETURN: sb.append('R'); break;
      case BUFFER: return sb.append('B').toString();
      default: sb.append('I'); // INPUT
    }
    switch(progressType) {
      case TEMPTABLE: sb.append('T'); break;
      case TABLEHANDLE: sb.append("TH"); break;
      case DATASET: sb.append('D'); break;
      case DATASETHANDLE: sb.append("DH"); break;
      case VARIABLE:
        Variable v = (Variable) symbol;
        if ((v != null) && (v.getDataType() != null)) {
          if (v.getDataType().getPrimitive() == PrimitiveDataType.CLASS) {
            sb.append('Z').append(v.getDataType().getClassName());
          } else {
            sb.append(v.getDataType().getPrimitive().getSignature());
          }
          if (v.getExtent() != 0)
            sb.append("[]");
        } else {
          sb.append("??");
        }
        break;
      default:
        sb.append("??");
    }
    return sb.toString();
  }

  public String getIDESignature(boolean chronological) {
    StringBuilder sb = new StringBuilder();
    switch (directionNode) {
      case INPUTOUTPUT: sb.append('⇅'); break;
      case OUTPUT: sb.append(chronological ? '↑' : '↓'); break;
      case RETURN: sb.append(chronological ? '⇈' : '⇊'); break;
      case BUFFER: return sb.append("BUFFER").toString();
      default: sb.append(chronological ? '↓' : '↑'); // INPUT
    }
    switch (progressType) {
      case TEMPTABLE:
        sb.append("TBL");
        break;
      case TABLEHANDLE:
        sb.append("TBL-HDL");
        break;
      case DATASET: sb.append("DS"); break;
      case DATASETHANDLE: sb.append("DS-HDL"); break;
      case VARIABLE:
        Variable v = (Variable) symbol;
        if (v != null) {
          if (v.getDataType().getPrimitive() == PrimitiveDataType.CLASS) {
            sb.append(v.getDataType().getClassName());
          } else {
            sb.append(v.getDataType().getPrimitive().getIDESignature());
          }
          if (v.getExtent() != 0)
            sb.append("[]");
        } else {
          sb.append("??");
        }
        break;
      default:
        sb.append("??");
    }

    return sb.toString();

  }

}
