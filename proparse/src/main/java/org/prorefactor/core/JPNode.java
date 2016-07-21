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
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.io.IOException;

import org.prorefactor.proparse.IntegerIndex;
import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser.FieldContainer;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.xfer.DataXferStream;
import org.prorefactor.xfer.Xferable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * Extension to antlr.BaseAST, which allows us to extract an external "antlr" AST view of a Proparse AST, which we can
 * then run tree parsers against. Note that tree transformation functions are currently (Feb 2004) untested and unused,
 * since we tend to only use the AST for analysis and not for code motion.
 */
public class JPNode extends BaseAST implements Xferable {
  private static final long serialVersionUID = 328939790131475436L;

  private int nodeNum = -1;
  private Map<Integer, Integer> attrMap;
  private Map<String, String> attrMapStrings;
  private Map<Integer, Object> linkMap;
  private Map<Integer, String> stringAttributes;
  private JPNode left;
  private JPNode up;
  private ProToken token;

  public enum AttributeKey {
    STORETYPE(IConstants.STORETYPE),
    OPERATOR(IConstants.OPERATOR),
    STATE2(IConstants.STATE2),
    STATEHEAD(IConstants.STATEHEAD),
    PROPARSEDIRECTIVE(IConstants.PROPARSEDIRECTIVE),
    NODE_TYPE_KEYWORD(IConstants.NODE_TYPE_KEYWORD),
    ABBREVIATED(IConstants.ABBREVIATED),
    FULLTEXT(IConstants.FULLTEXT),
    FROM_USER_DICT(IConstants.FROM_USER_DICT),
    INLINE_VAR_DEF(IConstants.INLINE_VAR_DEF),
    QUALIFIED_CLASS(IConstants.QUALIFIED_CLASS_INT);

    private int key;

    private AttributeKey(int key) {
      this.key = key;
    }

    public int getKey() {
      return key;
    }

    public String getName() {
      return name().toLowerCase().replace('_', '-');
    }
  }

  public enum AttributeValue {
    FALSE(IConstants.FALSE),
    TRUE(IConstants.TRUE),
    ST_VARIABLE(IConstants.ST_VAR),
    ST_DBTABLE(IConstants.ST_DBTABLE),
    ST_TTABLE(IConstants.ST_TTABLE),
    ST_WTABLE(IConstants.ST_WTABLE);

    int key;

    private AttributeValue(int key) {
      this.key = key;
    }

    public int getKey() {
      return key;
    }

    public String getName() {
      return name().toLowerCase().replace('_', '-');
    }
  }

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

  public JPNode() {
    this.token = new ProToken(null, 0, "");
  }

  public JPNode(ProToken t) {
    this.token = t;
    setType(t.getType());
  }

  /**
   * Create an node with a given token type. Used extensively by Antlr auto-generated tree constructors.
   */
  public JPNode(int type) {
    this.token = new ProToken(null, type, "");
    setType(type);
  }

  /**
   * If this AST is constructed from another, then create with link to the original.
   */
  public JPNode(int type, JPNode original) {
    this.token = new ProToken(original.token);
    setType(type);
    setLink(IConstants.ORIGINAL, original);
  }

  public JPNode(int type, String text) {
    this.token = new ProToken(null, type, text);
    setType(type);
  }

  /**
   * For temporary nodes for comparison in set of nodes sorted by position
   */
  public JPNode(int file, int line, int column) {
    this.token = new ProToken(null, 0, "", file, line, column, 0);
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

  public int attrGet(int key) {
    if ((attrMap != null) && attrMap.containsKey(key)) {
      return attrMap.get(key);
    }
    switch (key) {
      case IConstants.NODE_TYPE_KEYWORD:
        return NodeTypes.isKeywordType(getType()) ? 1 : 0;
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
        if (NodeTypes.isKeywordType(getType()))
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

  public static void finalizeTrailingHidden(JPNode root) {
    /*
     * The node passed in should be the Program_root. The last child of the Program_root should be the Program_tail, as
     * set by the parser. (See propar.g) We want to find the last descendant of the last child before Program_tail, and
     * then set up Program_tail with that node's hiddenAfter. Program_tail is the holder node for any trailing hidden
     * tokens. This function will have to change slightly if we change the layout of hidden tokens.
     */
    JPNode tailNode = root.firstChild();
    if (tailNode == null || tailNode.getType() == NodeTypes.Program_tail)
      return;
    JPNode lastNode = tailNode;
    while (tailNode != null && tailNode.getType() != NodeTypes.Program_tail) {
      lastNode = tailNode;
      tailNode = tailNode.nextSibling();
    }
    if (tailNode == null || tailNode.getType() != NodeTypes.Program_tail)
      return;
    lastNode = getLastDescendant(lastNode);
    ProToken lastT = lastNode.getHiddenAfter();
    ProToken tempT = lastT;
    while (tempT != null) {
      lastT = tempT;
      tempT = (ProToken) tempT.getHiddenAfter();
    }
    tailNode.setHiddenBefore(lastT);
  }

  /** Find the first hidden token after this node's last descendant. */
  public ProToken findFirstHiddenAfterLastDescendant() {
    // There's no direct way to get a "hidden after" token,
    // so to find the hidden tokens after the current node's last
    // descendant, we find the next sibling of the current node,
    // find the first "natural" descendant of it (if it is not
    // itself natural), and then get its first hidden token.
    JPNode nextNatural = nextSibling();
    if (nextNatural == null)
      return null;
    if (nextNatural.getType() != NodeTypes.Program_tail) {
      nextNatural = nextNatural.firstNaturalChild();
      if (nextNatural == null)
        return null;
    }
    return nextNatural.getHiddenFirst();
  }

  public JPNode firstChild() {
    return (JPNode) down;
  }

  /** Find the first direct child with a given node type. */
  public JPNode findDirectChild(int nodeType) {
    for (JPNode node = firstChild(); node != null; node = node.nextSibling()) {
      if (node.getType() == nodeType)
        return node;
    }
    return null;
  }

  /**
   * First Natural Child is found by repeating firstChild() until a natural node is found. If the start node is a
   * natural node, then it is returned. Note: This is very different than Prolint's "NextNaturalNode" in lintsuper.p.
   * 
   * @see NodeTypes#isNatural(int)
   */
  public JPNode firstNaturalChild() {
    if (NodeTypes.isNatural(getType()))
      return this;
    for (JPNode n = firstChild(); n != null; n = n.firstChild()) {
      if (NodeTypes.isNatural(n.getType()))
        return n;
    }
    return null;
  }

  /** Some nodes like RUN, USER_FUNC, LOCAL_METHOD_REF have a Call object linked to them by TreeParser01. */
  public Call getCall() {
    return (Call) getLink(IConstants.CALL);
  }

  @Override
  public int getColumn() {
    return token.getColumn();
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

  /** Get an ArrayList of the direct children of this node. */
  public List<JPNode> getDirectChildren() {
    List<JPNode> ret = new ArrayList<>();
    JPNode n = this.firstChild();
    while (n != null) {
      ret.add(n);
      n = n.nextSibling();
    }
    return ret;
  }

  /** This variant is primarily for ease of use from ABL. */
  public JPNode[] getDirectChildrenArray() {
    List<JPNode> list = getDirectChildren();
    JPNode[] ret = new JPNode[list.size()];
    list.toArray(ret);
    return ret;
  }

  /**
   * Get the FieldContainer (Frame or Browse) for a statement head node or a frame field reference. This value is set by
   * TreeParser01. Head nodes for statements with the [WITH FRAME | WITH BROWSE] option have this value set. Is also
   * available on the Field_ref node for #(Field_ref INPUT ...) and for #(USING #(Field_ref...)...).
   */
  public FieldContainer getFieldContainer() {
    return (FieldContainer) getLink(IConstants.FIELD_CONTAINER);
  }

  public String getFilename() {
    return token.getFilename();
  }

  /**
   * Get the array of file names. The file at index zero is always the compile unit. The others are include files. The
   * array index position corresponds to JPNode.getFileIndex(). The array is genereated every time this is called, so
   * don't make repeated calls to this.
   */
  public String[] getFilenames() {
    List<String> list = token.getFilenameList().getValues();
    return list.toArray(new String[list.size()]);
  }

  public int getFileIndex() {
    return token.getFileIndex();
  }

  public ProToken getHiddenAfter() {
    return (ProToken) token.getHiddenAfter();
  }

  public ProToken getHiddenBefore() {
    return (ProToken) token.getHiddenBefore();
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

  /** Find the last child of the last child of the... */
  public static JPNode getLastDescendant(JPNode top) {
    JPNode child = top.firstChild();
    if (child == null)
      return top;
    JPNode temp = child;
    while (temp != null) {
      child = temp;
      temp = temp.nextSibling();
    }
    return getLastDescendant(child);
  }

  @Override
  public int getLine() {
    return token.getLine();
  }

  /**
   * Get a link to an arbitrary object. Integers from -200 through -499 are reserved for Joanju.
   */
  public Object getLink(Integer key) {
    if (linkMap == null)
      return null;
    return linkMap.get(key);
  }

  /**
   * Node number, as counted in the syntax tree. If this node was created from PUB or from the "getTree" functions to
   * build the tree from Proparse, then the nodeNum is set. Otherwise, it is -1. The count begins at zero at the
   * Program_root node. The node numbers are simply derived by walking down through the tree, depth first. These node
   * numbers are useful for mapping from externally persistent data back to nodes that are no longer in memory but are
   * instead pulled out of PUB files when needed.
   */
  public int getNodeNum() {
    return nodeNum;
  }

  /** If this AST was constructed from another, then get the original. */
  public JPNode getOriginal() {
    if (linkMap == null)
      return null;
    return (JPNode) linkMap.get(IConstants.ORIGINAL);
  }

  /** Return int[3] of nodes file/line/col. */
  public int[] getPos() {
    return new int[] {getFileIndex(), getLine(), getColumn()};
  }

  /**
   * Source number in the macro tree.
   * 
   * @see org.prorefactor.macrolevel.ListingParser#sourceArray()
   */
  public int getSourceNum() {
    return token.getMacroSourceNum();
  }

  public int getState2() {
    return attrGet(IConstants.STATE2);
  }

  /** Return self if statehead, otherwise returns enclosing statehead. */
  public JPNode getStatement() {
    JPNode n = this;
    while (n != null && !n.isStateHead()) {
      n = n.parent();
    }
    return n;
  }

  /** Every JPNode subtype has its own index. Used for persistent storage. */
  public int getSubtypeIndex() {
    return 1;
  }

  /** Certain nodes will have a link to a Symbol, set by TreeParser01. */
  public Symbol getSymbol() {
    return (Symbol) getLink(IConstants.SYMBOL);
  }

  /**
   * @return The full name of the annotation, or an empty string is node is not an annotation
   */
  public String getAnnotationName() {
    if (getType() != NodeTypes.ANNOTATION)
      return "";
    StringBuilder annName = new StringBuilder(token.getText().substring(1));
    JPNode tok = firstChild();
    while ((tok != null) && (tok.getType() != NodeTypes.PERIOD) && (tok.getType() != NodeTypes.LEFTPAREN)) {
      annName.append(tok.getText());
      tok = (JPNode) tok.getNextSibling();
    }

    return annName.toString();
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
    super.setType(t.getType());
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
    return NodeTypes.isKeywordType(getType()) && NodeTypes.isAbbreviated(getText());
  }

  /**
   * Is this a natural node (from real source text)? If not, then it is a synthetic node, added just for tree structure.
   * 
   * @see NodeTypes#isNatural(int)
   */
  public boolean isNatural() {
    return NodeTypes.isNatural(getType());
  }

  /** Does this node have the Proparse STATEHEAD attribute? */
  public boolean isStateHead() {
    return attrGet(IConstants.STATEHEAD) == IConstants.TRUE;
  }

  /** Return the last immediate child (no grandchildren). */
  public JPNode lastChild() {
    JPNode ret = firstChild();
    if (ret == null)
      return null;
    while (ret.nextSibling() != null)
      ret = ret.nextSibling();
    return ret;
  }

  public JPNode lastDescendant() {
    JPNode ret = lastChild();
    for (JPNode temp = ret; temp != null; temp = ret.lastChild()) {
      ret = temp;
    }
    return ret;
  }

  /** First child if there is one, otherwise next sibling. */
  public JPNode nextNode() {
    if (firstChild() != null)
      return firstChild();
    return nextSibling();
  }

  public JPNode nextSibling() {
    return (JPNode) right;
  }

  @Deprecated
  public JPNode parent() {
    return getParent();
  }

  public JPNode getParent() {
    return up;
  }

  /** Previous sibling if there is one, otherwise parent. */
  public JPNode prevNode() {
    if (up == null)
      return null;
    JPNode n = parent().firstChild();
    if (n == null || n == this)
      return up;
    while (n != null) {
      if (n.nextSibling() == this)
        return n;
      n = n.nextSibling();
    }
    throw new AssertionError("JPNode.prevNode() failed - corrupt tree?");
  }

  public JPNode prevSibling() {
    return left;
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

  /** This variant is primarily for ease of use from ABL. */
  public JPNode[] query(String typeName) {
    return query(NodeTypes.getTypeNum(typeName)).toArray(new JPNode[] {});
  }

  /** Some nodes like RUN, USER_FUNC, LOCAL_METHOD_REF have a Call object linked to them by TreeParser01. */
  public void setCall(Call call) {
    setLink(IConstants.CALL, call);
  }

  /** Used when re-loading serialized nodes. */
  public void setColumn(int column) {
    token.setColumn(column);
  }

  /**
   * Set the comments preceding this node. CAUTION: Does not change any values in Proparse. Only use this if the JPNode
   * tree is "disconnected", because getComments returns the comments from the "hidden tokens" in Proparse in
   * "connected" mode.
   */
  public void setComments(String comments) {
    setLink(IConstants.COMMENTS, comments);
  }

  void setDown(JPNode down) {
    this.down = down;
  }

  /** @see #getFieldContainer() */
  public void setFieldContainer(FieldContainer fieldContainer) {
    setLink(IConstants.FIELD_CONTAINER, fieldContainer);
  }

  /** Used when re-loading serialized nodes. */
  public void setFileIndex(int fileIndex) {
    token.setFileIndex(fileIndex);
  }

  /** A reference to the collection of filenames from the parse. */
  public void setFilenameList(IntegerIndex<String> filenameList) {
    token.setFilenameList(filenameList);
  }

  /** Used when re-loading serialized nodes. */
  public void setLine(int line) {
    token.setLine(line);
  }

  /** @see #getLink(Integer) */
  public void setLink(Integer key, Object value) {
    if (linkMap == null)
      initLinkMap();
    linkMap.put(key, value);
  }

  /** For use by the JPNode tree construction classes only. */
  public void setNodeNum(int nodeNum) {
    this.nodeNum = nodeNum;
  }

  public void setParent(JPNode parent) {
    this.up = parent;
  }

  public void setParentInChildren() {
    for (JPNode child = firstChild(); child != null; child = child.nextSibling()) {
      child.up = this;
    }
  }

  void setRight(JPNode right) {
    this.right = right;
  }

  /** Used when re-loading serialized nodes. */
  public void setSourceNum(int n) {
    token.setMacroSourceNum(n);
  }

  /** Assigned by the tree parser. */
  public void setSymbol(Symbol symbol) {
    setLink(IConstants.SYMBOL, symbol);
  }

  public void setFirstChild(JPNode child) {
    down = child;
  }

  public void setHiddenAfter(ProToken t) {
    token.setHiddenAfter(t);
  }

  public void setHiddenBefore(ProToken t) {
    token.setHiddenBefore(t);
  }

  public void setNextSibling(JPNode sibling) {
    right = sibling;
  }

  public void setPrevSibling(JPNode n) {
    left = n;
  }

  public void setNextSiblingWithLinks(AST n) {
    for (AST next = getNextSibling(); next != null; next = next.getNextSibling()) {
      ((JPNode) next).up = null;
    }
    super.setNextSibling(n);
    for (AST next = getNextSibling(); next != null; next = next.getNextSibling()) {
      ((JPNode) next).up = this.up;
    }
  }

  @Override
  public void setText(String text) {
    token.setText(text);
  }

  @Override
  public void setType(int type) {
    super.setType(type);
    token.setType(type);
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    buff.append(NodeTypes.getTokenName(getType())).append(" \"").append(getText()).append("\" ").append(
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
        int type = t.getType();
        if (type == NodeTypes.COMMENT || type == NodeTypes.WS)
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

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    out.writeInt(getType());
    out.writeInt(getSourceNum());
    out.writeInt(getFileIndex());
    out.writeInt(getLine());
    out.writeInt(getColumn());
    out.writeRef(getText());
    out.writeRef(firstChild());
    out.writeRef(nextSibling());
    out.writeRef(parent());
    out.writeRef(prevSibling());
    out.writeRef(getHiddenTokens());
    out.writeRef(attrMap);
    out.writeRef(stringAttributes);
    out.writeRef(attrMapStrings);
    out.writeInt(nodeNum);
    out.writeRef(linkMap);
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    out.schemaInt("type");
    out.schemaInt("macroSourceNum");
    out.schemaInt("fileIndex");
    out.schemaInt("line");
    out.schemaInt("column");
    out.schemaRef("text");
    out.schemaRef("firstChild");
    out.schemaRef("nextSibling");
    out.schemaRef("parent");
    out.schemaRef("prevSibling");
    out.schemaRef("hiddenTokens");
    out.schemaRef("attributesIntInt");
    out.schemaRef("attributesIntString");
    out.schemaRef("attributesStringString");
    out.schemaInt("nodeNum");
    out.schemaRef("linkMap");
  }

  private static Integer attrEq(String attrName) {
    return attrStrEqs.inverse().get(attrName);
  }

  private static String attrEq(int attrNum) {
    return attrStrEqs.get(attrNum);
  }

}
