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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProToken;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;

import antlr.ANTLRException;

public class ParserSupport {

  private boolean currDefInheritable = false;
  private boolean unitIsInterface = false;
  private boolean inDynamicNew = false;

  private ClassFinder classFinder;
  private final RefactorSession session;

  private Map<String, SymbolScope> funcScopeMap = new HashMap<>();

  private String thisClassName = "";

  // We keep a separate scope object for inheritance, because PRIVATE members
  // at the unit scope are not inheritable.
  private SymbolScope inheritanceScope;

  // Current scope might be "unitScope" or an inner method/subprocedure scope.
  private SymbolScope currentScope;

  // This is the scope for the compile unit or class.
  // It might be "sub" to a super scope in a class hierarchy.
  private SymbolScope unitScope;

  // Last field referenced. Used for inline defines using LIKE or AS.
  private JPNode lastFieldRefNode;
  private JPNode lastFieldIDNode;

  ParserSupport(RefactorSession session) {
    unitScope = new SymbolScope(session);
    currentScope = unitScope;
    this.session = session;
    this.classFinder = new ClassFinder(session);
  }

  /**
   * An AS phrase allows further abbreviations on the datatype names. Input a token's text, this returns 0 if it is not
   * a datatype abbreviation, otherwise returns the integer token type for the abbreviation. Here's the normal keyword
   * abbreviation, with what AS phrase allows: char,c. date,da. dec,de. int,i. logical,l. recid,rec. rowid,rowi.
   * widget-h,widg.
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

  public boolean isInDynamicNew() {
    return inDynamicNew;
  }

  public void setInDynamicNew(boolean flag) {
    inDynamicNew = flag;
  }

  /** Mark a node as "operator" */
  static void attrOp(JPNode ast) {
    ast.attrSet(IConstants.OPERATOR, IConstants.TRUE);
  }

  void declareMethod(String s) {
    unitScope.defMethod(s);
  }

  void defBuffer(String bufferName, String tableName) {
    currentScope.defBuffer(bufferName, tableName);
    if (currDefInheritable)
      inheritanceScope.defBuffer(bufferName, tableName);
  }

  void defClass(JPNode classNode) {
    try {
      JPNode idNode = classNode.firstChild();
      thisClassName = ClassFinder.dequote(idNode.getText());

      // We always build an inheritance scope, because the parser is called recursively, and
      // we might only be parsing for the purpose of finding inherite symbols for a subclass.
      // That pointer is stored in the Environment, and that cache of SymbolScopes is cleaned
      // up via a method it provides.
      inheritanceScope = new SymbolScope(session);
      session.addToSuperCache(thisClassName, inheritanceScope);
      inheritanceScope.setScopeName(thisClassName);

      // Does this class have a super class?
      JPNode nextNode = idNode.nextSibling();
      if ((nextNode != null) && nextNode.getType() == NodeTypes.INHERITS) {
        String inheritName = nextNode.firstChild().attrGetS(IConstants.QUALIFIED_CLASS_INT);
        SymbolScope scope = session.lookupSuper(inheritName);
        if (scope == null)
          scope = parseSuper(classNode, inheritName);
        if (scope != null) {
          unitScope.setSuperScope(scope);
          inheritanceScope.setSuperScope(scope);
        }
      }

      // No nested classes (at 10.1), so classes aren't added to inheritance tables.
    } catch (ANTLRException | IOException e) {
      throw new ProparseRuntimeException(e);
    }
  }

  void defMethod(JPNode idNode) {
    String methodName = idNode.getText();
    // Methods can only be defined at the "unit" (class) scope.
    // Next line is redundant: method names were already picked up by the scan-ahead.
    unitScope.defMethod(methodName);
    if (currDefInheritable)
      inheritanceScope.defMethod(methodName);
  }

  void defTable(String name, SymbolScope.FieldType ttype) {
    // I think the compiler will only allow table defs at the class/unit scope,
    // but we don't need to enforce that here. It'll go in the right spot by the
    // nature of the code.
    currentScope.defTable(name, ttype);
    if (currDefInheritable)
      inheritanceScope.defTable(name, ttype);
  }

  void defVar(String name) {
    currentScope.defVar(name);
    if (currDefInheritable)
      inheritanceScope.defVar(name);
  }

  void defVarInline() {
    currentScope.defVar(lastFieldIDNode.getText());
    // I'm not sure if this would ever be inheritable. Doesn't hurt to check.
    if (currDefInheritable)
      inheritanceScope.defVar(lastFieldIDNode.getText());
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

  static boolean hasHiddenAfter(antlr.Token token) {
    return ((ProToken) token).getHiddenAfter() != null;
  }

  static boolean hasHiddenBefore(antlr.Token token) {
    return ((ProToken) token).getHiddenBefore() != null;
  }

  void interfaceNode(JPNode interfaceNode) {
    unitIsInterface = true;
    thisClassName = ClassFinder.dequote(interfaceNode.firstChild().getText());
  }

  boolean isClass() {
    return inheritanceScope != null;
  }

  boolean isInterface() {
    return unitIsInterface;
  }

  SymbolScope.FieldType isTable(String inName) {
    return currentScope.isTable(inName);
  }

  SymbolScope.FieldType isTableSchemaFirst(String inName) {
    return currentScope.isTableSchemaFirst(inName);
  }

  /** Returns true if the lookahead is a table name, and not a var name. */
  boolean isTableName(antlr.Token lt1, antlr.Token lt2, antlr.Token lt3, antlr.Token lt4) {
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
    return currentScope.isVar(name);
  }

  int methodOrFunc(String name) {
    // Methods and user functions are only at the "unit" (class) scope.
    // Methods can also be inherited from superclasses.
    return unitScope.methodOrFunc(name);
  }

  SymbolScope parseSuper(JPNode classNode, String qualSuperName)
      throws IOException, ANTLRException {
    String superFileName = classFinder.findClassFile(qualSuperName);
    if (superFileName.length() == 0) {
      // Could not find the super class. Will happen with Progress.lang.*, vendor libraries, etc.
      return null;
    }

    try {
      ParseUnit unit = new ParseUnit(new File(superFileName), session);
      unit.parse();

      // FIXME Find if this really has to be kept 
      // ParserSupport superSupport = parser.getParserSupport();
      // if (!superSupport.isClass())
      //   throw new ProparseRuntimeException(unitScope.getScopeName() + " inherits " + qualSuperName + " which is not a class."); */
      SymbolScope superScope = session.lookupSuper(qualSuperName);
      if (superScope == null) 
        throw new ProparseRuntimeException("Internal error. parseSuper failed to find superScope.");
      for (SymbolScope p = superScope.getSuperScope(); p != null; p = p.getSuperScope()) {
        if (p == superScope)
          throw new ProparseRuntimeException("Circular inheritance found from class: " + qualSuperName);
      }
      classNode.setLink(IConstants.SUPER_CLASS_TREE, unit.getTopNode());
      return superScope;
    } catch (RefactorException caught) {
      throw new ANTLRException(caught);
    }
  }

  void setCurrDefInheritable(boolean canInherit) {
    currDefInheritable = canInherit && inheritanceScope != null && currentScope == unitScope;
  }

  /** Set the 'store type' attribute on a RECORD_NAME node. */
  void setStoreType(JPNode node, SymbolScope.FieldType tabletype) {
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
    }
  }

  void typenameLookup(JPNode typenameNode) {
    typenameNode.attrSet(IConstants.QUALIFIED_CLASS_INT, classFinder.lookup(typenameNode.getText()));
  }

  void typenameThis(JPNode typenameNode) {
    typenameNode.attrSet(IConstants.QUALIFIED_CLASS_INT, thisClassName);
  }

  void usingState(JPNode typeNameNode) {
    classFinder.addPath(typeNameNode.getText());
  }

}
