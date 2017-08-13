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
package org.prorefactor.proparse;

import java.util.HashMap;
import java.util.Map;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.proparse.SymbolScope.FieldType;
import org.prorefactor.refactor.RefactorSession;

import com.google.common.base.Strings;

import antlr.Token;

/**
 * Helper class when parsing procedure or class.
 * One instance per class being parsed.
 */
public class ParserSupport {
  private final RefactorSession session;
  private final ClassFinder classFinder;
  // Scope for the compile unit or class. It might be "sub" to a super scope in a class hierarchy
  private final RootSymbolScope unitScope;

  // Current scope might be "unitScope" or an inner method/subprocedure scope
  private SymbolScope currentScope;

  private boolean unitIsInterface = false;
  private boolean inDynamicNew = false;

  private Map<String, SymbolScope> funcScopeMap = new HashMap<>();

  private String className = "";

  // Last field referenced. Used for inline defines using LIKE or AS.
  private JPNode lastFieldRefNode;
  private JPNode lastFieldIDNode;

  ParserSupport(RefactorSession session) {
    this.unitScope = new RootSymbolScope(session);
    this.currentScope = unitScope;
    this.session = session;
    this.classFinder = new ClassFinder(session);
  }

  /**
   * An AS phrase allows further abbreviations on the datatype names. Input a token's text, this returns 0 if it is not
   * a datatype abbreviation, otherwise returns the integer token type for the abbreviation. Here's the normal keyword
   * abbreviation, with what AS phrase allows:<ul>
   * <li>char: c
   * <li>date: da
   * <li>dec: de
   * <li>int: i
   * <li>logical: l
   * <li>recid: rec
   * <li>rowid: rowi
   * <li>widget-h: widg
   * </ul>
   */
  int abbrevDatatype(String text) {
    String s = text.toLowerCase();
    if ("cha".startsWith(s))
      return NodeTypes.CHARACTER;
    if ("da".equals(s) || "dat".equals(s))
      return NodeTypes.DATE;
    if ("de".equals(s))
      return NodeTypes.DECIMAL;
    if ("i".equals(s) || "in".equals(s))
      return NodeTypes.INTEGER;
    if ("logical".startsWith(s))
      return NodeTypes.LOGICAL;
    if ("rec".equals(s) || "reci".equals(s))
      return NodeTypes.RECID;
    if ("rowi".equals(s))
      return NodeTypes.ROWID;
    if ("widget-h".startsWith(s) && s.length() >= 4)
      return NodeTypes.WIDGETHANDLE;
    return 0;
  }

  void addInnerScope() {
    currentScope = new SymbolScope(session, currentScope);
  }

  // Functions triggered from proparse.g

  /**
   * When parsing class, all methods are added as part of the classState rule.
   */
  void declareMethod(String s) {
    // Not used anymore
  }

  void defBuffer(String bufferName, String tableName) {
    currentScope.defineBuffer(bufferName, tableName);
  }

  void defineClass(JPNode classNode) {
    JPNode idNode = classNode.firstChild();
    className = ClassFinder.dequote(idNode.getText());
    unitScope.attachTypeInfo(session.getTypeInfo(className));
  }

  void defInterface(JPNode interfaceNode) {
    unitIsInterface = true;
    className = ClassFinder.dequote(interfaceNode.firstChild().getText());
  }

  void defMethod(JPNode idNode) {
    // Not used anymore
  }

  void defTable(String name, SymbolScope.FieldType ttype) {
    // I think the compiler will only allow table defs at the class/unit scope,
    // but we don't need to enforce that here. It'll go in the right spot by the
    // nature of the code.
    currentScope.defineTable(name, ttype);
  }

  void defVar(String name) {
    currentScope.defineVar(name);
  }

  void defVarInline() {
    currentScope.defineVar(lastFieldIDNode.getText());
    // I'm not sure if this would ever be inheritable. Doesn't hurt to check.
    lastFieldRefNode.attrSet(IConstants.INLINE_VAR_DEF, IConstants.TRUE);
  }

  void dropInnerScope() {
    assert currentScope != unitScope;
    currentScope = currentScope.getSuperScope();
  }

  void fieldReference(JPNode refNode, JPNode idNode) {
    lastFieldRefNode = refNode;
    lastFieldIDNode = idNode;
  }

  void filenameMerge(JPNode node) {
    JPNode currNode = node;
    JPNode nextNode = node.nextSibling();
    while (nextNode != null) {
      if (currNode.getType() == NodeTypes.FILENAME && nextNode.getType() == NodeTypes.FILENAME
          && nextNode.getHiddenBefore() == null) {
        currNode.setHiddenAfter(nextNode.getHiddenAfter());
        currNode.setText(currNode.getText() + nextNode.getText());
        currNode.setNextSibling(nextNode.nextSibling());
        nextNode = currNode.nextSibling();
        continue;
      }
      currNode = currNode.nextSibling();
      nextNode = currNode.nextSibling();
    }
  }

  void funcBegin(JPNode idNode) {
    // Check if the function was forward declared.
    String lowername = idNode.getText().toLowerCase();
    SymbolScope ss = funcScopeMap.get(lowername);
    if (ss != null) {
      currentScope = ss;
    } else {
      SymbolScope newScope = new SymbolScope(session, currentScope);
      currentScope = newScope;
      funcScopeMap.put(lowername, newScope);
      // User functions are always at the "unit" scope.
      unitScope.defFunc(lowername);
      // User funcs are not inheritable.
    }
  }

  void funcEnd() {
    currentScope = currentScope.getSuperScope();
  }

  void usingState(JPNode typeNameNode) {
    classFinder.addPath(typeNameNode.getText());
  }

  // End of functions triggered from proparse.g

  FieldType isTable(String inName) {
    return currentScope.isTable(inName);
  }

  FieldType isTableSchemaFirst(String inName) {
    return currentScope.isTableSchemaFirst(inName);
  }

  /** Returns true if the lookahead is a table name, and not a var name. */
  boolean isTableName(Token lt1, Token lt2, Token lt3, Token lt4) {
    String name = lt1.getText();
    if (lt2.getType() == NodeTypes.NAMEDOT) {
      if (lt4.getType() == NodeTypes.NAMEDOT) {
        // Can't be more than one dot (db.table) in a table reference.
        // Maybe this is a field reference, but it sure isn't a table.
        return false;
      }
      name = name + "." + lt3.getText();
    }
    if (isVar(name))
      return false;
    return null != isTable(name.toLowerCase());
  }

  boolean isVar(String name) {
    return currentScope.isVariable(name);
  }

  int isMethodOrFunc(String name) {
    // Methods and user functions are only at the "unit" (class) scope.
    // Methods can also be inherited from superclasses.
    return unitScope.isMethodOrFunction(name);
  }

  /**
   * @return True if parsing a class or interface
   */
  boolean isClass() {
    return !Strings.isNullOrEmpty(className);
  }

  /**
   * @return True if parsing an interface
   */
  boolean isInterface() {
    return unitIsInterface;
  }

  /**
   * @return True if the parser in the middle of a DYNAMIC-NEW statement
   */
  public boolean isInDynamicNew() {
    return inDynamicNew;
  }

  public void setInDynamicNew(boolean flag) {
    inDynamicNew = flag;
  }

  void attrTypeNameLookup(JPNode node) {
    node.attrSet(IConstants.QUALIFIED_CLASS_INT, classFinder.lookup(node.getText()));
  }

  void attrTypeName(JPNode node) {
    node.attrSet(IConstants.QUALIFIED_CLASS_INT, className);
  }

  /**
   * Mark a node as "operator"
   */
  static void attrOp(JPNode node) {
    node.attrSet(IConstants.OPERATOR, IConstants.TRUE);
  }

  /** Set the 'store type' attribute on a RECORD_NAME node. */
  static void setStoreType(JPNode node, FieldType tabletype) {
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

  /**
   * @see ProToken#getHiddenAfter()
   */
  static boolean hasHiddenAfter(Token token) {
    return ((ProToken) token).getHiddenAfter() != null;
  }

  /**
   * @see ProToken#getHiddenBefore()
   */
  static boolean hasHiddenBefore(Token token) {
    return ((ProToken) token).getHiddenBefore() != null;
  }

}
