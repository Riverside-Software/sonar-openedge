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
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.TreeParserException;
import org.prorefactor.treeparser.symbols.Event;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.widgets.Browse;

import antlr.collections.AST;

/**
 * Actions triggered during the tree parsing phase
 */
public interface ITreeParserAction {
  /**
   * It would be unusual to already have a ParseUnit before calling TP01, since TP01 is usually the first tree parser
   * and it (by default) creates its own ParseUnit. However, after instantiating TP01, you can assign your own ParseUnit
   * before executing the tree parse.
   */
  void setParseUnit(ParseUnit parseUnit);

  ParseUnit getParseUnit() throws TreeParserException;


  /**
   * Add object to current symbol scope
   * 
   * @param o Should be Symbol
   */
  default void addToSymbolScope(Object o) throws TreeParserException { }

  /**
   * Beginning of a DO / FOR / REPEAT block
   * 
   * @param blockAST Reference to the node
   */
  default void blockBegin(AST blockAST) throws TreeParserException { }

  /**
   * End of a DO / FOR / REPEAT block
   */
  default void blockEnd() throws TreeParserException { }

  /**
   * The ID node in a BROWSE ID pair.
   */
  default void browseRef(AST idAST) throws TreeParserException { }

  /**
   * Node where a call occurs, such as USER_FUNC or LOCAL_METHOD_REF
   */
  default void callBegin(AST callAST) throws TreeParserException { }

  /**
   * Triggered once the call's parameters have been processed
   */
  default void callEnd() throws TreeParserException { }

  /**
   * Node where a NEW call occurs
   */
  default void callConstructorBegin(AST ast) throws TreeParserException { }

  default void callConstructorEnd() throws TreeParserException { }

  /**
   * Node where a method call occurs
   */
  default void callMethodBegin(AST ast) throws TreeParserException { }

  default void callMethodEnd() throws TreeParserException { }

  /**
   * The tree parser calls this at the start of a CAN-FIND, because it needs to have its own buffer and buffer scope.
   */
  default void canFindBegin(AST canfindAST, AST recordAST) throws TreeParserException { }

  /**
   * Called by the tree parser at the end of a CAN-FIND
   */
  default void canFindEnd(AST canfindAST) throws TreeParserException { }

  /**
   * Called by the tree parser at the CLASS node
   */
  default void classState(AST classAST, AST abstractKw, AST finalKw, AST serializableKw) throws TreeParserException { }

  /**
   * Called by the tree parser at the INTERFACE node
   */
  default void interfaceState(AST classAST) throws TreeParserException { }

  /**
   * Called at the end of a CLEAR statement
   */
  default void clearState(AST headAST) throws TreeParserException { }

  /**
   * The RECORD_NAME node for a buffer in a DATASET definition
   */
  default void datasetTable(AST tableAST) throws TreeParserException { }

  /**
   * The tree parser calls this at an AS node
   */
  default void defAs(AST asAST) throws TreeParserException { }

  /**
   * Called at an EXTENT node, first child is an expression
   */
  default void defExtent(AST extentAST) throws TreeParserException { }

  /**
   * The tree parser calls this at a LIKE node
   */
  default void defLike(AST likeAST) throws TreeParserException { }

  /**
   * Define a buffer. If the buffer is initialized at the same time it is defined (as in a buffer parameter), then
   * parameter init should be true.
   */
  default void defineBuffer(AST defAST, AST idAST, AST recAST, boolean init) throws TreeParserException { }

  /**
   * Called at the start of a DEFINE BROWSE statement.
   * 
   * @return
   * @param defAST The DEFINE node
   * @param idAST The ID node
   */
  default Browse defineBrowse(AST defAST, AST idAST) throws TreeParserException { return null; }

  /**
   * Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
   * 
   * @param recAST The RECORD_NAME node. Must already have the Table symbol linked to it.
   */
  default void defineBufferForTrigger(AST recAST) throws TreeParserException { }

  /** Called by the tree parser when an event is defined. */
  default Event defineEvent(AST defAST, AST idNode) throws TreeParserException { return null; }

  /**
   * Called by the tree parser to define anything other than buffers, temp/work tables, and variables/parameters.
   */
  default Symbol defineSymbol(int symbolType, AST defAST, AST idAST) throws TreeParserException { return null; }

  /** Called by the tree parser at the beginning of a temp or work table field definition. */
  default Object defineTableFieldInitialize(AST idNode) throws TreeParserException { return null; }

  /** Called by the tree parser at the end of a temp or work table field definition. */
  default void defineTableFieldFinalize(Object obj) throws TreeParserException { }

  /** Called by the tree parser if a LIKE node is encountered in a temp/work table definition. */
  default void defineTableLike(AST recNode) throws TreeParserException { }

  /** Called by the tree parser when a temp-table is defined. */
  default void defineTemptable(AST defAST, AST idNode) throws TreeParserException { }

  /** Called by the tree parser when a variable is defined. */
  default Variable defineVariable(AST defAST, AST idNode) throws TreeParserException { return null; }
  default Variable defineVariable(AST defAST, AST idNode, boolean parameter) throws TreeParserException { return null; }

  /** Some syntaxes imply a data type without LIKE/AS. */
  default Variable defineVariable(AST defAST, AST idAST, int dataType) throws TreeParserException { return null; }
  default Variable defineVariable(AST defAST, AST idAST, int dataType, boolean parameter) throws TreeParserException { return null; }

  /** Some syntaxes have an implicit LIKE. */
  default Variable defineVariable(AST defAST, AST idAST, AST likeAST) throws TreeParserException { return null; }
  default Variable defineVariable(AST defAST, AST idAST, AST likeAST, boolean parameter) throws TreeParserException { return null; }

  /** Called by the tree parser when a work-table is defined. */
  default void defineWorktable(AST defAST, AST idNode) throws TreeParserException { }

  /**
   * Process a Widget_ref node
   * @param widAST The Widget_ref node
   */
  default void widattr(AST widAST, AST idAST, ContextQualifier cq) throws TreeParserException { }

  /**
   * Process a Field_ref node.
   * 
   * @param refAST The Field_ref node.
   * @param idAST The ID node.
   * @param cq What sort of reference is this? Read? Update? Etc.
   * @param resolution Which table must this be a field of?
   */
  default void field(AST refAST, AST idAST, ContextQualifier cq, TableNameResolution resolution) throws TreeParserException { }

  /**
   * Action taken in: filenameorvalue: FILENAME production
   */
  default void fnvFilename(AST fn) throws TreeParserException { }

  /**
   * Action taken in: filenameorvalue: ... expression ... production
   */
  default void fnvExpression(AST exp) throws TreeParserException { }

  /** Called from Form_item node */
  default void formItem(AST ast) throws TreeParserException { }

  /** Called from DO|REPEAT|FOR blocks. */
  default void frameBlockCheck(AST ast) throws TreeParserException { }

  /** Called at tree parser DEFINE FRAME statement. */
  default void frameDef(AST defAST, AST idAST) throws TreeParserException { }

  /** This is a specialization of frameInitializingStatement, called for ENABLE|UPDATE|PROMPT-FOR. */
  default void frameEnablingStatement(AST ast) throws TreeParserException { }

  /** This is called at the beginning of a frame affecting statement, with the statement head node. */
  default void frameInitializingStatement(AST ast) throws TreeParserException { }

  /** This is called at the end of a frame affecting statement. */
  default void frameStatementEnd() throws TreeParserException { }

  /** Called for the ID node in a #(FRAME ID) pair. */
  default void frameRef(AST idAST) throws TreeParserException { }

  /** Called immediately after the ID node in a FUNCTION statement/block. */
  default void funcBegin(AST funcAST, AST idAST) throws TreeParserException { }

  /**
   * Called by the tree parser in a function definition immediately before the code block begins.
   * 
   * @param funcAST The FUNCTION node.
   * @param idAST The ID node (the function name).
   */
  default void funcDef(AST funcAST, AST idAST) throws TreeParserException { }

  /**
   * Called by the tree parser if a FUNCTION statement is found to be any sort of a function FORWARD, IN, or MAP TO.
   * 
   * @param idAST The ID node (name of the function).
   */
  default void funcForward(AST idAST) throws TreeParserException { }

  default void funcEnd(AST funcAST) throws TreeParserException { }

  /** Called at the Field_ref node after a lexical '@' sign in a frame phrase. */
  default void lexat(AST fieldRefAST) throws TreeParserException { }

  /**
   * Called by the tree parser at METHOD statement
   * 
   * @param methodAST The METHOD node
   * @param idAST The ID node (method name)
   */
  default void methodBegin(AST methodAST, AST idAST) throws TreeParserException { }

  default void methodEnd(AST methodAST) throws TreeParserException { }

  /**
   * Called by the tree parser at property GET or SET statement, after scope has been created
   */
  default void propGetSetBegin(AST propAST) throws TreeParserException { }

  default void propGetSetEnd(AST propAST) throws TreeParserException { }

  /**
   * Called by the tree parser at EVENT statement
   * 
   * @param methodAST The EVENT node
   * @param idAST The ID node (event name)
   */
  default void eventBegin(AST eventAST, AST idAST) throws TreeParserException { }

  default void eventEnd(AST eventAST) throws TreeParserException { }

  /** Called if there is a BIND keyword for a parameter. */
  default void paramBind() throws TreeParserException { }

  /** Called at the end of the syntax for a formal arg or a Call's parameter. */
  default void paramEnd() throws TreeParserException { }

  /** An expression being passed as a parameter (as part of a call). */
  default void paramExpression(AST exprAST) throws TreeParserException { }

  /** Called with the direction node (BUFFER|INPUT|OUTPUT|INPUTOUTPUT) for a new call arg. */
  default void paramForCall(AST directionAST) throws TreeParserException { }

  /** Called with the direction node (BUFFER|INPUT|OUTPUT|INPUTOUTPUT) for a new formal arg. */
  default void paramForRoutine(AST directionAST) throws TreeParserException { }

  /** Called for an unnamed parameter, with the datatype or CLASS node. */
  default void paramNoName(AST datatypeAST) throws TreeParserException { }

  /**
   * Called by the treeparser to set the parameter progressType from default VARIABLE to either TEMPTABLE or DATASET.
   */
  default void paramProgressType(int progressType) throws TreeParserException { }

  /** Called with the node that is linked to the Symbol for the current WIP parameter. */
  default void paramSymbol(AST symbolAST) throws TreeParserException { }

  /**
   * Called by the tree parser at the beginning of a PROCEDURESTATE rule.
   */
  default void procedureBegin(AST p, AST id) throws TreeParserException { }

  /**
   * Called by the tree parser at the end of a PROCEDURESTATE rule.
   */
  default void procedureEnd(AST p) throws TreeParserException { }

  /** Called by the tree parser right off the bat, at the Program_root node */
  default void programRoot(AST rootAST) throws TreeParserException { }

  /** Called by the tree parser at the end of the program, after Program_tail. */
  default void programTail() throws TreeParserException { }

  /** Action to take at RECORD_NAME nodes. */
  default void recordNameNode(AST anode, ContextQualifier contextQualifier) throws TreeParserException { }

  /** The datatype node or CLASS node for the return of a FUNCTION or METHOD. */
  default void routineReturnDatatype(AST datatypeAST) throws TreeParserException { }

  /** Action to take at the start of RUNSTATE. */
  default void runBegin(AST t) throws TreeParserException { }

  /** Action to take at the end of RUNSTATE. */
  default void runEnd(AST node) throws TreeParserException { }

  /**
   * Action to take in a RUNSTATE of the form run <pre>in &lt;handle expression&gt;</pre>.
   * 
   * @param hn - the node for &lt;handle expression&gt;.
   */
  default void runInHandle(AST hn) throws TreeParserException { }

  /**
   * Action to take in RUNSTATE of the form run &lt;p&gt; persistent set &lt;handle&gt;.
   * 
   * @param fld - the field node for &lt;handle&gt;.
   */
  default void runPersistentSet(AST fld) throws TreeParserException { }

  /**
   * Called by the tree parser where a symbol scope needs to be added, in other words, in functions, procedures, and
   * triggers.
   * 
   * @param anode The function, procedure, triggers, or on node.
   */
  default void scopeAdd(AST anode) throws TreeParserException { }

  /**
   * Called by the tree parser immediately after the end of a function, procedure, or trigger block (a symbol scope).
   * 
   * @param scopeRootNode The function, procedure, triggers, or on node.
   */
  default void scopeClose(AST scopeRootNode) throws TreeParserException { }

  /** Lookup and assign a symbol to an ID node. */
  default void setSymbol(int symbolType, AST idAST) throws TreeParserException { }

  /**
   * Create a "strong" buffer scope. This is called within a DO FOR or REPEAT FOR statement.
   * 
   * @param anode Is the RECORD_NAME node. It must already have the BufferSymbol linked to it.
   */
  default void strongScope(AST anode) throws TreeParserException { }

  /** Constructor or destructor node. */
  default void structorBegin(AST blockAST) throws TreeParserException { }

  /**
   * End of a constructor or destructor
   */
  default void structorEnd(AST blockAST) throws TreeParserException { }

  /**
   * Called with the VIEW statement head, after the VIEW branch has been traversed
   * @param headAST
   */
  default void viewState(AST headAST) throws TreeParserException { }

  public enum TableNameResolution {
    ANY,
    LAST,
    PREVIOUS;
  }
}
