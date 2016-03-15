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
package org.prorefactor.treeparser01;

import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.Event;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.TreeParserException;
import org.prorefactor.treeparser.Variable;
import org.prorefactor.widgettypes.Browse;

import antlr.collections.AST;

/**
 * Superclass of empty actions methods for ITreeParserAction. Subclasses can override and implement any of these
 * methods, which are all called directly by TreeParser01. TP01Support is the default implementation.
 */
public class TP01Action implements ITreeParserAction {
  private ParseUnit parseUnit;

  @Override
  public ParseUnit getParseUnit() {
    return parseUnit;
  }

  @Override
  public void setParseUnit(ParseUnit parseUnit) {
    this.parseUnit = parseUnit;
  }

  @Override
  public void addToSymbolScope(Object o) throws TreeParserException {
    // No-op
  }

  @Override
  public void blockBegin(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void blockEnd() throws TreeParserException {
    // No-op
  }

  @Override
  public void browseRef(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void callBegin(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void callEnd() throws TreeParserException {
    // No-op
  }

  @Override
  public void callConstructorBegin(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void callConstructorEnd() throws TreeParserException {
    // No-op
  }

  @Override
  public void callMethodBegin(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void callMethodEnd() throws TreeParserException {
    // No-op
  }

  @Override
  public void canFindBegin(AST ast, AST recordAST) throws TreeParserException {
    // No-op
  }

  @Override
  public void canFindEnd(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void classState(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void clearState(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void datasetTable(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void defAs(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void defExtent(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void defLike(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void defineBuffer(AST ast, AST idAST, AST recAST, boolean init) throws TreeParserException {
    // No-op
  }

  @Override
  public Browse defineBrowse(AST ast, AST idAST) throws TreeParserException {
    // No-op
    return null;
  }

  @Override
  public void defineBufferForTrigger(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public Event defineEvent(AST ast, AST idNode) throws TreeParserException {
    // No-op
    return null;
  }

  @Override
  public Symbol defineSymbol(int symbolType, AST defAST, AST idAST) throws TreeParserException {
    // No-op
    return null;
  }

  @Override
  public Object defineTableFieldInitialize(AST ast) throws TreeParserException {
    // No-op
    return null;
  }

  @Override
  public void defineTableFieldFinalize(Object obj) throws TreeParserException {
    // No-op
  }

  @Override
  public void defineTableLike(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void defineTemptable(AST ast, AST idNode) throws TreeParserException {
    // No-op
  }

  @Override
  public Variable defineVariable(AST ast, AST idNode) throws TreeParserException {
    // No-op
    return null;
  }

  @Override
  public Variable defineVariable(AST ast, AST idAST, int dataType) throws TreeParserException {
    // No-op
    return null;
  }

  @Override
  public Variable defineVariable(AST ast, AST idAST, AST likeAST) throws TreeParserException {
    // No-op
    return null;
  }

  @Override
  public void defineWorktable(AST ast, AST idNode) throws TreeParserException {
    // No-op
  }

  @Override
  public void field(AST ast, AST idAST, ContextQualifier cq, TableNameResolution resolution) throws TreeParserException {
    // No-op
  }

  @Override
  public void fnvFilename(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void fnvExpression(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void formItem(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void frameBlockCheck(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void frameDef(AST ast, AST idAST) throws TreeParserException {
    // No-op
  }

  @Override
  public void frameEnablingStatement(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void frameInitializingStatement(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void frameStatementEnd() throws TreeParserException {
    // No-op
  }

  @Override
  public void frameRef(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void funcBegin(AST ast, AST idAST) throws TreeParserException {
    // No-op
  }

  @Override
  public void funcDef(AST ast, AST idAST) throws TreeParserException {
    // No-op
  }

  @Override
  public void funcForward(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void funcEnd(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void lexat(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void methodBegin(AST ast, AST idAST) throws TreeParserException {
    // No-op
  }

  @Override
  public void methodEnd(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void paramBind() throws TreeParserException {
    // No-op
  }

  @Override
  public void paramEnd() throws TreeParserException {
    // No-op
  }

  @Override
  public void paramExpression(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void paramForCall(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void paramForRoutine(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void paramNoName(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void paramProgressType(int ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void paramSymbol(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void procedureBegin(AST ast, AST id) throws TreeParserException {
    // No-op
  }

  @Override
  public void procedureEnd(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void programRoot(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void programTail() throws TreeParserException {
    // No-op
  }

  @Override
  public void recordNameNode(AST ast, ContextQualifier contextQualifier) throws TreeParserException {
    // No-op
  }

  @Override
  public void routineReturnDatatype(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void runBegin(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void runEnd(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void runInHandle(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void runPersistentSet(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void scopeAdd(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void scopeClose(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void setSymbol(int symbolType, AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void strongScope(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void structorBegin(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void structorEnd(AST ast) throws TreeParserException {
    // No-op
  }

  @Override
  public void viewState(AST ast) throws TreeParserException {
    // No-op
  }

}
