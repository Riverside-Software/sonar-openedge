/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
package org.prorefactor.proparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.proparse.SymbolScope.FieldType;
import org.prorefactor.refactor.RefactorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

import antlr.Token;

/**
 * Helper class when parsing procedure or class.
 * One instance per class being parsed.
 */
public class ParserSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ParserSupport.class);

  private final RefactorSession session;
  private final ClassFinder classFinder;
  // Scope for the compile unit or class. It might be "sub" to a super scope in a class hierarchy
  private final RootSymbolScope unitScope;
  // For later reference
  private final IntegerIndex<String> fileNameList;

  // Current scope might be "unitScope" or an inner method/subprocedure scope
  private SymbolScope currentScope;

  private boolean schemaTablePriority = false;
  private boolean unitIsInterface = false;
  private boolean inDynamicNew = false;

  private Map<String, SymbolScope> funcScopeMap = new HashMap<>();

  private String className = "";

  // Last field referenced. Used for inline defines using LIKE or AS.
  private JPNode lastFieldRefNode;
  private JPNode lastFieldIDNode;

  private ParseTreeProperty<FieldType> recordExpressions = new ParseTreeProperty<>();

  // TODO Only for ANTLR4 migration
  private List<SymbolScope> innerScopes = new ArrayList<>();

  public ParserSupport(RefactorSession session, IntegerIndex<String> fileNameList) {
    this.session = session;
    this.unitScope = new RootSymbolScope(session);
    this.currentScope = unitScope;
    this.classFinder = new ClassFinder(session);
    this.fileNameList = fileNameList;
  }

  public String getClassName() {
    return className;
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
  public int abbrevDatatype(String text) {
    String s = text.toLowerCase();
    if ("cha".startsWith(s))
      return ProParserTokenTypes.CHARACTER;
    if ("da".equals(s) || "dat".equals(s))
      return ProParserTokenTypes.DATE;
    if ("de".equals(s))
      return ProParserTokenTypes.DECIMAL;
    if ("i".equals(s) || "in".equals(s))
      return ProParserTokenTypes.INTEGER;
    if ("logical".startsWith(s))
      return ProParserTokenTypes.LOGICAL;
    if ("rec".equals(s) || "reci".equals(s))
      return ProParserTokenTypes.RECID;
    if ("rowi".equals(s))
      return ProParserTokenTypes.ROWID;
    if ("widget-h".startsWith(s) && s.length() >= 4)
      return ProParserTokenTypes.WIDGETHANDLE;
    return 0;
  }

  public void addInnerScope() {
    currentScope = new SymbolScope(session, currentScope);
    innerScopes.add(currentScope);
  }

  // Functions triggered from proparse.g

  public void defBuffer(String bufferName, String tableName) {
    LOG.trace("defBuffer {} to {}", bufferName, tableName);
    currentScope.defineBuffer(bufferName, tableName);
  }

  void defineClass(JPNode classNode) {
    defineClass(classNode.getFirstChild().getText());
  }

  public void defineClass(String name) {
    LOG.trace("defineClass '{}'", name);
    className = ClassFinder.dequote(name);
    unitScope.attachTypeInfo(session.getTypeInfo(className));
  }

  void defInterface(JPNode interfaceNode) {
    defInterface(interfaceNode.getFirstChild().getText());
  }

  public void defInterface(String name) {
    LOG.trace("defineInterface");
    unitIsInterface = true;
    className = ClassFinder.dequote(name);
  }

  void defMethod(JPNode idNode) {
    // Not used anymore
  }

  public void defTable(String name, SymbolScope.FieldType ttype) {
    // I think the compiler will only allow table defs at the class/unit scope,
    // but we don't need to enforce that here. It'll go in the right spot by the
    // nature of the code.
    currentScope.defineTable(name.toLowerCase(), ttype);
  }

  public void defVar(String name) {
    currentScope.defineVar(name);
  }

  public void defVarInline() {
    if (lastFieldIDNode == null) {
      LOG.warn("Trying to define inline variable, but no ID symbol availble");
    } else {
      currentScope.defineVar(lastFieldIDNode.getText());
      // I'm not sure if this would ever be inheritable. Doesn't hurt to check.
      lastFieldRefNode.attrSet(IConstants.INLINE_VAR_DEF, IConstants.TRUE);
    }
  }

  public void dropInnerScope() {
    assert currentScope != unitScope;
    currentScope = currentScope.getSuperScope();
  }

  public void fieldReference(JPNode refNode, JPNode idNode) {
    lastFieldRefNode = refNode;
    lastFieldIDNode = idNode;
  }

  public void filenameMerge(JPNode node) {
    JPNode currNode = node;
    JPNode nextNode = node.getNextSibling();
    while (nextNode != null) {
      if (currNode.getNodeType() == ABLNodeType.FILENAME && nextNode.getNodeType() == ABLNodeType.FILENAME
          && nextNode.getHiddenBefore() == null) {
        currNode.setHiddenAfter(nextNode.getHiddenAfter());
        currNode.setText(currNode.getText() + nextNode.getText());
        currNode.setNextSibling(nextNode.getNextSibling());
        currNode.updateEndPosition(nextNode.getEndFileIndex(), nextNode.getEndLine(), nextNode.getEndColumn());
        nextNode = currNode.getNextSibling();
        continue;
      }
      currNode = currNode.getNextSibling();
      nextNode = currNode.getNextSibling();
    }
  }

  void funcBegin(JPNode idNode) {
    funcBegin(idNode.getText());
  }

  public void funcBegin(String name) {
    String lowername = name.toLowerCase();
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

  public void funcEnd() {
    currentScope = currentScope.getSuperScope();
  }

  public void usingState(JPNode typeNameNode) {
    classFinder.addPath(typeNameNode.getText());
  }

  public void usingState(String typeName) {
    classFinder.addPath(typeName);
  }

  // End of functions triggered from proparse.g

  public boolean recordSemanticPredicate(org.antlr.v4.runtime.Token lt1,
      org.antlr.v4.runtime.Token lt2, org.antlr.v4.runtime.Token lt3) {
    String recname = lt1.getText();
    if (lt2.getType() == ABLNodeType.NAMEDOT.getType()) {
      recname += ".";
      recname += lt3.getText();
    }
    return (schemaTablePriority ? isTableSchemaFirst(recname.toLowerCase()) : isTable(recname.toLowerCase())) != null;
  }

  public void pushRecordExpression(RuleContext ctx, String recName) {
    recordExpressions.put(ctx, schemaTablePriority ? currentScope.isTableSchemaFirst(recName.toLowerCase())
        : currentScope.isTable(recName.toLowerCase()));
  }

  public FieldType getRecordExpression(RuleContext ctx) {
    return recordExpressions.get(ctx);
  }

  public FieldType isTable(String inName) {
    return currentScope.isTable(inName);
  }

  public FieldType isTableSchemaFirst(String inName) {
    return currentScope.isTableSchemaFirst(inName);
  }

  /** Returns true if the lookahead is a table name, and not a var name. */
  boolean isTableName(Token lt1, Token lt2, Token lt3, Token lt4) {
    String name = lt1.getText();
    if (lt2.getType() == ProParserTokenTypes.NAMEDOT) {
      if (lt4.getType() == ProParserTokenTypes.NAMEDOT) {
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

  /** Returns true if the lookahead is a table name, and not a var name. */
  public boolean isTableNameANTLR4(org.antlr.v4.runtime.Token lt1) {
    int numDots = CharMatcher.is('.').countIn(lt1.getText());
    if (numDots >= 2)
      return false;
    if (isVar(lt1.getText()))
      return false;
    return null != isTable(lt1.getText().toLowerCase());
  }

  public boolean isVar(String name) {
    return currentScope.isVariable(name);
  }

  public int isMethodOrFunc(String name) {
    // Methods and user functions are only at the "unit" (class) scope.
    // Methods can also be inherited from superclasses.
    return unitScope.isMethodOrFunction(name);
  }

  public int isMethodOrFunc(org.antlr.v4.runtime.Token token) {
    if (token == null)
      return 0;
    return unitScope.isMethodOrFunction(token.getText());
  }

  /**
   * @return True if parsing a class or interface
   */
  public boolean isClass() {
    return !Strings.isNullOrEmpty(className);
  }

  /**
   * @return True if parsing an interface
   */
  public boolean isInterface() {
    return unitIsInterface;
  }

  public boolean isSchemaTablePriority() {
    return schemaTablePriority;
  }

  public void setSchemaTablePriority(boolean priority) {
    this.schemaTablePriority = priority;
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

  public String lookupClassName(String text) {
    return classFinder.lookup(text);
  }

  public void attrTypeNameLookup(JPNode node) {
    node.attrSet(IConstants.QUALIFIED_CLASS_INT, classFinder.lookup(node.getText()));
  }

  public void attrTypeName(JPNode node) {
    if (node == null) {
      LOG.error("Unable to assign attribute QUALIFIED_CLASS_INT");
    } else {
      node.attrSet(IConstants.QUALIFIED_CLASS_INT, className);
    }
  }

  public String getFilename(int fileIndex) {
    return fileNameList.getValue(fileIndex);
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
  public static boolean hasHiddenBefore(Token token) {
    return ((ProToken) token).getHiddenBefore() != null;
  }

  public int compareTo(ParserSupport other) {
    if (classFinder.compareTo(other.classFinder) != 0) {
      return 4;
    }
    if (unitScope.compareTo(other.unitScope) != 0) {
      return 3;
    }

    Iterator<SymbolScope> iter1 = innerScopes.iterator();
    Iterator<SymbolScope> iter2 = innerScopes.iterator();
    while (iter1.hasNext() && iter2.hasNext()) {
      if (iter1.next().compareTo(iter2.next()) != 0) {
        return 5;
      }
    }
    if (iter1.hasNext() || iter2.hasNext()) {
      System.err.println("Remaining scopes...");
      return 6;
    }
      
    if (!className.equals(other.className)) {
      System.err.println("Classname: " + className + " -- " + other.className);
      return 1;
    }
    if (unitIsInterface != other.unitIsInterface) {
      System.err.println("Interface: " + unitIsInterface + " -- " + other.unitIsInterface);
      return 2;
      
    }
    return 0;
  }
}
