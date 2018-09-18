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
package org.prorefactor.proparse.antlr4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.RuleNode;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.AttributeKey;
import org.prorefactor.core.AttributeValue;
import org.prorefactor.core.IConstants;
import org.prorefactor.proparse.ParserSupport;
import org.prorefactor.proparse.SymbolScope.FieldType;
import org.prorefactor.proparse.antlr4.nodetypes.BlockNode;
import org.prorefactor.proparse.antlr4.nodetypes.FieldRefNode;
import org.prorefactor.proparse.antlr4.nodetypes.ProgramRootNode;
import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser.symbols.FieldContainer;
import org.prorefactor.treeparser.symbols.Symbol;

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

  /* public String getFilename() {
    return token.getFileIndex();
  } */

  public int getFileIndex() {
    return token.getFileIndex();
  }

  public int getEndFileIndex() {
    return token.getEndFileIndex();
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
    // TODO
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

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   * @deprecated Since 2.1.3, use {@link JPNode#query(ABLNodeType, ABLNodeType...)}
   */
  @Deprecated
  public List<JPNode> query(Integer... findTypes) {
    JPNodeQuery query = new JPNodeQuery(findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   * @deprecated Since 2.1.3, use {@link JPNode#queryMainFile(ABLNodeType, ABLNodeType...)}
   */
  @Deprecated
  public List<JPNode> queryMainFile(Integer... findTypes) {
    JPNodeQuery query = new JPNodeQuery(false, true, null, findTypes);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   * @deprecated Since 2.1.3, use {@link JPNode#queryStateHead(ABLNodeType, ABLNodeType...)}
   */
  @Deprecated
  public List<JPNode> queryStateHead(Integer... findTypes) {
    JPNodeQuery query = new JPNodeQuery(true, findTypes);
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
    // TODO
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
    // TODO
    return null;
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
    // TODO
    return null;
  }

  public List<ProToken> getHiddenTokens() {
    LinkedList<ProToken> ret = new LinkedList<>();
    // TODO
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
    // TODO
    return false;

  }

  /**
   * Is this a natural node (from real source text)? If not, then it is a synthetic node, added just for tree structure.
   */
  public boolean isNatural() {
    // TODO
    return false;
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
    // TODO
    return null;
  }

  public static class Builder {
    private ProToken tok;
    private RuleNode ctx;
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
      this(type, "");
    }

    public Builder(ABLNodeType type, String text) {
      this(new ProToken(type, text));
    }

    public Builder setRuleNode(RuleNode ctx) {
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
      JPNode node;
      switch (tok.getNodeType()) {
        case FIELD_REF:
          node = new FieldRefNode(tok);
          break;
        case PROGRAM_ROOT:
          node = new ProgramRootNode(tok);
          break;

        case PROPERTY_GETTER:
        case PROPERTY_SETTER:
          node = new BlockNode(tok);
          break;
        default:
          node = new JPNode(tok);
          break;
      }
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
      if (down != null) {
        node.down = down.build(support);
        node.down.up = node;
      }
      if (right != null) {
        node.right = right.build(support);
        node.right.left = node;
      }
      if (ctx != null)
        support.pushNode(ctx, node);
      return node;
    }
  }

}
