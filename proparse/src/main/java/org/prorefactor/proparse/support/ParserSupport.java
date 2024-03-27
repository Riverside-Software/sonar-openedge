/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2024 Riverside Software
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
package org.prorefactor.proparse.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.proparse.support.SymbolScope.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;
import com.progress.xref.CrossReference;
import com.progress.xref.EmptyCrossReference;

/**
 * Helper class when parsing procedure or class. One instance per ParseUnit.
 */
public class ParserSupport {
  private static final Logger LOG = LoggerFactory.getLogger(ParserSupport.class);

  private final IProparseEnvironment session;
  private final ClassFinder classFinder;
  private final CrossReference xref;
  // Scope for the compile unit or class. It might be "sub" to a super scope in a class hierarchy
  private final RootSymbolScope unitScope;

  // Current scope might be "unitScope" or an inner method/subprocedure scope
  private SymbolScope currentScope;
  private List<SymbolScope> innerScopes = new ArrayList<>();
  private Map<RuleContext, SymbolScope> innerScopesMap = new HashMap<>();
  private Map<String, SymbolScope> funcScopeMap = new HashMap<>();

  private boolean schemaTablePriority = false;
  private boolean unitIsAbstract = true;
  private boolean unitIsInterface = false;
  private boolean unitIsEnum = false;
  private boolean allowUnknownMethodCalls = true;

  private String className = "";

  // Last field referenced. Used for inline defines using LIKE or AS.
  private String lastFieldIDStr;

  private ParseTreeProperty<FieldType> recordExpressions = new ParseTreeProperty<>();
  private ParseTreeProperty<JPNode> nodes = new ParseTreeProperty<>();

  public ParserSupport(IProparseEnvironment session, CrossReference xref) {
    this.session = session;
    this.unitScope = new RootSymbolScope(session);
    this.currentScope = unitScope;
    this.classFinder = new ClassFinder(session);
    this.xref = xref == null ? new EmptyCrossReference() : xref;
  }

  public CrossReference getXREF() {
    return xref;
  }

  public String getClassName() {
    return className;
  }

  public IProparseEnvironment getProparseSession() {
    return session;
  }

  // ************************************
  // Functions triggered from proparse.g4
  // ************************************

  public void addInnerScope(RuleContext ctx) {
    currentScope = new SymbolScope(session, currentScope);
    innerScopes.add(currentScope);
    innerScopesMap.put(ctx, currentScope);
  }

  public void defBuffer(String bufferName, String tableName) {
    LOG.trace("defBuffer {} to {}", bufferName, tableName);
    currentScope.defineBuffer(bufferName, tableName);
  }

  public void defineClass(String name) {
    LOG.trace("defineClass '{}'", name);
    className = ClassFinder.dequote(name);
    unitScope.attachTypeInfo(session.getTypeInfo(className));
  }

  public void defineAbstractClass(String name) {
    LOG.trace("defineAbstractClass '{}'", name);
    className = ClassFinder.dequote(name);
    unitIsAbstract = true;
    unitScope.attachTypeInfo(session.getTypeInfo(className));
  }

  public void defineInterface(String name) {
    LOG.trace("defineInterface");
    unitIsInterface = true;
    className = ClassFinder.dequote(name);
  }

  public void defineEnum(String name) {
    LOG.trace("defineEnum");
    unitIsEnum = true;
    className = ClassFinder.dequote(name);
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

  // TODO It probably doesn't make sense anymore to set the 'inline' attribute in ParserSupport.
  // This can probably be handled directly in TreeParserVariableDefinition
  public void defVarInlineAntlr4() {
    if (lastFieldIDStr == null) {
      LOG.warn("Trying to define inline variable, but no ID symbol available");
    } else {
      currentScope.defineInlineVar(lastFieldIDStr);
    }
  }

  public void dropInnerScope() {
    currentScope = currentScope.getSuperScope();
  }

  public void fieldReference(String idNode) {
    lastFieldIDStr = idNode;
  }

  public void funcBegin(String name, RuleContext ctx) {
    String lowername = name.toLowerCase();
    SymbolScope ss = funcScopeMap.get(lowername);
    if (ss != null) {
      currentScope = ss;
    } else {
      currentScope = new SymbolScope(session, currentScope);
      innerScopes.add(currentScope);
      if (ctx != null)
        innerScopesMap.put(ctx, currentScope);
      funcScopeMap.put(lowername, currentScope);
      // User functions are always at the "unit" scope.
      unitScope.defFunc(lowername);
      // User funcs are not inheritable.
    }
  }

  public void funcEnd() {
    currentScope = currentScope.getSuperScope();
  }

  public void usingState(String typeName) {
    classFinder.addPath(typeName);
  }

  public boolean recordSemanticPredicate(Token lt1, Token lt2, Token lt3) {
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

  /** Returns true if the lookahead is a table name, and not a var name. */
  public boolean isTableName(Token token) {
    int numDots = CharMatcher.is('.').countIn(token.getText());
    if (numDots >= 2)
      return false;
    if (isVar(token.getText()))
      return false;
    return null != isTable(token.getText().toLowerCase());
  }

  public boolean isDataTypeVariable(Token token) {
    return ABLNodeType.isValidDatatype(token.getType());
  }

  public boolean isVar(String name) {
    return currentScope.isVariable(name);
  }

  public int isMethodOrFunc(Token token) {
    if (token == null)
      return 0;
    return unitScope.isMethodOrFunction(token.getText());
  }

  public void setSchemaTablePriority(boolean priority) {
    this.schemaTablePriority = priority;
  }

  public void allowUnknownMethodCalls() {
    this.allowUnknownMethodCalls = true;
  }

  public void disallowUnknownMethodCalls() {
    this.allowUnknownMethodCalls = false;
  }

  /**
   * @return False if unknown method calls are not allowed in exprt2 rule. Usually returns true except when in
   *         DYNAMIC-NEW or RUN ... IN|ON statements
   */
  public boolean unknownMethodCallsAllowed() {
    return allowUnknownMethodCalls;
  }

  public boolean hasHiddenBefore(TokenStream stream) {
    int currIndex = stream.index();
    // Obviously no hidden token for first token
    if (currIndex == 0)
      return false;
    // Otherwise see if token is in different channel
    return stream.get(currIndex - 1).getChannel() != Token.DEFAULT_CHANNEL;
  }

  public boolean hasHiddenAfter(TokenStream stream) {
    int currIndex = stream.index();
    // Obviously no hidden token for last token
    if (currIndex == stream.size() - 1)
      return false;
    // Otherwise see if token is in different channel
    return stream.get(currIndex + 1).getChannel() != Token.DEFAULT_CHANNEL;
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

  public boolean isEnum() {
    return unitIsEnum;
  }

  public boolean isAbstractClass() {
    return unitIsAbstract;
  }

  private FieldType isTable(String inName) {
    return currentScope.isTable(inName);
  }

  private FieldType isTableSchemaFirst(String inName) {
    return currentScope.isTableSchemaFirst(inName);
  }

  // *******************************************
  // End of functions triggered from proparse.g4
  // *******************************************

  public void pushNode(ParseTree ctx, JPNode node) {
    nodes.put(ctx, node);
  }

  public JPNode getNode(ParseTree ctx) {
    return nodes.get(ctx);
  }

  public void visitorEnterScope(RuleContext ctx) {
    SymbolScope scope = innerScopesMap.get(ctx);
    if (scope != null) {
      currentScope = scope;
    }
  }

  public void visitorExitScope(RuleContext ctx) {
    SymbolScope scope = innerScopesMap.get(ctx);
    if (scope != null) {
      currentScope = currentScope.getSuperScope();
    }
  }

  public FieldType getRecordExpression(RuleContext ctx) {
    return recordExpressions.get(ctx);
  }

  public void clearRecordExpressions() {
    recordExpressions = new ParseTreeProperty<>();
  }

  public boolean isInlineVar(String name) {
    return currentScope.isInlineVariable(name);
  }

  public int isMethodOrFunc(String name) {
    // Methods and user functions are only at the "unit" (class) scope.
    // Methods can also be inherited from superclasses.
    return unitScope.isMethodOrFunction(name);
  }

  // TODO Speed issue in this function, multiplied JPNode tree generation time by a factor 10
  public String lookupClassName(String text) {
    return classFinder.lookup(text);
  }

}
