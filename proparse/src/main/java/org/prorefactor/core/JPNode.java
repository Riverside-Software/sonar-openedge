/********************************************************************************
 * Copyright (c) 2015-2023 Riverside Software
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
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.tree.ParseTree;
import org.prorefactor.core.nodetypes.AggregateExpression;
import org.prorefactor.core.nodetypes.ArrayReferenceNode;
import org.prorefactor.core.nodetypes.AttributeReferenceNode;
import org.prorefactor.core.nodetypes.BuiltinFunctionNode;
import org.prorefactor.core.nodetypes.CanFindNode;
import org.prorefactor.core.nodetypes.ConstantNode;
import org.prorefactor.core.nodetypes.EnteredFunction;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.IExpression;
import org.prorefactor.core.nodetypes.IStatement;
import org.prorefactor.core.nodetypes.IStatementBlock;
import org.prorefactor.core.nodetypes.IfStatementNode;
import org.prorefactor.core.nodetypes.InUIReferenceNode;
import org.prorefactor.core.nodetypes.LocalMethodCallNode;
import org.prorefactor.core.nodetypes.MethodCallNode;
import org.prorefactor.core.nodetypes.NamedMemberArrayNode;
import org.prorefactor.core.nodetypes.NamedMemberNode;
import org.prorefactor.core.nodetypes.NewTypeNode;
import org.prorefactor.core.nodetypes.NonStatementBlockNode;
import org.prorefactor.core.nodetypes.ProgramRootNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.nodetypes.SingleArgumentExpression;
import org.prorefactor.core.nodetypes.StatementBlockNode;
import org.prorefactor.core.nodetypes.StatementNode;
import org.prorefactor.core.nodetypes.SystemHandleNode;
import org.prorefactor.core.nodetypes.TwoArgumentsExpression;
import org.prorefactor.core.nodetypes.TypeNameNode;
import org.prorefactor.core.nodetypes.UserFunctionCallNode;
import org.prorefactor.core.nodetypes.WidgetNode;
import org.prorefactor.proparse.support.ParserSupport;
import org.prorefactor.proparse.support.SymbolScope.FieldType;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.symbols.FieldContainer;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;

import com.google.common.base.Splitter;

/**
 * TreeNode
 */
public class JPNode {
  private final ProToken token;
  private final JPNode parent;
  private final int childNum;
  @Nullable
  private final List<JPNode> children;

  // Fields are usually set in TreeParser
  private Symbol symbol;
  private FieldContainer container;
  private BufferScope bufferScope;

  private boolean operator;
  // TODO Create subtype for USE-INDEX nodes 
  private boolean invalidUseIndex;

  protected JPNode(ProToken token, JPNode parent, int num, boolean hasChildren) {
    this.token = token;
    this.parent = parent;
    this.childNum = num;
    this.children = hasChildren ? new ArrayList<>() : null;
  }

  public ProToken getToken() {
    return token;
  }

  /**
   * @return True if node was generated from a token in the error stream 
   */
  public boolean isErrorNode() {
    return false;
  }

  // Attributes from ProToken

  /**
   * @see ProToken#getText()
   */
  public String getText() {
    return token.getText();
  }

  /**
   * @see ProToken#getRawText()
   */
  public String getRawText() {
    return token.getRawText();
  }

  /**
   * @see ProToken#getNodeType()
   */
  public ABLNodeType getNodeType() {
    return token.getNodeType();
  }

  /**
   * @see ProToken#getType()
   */
  public int getType() {
    return token.getNodeType().getType();
  }

  /**
   * @see ProToken#getMacroSourceNum()
   */
  public int getSourceNum() {
    return token.getMacroSourceNum();
  }

  /**
   * @see ProToken#getLine()
   */
  public int getLine() {
    return token.getLine();
  }

  /**
   * @see ProToken#getEndLine()
   */
  public int getEndLine() {
    return token.getEndLine();
  }

  /**
   * @see ProToken#getCharPositionInLine()
   */
  public int getColumn() {
    return token.getCharPositionInLine();
  }

  /**
   * @see ProToken#getEndCharPositionInLine()
   */
  public int getEndColumn() {
    return token.getEndCharPositionInLine();
  }

  /**
   * @see ProToken#getFileIndex()
   */
  public int getFileIndex() {
    return token.getFileIndex();
  }

  /**
   * @see ProToken#getFileName()
   */
  public String getFileName() {
    return token.getFileName();
  }

  /**
   * @see ProToken#getEndFileIndex()
   */
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

  @Nonnull
  private List<JPNode> getChildren() {
    return children == null ? new ArrayList<>() : children;
  }

  public int getNumberOfChildren() {
    return children == null ? 0 : children.size();
  }

  public JPNode getFirstChild() {
    return children == null || children.isEmpty() ? null : children.get(0);
  }

  public JPNode getNextSibling() {
    return (parent != null) && (parent.getChildren().size() > childNum + 1) ? parent.getChildren().get(childNum + 1) : null;
  }

  public JPNode getParent() {
    return parent;
  }

  public ProgramRootNode getTopLevelParent() {
    if (parent == null)
      return this instanceof ProgramRootNode ? (ProgramRootNode) this : null;
    else
      return parent.getTopLevelParent();
  }

  /**
   * @return First sibling of given type
   */
  public JPNode getSibling(ABLNodeType type) {
    if (parent == null)
      return null;
    for (JPNode node : parent.children) {
      if (node.getNodeType() == type)
        return node;
    }
    return null;
  }

  /**
   * @return Previous sibling in line before this one
   */
  public JPNode getPreviousSibling() {
    return (childNum > 0) && (parent != null) ? parent.getChildren().get(childNum - 1) : null;
  }

  /**
   * @return First child node associated to a physical token in the source code. If token is an operator, it's not
   *         really the first token, but the operator itself
   */
  public JPNode firstNaturalChild() {
    if (token.isNatural())
      return this;
    if (children != null) {
      for (JPNode ch : children) {
        JPNode natCh = ch.firstNaturalChild();
        if (natCh != null)
          return natCh;
      }
    }

    return null;
  }

  /**
   * @return Last child of the last child of the...
   */
  public JPNode getLastDescendant() {
    if (children == null || children.isEmpty())
      return this;
    return children.get(children.size() - 1).getLastDescendant();
  }

  /**
   * @return First child if there is one, otherwise next sibling
   * @deprecated Use JPNode#getNextNode()
   */
  @Deprecated
  public JPNode nextNode() {
    return getNextNode();
  }

  /**
   * @return First child if there is one, otherwise next sibling, otherwise parent's next sibling, or null if last node
   */
  public JPNode getNextNode() {
    if ((children != null) && !children.isEmpty())
      return children.get(0);

    JPNode tmp = parent;
    int num = childNum;
    while ((tmp.children.size() <= num + 1)) {
      if (tmp.parent == null)
        return null;
      num = tmp.childNum;
      tmp = tmp.parent;
    }
    return tmp.children.get(num + 1);
  }

  /**
   * @return Previous sibling if there is one, otherwise parent
   */
  public JPNode getPreviousNode() {
    return childNum > 0 ? getPreviousSibling() : getParent();
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
  @Nonnull
  public List<JPNode> getDirectChildren() {
    return getChildren();
  }

  /**
   * Use {@link JPNode#findDirectChild(ABLNodeType)}
   * @deprecated 
   */
  @Deprecated
  public JPNode getFirstDirectChild(ABLNodeType type) {
    return findDirectChild(type);
  }

  /**
   * Get a list of the direct children of a given type
   */
  public List<JPNode> getDirectChildren(ABLNodeType type, ABLNodeType... types) {
    if (children == null)
      return new ArrayList<>();
    EnumSet<ABLNodeType> filter = EnumSet.of(type, types);
    return children.stream().filter(node -> filter.contains(node.getNodeType())).collect(Collectors.toList());
  }

  public boolean hasDirectChildOfType(ABLNodeType type) {
    if (children == null)
      return false;
    return children.stream().anyMatch(node -> node.getNodeType() == type);
  }

  public boolean hasDirectChildOfType(ABLNodeType type, ABLNodeType... types) {
    if (children == null)
      return false;
    EnumSet<ABLNodeType> filter = EnumSet.of(type, types);
    return children.stream().anyMatch(node -> filter.contains(node.getNodeType()));
  }


  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> query2(Predicate<JPNode> pred) {
    JPNodePredicateQuery query = new JPNodePredicateQuery(pred);
    walk(query);

    return query.getResult();
  }

  /**
   * @see JPNode#query2(Predicate)
   */
  public List<JPNode> query2(Predicate<JPNode> pred1, Predicate<JPNode> pred2) {
    JPNodePredicateQuery query = new JPNodePredicateQuery(pred1, pred2);
    walk(query);

    return query.getResult();
  }

  /**
   * @see JPNode#query2(Predicate)
   */
  public List<JPNode> query2(Predicate<JPNode> pred1, Predicate<JPNode> pred2, Predicate<JPNode> pred3) {
    JPNodePredicateQuery query = new JPNodePredicateQuery(pred1, pred2, pred3);
    walk(query);

    return query.getResult();
  }

  /**
   * Get an array of all descendant nodes (including this node) of a given type
   */
  public List<JPNode> query2(ABLNodeType type, ABLNodeType... findTypes) {
    final EnumSet<ABLNodeType> tmp = EnumSet.of(type, findTypes);
    JPNodePredicateQuery query = new JPNodePredicateQuery(node -> tmp.contains(node.getNodeType()));
    walk(query);

    return query.getResult();
  }

  /**
   * Does this node contain another node ?
   */
  public boolean contains(JPNode node) {
    if (this == node)
      return true;
    if (children == null)
      return false;
    for (JPNode ch : children) {
      if ((ch == node) || ch.contains(node))
        return true;
    }
    return false;
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
   * Get an array of all expressions
   */
  public List<IExpression> queryExpressions() {
    JPNodeExpressionQuery query = new JPNodeExpressionQuery();
    walk2(query);

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
    if (children == null)
      return null;
    return children.stream().filter(node -> node.getNodeType() == nodeType).findFirst().orElse(null);
  }

  /** Find the first direct child with a given node type. */
  public JPNode findDirectChild(int nodeType) {
    return findDirectChild(ABLNodeType.getNodeType(nodeType));
  }

  // *****************************
  // Various attributes management
  // *****************************

  /**
   * Mark a node as "operator"
   */
  public void setOperator() {
    this.operator = true;
  }

  public boolean isOperator() {
    return operator;
  }

  public void setInvalidUseIndex(boolean invalidUseIndex) {
    this.invalidUseIndex = invalidUseIndex;
  }

  public boolean isInvalidUseIndex() {
    return invalidUseIndex;
  }

  /**
   * @return True is node is an expression
   */
  public boolean isIExpression() {
    return false;
  }

  /**
   * @return Cast to IExpression if isIExpression is true, otherwise null
   */
  public IExpression asIExpression() {
    return null;
  }

  /** Certain nodes will have a link to a Symbol, set by TreeParser. */
  @Nullable
  public Symbol getSymbol() {
    return symbol;
  }

  public boolean hasTableBuffer() {
    return false;
  }

  public boolean hasBufferScope() {
    return bufferScope != null;
  }

  public boolean hasBlock() {
    return false;
  }

  @Nullable
  public Block getBlock() {
    return null;
  }

  @Nullable
  public TableBuffer getTableBuffer() {
    return null;
  }

  /**
   * Get the FieldContainer (Frame or Browse) for a statement head node or a frame field reference. This value is set by
   * TreeParser01. Head nodes for statements with the [WITH FRAME | WITH BROWSE] option have this value set. Is also
   * available on the Field_ref node for #(Field_ref INPUT ...) and for #(USING #(Field_ref...)...).
   */
  public FieldContainer getFieldContainer() {
    return container;
  }

  public BufferScope getBufferScope() {
    return bufferScope;
  }

  public void setSymbol(Symbol symbol) {
    this.symbol = symbol;
  }

  public void setBufferScope(BufferScope scope) {
    this.bufferScope = scope;
  }

  /** @see #getFieldContainer() */
  public void setFieldContainer(FieldContainer fieldContainer) {
    this.container = fieldContainer;
  }

  public void setBlock(Block block) {
    throw new IllegalArgumentException("Not a Block node");
  }

  public void setTableBuffer(TableBuffer buffer) {
    throw new IllegalArgumentException("Not a Block node");
  }

  public boolean hasProparseDirective(String directive) {
    ProToken tok = getHiddenBefore();
    while (tok != null) {
      if (isProlintNoWarn(tok, directive))
        return true;
      tok = tok.getHiddenBefore();
    }
    // If token has been generated by the parser (ie synthetic token), then we look for hidden token attached to the
    // first child
    if (token.isSynthetic()) {
      JPNode child = getFirstChild();
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

  private boolean isProlintNoWarn(ProToken tok, String directive) {
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
    return false;
  }

  public ProToken getHiddenFirst() {
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
    JPNode tok = getFirstChild();
    while ((tok != null) && (tok.getNodeType() != ABLNodeType.PERIOD) && (tok.getNodeType() != ABLNodeType.LEFTPAREN)) {
      annName.append(tok.getText());
      tok = tok.getNextSibling();
    }

    return annName.toString();
  }

  public boolean isAbbreviated() {
    return token.isAbbreviated();
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
    return isStatement();
  }

  /**
   * @deprecated Use JPNode()#asIStatement()#getNodeType2()
   */
  @Deprecated
  public int getState2() {
    return 0;
  }

  public boolean isStatement() {
    return false;
  }

  public boolean isIStatement() {
    return false;
  }

  public boolean isIStatementBlock() {
    return false;
  }

  public IStatement asIStatement() {
    return null;
  }

  public IStatementBlock asIStatementBlock() {
    return null;
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
      JPNode nxt = getNextNode();
      if ((nxt != null) && (nxt.getNodeType() == ABLNodeType.ID))
        return nxt;
      if ((nxt != null) && (nxt.getNodeType() == ABLNodeType.BUFFER)) {
        nxt = nxt.getNextNode();
        if ((nxt != null) && (nxt.getNodeType() == ABLNodeType.ID))
          return nxt;
        else
          return this;
      }
      return this;
    } else if ((getNodeType() == ABLNodeType.TABLEHANDLE) || (getNodeType() == ABLNodeType.DATASETHANDLE)) {
      if ((getNextNode() != null) && (getNextNode().getNodeType() == ABLNodeType.ID))
        return getNextNode();
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

  public String getPositionString() {
    return String.format("F%d/%d:%d", getFileIndex(), getLine(), getColumn());
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
   * COMPILE ... PREPROCESS.
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

  public String toExpressionString() {
    ICallback<List<JPNode>> callback = new FlatListBuilder();
    walk(callback);
    List<JPNode> list = callback.getResult();
    StringBuilder bldr = new StringBuilder();
    for (JPNode node : list) {
      if (node.getHiddenBefore() != null)
        bldr.append(' ');
      bldr.append(node.getText());
    }

    return bldr.toString();
  }

  /**
   * Walk down the tree from the input node
   */
  public void walk(ICallback<?> callback) {
    if (isOperator() || (this instanceof TwoArgumentsExpression)) {
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

  /**
   * Walk down the tree from the input node
   */
  public void walk2(ICallback<?> callback) {
    if (callback.visitNode(this)) {
      for (JPNode child : getDirectChildren()) {
        child.walk2(callback);
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

  public boolean hasAnnotation(String str) {
    return getStatement().hasAnnotation(str);
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
    private String xtra1;
    private boolean expression;
    private boolean block;
    private String blockLabel;
    private boolean error;

    public Builder(ProToken tok) {
      this.tok = tok;
    }

    public Builder(ABLNodeType type) {
      this(new ProToken.Builder(type, "").setSynthetic(true).build());
    }

    public Builder setError(boolean error) {
      this.error = error;
      return this;
    }

    public Builder updateToken(ProToken tok) {
      this.tok = tok;
      return this;
    }

    public Builder setRuleNode(ParseTree ctx) {
      this.ctx = ctx;
      return this;
    }

    public Builder unsetRuleNode() {
      this.ctx = null;
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

    public Builder setExtraField1(String xtra1) {
      this.xtra1 = xtra1;
      return this;
    }

    public Builder setExpression(boolean expression) {
      this.expression = expression;
      return this;
    }

    public Builder setBlock(boolean block) {
      this.block = block;
      return this;
    }

    public Builder setBlockLabel(String label) {
      this.blockLabel = label;
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
      return build(support, null, 0);
    }

    private JPNode build(ParserSupport support, JPNode up, int num) {
      JPNode node;
      boolean hasChildren = (down != null) && ((down.getNodeType() != ABLNodeType.EMPTY_NODE) || down.right != null || down.down != null);
      if (error) {
        return new JPErrorNode(tok, up, num, false);
      } else if (expression) {
        switch (tok.getNodeType()) {
          case UNARY_MINUS:
          case UNARY_PLUS:
          case NOT:
          case PAREN_EXPR:
            node = new SingleArgumentExpression(tok, up, num, hasChildren);
            break;
          case METHOD_REF:
            node = new MethodCallNode(tok, up, num, hasChildren, xtra1);
            break;
          case LOCAL_METHOD_REF:
            node = new LocalMethodCallNode(tok, up, num, hasChildren, xtra1);
            break;
          case USER_FUNC:
            node = new UserFunctionCallNode(tok, up, num, hasChildren, xtra1);
            break;
          case ATTRIBUTE_REF:
            node = new AttributeReferenceNode(tok, up, num, hasChildren, xtra1);
            break;
          case NAMED_MEMBER:
            node = new NamedMemberNode(tok, up, num, hasChildren, xtra1);
            break;
          case NAMED_MEMBER_ARRAY:
            node = new NamedMemberArrayNode(tok, up, num, hasChildren, xtra1);
            break;
          case ARRAY_REFERENCE:
            node = new ArrayReferenceNode(tok, up, num, hasChildren);
            break;
          case IN_UI_REF:
            node = new InUIReferenceNode(tok, up, num, hasChildren);
            break;
          case CONSTANT_REF:
            node = new ConstantNode(tok, up, num, hasChildren);
            break;
          case SYSTEM_HANDLE_REF:
            node = new SystemHandleNode(tok, up, num, hasChildren);
            break;
          case WIDGET_REF:
            node = new WidgetNode(tok, up, num, hasChildren);
            break;
          case BUILTIN_FUNCTION:
            node = new BuiltinFunctionNode(tok, up, num, hasChildren);
            break;
          case NEW_TYPE_REF:
            node = new NewTypeNode(tok, up, num, hasChildren);
            break;
          case FIELD_REF:
            node = new FieldRefNode(tok, up, num, hasChildren);
            break;
          case ENTERED_FUNC:
            node = new EnteredFunction(tok, up, num, hasChildren);
            break;
          case AGGREGATE_EXPRESSION:
            node = new AggregateExpression(tok, up, num, hasChildren);
            break;
          case STAR:
          case MULTIPLY:
          case SLASH:
          case DIVIDE:
          case MODULO:
          case PLUS:
          case MINUS:
          case EQUAL:
          case EQ:
          case GTORLT:
          case NE:
          case RIGHTANGLE:
          case GTHAN:
          case GTOREQUAL:
          case GE:
          case LEFTANGLE:
          case LTHAN:
          case LTOREQUAL:
          case LE:
          case MATCHES:
          case BEGINS:
          case CONTAINS:
          case XOR:
          case AND:
          case OR:
            node = new TwoArgumentsExpression(tok, up, num, hasChildren);
            break;
          default:
            throw new IllegalStateException("Invalid Expression node: " + tok.getNodeType());
        }
      } else {
        switch (tok.getNodeType()) {
          case EMPTY_NODE:
            throw new IllegalStateException("Empty node can't generate JPNode");
          case RECORD_NAME:
            node = new RecordNameNode(tok, up, num, hasChildren);
            if (tabletype != null) {
              switch (tabletype) {
                case DBTABLE:
                  ((RecordNameNode) node).setStoreType(IConstants.ST_DBTABLE);
                  break;
                case TTABLE:
                  ((RecordNameNode) node).setStoreType(IConstants.ST_TTABLE);
                  break;
                case WTABLE:
                  ((RecordNameNode) node).setStoreType(IConstants.ST_WTABLE);
                  break;
                case VARIABLE:
                  // Never happens
                  break;
              }
            }
            break;
          case FIELD_REF:
            node = new FieldRefNode(tok, up, num, hasChildren);
            break;
          case PROGRAM_ROOT:
            node = new ProgramRootNode(tok, up, num, hasChildren, support);
            break;
          case TYPE_NAME:
            node = new TypeNameNode(tok, up, num, hasChildren, className);
            break;
          case CANFIND:
            node = new CanFindNode(tok, up, num, hasChildren);
            break;
          case IF:
            if (stmt)
              node = new IfStatementNode(tok, up, num, hasChildren);
            else
              node = new JPNode(tok, up, num, hasChildren);
            break;
          default:
            if (stmt && block)
              node = new StatementBlockNode(tok, up, num, hasChildren, stmt2, blockLabel);
            else if (stmt)
              node = new StatementNode(tok, up, num, hasChildren, stmt2);
            else if (block)
              node = new NonStatementBlockNode(tok, up, num, hasChildren);
            else
              node = new JPNode(tok, up, num, hasChildren);
            break;
        }
      }

      if (inline && (tok.getNodeType() == ABLNodeType.FIELD_REF))
        ((FieldRefNode) node).setInlineVar(true);
      if (operator)
        node.setOperator();

      if ((ctx != null) && (support != null))
        support.pushNode(ctx, node);
      // Attach first non-empty builder node to node.down
      Builder tmp = down;
      Builder tmpRight = null;
      while (tmp != null) {
        if (tmp.getNodeType() == ABLNodeType.EMPTY_NODE) {
          // Safety net: EMPTY_NODE can't have children
          if (tmp.down != null) {
            throw new IllegalStateException("Found EMPTY_NODE with children (first is " + tmp.down.getNodeType());
          }
          tmp = tmp.right;
        } else {
          node.children.add(tmp.build(support, node, 0));
          tmpRight = tmp.right;
          tmp = null;
        }
      }
      int numCh = 1;
      // Same for node.right
      while (tmpRight != null) {
        if (tmpRight.getNodeType() == ABLNodeType.EMPTY_NODE) {
          // Safety net: EMPTY_NODE can't have children
          if (tmpRight.down != null) {
            throw new IllegalStateException("Found EMPTY_NODE with children (first is " + tmpRight.down.getNodeType());
          }
          tmpRight = tmpRight.right;
        } else {
          node.children.add(tmpRight.build(support, node, numCh++));
          tmpRight = tmpRight.right;
        }
      }

      return node;
    }
  }

}
