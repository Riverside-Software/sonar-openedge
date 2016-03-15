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
package org.prorefactor.core;

/**
 * Use this class to find specific comments in, before, or after a node hierarchy. See
 * org.prorefactor.refactor.unittest.NoUndoT.java (and its data file) for a complete unit test and example for this
 * class. Currently the only search option is a case insensitive search.
 */
public class CommentFinder {

  private String findString = null;
  private int numResults = 0;

  /** Review the text of current comment, to see if it matches. */
  public int commentTextReview(ProToken t) {
    if (t.getType() != NodeTypes.COMMENT)
      return 0;
    String theText = t.getText();
    if (theText == null || theText.length() == 0)
      return 0;
    if (theText.toLowerCase().indexOf(findString) > -1)
      return 1;
    return 0;
  }

  /**
   * Find comments which come after the last descendant of the node, which match, and are not separated from that last
   * sibling by any newline characters.
   * 
   * @param node
   * @return number of COMMENT tokens which match.
   */
  public int examineAfter(JPNode node) {
    int numAfter = 0;
    ProToken t = node.findFirstHiddenAfterLastDescendant();
    while (t != null) {
      if (t.getType() == NodeTypes.COMMENT) {
        numAfter += commentTextReview(t);
      } else {
        if (t.getText().indexOf('\n') > -1)
          break;
      }
      t = (ProToken) t.getHiddenAfter();
    }
    return numAfter;
  }

  /**
   * Find comments before the node which match and are not separated from the node by any blank lines.
   * 
   * @param node
   * @return number of COMMENT tokens which match.
   */
  public int examineBefore(JPNode node) {
    int numBefore = 0;
    int consecutiveBreaks = 0;
    ProToken t = node.getHiddenBefore();
    while (t != null) {
      if (t.getType() == NodeTypes.COMMENT) {
        numBefore += commentTextReview(t);
        consecutiveBreaks = 0;
      } else {
        String theText = t.getText();
        int firstBreak = theText.indexOf('\n');
        if (firstBreak > -1) {
          // Look for two line breaks in the same token
          int secondBreak = theText.lastIndexOf('\n');
          if (secondBreak != firstBreak)
            break;
          consecutiveBreaks++;
          if (consecutiveBreaks > 1)
            break;
        }
      }
      t = (ProToken) t.getHiddenBefore();
    }
    return numBefore;
  }

  /**
   * Find comments before the node which match.
   * 
   * @param node
   * @return number of COMMENT tokens which match.
   */
  public int examineInner(JPNode node) {
    if (node == null)
      return 0;
    int numInner = 0;
    ProToken t = node.getHiddenBefore();
    while (t != null) {
      numInner += commentTextReview(t);
      t = (ProToken) t.getHiddenBefore();
    }
    return numInner;
  }

  public void setFindString(String input) {
    findString = input.toLowerCase();
  }

  /** Return the number of COMMENT tokens which meet the search criteria */
  public int search(JPNode node) {
    if (node == null)
      return 0;
    numResults = 0;
    numResults += examineBefore(node);
    numResults += examineAfter(node);
    walkDescendants(node.firstChild());
    return numResults;
  }

  /**
   * Recursively examine the descendants of the node, incrementing numResults.
   */
  private void walkDescendants(JPNode node) {
    if (node == null)
      return;
    numResults += examineInner(node);
    walkDescendants(node.firstChild());
    walkDescendants(node.nextSibling());
  }

}
