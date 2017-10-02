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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser.symbols.FieldContainer;
import org.prorefactor.treeparser.symbols.Symbol;

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import antlr.Token;
import antlr.collections.AST;
import antlr.collections.ASTEnumeration;

/**
 * Implementation of antlr.AST. Most "simple" methods are just copy/pasted from antlr.BaseAST.
 */
public class JPNode implements AST {
  private ProToken token;

  private JPNode down;
  private JPNode right;
  private JPNode left;
  private JPNode up;

  private Map<Integer, Integer> attrMap;
  private Map<String, String> attrMapStrings;
  private Map<Integer, Object> linkMap;
  private Map<Integer, String> stringAttributes;

  private static final BiMap<Integer, String> attrStrEqs;

  // Static class initializer.
  static {
    attrStrEqs = HashBiMap.create();
    for (AttributeKey attr : AttributeKey.values()) {
      attrStrEqs.put(attr.getKey(), attr.getName());
    }
    for (AttributeValue attr : AttributeValue.values()) {
      attrStrEqs.put(attr.getKey(), attr.getName());
    }
  }

  public JPNode(ProToken t) {
    this.token = t;
    setType(t.getType());
  }

  /**
   * Call after tree has been created, in order to deal with tree traversing and trailing nodes
   */
  public void backLinkAndFinalize() {
    // Assert that this is done on the top level node
    backLink();
    finalizeTrailingHidden();
  }

  /**
   * Set parent and prevSibling links
   */
  private void backLink() {
    JPNode currNode = down;
    while (currNode != null) {
      currNode.up = this;
      currNode.backLink();
      JPNode nextNode = currNode.right;
      if (nextNode != null)
        nextNode.left = currNode;
      currNode = nextNode;
    }
  }

  private void finalizeTrailingHidden() {
    /*
     * The node passed in should be the Program_root. The last child of the Program_root should be the Program_tail, as
     * set by the parser. (See propar.g) We want to find the last descendant of the last child before Program_tail, and
     * then set up Program_tail with that node's hiddenAfter. Program_tail is the holder node for any trailing hidden
     * tokens. This function will have to change slightly if we change the layout of hidden tokens.
     */
    JPNode tailNode = down;
    if (tailNode == null || tailNode.getNodeType() == ABLNodeType.PROGRAM_TAIL)
      return;
    JPNode lastNode = tailNode;
    while (tailNode != null && tailNode.getNodeType() != ABLNodeType.PROGRAM_TAIL) {
      lastNode = tailNode;
      tailNode = tailNode.getNextSibling();
    }
    if (tailNode == null || tailNode.getNodeType() != ABLNodeType.PROGRAM_TAIL)
      return;
    lastNode = lastNode.getLastDescendant();
    ProToken lastT = lastNode.getHiddenAfter();
    ProToken tempT = lastT;
    while (tempT != null) {
      lastT = tempT;
      tempT = (ProToken) tempT.getHiddenAfter();
    }
    tailNode.setHiddenBefore(lastT);
  }

  // *************
  // AST interface
  // *************

  @Override
  public void addChild(AST child) {
    if (child == null)
      return;
    JPNode node = down;
    if (node != null) {
      while (node.right != null) {
        node = node.right;
      }
      node.right = (JPNode) child;
    } else {
      down = (JPNode) child;
    }
  }

  @Override
  public int getNumberOfChildren() {
    int n = 0;
    JPNode node = down;
    if (node != null) {
      n = 1;
      while (node.right != null) {
        node = node.right;
        n++;
      }
      return n;
    }
    return n;
  }

  @Override
  public void initialize(int t, String txt) {
    setType(t);
    setText(txt);
  }

  @Override
  public void initialize(AST t) {
    setType(t.getType());
    setText(t.getText());
  }

  @Override
  public void initialize(Token t) {
    this.token = (ProToken) t;
    setType(t.getType());
  }


  @Override
  public JPNode getFirstChild() {
    return down;
  }

  @Override
  public JPNode getNextSibling() {
    return right;
  }

  @Override
  public String getText() {
    return token.getText();
  }

  @Override
  public int getType() {
    return token.getType();
  }

  @Override
  public int getLine() {
    return token.getLine();
  }

  @Override
  public int getColumn() {
    return token.getColumn();
  }

  @Override
  public void setFirstChild(AST c) {
    down = (JPNode) c;
  }

  @Override
  public void setNextSibling(AST n) {
    right = (JPNode) n;
  }

  @Override
  public void setText(String text) {
    token.setText(text);
  }
  
  @Override
  public void setType(int type) {
    token.setType(type);
  }

  @Override
  public boolean equals(AST t) { // NOSONAR
    throw new UnsupportedOperationException();
  }

  @Override
  public String toStringList() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toStringTree() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equalsList(AST t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equalsListPartial(AST sub) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equalsTree(AST t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equalsTreePartial(AST sub) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ASTEnumeration findAll(AST target) {
    throw new UnsupportedOperationException();
  }

  @Override
  public ASTEnumeration findAllPartial(AST sub) {
    throw new UnsupportedOperationException();
  }

  // ********************
  // End of AST interface
  // ********************

  // Attributes from ProToken

  public ABLNodeType getNodeType() {
    return token.getNodeType();
  }

  /**
   * Source number in the macro tree.
   * 
   * @see org.prorefactor.macrolevel.ListingParser#sourceArray()
   */
  public int getSourceNum() {
    return token.getMacroSourceNum();
  }

  public int getEndLine() {
    return token.getEndLine();
  }

  public int getEndColumn() {
    return token.getEndColumn();
  }

  public String getFilename() {
    return token.getFilename();
  }

  public int getFileIndex() {
    return token.getFileIndex();
  }

  public int getEndFileIndex() {
    return token.getEndFileIndex();
  }

  public ProToken getHiddenAfter() {
    return (ProToken) token.getHiddenAfter();
  }

  public ProToken getHiddenBefore() {
    return (ProToken) token.getHiddenBefore();
  }


  public String getAnalyzeSuspend() {
    return token.getAnalyzeSuspend();
  }

  // ******************
  // Navigation methods
  // ******************

  public JPNode getParent() {
    return up;
  }

  /**
   * @return Previous sibling in line before this one
   */
  public JPNode getPreviousSibling() {
    return left;
  }

  /**
   * First Natural Child is found by repeating firstChild() until a natural node is found. If the start node is a
   * natural node, then it is returned.
   */
  public JPNode firstNaturalChild() {
    if (token.isNatural())
      return this;
    for (JPNode n = down; n != null; n = n.down) {
      if (n.token.isNatural())
        return n;
    }
    return null;
  }

  /** 
   * @return Last child of the last child of the...
   */
  public JPNode getLastDescendant() {
    if (down == null)
      return this;
    JPNode node = down;
    while (node.right != null) {
      node = node.right;
    }
    return node.getLastDescendant();
  }

  /**
   * @return First child if there is one, otherwise next sibling
   */
  public JPNode nextNode() {
    return (down == null ? right : down);
  }

  /** 
   * @return Previous sibling if there is one, otherwise parent
   */
  public JPNode getPreviousNode() {
    return (left == null ? up : left);
  }

  // *************************
  // End of navigation methods
  // *************************

  // ***************
  // Various queries
  // ***************

  /** Get an ArrayList of the direct children of this node. */
  public List<JPNode> getDirectChildren() {
    List<JPNode> ret = new ArrayList<>();
    JPNode n = getFirstChild();
    while (n != null) {
      ret.add(n);
      n = n.getNextSibling();
    }
    return ret;
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> query(Integer... findTypes) {
    JPNodeQuery query = new JPNodeQuery(findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> queryMainFile(Integer... findTypes) {
    JPNodeQuery query = new JPNodeQuery(false, true, findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> queryStateHead(Integer... findTypes) {
    JPNodeQuery query = new JPNodeQuery(true, findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> queryStateHeadInMainFile(Integer... findTypes) {
    JPNodeQuery query = new JPNodeQuery(true, true, findTypes);
    walk(query);

    return query.getResult();
  }

  /** Find the first hidden token after this node's last descendant. */
  public ProToken findFirstHiddenAfterLastDescendant() {
    // There's no direct way to get a "hidden after" token,
    // so to find the hidden tokens after the current node's last
    // descendant, we find the next sibling of the current node,
    // find the first "natural" descendant of it (if it is not
    // itself natural), and then get its first hidden token.
    JPNode nextNatural = getNextSibling();
    if (nextNatural == null)
      return null;
    if (nextNatural.getNodeType() != ABLNodeType.PROGRAM_TAIL) {
      nextNatural = nextNatural.firstNaturalChild();
      if (nextNatural == null)
        return null;
    }
    return nextNatural.getHiddenFirst();
  }


  /** Find the first direct child with a given node type. */
  public JPNode findDirectChild(int nodeType) {
    for (JPNode node = down; node != null; node = node.getNextSibling()) {
      if (node.getType() == nodeType)
        return node;
    }
    return null;
  }

  // *****************************
  // Various attributes management
  // *****************************

  public int attrGet(int key) {
    if ((attrMap != null) && attrMap.containsKey(key)) {
      return attrMap.get(key);
    }
    switch (key) {
      case IConstants.NODE_TYPE_KEYWORD:
        return getNodeType().isKeyword() ? 1 : 0;
      case IConstants.ABBREVIATED:
        return isAbbreviated() ? 1 : 0;
      case IConstants.FROM_USER_DICT:
        return NodeTypes.userLiteralTest(getText(), getType()) ? 1 : 0;
      case IConstants.SOURCENUM:
        return token.getMacroSourceNum();
      default:
        return 0;
    }
  }

  public String attrGetS(int attrNum) {
    if ((stringAttributes != null) && stringAttributes.containsKey(attrNum)) {
      return stringAttributes.get(attrNum);
    }
    if (attrMap != null && attrMap.containsKey(attrNum)) {
      if (attrNum == IConstants.STATE2) {
        String typename = NodeTypes.getTypeName(attrMap.get(attrNum));
        return typename == null ? "" : typename;
      } else {
        String ret = attrEq(attrMap.get(attrNum));
        if (ret != null)
          return ret;
      }
    }
    switch (attrNum) {
      case IConstants.NODE_TYPE_KEYWORD:
        if (getNodeType().isKeyword())
          return "t";
        else
          return "";
      case IConstants.ABBREVIATED:
        if (isAbbreviated())
          return "t";
        else
          return "";
      case IConstants.FULLTEXT:
        if (NodeTypes.isKeywordType(getType()))
          return NodeTypes.getFullText(getText());
        else
          return getText();
      case IConstants.FROM_USER_DICT:
        if (NodeTypes.userLiteralTest(getText(), getType()))
          return "t";
        else
          return "";
      default:
        return "";
    }
  }

  public String attrGetS(String attrName) {
    if (attrMapStrings != null) {
      String ret = attrMapStrings.get(attrName);
      if (ret != null)
        return ret;
    }
    Integer intKey = attrEq(attrName);
    if (intKey != null)
      return attrGetS(intKey);
    return "";
  }

  public void attrSet(int key, String value) {
    if (stringAttributes == null)
      stringAttributes = new HashMap<>();
    stringAttributes.put(key, value);
  }

  public void attrSet(Integer key, int val) {
    if (attrMap == null)
      initAttrMap();
    attrMap.put(key, val);
  }

  public void attrSetS(String key, String value) {
    if (attrMapStrings == null)
      attrMapStrings = new HashMap<>();
    attrMapStrings.put(key, value);
  }

  /**
   * Mark a node as "operator"
   */
  public void setOperator() {
    attrSet(IConstants.OPERATOR, IConstants.TRUE);
  }

  /**
   * Get a link to an arbitrary object. Integers from -200 through -499 are reserved for Joanju.
   */
  public Object getLink(Integer key) {
    if (linkMap == null)
      return null;
    return linkMap.get(key);
  }

  /** If this AST was constructed from another, then get the original. */
  public JPNode getOriginal() {
    if (linkMap == null)
      return null;
    return (JPNode) linkMap.get(IConstants.ORIGINAL);
  }

  public int getState2() {
    return attrGet(IConstants.STATE2);
  }

  /** Some nodes like RUN, USER_FUNC, LOCAL_METHOD_REF have a Call object linked to them by TreeParser01. */
  public Call getCall() {
    return (Call) getLink(IConstants.CALL);
  }

  /** Mark a node as a "statement head" */
  public void setStatementHead() {
    attrSet(IConstants.STATEHEAD, IConstants.TRUE);
  }

  /** Mark a node as a "statement head" */
  public void setStatementHead(int state2) {
    attrSet(IConstants.STATEHEAD, IConstants.TRUE);
    if (state2 != 0)
      attrSet(IConstants.STATE2, state2);
  }

  /** Certain nodes will have a link to a Symbol, set by TreeParser01. */
  public Symbol getSymbol() {
    return (Symbol) getLink(IConstants.SYMBOL);
  }

  private static Integer attrEq(String attrName) {
    return attrStrEqs.inverse().get(attrName);
  }

  private static String attrEq(int attrNum) {
    return attrStrEqs.get(attrNum);
  }

  public boolean hasTableBuffer() {
    return getLink(IConstants.SYMBOL) != null;
  }

  public boolean hasBufferScope() {
    return getLink(IConstants.BUFFERSCOPE) != null;
  }

  public boolean hasBlock() {
    return getLink(IConstants.BLOCK) != null;
  }

  public boolean hasProparseDirective(String directive) {
    ProToken tok = getHiddenBefore();
    while (tok != null) {
      if (tok.getNodeType() == ABLNodeType.PROPARSEDIRECTIVE) {
        String str = tok.getText().trim();
        if (str.startsWith("prolint-nowarn(") && str.charAt(str.length() - 1) == ')') {
          for (String rule : Splitter.on(',').omitEmptyStrings().trimResults().split(
              str.substring(15, str.length() - 1))) {
            if (rule.equals(directive))
              return true;
          }
        }
      }
      tok = (ProToken) tok.getHiddenBefore();
    }
    // If token has been generated by the parser (ie synthetic token), then we look for hidden token attached to the
    // first child
    if (token.isSynthetic()) {
      JPNode child = down;
      if ((child != null) && (child.hasProparseDirective(directive))) {
        return true;
      }
      // And for synthetic ASSIGN statements, we have to look for the first grandchild
      // See root node of assignstate2
      if ((child != null) && (token.getNodeType() == ABLNodeType.ASSIGN)) {
        child = child.getFirstChild();
        if ((child != null) && child.hasProparseDirective(directive))
          return true;
      }
    }

    return false;
  }

  /**
   * Get the comments that precede this node. Gets the <b>consecutive</b> comments from Proparse if "connected",
   * otherwise gets the comments stored within this node object. CAUTION: We want to know if line breaks exist between
   * comments and nodes, and if they exist between consecutive comments. To preserve that information, the String
   * returned here may have "\n" in front of the first comment, may have "\n" separating comments, and may have "\n"
   * appended to the last comment. We do not preserve the number of newlines, nor do we preserve any other whitespace.
   * 
   * @return null if no comments.
   */
  public String getComments() {
    String ret = (String) getLink(IConstants.COMMENTS);
    if (ret != null)
      return ret;
    StringBuilder buff = new StringBuilder();
    boolean hasComment = false;
    int filenum = getFileIndex();
    for (ProToken t = getHiddenBefore(); t != null; t = (ProToken) t.getHiddenBefore()) {
      int hiddenType = t.getType();
      if (t.getFileIndex() != filenum)
        break;
      if (hiddenType == NodeTypes.WS) {
        if (t.getText().indexOf('\n') > -1)
          buff.insert(0, '\n');
      } else if (hiddenType == NodeTypes.COMMENT) {
        buff.insert(0, t.getText());
        hasComment = true;
      } else {
        break;
      }
    }
    return hasComment ? buff.toString() : null;
  }

  /**
   * Get the FieldContainer (Frame or Browse) for a statement head node or a frame field reference. This value is set by
   * TreeParser01. Head nodes for statements with the [WITH FRAME | WITH BROWSE] option have this value set. Is also
   * available on the Field_ref node for #(Field_ref INPUT ...) and for #(USING #(Field_ref...)...).
   */
  public FieldContainer getFieldContainer() {
    return (FieldContainer) getLink(IConstants.FIELD_CONTAINER);
  }

  public ProToken getHiddenFirst() {
    // Some day, I'd like to change the structure for the hidden tokens,
    // so that nodes only store a reference to "first before", and each of those
    // only store a pointer to "next".
    ProToken t = getHiddenBefore();
    if (t != null) {
      ProToken ttemp = t;
      while (ttemp != null) {
        t = ttemp;
        ttemp = (ProToken) t.getHiddenBefore();
      }
    }
    return t;
  }

  public List<ProToken> getHiddenTokens() {
    LinkedList<ProToken> ret = new LinkedList<>();
    ProToken tkn = getHiddenBefore();
    while (tkn != null) {
      ret.addFirst(tkn);
      tkn = (ProToken) tkn.getHiddenBefore();
    }
    return ret;
  }

  /** Return self if statehead, otherwise returns enclosing statehead. */
  public JPNode getStatement() {
    JPNode n = this;
    while (n != null && !n.isStateHead()) {
      n = n.getParent();
    }
    return n;
  }


  /**
   * @return The full name of the annotation, or an empty string is node is not an annotation
   */
  public String getAnnotationName() {
    if (getNodeType() != ABLNodeType.ANNOTATION)
      return "";
    StringBuilder annName = new StringBuilder(token.getText().substring(1));
    JPNode tok = down;
    while ((tok != null) && (tok.getNodeType() != ABLNodeType.PERIOD) && (tok.getNodeType() != ABLNodeType.LEFTPAREN)) {
      annName.append(tok.getText());
      tok = tok.getNextSibling();
    }

    return annName.toString();
  }



  private void initAttrMap() {
    if (attrMap == null) {
      attrMap = new HashMap<>();
    }
  }

  private void initLinkMap() {
    if (linkMap == null) {
      linkMap = new HashMap<>();
    }
  }

  public boolean isAbbreviated() {
    return token.getNodeType().isAbbreviated(getText());
  }

  /**
   * Is this a natural node (from real source text)? If not, then it is a synthetic node, added just for tree structure.
   */
  public boolean isNatural() {
    return token.isNatural();
  }

  /** Does this node have the Proparse STATEHEAD attribute? */
  public boolean isStateHead() {
    return attrGet(IConstants.STATEHEAD) == IConstants.TRUE;
  }


  /** Some nodes like RUN, USER_FUNC, LOCAL_METHOD_REF have a Call object linked to them by TreeParser01. */
  public void setCall(Call call) {
    setLink(IConstants.CALL, call);
  }

  /**
   * Set the comments preceding this node. CAUTION: Does not change any values in Proparse. Only use this if the JPNode
   * tree is "disconnected", because getComments returns the comments from the "hidden tokens" in Proparse in
   * "connected" mode.
   */
  public void setComments(String comments) {
    setLink(IConstants.COMMENTS, comments);
  }

  /** @see #getFieldContainer() */
  public void setFieldContainer(FieldContainer fieldContainer) {
    setLink(IConstants.FIELD_CONTAINER, fieldContainer);
  }

  /** @see #getLink(Integer) */
  public void setLink(Integer key, Object value) {
    if (linkMap == null)
      initLinkMap();
    linkMap.put(key, value);
  }

  /** Assigned by the tree parser. */
  public void setSymbol(Symbol symbol) {
    setLink(IConstants.SYMBOL, symbol);
  }

  public void copyHiddenAfter(JPNode to) {
    to.setHiddenAfter(getHiddenAfter());
  }

  public void copyHiddenBefore(JPNode to) {
    to.setHiddenBefore(getHiddenBefore());
  }

  public void setHiddenAfter(ProToken t) {
    token.setHiddenAfter(t);
  }

  public void setHiddenBefore(ProToken t) {
    token.setHiddenBefore(t);
  }

  public void setNextSiblingWithLinks(AST n) {
    for (AST next = getNextSibling(); next != null; next = next.getNextSibling()) {
      ((JPNode) next).up = null;
    }
    setNextSibling(n);
    for (AST next = getNextSibling(); next != null; next = next.getNextSibling()) {
      ((JPNode) next).up = this.up;
    }
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    buff.append(token.getNodeType()).append(" \"").append(getText()).append("\" ").append(
        getFilename()).append(':').append(getLine()).append(':').append(getColumn());
    return buff.toString();
  }

  /**
   * Get the full, preprocessed text from a node. When run on top node, the result is very comparable to
   * COMPILE..PREPROCESS. This is the same as the old C++ Proparse API writeNode(). Also see org.joanju.proparse.Iwdiff.
   */
  public String toStringFulltext() {
    ICallback<List<JPNode>> callback = new FlatListBuilder();
    walk(callback);
    List<JPNode> list = callback.getResult();
    StringBuilder bldr = new StringBuilder();
    for (JPNode node : list) {
      for (ProToken t = node.getHiddenFirst(); t != null; t = t.getNext()) {
        if ((t.getNodeType() == ABLNodeType.COMMENT) || (t.getNodeType() == ABLNodeType.WS))
          bldr.append(t.getText());
      }
      bldr.append(node.getText());
    }

    return bldr.toString();
  }

  /**
   * Walk the tree from the input node down
   */
  public void walk(ICallback<?> callback) {
    boolean visitChildren = callback.visitNode(this);
    if (visitChildren) {
      for (JPNode child : getDirectChildren()) {
        child.walk(callback);
      }
    }
  }

  public String allLeadingHiddenText() {
    String ret = "";
    ProToken t = getHiddenFirst();
    while (t != null) {
      ret += t.getText();
      t = (ProToken) t.getHiddenAfter();
    }
    return ret;
  }


}
