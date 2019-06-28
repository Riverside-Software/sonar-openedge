/********************************************************************************
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
package org.prorefactor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.proparse.ParserSupport;
import org.prorefactor.proparse.SymbolScope.FieldType;
import org.prorefactor.proparse.antlr4.AST;
import org.prorefactor.treeparser.symbols.FieldContainer;
import org.prorefactor.treeparser.symbols.ISymbol;
import org.prorefactor.treeparser.symbols.Symbol;

import com.google.common.base.Splitter;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * ANTLR4 version of JPNode.
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


  protected JPNode(ProToken t) {
    this.token = t;
  }

  // *************
  // AST interface
  // *************

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
    return token.getNodeType().getType();
  }

  @Override
  public int getLine() {
    return token.getLine();
  }

  @Override
  public int getColumn() {
    return token.getCharPositionInLine();
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
   */
  public int getSourceNum() {
    return token.getMacroSourceNum();
  }

  public int getEndLine() {
    return token.getEndLine();
  }

  public int getEndColumn() {
    return token.getEndCharPositionInLine();
  }

  public int getFileIndex() {
    return token.getFileIndex();
  }

  public String getFileName() {
    return token.getFileName();
  }

  public int getEndFileIndex() {
    return token.getEndFileIndex();
  }

  public ProToken getHiddenBefore() {
    return token.getHiddenBefore();
  }

  public boolean isMacroExpansion() {
    return token.isMacroExpansion();
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

  /**
   * Get list of the direct children of this node.
   */
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
   * Return first direct child of a given type, or null if not found
   */
  public JPNode getFirstDirectChild(ABLNodeType type) {
    JPNode n = getFirstChild();
    while (n != null) {
      if (n.getNodeType() == type)
        return n;
      n = n.getNextSibling();
    }
    return null;
  }

  /**
   * Get a list of the direct children of a given type
   */
  public List<JPNode> getDirectChildren(ABLNodeType type) {
    List<JPNode> ret = new ArrayList<>();
    JPNode n = getFirstChild();
    while (n != null) {
      if (n.getNodeType() == type)
        ret.add(n);
      n = n.getNextSibling();
    }
    return ret;
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> query(ABLNodeType type, ABLNodeType... findTypes) {
    JPNodeQuery query = new JPNodeQuery(type, findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> queryMainFile(ABLNodeType type, ABLNodeType... findTypes) {
    JPNodeQuery query = new JPNodeQuery(false, true, null, type, findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant statement nodes (including this node)
   */
  public List<JPNode> queryStateHead() {
    JPNodeQuery query = new JPNodeQuery(true);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> queryStateHead(ABLNodeType type, ABLNodeType... findTypes) {
    JPNodeQuery query = new JPNodeQuery(true, type, findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes of a given type within current statement
   */
  public List<JPNode> queryCurrentStatement(ABLNodeType type, ABLNodeType... findTypes) {
    JPNodeQuery query = new JPNodeQuery(false, false, this.getStatement(), type, findTypes);
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
  public JPNode findDirectChild(ABLNodeType nodeType) {
    for (JPNode node = down; node != null; node = node.getNextSibling()) {
      if (node.getNodeType() == nodeType)
        return node;
    }
    return null;
  }

  /** Find the first direct child with a given node type. */
  public JPNode findDirectChild(int nodeType) {
    return findDirectChild(ABLNodeType.getNodeType(nodeType));
  }

  // *****************************
  // Various attributes management
  // *****************************

  public int attrGet(int key) {
    if ((attrMap != null) && attrMap.containsKey(key)) {
      return attrMap.get(key);
    }
    switch (key) {
      case IConstants.ABBREVIATED:
        return isAbbreviated() ? 1 : 0;
      case IConstants.SOURCENUM:
        return token.getMacroSourceNum();
      default:
        return 0;
    }
  }

  public String attrGetS(int attrNum) {
    if (attrNum != IConstants.QUALIFIED_CLASS_INT)
      throw new IllegalArgumentException("Invalid value " + attrNum);
    if ((stringAttributes != null) && stringAttributes.containsKey(attrNum)) {
      return stringAttributes.get(attrNum);
    }
    return "";
  }

  public String attrGetS(String attrName) {
    if (IConstants.QUALIFIED_CLASS_STRING.equalsIgnoreCase(attrName))
      throw new IllegalArgumentException("Invalid value " + attrName);
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

  /** Certain nodes will have a link to a Symbol, set by TreeParser. */
  public Symbol getSymbol() {
    return (Symbol) getLink(IConstants.SYMBOL);
  }

  private static Integer attrEq(String attrName) {
    return attrStrEqs.inverse().get(attrName);
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
      tok = tok.getHiddenBefore();
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
    for (ProToken t = getHiddenBefore(); t != null; t = t.getHiddenBefore()) {
      if (t.getFileIndex() != filenum)
        break;
      if (t.getNodeType() == ABLNodeType.WS) {
        if (t.getText().indexOf('\n') > -1)
          buff.insert(0, '\n');
      } else if (t.getNodeType() == ABLNodeType.COMMENT) {
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
        ttemp = t.getHiddenBefore();
      }
    }
    return t;
  }

  public List<ProToken> getHiddenTokens() {
    LinkedList<ProToken> ret = new LinkedList<>();
    ProToken tkn = getHiddenBefore();
    while (tkn != null) {
      ret.addFirst(tkn);
      tkn = tkn.getHiddenBefore();
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
   * @param type ABLNodeType to search for
   * @return Parent node within the current statement of the given type. Null if not found
   */
  public JPNode getParent(ABLNodeType type) {
    if (type == getNodeType())
      return this;
    if (isStateHead())
      return null;
    if (getParent() != null)
      return getParent().getParent(type);
    return null;
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
   * @return True if token is part of an editable section in AppBuilder managed code
   */
  public boolean isEditableInAB() {
    return firstNaturalChild().token.isEditableInAB();
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
  public void setSymbol(ISymbol symbol) {
    setLink(IConstants.SYMBOL, symbol);
  }

  /**
   * Used by TreeParser in order to assign Symbol to the right node
   * Never returns null
   */
  public JPNode getIdNode() {
    // TODO Probably a better way to do that...
    if ((getNodeType() == ABLNodeType.DEFINE) || (getNodeType() == ABLNodeType.BUFFER) || (getNodeType() == ABLNodeType.BEFORETABLE)) {
      for (JPNode child : getDirectChildren()) {
        if (child.getNodeType() == ABLNodeType.ID)
          return child;
      }
      return this;
    } else if ((getNodeType() == ABLNodeType.NEW)|| (getNodeType() == ABLNodeType.OLD)) {
      JPNode nxt = nextNode();
      if ((nxt != null) && (nxt.getNodeType() == ABLNodeType.ID))
        return nxt;
      if ((nxt != null) && (nxt.getNodeType() == ABLNodeType.BUFFER)) {
        nxt = nxt.nextNode();
        if ((nxt != null) && (nxt.getNodeType() == ABLNodeType.ID))
          return nxt;
        else
          return this;
      }
      return this;
    } else if  (getNodeType() == ABLNodeType.TABLEHANDLE) {
      if ((nextNode() != null) && (nextNode().getNodeType() == ABLNodeType.ID))
        return nextNode();
      else return this;
    } else {
      return this;
    }
  }

  /**
   * @return Number total number of JPNode objects 
   */
  public int size() {
    int sz = 1;
    for (JPNode node : getDirectChildren()) {
      sz += node.size();
    }
    return sz;
  }

  /**
   * @return Number total number of natural JPNode objects 
   */
  public int naturalSize() {
    int sz = isNatural() ? 1 : 0;
    for (JPNode node : getDirectChildren()) {
      sz += node.naturalSize();
    }
    return sz;
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    buff.append(token.getNodeType()).append(" \"").append(getText()).append("\" F").append(
        getFileIndex()).append('/').append(getLine()).append(':').append(getColumn());
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
      StringBuilder hiddenText = new StringBuilder();
      ProToken tok = node.getHiddenBefore();
      while (tok != null) {
        if ((tok.getNodeType() == ABLNodeType.COMMENT) || (tok.getNodeType() == ABLNodeType.WS))
          hiddenText.insert(0, tok.getText());
        tok = tok.getHiddenBefore();
      }
      bldr.append(hiddenText.toString());
      bldr.append(node.getText());
    }

    return bldr.toString();
  }

  /**
   * Walk down the tree from the input node
   */
  public void walk(ICallback<?> callback) {
    if (attrGet(IConstants.OPERATOR) == IConstants.TRUE) {
      // Assuming OPERATORs only have two children (which should be the case)
      getFirstChild().walk(callback);
      callback.visitNode(this);
      getFirstChild().getNextSibling().walk(callback);
    } else {
      if (callback.visitNode(this)) {
        for (JPNode child : getDirectChildren()) {
          child.walk(callback);
        }
      }
    }
  }

  public String allLeadingHiddenText() {
    StringBuilder ret = new StringBuilder();
    ProToken t = getHiddenBefore();
    while (t != null) {
      ret.insert(0, t.getText());
      t = t.getHiddenBefore();
    }
    return ret.toString();
  }

  public static class Builder {
    private ProToken tok;
    private ParseTree ctx;
    private Builder right;
    private Builder down;
    private boolean stmt;
    private ABLNodeType stmt2;
    private boolean operator;
    private FieldType tabletype;
    private String className;
    private boolean inline;

    public Builder(ProToken tok) {
      this.tok = tok;
    }

    public Builder(ABLNodeType type) {
      this(new ProToken.Builder(type, "").setSynthetic(true).build());
    }

    public Builder updateToken(ProToken tok) {
      this.tok = tok;
      return this;
    }

    public Builder setRuleNode(ParseTree ctx) {
      this.ctx = ctx;
      return this;
    }

    public Builder setRight(Builder right) {
      this.right = right;
      return this;
    }

    public Builder setDown(Builder down) {
      this.down = down;
      return this;
    }

    public Builder getDown() {
      return down;
    }

    public Builder getRight() {
      return right;
    }

    public Builder changeType(ABLNodeType type) {
      this.tok.setNodeType(type);
      return this;
    }

    public Builder getLast() {
      if (right == null)
        return this;
      return right.getLast();
    }

    public Builder setStatement() {
      this.stmt = true;
      return this;
    }

    public Builder setStatement(ABLNodeType stmt2) {
      this.stmt = true;
      this.stmt2 = stmt2;
      return this;
    }

    public Builder setOperator() {
      this.operator = true;
      return this;
    }

    public Builder setStoreType(FieldType tabletype) {
      this.tabletype = tabletype;
      return this;
    }

    public Builder setClassname(String name) {
      this.className = name;
      return this;
    }

    public ProToken getToken() {
      return tok;
    }

    public ABLNodeType getNodeType() {
      return tok.getNodeType();
    }

    public Builder setInlineVar() {
      this.inline = true;
      return this;
    }

    /**
     * Transforms <pre>x1 - x2 - x3 - x4</pre> into
     * <pre>
     * x1 - x3 - x4
     * |
     * x2
     * </pre>
     * Then to: <pre>
     * x1 - x4
     * |
     * x2 - x3
     * </pre>
     * @return
     */
    public Builder moveRightToDown() {
      if (this.right == null)
        throw new NullPointerException();
      if (this.down == null) {
        this.down = this.right;
        this.right = this.down.right;
        this.down.right = null;
      } else {
        Builder target = this.down;
        while (target.getRight() != null) {
          target = target.getRight();
        }
        target.right = this.right;
        this.right = target.right.right;
        target.right.right = null;
      }

      return this;
    }

    public JPNode build(ParserSupport support) {
      return build(support, null, null);
    }

    private JPNode build(ParserSupport support, JPNode up, JPNode left) {
      JPNode node;
      switch (tok.getNodeType()) {
        case EMPTY_NODE:
          throw new IllegalStateException("Empty node can't generate JPNode");
        case RECORD_NAME:
          node = new RecordNameNode(tok);
          break;
        case FIELD_REF:
          node = new FieldRefNode(tok);
          break;
        case PROGRAM_ROOT:
          node = new ProgramRootNode(tok);
          break;
        case FOR:
          // FOR in 'DEFINE BUFFER x FOR y' is not a BlockNode
          node = stmt ? new BlockNode(tok) : new JPNode(tok);
          break;
        case DO:
        case REPEAT:
        case FUNCTION:
        case PROCEDURE:
        case CONSTRUCTOR:
        case DESTRUCTOR:
        case METHOD:
        case CANFIND:
        case CATCH:
        case ON:
        case PROPERTY_GETTER:
        case PROPERTY_SETTER:
          node = new BlockNode(tok);
          break;
        default:
          node = new JPNode(tok);
          break;
      }
      node.up = up;
      node.left = left;

      if (className != null)
        node.attrSet(IConstants.QUALIFIED_CLASS_INT, className);
      if (stmt)
        node.setStatementHead(stmt2 == null ? 0 : stmt2.getType());
      if (operator)
        node.setOperator();
      if (inline)
        node.attrSet(IConstants.INLINE_VAR_DEF, IConstants.TRUE);
      if (tabletype != null) {
        switch (tabletype) {
          case DBTABLE:
            node.attrSet(IConstants.STORETYPE, IConstants.ST_DBTABLE);
            break;
          case TTABLE:
            node.attrSet(IConstants.STORETYPE, IConstants.ST_TTABLE);
            break;
          case WTABLE:
            node.attrSet(IConstants.STORETYPE, IConstants.ST_WTABLE);
            break;
          case VARIABLE:
            // Never happens
            break;
        }
      }

      if ((ctx != null) && (support != null))
        support.pushNode(ctx, node);
      // Attach first non-empty builder node to node.down
      Builder tmp = down;
      while (tmp != null) {
        if (tmp.getNodeType() == ABLNodeType.EMPTY_NODE) {
          // Safety net: EMPTY_NODE can't have children
          if (tmp.down != null) {
            throw new IllegalStateException("Found EMPTY_NODE with children (first is " + tmp.down.getNodeType());
          }
          tmp = tmp.right;
        } else {
          node.down = tmp.build(support, node, null);
          tmp = null;
        }
      }
      // Same for node.right
      tmp = right;
      while (tmp != null) {
        if (tmp.getNodeType() == ABLNodeType.EMPTY_NODE) {
          // Safety net: EMPTY_NODE can't have children
          if (tmp.down != null) {
            throw new IllegalStateException("Found EMPTY_NODE with children (first is " + tmp.down.getNodeType());
          }
          tmp = tmp.right;
        } else {
          node.right = tmp.build(support, up, node);
          tmp = null;
        }
      }

      return node;
    }
  }

}
