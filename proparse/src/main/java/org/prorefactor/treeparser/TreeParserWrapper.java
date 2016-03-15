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
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.PRCException;

import antlr.RecognitionException;

/**
 * This class just makes it easier to interface with an Antlr generated tree parser.
 */
public abstract class TreeParserWrapper {

  private TreeParserWrapper() {

  }

  /**
   * Run a tree parser for a given JPNode.
   * 
   * @throws PRCException
   */
  public static void run2(IJPTreeParser tp, JPNode theAST) throws PRCException {
    try {
      tp.program(theAST);
    } catch (RecognitionException caught) {
      JPNode node = (JPNode) tp.get_retTree();
      if (node == null) {
        // Last node where analysis failed can't be found
        // Then we throw based on the first natural child
        JPNode firstNatural = theAST.firstNaturalChild();
        throw new PRCException((firstNatural == null ? "" : firstNatural.getFilename()), caught);
      }

      StringBuilder sb = new StringBuilder(caught.getMessage());
      boolean done = false;
      while (!done) {
        sb.append(" -> File ").append(node.getFilename()).append(" Line: ").append(node.getLine()).append(
            " Column: ").append(node.getColumn()).append(" Type: ").append(
                NodeTypes.getTokenName(node.getType())).append(" Text: ").append(node.getText());
        if (node.getLine() == 0) {
          node = node.firstChild();
          if (node == null) {
            // this shouldn't happen, but we'll check anyway.
            done = true;
          }
        } else {
          done = true;
        }
      }
      throw new PRCException(sb.toString(), caught);
    } catch (TreeParserException caught) {
      throw new PRCException(caught);
    }
  }

}
