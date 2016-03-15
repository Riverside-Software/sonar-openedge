/*******************************************************************************
 * Copyright (c) 2003-2015 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
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

public interface ITreeParserAction {
  void addToSymbolScope(Object o) throws TreeParserException;

  /**
   * Beginning of a block
   */
  void blockBegin(AST blockAST) throws TreeParserException;

  /**
   * End of a block
   */
  void blockEnd() throws TreeParserException;

  /**
   * The ID node in a BROWSE ID pair.
   */
  void browseRef(AST idAST) throws TreeParserException;

  /** Node where a call occurs, such as USER_FUNC or LOCAL_METHOD_REF. */
  void callBegin(AST callAST) throws TreeParserException;

  /** Triggered once the call's parameters have been processed. */
  void callEnd() throws TreeParserException;

  /**
   * Node where a NEW call occurs 
   */
  public void callConstructorBegin(AST ast) throws TreeParserException;
  public void callConstructorEnd() throws TreeParserException;

  /**
   * Node where a method call occurs
   */
  public void callMethodBegin(AST ast) throws TreeParserException;
  public void callMethodEnd() throws TreeParserException;

  /**
   * The tree parser calls this at the start of a can-find, because it needs to have its own buffer and buffer scope.
   */
  void canFindBegin(AST canfindAST, AST recordAST) throws TreeParserException;

  /** Called by the tree parser at the end of a can-find. */
  void canFindEnd(AST canfindAST) throws TreeParserException;

  /** Called by the tree parser at the CLASS node. */
  void classState(AST classAST) throws TreeParserException;

  /** Called at the end of a CLEAR statement. */
  void clearState(AST headAST) throws TreeParserException;

  /** The RECORD_NAME node for a buffer in a DATASET definition. */
  void datasetTable(AST tableAST) throws TreeParserException;

  /** The tree parser calls this at an AS node */
  void defAs(AST asAST) throws TreeParserException;

  /** Called at an EXTENT node, first child is an expression. */
  void defExtent(AST extentAST) throws TreeParserException;

  /** The tree parser calls this at a LIKE node */
  void defLike(AST likeAST) throws TreeParserException;

  /**
   * Define a buffer. If the buffer is initialized at the same time it is defined (as in a buffer parameter), then
   * parameter init should be true.
   */
  void defineBuffer(AST defAST, AST idAST, AST recAST, boolean init) throws TreeParserException;

  /** Called at the start of a DEFINE BROWSE statement. */
  Browse defineBrowse(AST defAST, AST idAST) throws TreeParserException;

  /**
   * Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
   * 
   * @param anode The RECORD_NAME node. Must already have the Table symbol linked to it.
   */
  void defineBufferForTrigger(AST recAST) throws TreeParserException;

  /** Called by the tree parser when an event is defined. */
  Event defineEvent(AST defAST, AST idNode) throws TreeParserException;

  /**
   * Called by the tree parser to define anything other than buffers, temp/work tables, and variables/parameters.
   */
  Symbol defineSymbol(int symbolType, AST defAST, AST idAST) throws TreeParserException;

  /** Called by the tree parser at the beginning of a temp or work table field definition. */
  Object defineTableFieldInitialize(AST idNode) throws TreeParserException;

  /** Called by the tree parser at the end of a temp or work table field definition. */
  void defineTableFieldFinalize(Object obj) throws TreeParserException;

  /** Called by the tree parser if a LIKE node is encountered in a temp/work table definition. */
  void defineTableLike(AST recNode) throws TreeParserException;

  /** Called by the tree parser when a temp-table is defined. */
  void defineTemptable(AST defAST, AST idNode) throws TreeParserException;

  /** Called by the tree parser when a variable is defined. */
  Variable defineVariable(AST defAST, AST idNode) throws TreeParserException;

  /** Some syntaxes imply a data type without LIKE/AS. */
  Variable defineVariable(AST defAST, AST idAST, int dataType) throws TreeParserException;

  /** Some syntaxes have an implicit LIKE. */
  Variable defineVariable(AST defAST, AST idAST, AST likeAST) throws TreeParserException;

  /** Called by the tree parser when a work-table is defined. */
  void defineWorktable(AST defAST, AST idNode) throws TreeParserException;

  /**
   * Process a Field_ref node.
   * 
   * @param refAST The Field_ref node.
   * @param idAST The ID node.
   * @param cq What sort of reference is this? Read? Update? Etc.
   * @param resolution Which table must this be a field of?
   */
  void field(AST refAST, AST idAST, ContextQualifier cq, TableNameResolution resolution) throws TreeParserException;

  /**
   * Action taken in: filenameorvalue: FILENAME production
   */
  void fnvFilename(AST fn) throws TreeParserException;

  /**
   * Action taken in: filenameorvalue: ... expression ... production
   */
  void fnvExpression(AST exp) throws TreeParserException;

  /** Called from Form_item node */
  void formItem(AST ast) throws TreeParserException;

  /** Called from DO|REPEAT|FOR blocks. */
  void frameBlockCheck(AST ast) throws TreeParserException;

  /** Called at tree parser DEFINE FRAME statement. */
  void frameDef(AST defAST, AST idAST) throws TreeParserException;

  /** This is a specialization of frameInitializingStatement, called for ENABLE|UPDATE|PROMPT-FOR. */
  void frameEnablingStatement(AST ast) throws TreeParserException;

  /** This is called at the beginning of a frame affecting statement, with the statement head node. */
  void frameInitializingStatement(AST ast) throws TreeParserException;

  /** This is called at the end of a frame affecting statement. */
  void frameStatementEnd() throws TreeParserException;

  /** Called for the ID node in a #(FRAME ID) pair. */
  void frameRef(AST idAST) throws TreeParserException;

  /** Called immediately after the ID node in a FUNCTION statement/block. */
  void funcBegin(AST funcAST, AST idAST) throws TreeParserException;

  /**
   * Called by the tree parser in a function definition immediately before the code block begins.
   * 
   * @param funcAST The FUNCTION node.
   * @param idAST The ID node (the function name).
   */
  void funcDef(AST funcAST, AST idAST) throws TreeParserException;

  /**
   * Called by the tree parser if a FUNCTION statement is found to be any sort of a function FORWARD, IN, or MAP TO.
   * 
   * @param idAST The ID node (name of the function).
   */
  void funcForward(AST idAST) throws TreeParserException;

  void funcEnd(AST funcAST) throws TreeParserException;

  public ParseUnit getParseUnit() throws TreeParserException;

  /** Called at the Field_ref node after a lexical '@' sign in a frame phrase. */
  void lexat(AST fieldRefAST) throws TreeParserException;

  /** Called by the tree parser at METHOD statement, after method's scope has been created. */
  void methodBegin(AST methodAST, AST idAST) throws TreeParserException;

  void methodEnd(AST methodAST) throws TreeParserException;

  /** Called if there is a BIND keyword for a parameter. */
  void paramBind() throws TreeParserException;

  /** Called at the end of the syntax for a formal arg or a Call's parameter. */
  void paramEnd() throws TreeParserException;

  /** An expression being passed as a parameter (as part of a call). */
  void paramExpression(AST exprAST) throws TreeParserException;

  /** Called with the direction node (BUFFER|INPUT|OUTPUT|INPUTOUTPUT) for a new call arg. */
  void paramForCall(AST directionAST) throws TreeParserException;

  /** Called with the direction node (BUFFER|INPUT|OUTPUT|INPUTOUTPUT) for a new formal arg. */
  void paramForRoutine(AST directionAST) throws TreeParserException;

  /** Called for an unnamed parameter, with the datatype or CLASS node. */
  void paramNoName(AST datatypeAST) throws TreeParserException;

  /**
   * Called by the treeparser to set the parameter progressType from default VARIABLE to either TEMPTABLE or DATASET.
   */
  void paramProgressType(int progressType) throws TreeParserException;

  /** Called with the node that is linked to the Symbol for the current WIP parameter. */
  void paramSymbol(AST symbolAST) throws TreeParserException;

  /**
   * Called by the tree parser at the beginning of a PROCEDURESTATE rule.
   */
  void procedureBegin(AST p, AST id) throws TreeParserException;

  /**
   * Called by the tree parser at the end of a PROCEDURESTATE rule.
   */
  void procedureEnd(AST p) throws TreeParserException;

  /** Called by the tree parser right off the bat, at the Program_root node */
  void programRoot(AST rootAST) throws TreeParserException;

  /** Called by the tree parser at the end of the program, after Program_tail. */
  void programTail() throws TreeParserException;

  /** Action to take at RECORD_NAME nodes. */
  void recordNameNode(AST anode, ContextQualifier contextQualifier) throws TreeParserException;

  /** The datatype node or CLASS node for the return of a FUNCTION or METHOD. */
  void routineReturnDatatype(AST datatypeAST) throws TreeParserException;

  /** Action to take at the start of RUNSTATE. */
  void runBegin(AST t) throws TreeParserException;

  /** Action to take at the end of RUNSTATE. */
  void runEnd(AST node) throws TreeParserException;

  /**
   * Action to take in a RUNSTATE of the form run <pre>in &lt;handle expression&gt;</pre>.
   * 
   * @param hn - the node for &lt;handle expression&gt;.
   */
  void runInHandle(AST hn) throws TreeParserException;

  /**
   * Action to take in RUNSTATE of the form run
   * <p>
   * persistent set <handle>.
   * 
   * @param fld - the field node for <handle>.
   */
  void runPersistentSet(AST fld) throws TreeParserException;

  /**
   * Called by the tree parser where a symbol scope needs to be added, in other words, in functions, procedures, and
   * triggers.
   * 
   * @param anode The function, procedure, triggers, or on node.
   */
  void scopeAdd(AST anode) throws TreeParserException;

  /**
   * Called by the tree parser immediately after the end of a function, procedure, or trigger block (a symbol scope).
   * 
   * @param scopeRootNode The function, procedure, triggers, or on node.
   */
  void scopeClose(AST scopeRootNode) throws TreeParserException;

  /**
   * It would be unusual to already have a ParseUnit before calling TP01, since TP01 is usually the first tree parser
   * and it (by default) creates its own ParseUnit. However, after instantiating TP01, you can assign your own ParseUnit
   * before executing the tree parse.
   */
  void setParseUnit(ParseUnit parseUnit);

  /** Lookup and assign a symbol to an ID node. */
  void setSymbol(int symbolType, AST idAST) throws TreeParserException;

  /**
   * Create a "strong" buffer scope. This is called within a DO FOR or REPEAT FOR statement.
   * 
   * @param anode Is the RECORD_NAME node. It must already have the BufferSymbol linked to it.
   */
  void strongScope(AST anode) throws TreeParserException;

  /** Constructor or destructor node. */
  void structorBegin(AST blockAST) throws TreeParserException;

  /** End of a constructor or destructor. */
  void structorEnd(AST blockAST) throws TreeParserException;

  /** Called with the VIEW statement head, after the VIEW branch has been traversed. */
  void viewState(AST headAST) throws TreeParserException;

  public enum TableNameResolution {
    ANY, LAST, PREVIOUS;
  }
}
