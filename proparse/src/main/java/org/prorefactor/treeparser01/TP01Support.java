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

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Table;
import org.prorefactor.refactor.RefactorException;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Dataset;
import org.prorefactor.treeparser.Event;
import org.prorefactor.treeparser.Expression;
import org.prorefactor.treeparser.FieldBuffer;
import org.prorefactor.treeparser.FieldLookupResult;
import org.prorefactor.treeparser.Parameter;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.Routine;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.SymbolFactory;
import org.prorefactor.treeparser.SymbolScope;
import org.prorefactor.treeparser.SymbolScopeRoot;
import org.prorefactor.treeparser.SymbolScopeSuper;
import org.prorefactor.treeparser.TableBuffer;
import org.prorefactor.treeparser.TreeParserException;
import org.prorefactor.treeparser.Variable;
import org.prorefactor.widgettypes.Browse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.SemanticException;
import antlr.collections.AST;

/**
 * Provides all functions called by TreeParser01. TreeParser01 does not, itself, define any actions. Instead, it only
 * makes calls to the functions defined in this class.
 */
public class TP01Support extends TP01Action {
  private static final Logger LOG = LoggerFactory.getLogger(TP01Support.class);

  /*
   * Note that blockStack is *only* valid for determining the current block - the stack itself cannot be used for
   * determining a block's parent, buffer scopes, etc. That logic is found within the Block class. Conversely, we cannot
   * use Block.parent to find the current block when we close out a block. That is because a scope's root block parent
   * is always the program block, but a programmer may code a scope into a non-root block... which we need to make
   * current again once done inside the scope.
   */
  private List<Block> blockStack = new ArrayList<>();

  private Block currentBlock;
  private Expression wipExpression;
  private FrameStack frameStack = new FrameStack();
  private Map<String, SymbolScope> funcForwards = new HashMap<>();
  /** There may be more than one WIP call, since a functioncall is a perfectly valid parameter. */
  private Deque<Call> wipCalls = new LinkedList<>();
  /** Since there can be more than one WIP Call, there can be more than one WIP Parameter. */
  private Deque<Parameter> wipParameters = new LinkedList<>();

  private Routine currentRoutine;
  private Routine rootRoutine;
  private RefactorSession refSession;

  /**
   * The symbol last, or currently being, defined. Needed when we have complex syntax like DEFINE id ... LIKE, where we
   * want to track the LIKE but it's not in the same grammar production as the DEFINE.
   */
  private Symbol currSymbol;

  private SymbolScope currentScope;
  private SymbolScopeRoot rootScope;
  private TableBuffer lastTableReferenced;
  private TableBuffer prevTableReferenced;
  private TableBuffer currDefTable;

  // Temporary work-around
  private boolean inDefineEvent = false;

  public TP01Support(RefactorSession session) {
    this.refSession = session;
    rootScope = new SymbolScopeRoot(refSession);
    currentScope = rootScope;
    setParseUnit(new ParseUnit(null, session));
  }

  /** Called at the *end* of the statement that defines the symbol. */
  @Override
  public void addToSymbolScope(Object o) {
    LOG.trace("addToSymbolScope - Adding {} to {}", o, currentScope);
    if (inDefineEvent) return;
    currentScope.add((Symbol) o);
  }

  /** Beginning of a block. */
  @Override
  public void blockBegin(AST blockAST) {
    LOG.trace("Entering blockBegin {}", blockAST);
    BlockNode blockNode = (BlockNode) blockAST;
    currentBlock = pushBlock(new Block(currentBlock, blockNode));
    blockNode.setBlock(currentBlock);
  }

  /** End of a block. */
  @Override
  public void blockEnd() {
    LOG.trace("Entering blockEnd");
    currentBlock = popBlock();
  }

  /** The ID node in a BROWSE ID pair. */
  @Override
  public void browseRef(AST idAST) {
    frameStack.browseRefNode((JPNode) idAST, currentScope);
  }

  @Override
  public void callBegin(AST callAST) {
    LOG.trace("Entering callBegin {}", callAST);
    JPNode callNode = (JPNode) callAST;
    Call call = new Call(callNode);
    callNode.setCall(call);
    wipCalls.addFirst(call);
  }

  @Override
  public void callEnd() {
    // Record the call in the current context.
    currentScope.registerCall(wipCalls.getFirst());
    wipCalls.removeFirst();
  }

  @Override
  public void callConstructorBegin(AST callAST) {
    LOG.trace("Entering callConstructorBegin {}", callAST);
    JPNode callNode = (JPNode) callAST;
    Call call = new Call(callNode);
    callNode.setCall(call);
    wipCalls.addFirst(call);
  }

  @Override
  public void callConstructorEnd() {
    // Record the call in the current context.
    currentScope.registerCall(wipCalls.getFirst());
    wipCalls.removeFirst();
  }

  @Override
  public void callMethodBegin(AST callAST) {
    LOG.trace("Entering callMethodBegin {}", callAST);
    JPNode callNode = (JPNode) callAST;
    Call call = new Call(callNode);
    callNode.setCall(call);
    wipCalls.addFirst(call);
  }

  @Override
  public void callMethodEnd() {
    // Record the call in the current context.
    currentScope.registerCall(wipCalls.getFirst());
    wipCalls.removeFirst();
  }

  /**
   * A CAN-FIND needs to have its own buffer and buffer scope, because CAN-FIND(x where x.y = z) does *not* cause a
   * buffer reference to be created for x within the surrounding block. (Ensuring that the x.y reference does not create
   * a buffer reference was the tricky part.) Also note the behaviour of the 4GL: You can use an existing named buffer
   * within a CAN-FIND, but of course the CAN-FIND does not move any pointers around. We accomplish this by making a
   * local-scoped named buffer using that same name.
   */
  @Override
  public void canFindBegin(AST canfindAST, AST recordAST) {
    RecordNameNode recordNode = (RecordNameNode) recordAST;
    // Keep a ref to the current block...
    Block b = currentBlock;
    // ...create a can-find scope and block (assigns currentBlock)...
    scopeAdd(canfindAST);
    // ...and then set this "can-find block" to use it as its parent.
    currentBlock.setParent(b);
    String buffName = recordAST.getText();
    Table table;
    boolean isDefault;
    TableBuffer tableBuffer = currentScope.lookupBuffer(buffName);
    if (tableBuffer != null) {
      table = tableBuffer.getTable();
      isDefault = tableBuffer.isDefault();
    } else {
      table = refSession.getSchema().lookupTable(buffName);
      isDefault = true;
    }
    TableBuffer newBuff = currentScope.defineBuffer(isDefault ? "" : buffName, table);
    recordNode.setTableBuffer(newBuff);
    currentBlock.addHiddenCursor(recordNode);
  }

  @Override
  public void canFindEnd(AST canfindAST) {
    scopeClose(canfindAST);
  }

  @Override
  public void classState(AST classAST, AST abstractKw, AST finalKw, AST serializableKw) {
    LOG.trace("Entering classState {}", classAST);
    JPNode classNode = (JPNode) classAST;
    JPNode idNode = classNode.firstChild();
    rootScope.setClassName(idNode.getText());
    rootScope.setAbstractClass(abstractKw != null);
    rootScope.setFinalClass(finalKw != null);
    rootScope.setSerializableClass(serializableKw != null);
    if (idNode.nextSibling().getType() == NodeTypes.INHERITS)
      classStateInherits(classNode, idNode.nextSibling().firstChild());
  }

  @Override
  public void interfaceState(AST ast) {
    LOG.trace("Entering interfaceState {}", ast);
    JPNode classNode = (JPNode) ast;
    JPNode idNode = classNode.firstChild();
    rootScope.setClassName(idNode.getText());
    rootScope.setInterface(true);
  };

  @Override
  public void clearState(AST headAST) {
    JPNode headNode = (JPNode) headAST;
    JPNode firstChild = headNode.firstChild();
    if (firstChild.getType() == NodeTypes.FRAME)
      frameStack.simpleFrameInitStatement(headNode, firstChild.nextNode(), currentBlock);
  }

  @Override
  public void datasetTable(AST tableAST) {
    RecordNameNode tableNode = (RecordNameNode) tableAST;
    Dataset dataset = (Dataset) currSymbol;
    dataset.addBuffer(tableNode.getTableBuffer());
  }

  /** The tree parser calls this at an AS node */
  @Override
  public void defAs(AST asAST) {
    JPNode asNode = (JPNode) asAST;
    currSymbol.setAsNode(asNode);
    Primative primative = (Primative) currSymbol;
    JPNode typeNode = asNode.nextNode();
    if (typeNode.getType() == NodeTypes.CLASS)
      typeNode = typeNode.nextNode();
    if (typeNode.getType() == NodeTypes.TYPE_NAME) {
      primative.setDataType(DataType.getDataType(NodeTypes.CLASS));
      primative.setClassName(typeNode);
    } else {
      primative.setDataType(DataType.getDataType(typeNode.getType()));
    }
    assert primative.getDataType() != null : "Failed to set datatype at " + asNode.getFilename() + " line "
        + asNode.getLine();
  }

  @Override
  public void defExtent(AST extentAST) {
    JPNode extentNode = (JPNode) extentAST;
    Primative primative = (Primative) currSymbol;
    JPNode exprNode = extentNode.firstChild();
    // If there is no expression node, then it's an "indeterminate extent".
    // If it's not a numeric literal, then we give up.
    if (exprNode == null || exprNode.getType() != NodeTypes.NUMBER) {
      primative.setExtent(-1);
    } else {
      primative.setExtent(Integer.parseInt(exprNode.getText()));
    }
  }

  /** The tree parser calls this at a LIKE node */
  @Override
  public void defLike(AST likeAST) {
    JPNode likeNode = (JPNode) likeAST;
    currSymbol.setLikeNode(likeNode);
    Primative likePrim = (Primative) likeNode.nextNode().getSymbol();
    Primative newPrim = (Primative) currSymbol;
    if (likePrim != null) {
      newPrim.assignAttributesLike(likePrim);
      assert newPrim.getDataType() != null : "Failed to set datatype at " + likeNode.getFilename() + " line "
          + likeNode.getLine();
    } else {
      LOG.error("Failed to find LIKE datatype at {} line {}", likeNode.getFilename(), likeNode.getLine());
    }
  }

  /** Called at the start of a DEFINE BROWSE statement. */
  @Override
  public Browse defineBrowse(AST defAST, AST idAST) {
    Browse browse = (Browse) defineSymbol(NodeTypes.BROWSE, defAST, idAST);
    frameStack.nodeOfDefineBrowse(browse, (JPNode) defAST);
    return browse;
  }

  /**
   * Define a buffer. If the buffer is initialized at the same time it is defined (as in a buffer parameter), then
   * parameter init should be true.
   */
  @Override
  public void defineBuffer(AST defAST, AST idAST, AST tableAST, boolean init) {
    JPNode idNode = (JPNode) idAST;
    Table table = astTableLink(tableAST);
    TableBuffer bufSymbol = currentScope.defineBuffer(idNode.getText(), table);
    currSymbol = bufSymbol;
    bufSymbol.setDefOrIdNode((JPNode) defAST);
    idNode.setLink(IConstants.SYMBOL, bufSymbol);
    if (init) {
      BufferScope bufScope = currentBlock.getBufferForReference(bufSymbol);
      idNode.setLink(IConstants.BUFFERSCOPE, bufScope);
    }
  }

  /**
   * Define an unnamed buffer which is scoped (symbol and buffer) to the trigger scope/block.
   * 
   * @param tableAST The RECORD_NAME node. Must already have the Table symbol linked to it.
   */
  @Override
  public void defineBufferForTrigger(AST tableAST) {
    Table table = astTableLink(tableAST);
    TableBuffer bufSymbol = currentScope.defineBuffer("", table);
    currentBlock.getBufferForReference(bufSymbol); // Create the BufferScope
    currSymbol = bufSymbol;
  }

  @Override
  public Event defineEvent(AST defAST, AST idAST) {
    JPNode defNode = (JPNode) defAST;
    JPNode idNode = (JPNode) idAST;
    String name = idNode.getText();
    if (name == null || name.length() == 0)
      name = NodeTypes.getTokenName(idNode.getType());
    Event event = new Event(name, currentScope);
    event.setDefOrIdNode(defNode);
    currSymbol = event;
    idNode.setLink(IConstants.SYMBOL, event);
    return event;
  }

  @Override
  public Symbol defineSymbol(int symbolType, AST defAST, AST idAST) {
    /*
     * Some notes: We need to create the Symbol right away, because further actions in the grammar might need to set
     * attributes on it. We can't add it to the scope yet, because of statements like this: def var xyz like xyz. The
     * tree parser is responsible for calling addToScope at the end of the statement or when it is otherwise safe to do
     * so.
     */
    JPNode defNode = (JPNode) defAST;
    JPNode idNode = (JPNode) idAST;
    Symbol symbol = SymbolFactory.create(symbolType, idNode.getText(), currentScope);
    symbol.setDefOrIdNode(defNode);
    currSymbol = symbol;
    idNode.setLink(IConstants.SYMBOL, symbol);
    return symbol;
  }

  /**
   * Defining a table field is done in two steps. The first step creates the field and field buffer but does not assign
   * the field to the table yet. The second step assigns the field to the table. We don't want the field assigned to the
   * table until we're done examining the field options, because we don't want the field available for lookup due to
   * situations like this: def temp-table tt1 field DependentCare like DependentCare.
   * 
   * @return The Object that is expected to be passed as an argument to defineTableFieldFinalize.
   * @see #defineTableFieldFinalize(Object)
   */
  @Override
  public Object defineTableFieldInitialize(AST idAST) {
    JPNode idNode = (JPNode) idAST;
    FieldBuffer fieldBuff = rootScope.defineTableFieldDelayedAttach(idNode.getText(), currDefTable);
    currSymbol = fieldBuff;
    fieldBuff.setDefOrIdNode(idNode);
    idNode.setLink(IConstants.SYMBOL, fieldBuff);
    return fieldBuff;
  }

  @Override
  public void defineTableFieldFinalize(Object obj) {
    ((FieldBuffer) obj).getField().setTable(currDefTable.getTable());
  }

  @Override
  public void defineTableLike(AST tableAST) {
    // Get table for "LIKE table"
    Table table = astTableLink(tableAST);
    // For each field in "table", create a field def in currDefTable
    for (Field field : table.getFieldPosOrder()) {
      rootScope.defineTableField(field.getName(), currDefTable).assignAttributesLike(field);
    }
  }

  public void defineTable(JPNode defNode, JPNode idNode, int storeType) {
    TableBuffer buffer = rootScope.defineTable(idNode.getText(), storeType);
    buffer.setDefOrIdNode(defNode);
    currSymbol = buffer;
    currDefTable = buffer;
    idNode.setLink(IConstants.SYMBOL, buffer);
  }

  @Override
  public void defineTemptable(AST defAST, AST idAST) {
    defineTable((JPNode) defAST, (JPNode) idAST, IConstants.ST_TTABLE);
  }

  @Override
  public Variable defineVariable(AST defAST, AST idAST) {
    /*
     * Some notes: We need to create the Variable Symbol right away, because further actions in the grammar might need
     * to set attributes on it. We can't add it to the scope yet, because of statements like this: def var xyz like xyz.
     * The tree parser is responsible for calling addToScope at the end of the statement or when it is otherwise safe to
     * do so.
     */
    JPNode defNode = (JPNode) defAST;
    JPNode idNode = (JPNode) idAST;
    String name = idNode.getText();
    if (name == null || name.length() == 0) {
      /*
       * Variable Name: There was a subtle bug here when parsing trees extracted from PUB files. In PUB files, the text
       * of keyword nodes are not stored. But in the case of an ACCUMULATE statement -> aggregatephrase ->
       * aggregate_opt, we are defining variable/symbols using the COUNT|MAXIMUM|TOTAL|whatever node. I added a check
       * for empty text from the "id" node.
       */
      name = NodeTypes.getTokenName(idNode.getType());
    }
    Variable variable = new Variable(name, currentScope);
    variable.setDefOrIdNode(defNode);
    currSymbol = variable;
    idNode.setLink(IConstants.SYMBOL, variable);
    return variable;
  }

  @Override
  public Variable defineVariable(AST defAST, AST idAST, int dataType) {
    assert dataType != NodeTypes.CLASS;
    Variable v = defineVariable(defAST, idAST);
    v.setDataType(DataType.getDataType(dataType));
    return v;
  }

  @Override
  public Variable defineVariable(AST defAST, AST idAST, AST likeAST) {
    Variable v = defineVariable(defAST, idAST);
    FieldRefNode likeRefNode = (FieldRefNode) likeAST;
    v.setDataType(likeRefNode.getDataType());
    v.setClassName(likeRefNode.getClassName());
    return v;
  }

  @Override
  public void defineWorktable(AST defAST, AST idAST) {
    defineTable((JPNode) defAST, (JPNode) idAST, IConstants.ST_WTABLE);
  }

  @Override
  public void field(AST refAST, AST idAST, ContextQualifier cq, TableNameResolution resolution) throws TreeParserException {
    JPNode idNode = (JPNode) idAST;
    FieldRefNode refNode = (FieldRefNode) refAST;
    String name = idNode.getText();
    FieldLookupResult result = null;

    refNode.attrSet(IConstants.CONTEXT_QUALIFIER, cq.toString());

    // Check if this is a Field_ref being "inline defined"
    // If so, we define it right now.
    if (refNode.attrGet(IConstants.INLINE_VAR_DEF) == 1)
      addToSymbolScope(defineVariable(idAST, idAST));

    if ((refNode.getParent().getType() == NodeTypes.USING && refNode.getParent().getParent().getType() == NodeTypes.RECORD_NAME)
        || (refNode.firstChild().getType() == NodeTypes.INPUT &&
            (refNode.nextSibling() == null || refNode.nextSibling().getType() != NodeTypes.OBJCOLON))) {
      // First condition : there seems to be an implicit INPUT in USING phrases in a record phrase.
      // Second condition :I've seen at least one instance of "INPUT objHandle:attribute" in code,
      // which for some reason compiled clean. As far as I'm aware, the INPUT was
      // meaningless, and the compiler probably should have complained about it.
      // At any rate, the handle:attribute isn't an input field, and we don't want
      // to try to look up the handle using frame field rules.
      // Searching the frames for an existing INPUT field is very different than
      // the usual field/variable lookup rules. It is done based on what is in
      // the referenced FRAME or BROWSE, or what is found in the frames most
      // recently referenced list.
      result = frameStack.inputFieldLookup(refNode, currentScope);
    } else if (resolution == TableNameResolution.ANY) {
      // Lookup the field, with special handling for FIELDS/USING/EXCEPT phrases
      boolean getBufferScope = (cq != ContextQualifier.SYMBOL);
      result = currentBlock.lookupField(name, getBufferScope);
    } else {
      // If we are in a FIELDS phrase, then we know which table the field is from.
      // The field lookup in Table expects an unqualified name.
      String[] parts = name.split("\\.");
      String fieldPart = parts[parts.length - 1];
      TableBuffer ourBuffer = resolution == TableNameResolution.PREVIOUS ? prevTableReferenced : lastTableReferenced;
      Field field = ourBuffer.getTable().lookupField(fieldPart);
      if (field == null) {
        // The OpenEdge compiler seems to ignore invalid tokens in a FIELDS phrase.
        // As a result, some questionable code will fail to parse here if we don't also ignore those here.
        // Sigh. This would be a good lint rule.
        int parentType = refNode.getParent().getType();
        if (parentType == NodeTypes.FIELDS || parentType == NodeTypes.EXCEPT)
          return;
        throw new TreeParserException(
            idNode.getFilename() + ":" + idNode.getLine() + " Unknown field or variable name: " + fieldPart);
      }
      FieldBuffer fieldBuffer = ourBuffer.getFieldBuffer(field);
      result = new FieldLookupResult();
      result.field = fieldBuffer;
    }

    // TODO Once we've added static member resolution, we can re-add this test.
    if (result == null)
      return;
    // if (result == null)
    // throw new Error(
    // idNode.getFilename()
    // + ":"
    // + idNode.getLine()
    // + " Unknown field or variable name: " + name
    // );

    if (result.isUnqualified)
      refNode.attrSet(IConstants.UNQUALIFIED_FIELD, IConstants.TRUE);
    if (result.isAbbreviated)
      refNode.attrSet(IConstants.ABBREVIATED, IConstants.TRUE);
    // Variable
    if (result.variable != null) {
      refNode.setSymbol(result.variable);
      refNode.attrSet(IConstants.STORETYPE, IConstants.ST_VAR);
      result.variable.noteReference(cq);
    }
    // FieldLevelWidget
    if (result.fieldLevelWidget != null) {
      refNode.setSymbol(result.fieldLevelWidget);
      refNode.attrSet(IConstants.STORETYPE, IConstants.ST_VAR);
      result.fieldLevelWidget.noteReference(cq);
    }
    // Buffer attributes
    if (result.bufferScope != null) {
      refNode.setBufferScope(result.bufferScope);
    }
    // Table field
    if (result.field != null) {
      refNode.setSymbol(result.field);
      refNode.attrSet(IConstants.STORETYPE, result.field.getField().getTable().getStoretype());
      result.field.noteReference(cq);
    }
    // Event
    if (result.event != null) {
      refNode.setSymbol(result.event);
      refNode.attrSet(IConstants.STORETYPE, IConstants.ST_VAR);
      result.event.noteReference(cq);
    }

  } // field()

  /**
   * Called by the tree parser at filenameorvalue: VALUE(expression), passing in the expression node. Partly implemented
   * for Calls and Routines.
   * 
   * @author pcd
   */
  @Override
  public void fnvExpression(AST node) {
    wipExpression = new Expression((JPNode) node);
  }

  /**
   * Called by the tree parser for filenameorvalue: FILENAME production Partly implemented for Calls and Routines.
   * 
   * @author pcd
   */
  @Override
  public void fnvFilename(AST node) {
    Expression exp = new Expression((JPNode) node);
    exp.setValue(node.getText());
    wipExpression = exp;
  }

  /** Called from Form_item node */
  @Override
  public void formItem(AST ast) {
    frameStack.formItem((JPNode) ast);
  }

  /** Called from DO|REPEAT|FOR blocks. */
  @Override
  public void frameBlockCheck(AST ast) {
    frameStack.nodeOfBlock((JPNode) ast, currentBlock);
  }

  /** Called at tree parser DEFINE FRAME statement. */
  @Override
  public void frameDef(AST defAST, AST idAST) {
    frameStack.nodeOfDefineFrame((JPNode) defAST, (JPNode) idAST, currentScope);
  }

  /** This is a specialization of frameInitializingStatement, called for ENABLE|UPDATE|PROMPT-FOR. */
  @Override
  public void frameEnablingStatement(AST ast) {
    // Flip this flag before calling nodeOfInitializingStatement.
    frameStack.statementIsEnabler();
    frameStack.nodeOfInitializingStatement((JPNode) ast, currentBlock);
  }

  /** This is called at the beginning of a frame affecting statement, with the statement head node. */
  @Override
  public void frameInitializingStatement(AST ast) {
    frameStack.nodeOfInitializingStatement((JPNode) ast, currentBlock);
  }

  /** This is called at the end of a frame affecting statement. */
  @Override
  public void frameStatementEnd() {
    frameStack.statementEnd();
  }

  @Override
  public void frameRef(AST idAST) {
    frameStack.frameRefNode((JPNode) idAST, currentScope);
  }

  @Override
  public void funcBegin(AST funcAST, AST idAST) {
    // John: Need some comments here. Why don't I just fetch any
    // function forward scope right away? Why wait until funcDef()?
    // Why bother with a funcForward map specifically, rather than
    // just a funcScope map generally?
    scopeAdd(funcAST);
    JPNode idNode = (JPNode) idAST;
    BlockNode blockNode = (BlockNode) idNode.getParent();
    SymbolScope definingScope = currentScope.getParentScope();
    Routine r = new Routine(idAST.getText(), definingScope, currentScope);
    r.setProgressType(NodeTypes.FUNCTION);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    definingScope.add(r);
    currentRoutine = r;
  }

  @Override
  public void funcDef(AST funcAST, AST idAST) {
    /*
     * If this function definition had a function forward declaration, then we use the block and scope from that
     * declaration, in case it is where the parameters were defined. (You can define the params in the FORWARD, and
     * leave them out at the body.)
     *
     * However, if this statement re-defines the formal args, then we use this statement's scope - because the formal
     * arg names from here will be in effect rather than the names from the FORWARD. (The names don't have to match.)
     */
    if (currentRoutine.getParameters().size() > 0)
      return;
    SymbolScope forwardScope = funcForwards.get(idAST.getText());
    if (forwardScope != null) {
      Routine routine = (Routine) forwardScope.getRootBlock().getNode().getSymbol();
      scopeSwap(forwardScope);
      BlockNode blocknode = (BlockNode) funcAST;
      blocknode.setBlock(currentBlock);
      blocknode.setSymbol(routine);
      routine.setDefOrIdNode(blocknode);
      currentRoutine = routine;
    }
  }

  @Override
  public void funcEnd(AST funcAST) {
    scopeClose(funcAST);
    currentRoutine = rootRoutine;
  }

  @Override
  public void funcForward(AST idAST) {
    funcForwards.put(idAST.getText(), currentScope);
  }

  @Override
  public void lexat(AST fieldRefAST) {
    frameStack.lexAt((JPNode) fieldRefAST);
  }

  @Override
  public void methodBegin(AST blockAST, AST idAST) {
    scopeAdd(blockAST);
    JPNode idNode = (JPNode) idAST;
    BlockNode blockNode = (BlockNode) idNode.getParent();
    SymbolScope definingScope = currentScope.getParentScope();
    Routine r = new Routine(idAST.getText(), definingScope, currentScope);
    r.setProgressType(NodeTypes.METHOD);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    definingScope.add(r);
    currentRoutine = r;
  }

  @Override
  public void methodEnd(AST blockAST) {
    scopeClose(blockAST);
    currentRoutine = rootRoutine;
  }

  @Override
  public void eventBegin(AST eventAST, AST idAST) throws TreeParserException {
    this.inDefineEvent = true;
  }

  @Override
  public void eventEnd(AST eventAST) throws TreeParserException {
    this.inDefineEvent = false;
  }

  @Override
  public void paramBind() {
    wipParameters.getFirst().setBind(true);
  }

  @Override
  public void paramEnd() {
    wipParameters.removeFirst();
  }

  @Override
  public void paramExpression(AST exprAST) {
    JPNode exprNode = (JPNode) exprAST;
    // The expression may or may not be a Field_ref node with a symbol. We don't dig any deeper.
    // As a result, the symbol for an expression parameter might be null.
    wipParameters.getFirst().setSymbol(exprNode.getSymbol());
  }

  @Override
  public void paramForCall(AST directionAST) {
    Parameter param = new Parameter();
    param.setDirectionNode((JPNode) directionAST);
    wipParameters.addFirst(param);
    wipCalls.getFirst().addParameter(param);
  }

  @Override
  public void paramForRoutine(AST directionAST) {
    LOG.trace("Adding parameter {} for routine {}", directionAST.getText(), currentRoutine.fullName());
    Parameter param = new Parameter();
    param.setDirectionNode((JPNode) directionAST);
    wipParameters.addFirst(param);
    currentRoutine.addParameter(param);
  }

  /**
   * Called for a parameter with no identifier. You may have a parameter that has no name, which means that it is a
   * formal argument that is unused in the function/method. (Also possible in some calls to specify {ID AS datatype}.)
   * However, we *do* need to have a WIP Symbol that can be assigned to the Parameter object, get any EXTENT assigned to
   * it, etc.
   * 
   * @param datatypeAST The node of the datatype, might be a CLASS node.
   */
  @Override
  public void paramNoName(AST datatypeAST) {
    Variable variable = new Variable("", currentScope);
    currSymbol = variable;
    JPNode typeNode = (JPNode) datatypeAST;
    if (typeNode.getType() == NodeTypes.CLASS)
      typeNode = typeNode.nextNode();
    if (typeNode.getType() == NodeTypes.TYPE_NAME) {
      variable.setDataType(DataType.getDataType(NodeTypes.CLASS));
      variable.setClassName(typeNode);
    } else {
      variable.setDataType(DataType.getDataType(typeNode.getType()));
    }
  }

  @Override
  public void paramProgressType(int progressType) {
    wipParameters.getFirst().setProgressType(progressType);
  }

  @Override
  public void paramSymbol(AST symbolAST) {
    wipParameters.getFirst().setSymbol(((JPNode) symbolAST).getSymbol());
  }

  @Override
  public void procedureBegin(AST procAST, AST idAST) {
    BlockNode blockNode = (BlockNode) procAST;
    SymbolScope definingScope = currentScope;
    scopeAdd(blockNode);
    Routine r = new Routine(idAST.getText(), definingScope, currentScope);
    r.setProgressType(NodeTypes.PROCEDURE);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    definingScope.add(r);
    currentRoutine = r;
  }

  @Override
  public void procedureEnd(AST node) {
    scopeClose(node);
    currentRoutine = rootRoutine;
  }

  @Override
  public void programRoot(AST rootAST) {
    BlockNode blockNode = (BlockNode) rootAST;
    currentBlock = pushBlock(new Block(rootScope, blockNode));
    rootScope.setRootBlock(currentBlock);
    blockNode.setBlock(currentBlock);
    getParseUnit().setTopNode(blockNode);
    getParseUnit().setRootScope(rootScope);
    Routine r = new Routine("", rootScope, rootScope);
    r.setProgressType(NodeTypes.Program_root);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    rootScope.add(r);
    currentRoutine = r;
    rootRoutine = r;
  }

  @Override
  public void programTail() throws TreeParserException {
    // Now that we know what all the internal Routines are, wrap up the Calls.
    List<SymbolScope> allScopes = new ArrayList<>();
    allScopes.add(rootScope);
    allScopes.addAll(rootScope.getChildScopesDeep());
    LinkedList<Call> calls = new LinkedList<>();
    for (SymbolScope scope : allScopes) {
      for (Call call : scope.getCallList()) {
        // Process IN HANDLE last to make sure PERSISTENT SET is processed first.
        if (call.isInHandle()) {
          calls.addLast(call);
        } else {
          calls.addFirst(call);
        }
      }
    }
    for (Call call : calls) {
      try {
        String routineId = call.getRunArgument();
        call.wrapUp(rootScope.hasRoutine(routineId));
      } catch (SemanticException e) {
        throw new TreeParserException("Unhandled SemanticException.", e);
      }
    }
  }

  /** For a RECORD_NAME node, do checks and assignments for the TableBuffer. */
  private void recordNodeSymbol(JPNode node, TableBuffer buffer) throws TreeParserException {
    String nodeText = node.getText();
    if (buffer == null)
      throw new TreeParserException(node.getFilename() + ":" + node.getLine() + " Could not resolve table: " + nodeText);
    Table table = buffer.getTable();
    // If we get a mismatch between storetype here and the storetype determined
    // by proparse.dll then there's a bug somewhere. This is just a double-check.
    if (table.getStoretype() != node.attrGet(IConstants.STORETYPE))
      throw new TreeParserException(
          node.getFilename() + ":" + node.getLine() + " Storetype mismatch between proparse.dll and treeparser01: "
              + nodeText + " " + node.attrGet(IConstants.STORETYPE) + " " + table.getStoretype());
    prevTableReferenced = lastTableReferenced;
    lastTableReferenced = buffer;
    // For an unnamed buffer, determine if it's abbreviated.
    // Note that named buffers, temp and work table names cannot be abbreviated.
    if (buffer.isDefault() && table.getStoretype() == IConstants.ST_DBTABLE) {
      String[] nameParts = nodeText.split("\\.");
      int tableNameLen = nameParts[nameParts.length - 1].length();
      if (table.getName().length() > tableNameLen)
        node.attrSet(IConstants.ABBREVIATED, 1);
    }
  }

  /** Action to take at various RECORD_NAME nodes. */
  @Override
  public void recordNameNode(AST anode, ContextQualifier contextQualifier) throws TreeParserException {
    RecordNameNode recordNode = (RecordNameNode) anode;
    recordNode.attrSet(IConstants.CONTEXT_QUALIFIER, contextQualifier.toString());
    TableBuffer buffer = null;
    switch (contextQualifier) {
      case INIT:
      case INITWEAK:
      case REF:
      case REFUP:
      case UPDATING:
      case BUFFERSYMBOL:
        buffer = currentScope.getBufferSymbol(recordNode.getText());
        break;
      case SYMBOL:
        buffer = currentScope.lookupTableOrBufferSymbol(anode.getText());
        break;
      case TEMPTABLESYMBOL:
        buffer = currentScope.lookupTempTable(anode.getText());
        break;
      case SCHEMATABLESYMBOL:
        Table table = refSession.getSchema().lookupTable(anode.getText());
        if (table != null)
          buffer = currentScope.getUnnamedBuffer(table);
        break;
      default:
        assert false;
    }
    recordNodeSymbol(recordNode, buffer); // Does checks, sets attributes.
    recordNode.setTableBuffer(buffer);
    switch (contextQualifier) {
      case INIT:
      case REF:
      case REFUP:
      case UPDATING:
        recordNode.setBufferScope(currentBlock.getBufferForReference(buffer));
        break;
      case INITWEAK:
        recordNode.setBufferScope(currentBlock.addWeakBufferScope(buffer));
        break;
    }
    buffer.noteReference(contextQualifier);
  }

  @Override
  public void routineReturnDatatype(AST datatypeAST) {
    JPNode datatypeNode = (JPNode) datatypeAST;
    if (datatypeNode.getType() == NodeTypes.CLASS)
      datatypeNode = datatypeNode.nextNode();
    currentRoutine.setReturnDatatypeNode(datatypeNode);
  }

  /**
   * Called by the tree parser at the beginning of a RUN statement.
   * 
   * @author pcd
   */
  @Override
  public void runBegin(AST runAST) {
    JPNode runNode = (JPNode) runAST;
    // Expect a FileName at the top of semantic stack
    String fileName = (String) wipExpression.getValue();
    Call call = new Call(runNode);
    call.setRunArgument(fileName);
    runNode.setCall(call);
    wipCalls.addFirst(call);
  }

  /**
   * Called by the tree parser in the RUN statement right before any parameters.
   * 
   * @author pcd
   */
  @Override
  public void runEnd(AST node) {
    // Record the call in the current context.
    currentScope.registerCall(wipCalls.getFirst());
    wipCalls.removeFirst();
  }

  /**
   * Called by the tree parser for RUN IN HANDLE. Get the RunHandle value in "run &lt;proc&gt; in &lt;handle&gt;." Where
   * &lt;handle&gt; is a handle valued Expression; then save the RunHandle value in the current call. Partly implemented
   * for Calls and Routines.
   * 
   * @author pcd
   */
  @Override
  public void runInHandle(AST exprNode) {
    wipCalls.getFirst().setRunHandleNode((JPNode) exprNode);
  }

  /**
   * Called by the tree parser for RUN PERSISTENT SET. Update the &lt;handle&gt; in "run &lt;proc&gt; persistent set &lt;handle&gt;.":
   * save a reference to the external procedure &lt;proc&gt; in &lt;handle&gt;. The AST structure for this form of the run is:
   * runstate : #( RUN filenameorvalue (#(PERSISTENT ( #(SET (field)? ) &lt;A&gt; )? ) where &lt;A&gt; is this action. Thus, we
   * expect a value in wipFieldNode with the name of the handle variable. This method gets the variable from the current
   * scope and stores a reference to it in the current call (being built), so that the Call.finalize method can update
   * its value. Partly implemented for Calls and Routines.
   * 
   * @author pcd
   * @param fld is used for error reporting.
   */
  @Override
  public void runPersistentSet(AST fld) {
    wipCalls.getFirst().setPersistentHandleNode((JPNode) fld);
  }

  @Override
  public void scopeAdd(AST anode) {
    LOG.trace("Creating new scope {}", anode);
    BlockNode blockNode = (BlockNode) anode;
    currentScope = currentScope.addScope();
    currentBlock = pushBlock(new Block(currentScope, blockNode));
    currentScope.setRootBlock(currentBlock);
    blockNode.setBlock(currentBlock);
  }

  @Override
  public void scopeClose(AST scopeRootNode) {
    currentScope = currentScope.getParentScope();
    blockEnd();
  }

  /**
   * In the case of a function definition that comes some time after a function forward declaration, we want to use the
   * scope that was created with the forward declaration, because it is the scope that has all of the parameter
   * definitions. We have to do this because the definition itself may have left out the parameter list - it's not
   * required - it just uses the parameter list from the declaration.
   */
  private void scopeSwap(SymbolScope scope) {
    currentScope = scope;
    blockEnd(); // pop the unused block from the stack
    currentBlock = pushBlock(scope.getRootBlock());
  }

  @Override
  public void setSymbol(int symbolType, AST idAST) {
    JPNode idNode = (JPNode) idAST;
    idNode.setSymbol(currentScope.lookupSymbol(symbolType, idNode.getText()));
  }

  /**
   * Create a "strong" buffer scope. This is called within a DO FOR or REPEAT FOR statement.
   * 
   * @param anode Is the RECORD_NAME node. It must already have the BufferSymbol linked to it.
   */
  @Override
  public void strongScope(AST anode) {
    currentBlock.addStrongBufferScope((RecordNameNode) anode);
  }

  /** Constructor or destructor. */
  @Override
  public void structorBegin(AST blockAST) {
    /*
     * Since 'structors don't have a name, we don't add them to any sort of map in the parent scope.
     */
    scopeAdd(blockAST);
    BlockNode blockNode = (BlockNode) blockAST;
    SymbolScope definingScope = currentScope.getParentScope();
    // 'structors don't have names, so use empty string.
    Routine r = new Routine("", definingScope, currentScope);
    r.setProgressType(blockNode.getType());
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    currentRoutine = r;
  }

  /** End of constructor or destructor. */
  @Override
  public void structorEnd(AST blockAST) {
    scopeClose(blockAST);
    currentRoutine = rootRoutine;
  }

  /** Called at the end of a VIEW statement. */
  @Override
  public void viewState(AST headAST) {
    // The VIEW statement grammar uses gwidget, so we have to do some
    // special searching for FRAME to initialize.
    JPNode headNode = (JPNode) headAST;
    for (JPNode frameNode : headNode.query(NodeTypes.FRAME)) {
      int parentType = frameNode.getParent().getType();
      if (parentType == NodeTypes.Widget_ref || parentType == NodeTypes.IN_KW) {
        frameStack.simpleFrameInitStatement(headNode, frameNode.nextNode(), currentBlock);
        return;
      }
    }
  }

  private Block popBlock() {
    blockStack.remove(blockStack.size() - 1);
    return blockStack.get(blockStack.size() - 1);
  }

  private Block pushBlock(Block block) {
    blockStack.add(block);
    return block;
  }

  public SymbolScope getCurrentScope() {
    return currentScope;
  }

  public SymbolScopeRoot getRootScope() {
    return rootScope;
  }

  private void classStateInherits(JPNode classNode, JPNode inheritsTypeNode) {
    LOG.trace("Entering classStateInherits {} {}", classNode, inheritsTypeNode);
    String className = inheritsTypeNode.attrGetS(IConstants.QUALIFIED_CLASS_STRING);
    SymbolScopeSuper cachedCopy = null;
    try {
      cachedCopy = SymbolScopeSuper.cache.get(className.toLowerCase(), new Callable<SymbolScopeSuper>() {
        @Override
        public SymbolScopeSuper call() throws Exception {
            SymbolScopeSuper clz = classStateSuper(classNode, className);
            if (clz == null) {
              throw new ExecutionException("Unable to find class " + className, null);
            } else {
              return clz;
            }
        }
      });
    } catch (ExecutionException caught) {
      // Parse error from parent classes are thrown to caller
      if (caught.getCause() instanceof RefactorException) {
        throw new ProparseRuntimeException(caught.getCause());
      }
      LOG.trace("ExecutionException", caught);
      return;
    }

    // We take a copy of the cached superScope, because the tree parser messes with
    // the attributes of the symbols, and we don't want to mess with the symbols that
    // are in the super scopes in the cache.
    rootScope.assignSuper(cachedCopy.generateSymbolScopeSuper());
  }

  /** Get the Table symbol linked from a RECORD_NAME AST. */
  private Table astTableLink(AST tableAST) {
    LOG.trace("Entering astTableLink {}", tableAST);
    TableBuffer buffer = (TableBuffer) ((JPNode) tableAST).getLink(IConstants.SYMBOL);
    assert buffer != null;
    return buffer.getTable();
  }

  private SymbolScopeSuper classStateSuper(JPNode classNode, String className) throws RefactorException {
    LOG.trace("Entering classStateSuper {} {}", classNode, className);
    File file = refSession.findFileForClassName(className);
    if ((file == null) || !file.exists()) {
      // Could not find the super class. Will happen with Progress.lang.*, vendor libraries, etc.
      return null;
    }
    ParseUnit pu = new ParseUnit(file, refSession);
    JPNode superClassTree = (JPNode) classNode.getLink(IConstants.SUPER_CLASS_TREE);
    if (superClassTree != null) {
      pu.setTopNode(superClassTree);
    }
    pu.treeParser01();

    return pu.getRootScope().generateSymbolScopeSuper();
  }

}
