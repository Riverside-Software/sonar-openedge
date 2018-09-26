/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.treeparser01;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.IIndex;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.core.schema.Index;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.Call;
import org.prorefactor.treeparser.ContextQualifier;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Expression;
import org.prorefactor.treeparser.FieldLookupResult;
import org.prorefactor.treeparser.Parameter;
import org.prorefactor.treeparser.ParseUnit;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.SymbolFactory;
import org.prorefactor.treeparser.TreeParserException;
import org.prorefactor.treeparser.TreeParserRootSymbolScope;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.Dataset;
import org.prorefactor.treeparser.symbols.Event;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.Routine;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.widgets.Browse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import antlr.SemanticException;

/**
 * Provides all functions called by TreeParser01. TreeParser01 does not, itself, define any actions. Instead, it only
 * makes calls to the functions defined in this class.
 */
public class TP01Support implements ITreeParserAction {
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
  private Map<String, TreeParserSymbolScope> funcForwards = new HashMap<>();
  /** There may be more than one WIP call, since a functioncall is a perfectly valid parameter. */
  private Deque<Call> wipCalls = new LinkedList<>();
  /** Since there can be more than one WIP Call, there can be more than one WIP Parameter. */
  private Deque<Parameter> wipParameters = new LinkedList<>();

  private Routine currentRoutine;
  private Routine rootRoutine;

  private final RefactorSession refSession;
  private final ParseUnit unit;
  private final TreeParserRootSymbolScope rootScope;

  /**
   * The symbol last, or currently being, defined. Needed when we have complex syntax like DEFINE id ... LIKE, where we
   * want to track the LIKE but it's not in the same grammar production as the DEFINE.
   */
  private Symbol currSymbol;

  private TreeParserSymbolScope currentScope;
  private TableBuffer lastTableReferenced;
  private TableBuffer prevTableReferenced;
  private TableBuffer currDefTable;
  private Index currDefIndex;
  // LIKE tables management for index copy
  private boolean currDefTableUseIndex = false;
  private ITable currDefTableLike = null;

  // Temporary work-around
  private boolean inDefineEvent = false;

  public TP01Support(RefactorSession session, ParseUnit unit) {
    this.refSession = session;
    this.unit = unit;
    this.rootScope = new TreeParserRootSymbolScope(refSession);

    currentScope = rootScope;
  }

  @Override
  public ParseUnit getParseUnit() {
    return unit;
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
  public void blockBegin(JPNode blockAST) {
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
  public void browseRef(JPNode idAST) {
    LOG.trace("Entering browseRef {}", idAST);
    frameStack.browseRefNode((JPNode) idAST, currentScope);
  }

  @Override
  public void bufferRef(JPNode idAST) {
    LOG.trace("Entering bufferRef {}", idAST);
    TableBuffer tableBuffer = currentScope.lookupBuffer(idAST.getText());
    if (tableBuffer != null) {
      tableBuffer.noteReference(ContextQualifier.SYMBOL);
    }
  }

  @Override
  public void callBegin(JPNode callNode) {
    LOG.trace("Entering callBegin {}", callNode);
    Call call = new Call(callNode);
    callNode.setCall(call);
    wipCalls.addFirst(call);
  }

  @Override
  public void callEnd() {
    LOG.trace("Entering callEnd");
    // Record the call in the current context.
    currentScope.registerCall(wipCalls.getFirst());
    wipCalls.removeFirst();
  }

  @Override
  public void callConstructorBegin(JPNode callNode) {
    LOG.trace("Entering callConstructorBegin {}", callNode);
    Call call = new Call(callNode);
    callNode.setCall(call);
    wipCalls.addFirst(call);
  }

  @Override
  public void callConstructorEnd() {
    LOG.trace("Entering callConstructorEnd");
    // Record the call in the current context.
    currentScope.registerCall(wipCalls.getFirst());
    wipCalls.removeFirst();
  }

  @Override
  public void callMethodBegin(JPNode callNode) {
    LOG.trace("Entering callMethodBegin {}", callNode);
    Call call = new Call(callNode);
    callNode.setCall(call);
    wipCalls.addFirst(call);
  }

  @Override
  public void callMethodEnd() {
    LOG.trace("Entering callMethodEnd");
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
  public void canFindBegin(JPNode canfindAST, JPNode recordAST) {
    LOG.trace("Entering canFindBegin {} - {}", canfindAST, recordAST);
    RecordNameNode recordNode = (RecordNameNode) recordAST;
    // Keep a ref to the current block...
    Block b = currentBlock;
    // ...create a can-find scope and block (assigns currentBlock)...
    scopeAdd(canfindAST);
    // ...and then set this "can-find block" to use it as its parent.
    currentBlock.setParent(b);
    String buffName = recordAST.getText();
    ITable table;
    boolean isDefault;
    TableBuffer tableBuffer = currentScope.lookupBuffer(buffName);
    if (tableBuffer != null) {
      table = tableBuffer.getTable();
      isDefault = tableBuffer.isDefault();
      // Notify table buffer that it's used in a CAN-FIND
      tableBuffer.noteReference(ContextQualifier.INIT);
    } else {
      table = refSession.getSchema().lookupTable(buffName);
      isDefault = true;
    }
    TableBuffer newBuff = currentScope.defineBuffer(isDefault ? "" : buffName, table);
    recordNode.setTableBuffer(newBuff);
    currentBlock.addHiddenCursor(recordNode);
  }

  @Override
  public void canFindEnd(JPNode canfindAST) {
    LOG.trace("Entering canFindEnd {}", canfindAST);
    scopeClose(canfindAST);
  }

  @Override
  public void classState(JPNode classNode, JPNode abstractKw, JPNode finalKw, JPNode serializableKw) {
    LOG.trace("Entering classState {}", classNode);
    JPNode idNode = classNode.getFirstChild();
    rootScope.setClassName(idNode.getText());
    rootScope.setTypeInfo(refSession.getTypeInfo(idNode.getText()));
    rootScope.setAbstractClass(abstractKw != null);
    rootScope.setFinalClass(finalKw != null);
    rootScope.setSerializableClass(serializableKw != null);
  }

  @Override
  public void interfaceState(JPNode classNode) {
    LOG.trace("Entering interfaceState {}", classNode);
    JPNode idNode = classNode.getFirstChild();
    rootScope.setClassName(idNode.getText());
    rootScope.setTypeInfo(refSession.getTypeInfo(idNode.getText()));
    rootScope.setInterface(true);
  }

  @Override
  public void clearState(JPNode headNode) {
    LOG.trace("Entering clearState {}", headNode);
    JPNode firstChild = headNode.getFirstChild();
    if (firstChild.getType() == ProParserTokenTypes.FRAME)
      frameStack.simpleFrameInitStatement(headNode, firstChild.nextNode(), currentBlock);
  }

  @Override
  public void datasetTable(JPNode tableAST) {
    LOG.trace("Entering datasetTable {}", tableAST);
    RecordNameNode tableNode = (RecordNameNode) tableAST;
    Dataset dataset = (Dataset) currSymbol;
    dataset.addBuffer(tableNode.getTableBuffer());
  }

  /** The tree parser calls this at an AS node */
  @Override
  public void defAs(JPNode asNode) {
    LOG.trace("Entering defAs {}", asNode);
    currSymbol.setAsNode(asNode);
    Primative primative = (Primative) currSymbol;
    JPNode typeNode = asNode.nextNode();
    if (typeNode.getType() == ProParserTokenTypes.CLASS)
      typeNode = typeNode.nextNode();
    if (typeNode.getType() == ProParserTokenTypes.TYPE_NAME) {
      primative.setDataType(DataType.getDataType(ProParserTokenTypes.CLASS));
      primative.setClassName(typeNode);
    } else {
      primative.setDataType(DataType.getDataType(typeNode.getType()));
    }
    assert primative.getDataType() != null : "Failed to set datatype at " + asNode.getFilename() + " line "
        + asNode.getLine();
  }

  @Override
  public void defExtent(JPNode extentNode) {
    LOG.trace("Entering defExtent {}", extentNode);
    Primative primative = (Primative) currSymbol;
    JPNode exprNode = extentNode.getFirstChild();
    // If there is no expression node, then it's an "indeterminate extent".
    // If it's not a numeric literal, then we give up.
    if (exprNode == null || exprNode.getType() != ProParserTokenTypes.NUMBER) {
      primative.setExtent(-1);
    } else {
      primative.setExtent(Integer.parseInt(exprNode.getText()));
    }
  }

  /** The tree parser calls this at a LIKE node */
  @Override
  public void defLike(JPNode likeNode) {
    LOG.trace("Entering defLike {}", likeNode);
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
  public Browse defineBrowse(JPNode defAST, JPNode idAST) {
    LOG.trace("Entering defineBrowse {} - {}", defAST, idAST);
    Browse browse = (Browse) defineSymbol(ProParserTokenTypes.BROWSE, defAST, idAST);
    frameStack.nodeOfDefineBrowse(browse, (JPNode) defAST);
    return browse;
  }

  /**
   * Define a buffer. If the buffer is initialized at the same time it is defined (as in a buffer parameter), then
   * parameter init should be true.
   */
  @Override
  public void defineBuffer(JPNode defAST, JPNode idNode, JPNode tableAST, boolean init) {
    LOG.trace("Entering defineBuffer {} {} {} {}", defAST, idNode, tableAST, init);
    ITable table = astTableLink(tableAST);
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
  public void defineBufferForTrigger(JPNode tableAST) {
    LOG.trace("Entering defineBufferForTrigger {}", tableAST);
    ITable table = astTableLink(tableAST);
    TableBuffer bufSymbol = currentScope.defineBuffer("", table);
    currentBlock.getBufferForReference(bufSymbol); // Create the BufferScope
    currSymbol = bufSymbol;
  }

  @Override
  public Event defineEvent(JPNode defNode, JPNode idNode) {
    LOG.trace("Entering defineEvent {} - {}", defNode, idNode);
    String name = idNode.getText();
    if (name == null || name.length() == 0)
      name = idNode.getNodeType().name();
    Event event = new Event(name, currentScope);
    event.setDefOrIdNode(defNode);
    currSymbol = event;
    idNode.setLink(IConstants.SYMBOL, event);
    return event;
  }

  @Override
  public Symbol defineSymbol(int symbolType, JPNode defNode, JPNode idNode) {
    LOG.trace("Entering defineSymbol {} - {} - {}", symbolType, defNode, idNode);
    /*
     * Some notes: We need to create the Symbol right away, because further actions in the grammar might need to set
     * attributes on it. We can't add it to the scope yet, because of statements like this: def var xyz like xyz. The
     * tree parser is responsible for calling addToScope at the end of the statement or when it is otherwise safe to do
     * so.
     */
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
  public Symbol defineTableFieldInitialize(JPNode idNode) {
    LOG.trace("Entering defineTableFieldInitialize {}", idNode);
    FieldBuffer fieldBuff = rootScope.defineTableFieldDelayedAttach(idNode.getText(), currDefTable);
    currSymbol = fieldBuff;
    fieldBuff.setDefOrIdNode(idNode);
    idNode.setLink(IConstants.SYMBOL, fieldBuff);
    return fieldBuff;
  }

  @Override
  public void defineTableFieldFinalize(Object obj) {
    LOG.trace("Entering defineTableFieldFinalize {}", obj);
    ((FieldBuffer) obj).getField().setTable(currDefTable.getTable());
  }

  @Override
  public void defineTableLike(JPNode tableAST) {
    LOG.trace("Entering defineTableLike {}", tableAST);
    // Get table for "LIKE table"
    ITable table = astTableLink(tableAST);
    currDefTableLike = table;
    // For each field in "table", create a field def in currDefTable
    for (IField field : table.getFieldPosOrder()) {
      rootScope.defineTableField(field.getName(), currDefTable).assignAttributesLike(field);
    }
  }

  @Override
  public void defineUseIndex(JPNode recNode, JPNode idNode) throws SemanticException {
    LOG.trace("Entering defineUseIndex {}", idNode);
    ITable table = astTableLink(recNode);
    IIndex idx = table.lookupIndex(idNode.getText());
    currDefTable.getTable().add(new Index(currDefTable.getTable(), idx.getName(), idx.isUnique(), idx.isPrimary()));
    currDefTableUseIndex = true;
  }

  @Override
  public void defineIndexInitialize(JPNode idNode, JPNode unique, JPNode primary, JPNode word) throws SemanticException {
    LOG.trace("Entering defineIndexInitialize {} - {} - {} - {}", idNode, unique, primary, word);
    currDefIndex = new Index(currDefTable.getTable(), idNode.getText(), (unique != null), (primary != null));
    currDefTable.getTable().add(currDefIndex);
  }

  @Override
  public void defineIndexField(JPNode idNode) throws SemanticException {
    LOG.trace("Entering defineIndexField{}", idNode);
    IField fld = currDefTable.getTable().lookupField(idNode.getText());
    if (fld != null)
      currDefIndex.addField(fld);
  }

  public void defineTable(JPNode defNode, JPNode idNode, int storeType) {
    LOG.trace("Entering defineTable {} {} {}", defNode, idNode, storeType);
    TableBuffer buffer = rootScope.defineTable(idNode.getText(), storeType);
    buffer.setDefOrIdNode(defNode);
    currSymbol = buffer;
    currDefTable = buffer;
    currDefTableLike = null;
    currDefTableUseIndex = false;
    idNode.setLink(IConstants.SYMBOL, buffer);
  }

  @Override
  public void postDefineTempTable(JPNode defAST, JPNode idNode) throws SemanticException {
    LOG.trace("Entering postDefineTempTable {} {}", defAST, idNode);
    // In case of DEFINE TT LIKE, indexes are copied only if USE-INDEX and INDEX are never used 
    if ((currDefTableLike != null) && !currDefTableUseIndex && currDefTable.getTable().getIndexes().isEmpty()) {
      LOG.trace("Copying all indexes from {}", currDefTableLike.getName());
      for (IIndex idx : currDefTableLike.getIndexes()) {
        Index newIdx = new Index(currDefTable.getTable(), idx.getName(), idx.isUnique(), idx.isPrimary());
        for (IField fld : idx.getFields()) {
          IField ifld = newIdx.getTable().lookupField(fld.getName());
          if (ifld == null) {
            LOG.info("Unable to find field name {} in table {}", fld.getName(), currDefTable.getTable().getName());
          } else {
            newIdx.addField(ifld);
          }
        }
        currDefTable.getTable().add(newIdx);
      }
    }
  }

  @Override
  public void defineTempTable(JPNode defAST, JPNode idAST) {
    defineTable((JPNode) defAST, (JPNode) idAST, IConstants.ST_TTABLE);
  }

  @Override
  public Variable defineVariable(JPNode defAST, JPNode idAST) {
    return defineVariable(defAST, idAST, false);
  }

  @Override
  public Variable defineVariable(JPNode defNode, JPNode idNode, boolean parameter) {
    LOG.trace("Entering defineVariable {} {} {}", defNode, idNode, parameter);
    /*
     * Some notes: We need to create the Variable Symbol right away, because further actions in the grammar might need
     * to set attributes on it. We can't add it to the scope yet, because of statements like this: def var xyz like xyz.
     * The tree parser is responsible for calling addToScope at the end of the statement or when it is otherwise safe to
     * do so.
     */
    String name = idNode.getText();
    if (name == null || name.length() == 0) {
      /*
       * Variable Name: There was a subtle bug here when parsing trees extracted from PUB files. In PUB files, the text
       * of keyword nodes are not stored. But in the case of an ACCUMULATE statement -> aggregatephrase ->
       * aggregate_opt, we are defining variable/symbols using the COUNT|MAXIMUM|TOTAL|whatever node. I added a check
       * for empty text from the "id" node.
       */
      name = idNode.getNodeType().name();
    }
    Variable variable = new Variable(name, currentScope, parameter);
    variable.setDefOrIdNode(defNode);
    currSymbol = variable;
    idNode.setLink(IConstants.SYMBOL, variable);
    return variable;
  }

  @Override
  public Variable defineVariable(JPNode defAST, JPNode idAST, int dataType) {
    return defineVariable(defAST, idAST, dataType, false);
  }

  @Override
  public Variable defineVariable(JPNode defAST, JPNode idAST, int dataType, boolean parameter) {
    assert dataType != ProParserTokenTypes.CLASS;
    Variable v = defineVariable(defAST, idAST, parameter);
    v.setDataType(DataType.getDataType(dataType));
    return v;
  }

  @Override
  public Variable defineVariable(JPNode defAST, JPNode idAST, JPNode likeAST) {
    return defineVariable(defAST, idAST, likeAST, false);
  }

  @Override
  public Variable defineVariable(JPNode defAST, JPNode idAST, JPNode likeAST, boolean parameter) {
    Variable v = defineVariable(defAST, idAST, parameter);
    FieldRefNode likeRefNode = (FieldRefNode) likeAST;
    v.setDataType(likeRefNode.getDataType());
    v.setClassName(likeRefNode.getClassName());
    return v;
  }

  @Override
  public void defineWorktable(JPNode defAST, JPNode idAST) {
    defineTable((JPNode) defAST, (JPNode) idAST, IConstants.ST_WTABLE);
  }

  @Override
  public void widattr(JPNode widAST, JPNode idNode, ContextQualifier cq) {
    LOG.trace("Entering {} mode {}", idNode, cq);
    if (idNode.getType() == ProParserTokenTypes.THISOBJECT) {
      JPNode tok = idNode.getNextSibling();
      if (tok.getType() == ProParserTokenTypes.OBJCOLON) {
        JPNode fld = tok.getNextSibling();
        String name = fld.getText();

        FieldLookupResult result =  currentBlock.lookupField(name, true);
        if (result == null)
          return;

        // Variable
        if (result.variable != null) {
          result.variable.noteReference(cq);
        }
      }
    } else if (idNode.getType() == ProParserTokenTypes.Field_ref) {
      // Reference to a static field
      if ((idNode.getFirstChild().getType()) == ProParserTokenTypes.ID && (idNode.getNextSibling() != null) && (idNode.getNextSibling().getType() == ProParserTokenTypes.OBJCOLON)) {
        String clsRef = idNode.getFirstChild().getText();
        String clsName = rootScope.getClassName();
        if ((clsRef != null) && (clsName != null) && (clsRef.indexOf('.') == -1) && (clsName.indexOf('.') != -1))
          clsName = clsName.substring(clsName.indexOf('.') + 1);
        
        if ((clsRef != null) && (clsName != null) && clsRef.equalsIgnoreCase(clsName)) {
          String right = idNode.getNextSibling().getNextSibling().getText();
          
          FieldLookupResult result =  currentBlock.lookupField(right, true);
          if (result == null)
            return;

          // Variable
          if (result.variable != null) {
            result.variable.noteReference(cq);
          }
        }
      }
    }
  }

  @Override
  public void field(JPNode refAST, JPNode idNode, ContextQualifier cq, TableNameResolution resolution) throws SemanticException {
    LOG.trace("Entering field {} {} {} {}", refAST, idNode, cq, resolution);
    FieldRefNode refNode = (FieldRefNode) refAST;
    String name = idNode.getText();
    FieldLookupResult result = null;

    refNode.attrSet(IConstants.CONTEXT_QUALIFIER, cq.toString());

    // Check if this is a Field_ref being "inline defined"
    // If so, we define it right now.
    if (refNode.attrGet(IConstants.INLINE_VAR_DEF) == 1)
      addToSymbolScope(defineVariable(idNode, idNode));

    if ((refNode.getParent().getType() == ProParserTokenTypes.USING && refNode.getParent().getParent().getType() == ProParserTokenTypes.RECORD_NAME)
        || (refNode.getFirstChild().getType() == ProParserTokenTypes.INPUT &&
            (refNode.getNextSibling() == null || refNode.getNextSibling().getType() != ProParserTokenTypes.OBJCOLON))) {
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
      IField field = ourBuffer.getTable().lookupField(fieldPart);
      if (field == null) {
        // The OpenEdge compiler seems to ignore invalid tokens in a FIELDS phrase.
        // As a result, some questionable code will fail to parse here if we don't also ignore those here.
        // Sigh. This would be a good lint rule.
        int parentType = refNode.getParent().getType();
        if (parentType == ProParserTokenTypes.FIELDS || parentType == ProParserTokenTypes.EXCEPT)
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
      if (result.field.getBuffer() != null) {
        result.field.getBuffer().noteReference(cq);
      }
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
  public void fnvExpression(JPNode node) {
    LOG.trace("fnvExpression  {}", node);
    wipExpression = new Expression((JPNode) node);
  }

  /**
   * Called by the tree parser for filenameorvalue: FILENAME production Partly implemented for Calls and Routines.
   * 
   * @author pcd
   */
  @Override
  public void fnvFilename(JPNode node) {
    LOG.trace("Entering fnvFilename {}", node);

    Expression exp = new Expression((JPNode) node);
    exp.setValue(node.getText());
    wipExpression = exp;
  }

  /** Called from Form_item node */
  @Override
  public void formItem(JPNode ast) {
    LOG.trace("Entering formItem {}", ast);
    frameStack.formItem((JPNode) ast);
  }

  /** Called from DO|REPEAT|FOR blocks. */
  @Override
  public void frameBlockCheck(JPNode ast) {
    LOG.trace("Entering frameBlockCheck {}", ast);
    frameStack.nodeOfBlock((JPNode) ast, currentBlock);
  }

  /** Called at tree parser DEFINE FRAME statement. */
  @Override
  public void frameDef(JPNode defAST, JPNode idAST) {
    LOG.trace("Entering frameDef {} {}", defAST, idAST);
    frameStack.nodeOfDefineFrame((JPNode) defAST, (JPNode) idAST, currentScope);
  }

  /** This is a specialization of frameInitializingStatement, called for ENABLE|UPDATE|PROMPT-FOR. */
  @Override
  public void frameEnablingStatement(JPNode ast) {
    LOG.trace("Entering frameEnablingStatement {}", ast);

    // Flip this flag before calling nodeOfInitializingStatement.
    frameStack.statementIsEnabler();
    frameStack.nodeOfInitializingStatement((JPNode) ast, currentBlock);
  }

  /** This is called at the beginning of a frame affecting statement, with the statement head node. */
  @Override
  public void frameInitializingStatement(JPNode ast) {
    frameStack.nodeOfInitializingStatement((JPNode) ast, currentBlock);
  }

  /** This is called at the end of a frame affecting statement. */
  @Override
  public void frameStatementEnd() {
    frameStack.statementEnd();
  }

  @Override
  public void frameRef(JPNode idAST) {
    frameStack.frameRefNode((JPNode) idAST, currentScope);
  }

  @Override
  public void funcBegin(JPNode funcAST, JPNode idNode) {
    LOG.trace("Entering funcBegin {} {}", funcAST, idNode);

    // John: Need some comments here. Why don't I just fetch any
    // function forward scope right away? Why wait until funcDef()?
    // Why bother with a funcForward map specifically, rather than
    // just a funcScope map generally?
    scopeAdd(funcAST);
    BlockNode blockNode = (BlockNode) idNode.getParent();
    TreeParserSymbolScope definingScope = currentScope.getParentScope();
    Routine r = new Routine(idNode.getText(), definingScope, currentScope);
    r.setProgressType(ProParserTokenTypes.FUNCTION);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    definingScope.add(r);
    currentRoutine = r;
  }

  @Override
  public void funcDef(JPNode funcAST, JPNode idAST) {
    LOG.trace("Entering funcDef {} {}", funcAST, idAST);
    /*
     * If this function definition had a function forward declaration, then we use the block and scope from that
     * declaration, in case it is where the parameters were defined. (You can define the params in the FORWARD, and
     * leave them out at the body.)
     *
     * However, if this statement re-defines the formal args, then we use this statement's scope - because the formal
     * arg names from here will be in effect rather than the names from the FORWARD. (The names don't have to match.)
     */
    if (!currentRoutine.getParameters().isEmpty())
      return;
    TreeParserSymbolScope forwardScope = funcForwards.get(idAST.getText());
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
  public void funcEnd(JPNode funcAST) {
    LOG.trace("Entering funcEnd {}", funcAST);
    scopeClose(funcAST);
    currentRoutine = rootRoutine;
  }

  @Override
  public void funcForward(JPNode idAST) {
    LOG.trace("Entering funcForward {}", idAST);
    funcForwards.put(idAST.getText(), currentScope);
  }

  @Override
  public void lexat(JPNode fieldRefAST) {
    LOG.trace("Entering lexAt {}", fieldRefAST);
    frameStack.lexAt((JPNode) fieldRefAST);
  }

  @Override
  public void methodBegin(JPNode blockAST, JPNode idNode) {
    LOG.trace("Entering methodBegin {} - {}", blockAST, idNode);

    scopeAdd(blockAST);
    BlockNode blockNode = (BlockNode) idNode.getParent();
    TreeParserSymbolScope definingScope = currentScope.getParentScope();
    Routine r = new Routine(idNode.getText(), definingScope, currentScope);
    r.setProgressType(ProParserTokenTypes.METHOD);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    definingScope.add(r);
    currentRoutine = r;
  }

  @Override
  public void methodEnd(JPNode blockAST) {
    LOG.trace("Entering methodEnd {}", blockAST);
    scopeClose(blockAST);
    currentRoutine = rootRoutine;
  }

  @Override
  public void propGetSetBegin(JPNode propAST) {
    LOG.trace("Entering propGetSetBegin {}", propAST);
    scopeAdd(propAST);
    BlockNode blockNode = (BlockNode) propAST;
    TreeParserSymbolScope definingScope = currentScope.getParentScope();
    Routine r = new Routine(propAST.getText(), definingScope, currentScope);
    r.setProgressType(propAST.getType());
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    definingScope.add(r);
    currentRoutine = r;
  }

  @Override
  public void propGetSetEnd(JPNode propAST) {
    LOG.trace("Entering propGetSetEnd {}", propAST);
    scopeClose(propAST);
    currentRoutine = rootRoutine;
  }

  @Override
  public void eventBegin(JPNode eventAST, JPNode idAST) {
    this.inDefineEvent = true;
  }

  @Override
  public void eventEnd(JPNode eventAST) {
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
  public void paramExpression(JPNode exprNode, ContextQualifier cq) {
    LOG.trace("Entering paramExpression {}", exprNode);
    // The expression may or may not be a Field_ref node with a symbol. We don't dig any deeper.
    // As a result, the symbol for an expression parameter might be null.
    wipParameters.getFirst().setSymbol(exprNode.getSymbol());
    if (exprNode.getSymbol() != null)
      exprNode.getSymbol().noteReference(cq);
  }

  @Override
  public void paramForCall(JPNode directionAST) {
    LOG.trace("Entering paramForCall {}", directionAST);
    Parameter param = new Parameter();
    param.setDirectionNode((JPNode) directionAST);
    wipParameters.addFirst(param);
    wipCalls.getFirst().addParameter(param);
  }

  @Override
  public void paramForRoutine(JPNode directionAST) {
    LOG.trace("Entering paramForRoutine '{}' -- '{}'", directionAST.getText(), currentRoutine.fullName());
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
   * @param typeNode The node of the datatype, might be a CLASS node.
   */
  @Override
  public void paramNoName(JPNode typeNode) {
    LOG.trace("Entering paramNoName {}", typeNode);
    Variable variable = new Variable("", currentScope);
    currSymbol = variable;
    if (typeNode.getType() == ProParserTokenTypes.CLASS)
      typeNode = typeNode.nextNode();
    if (typeNode.getType() == ProParserTokenTypes.TYPE_NAME) {
      variable.setDataType(DataType.getDataType(ProParserTokenTypes.CLASS));
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
  public void paramSymbol(JPNode symbolAST) {
    wipParameters.getFirst().setSymbol(symbolAST.getSymbol());
  }

  @Override
  public void procedureBegin(JPNode procAST, JPNode idAST) {
    LOG.trace("Entering procedureBegin {} - {}", procAST, idAST);
    BlockNode blockNode = (BlockNode) procAST;
    TreeParserSymbolScope definingScope = currentScope;
    scopeAdd(blockNode);
    Routine r = new Routine(idAST.getText(), definingScope, currentScope);
    r.setProgressType(ProParserTokenTypes.PROCEDURE);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    definingScope.add(r);
    currentRoutine = r;
  }

  @Override
  public void procedureEnd(JPNode node) {
    scopeClose(node);
    currentRoutine = rootRoutine;
  }

  @Override
  public void programRoot(JPNode rootAST) {
    LOG.trace("Entering programRoot {}", rootAST);
    BlockNode blockNode = (BlockNode) rootAST;
    currentBlock = pushBlock(new Block(rootScope, blockNode));
    rootScope.setRootBlock(currentBlock);
    blockNode.setBlock(currentBlock);
    getParseUnit().setRootScope(rootScope);
    Routine r = new Routine("", rootScope, rootScope);
    r.setProgressType(ProParserTokenTypes.Program_root);
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    rootScope.add(r);
    currentRoutine = r;
    rootRoutine = r;
  }

  @Override
  public void programTail() throws SemanticException {
    LOG.trace("Entering programTail");
    // Now that we know what all the internal Routines are, wrap up the Calls.
    List<TreeParserSymbolScope> allScopes = new ArrayList<>();
    allScopes.add(rootScope);
    allScopes.addAll(rootScope.getChildScopesDeep());
    LinkedList<Call> calls = new LinkedList<>();
    for (TreeParserSymbolScope scope : allScopes) {
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
      String routineId = call.getRunArgument();
      call.wrapUp(rootScope.hasRoutine(routineId));
    }
  }

  /** For a RECORD_NAME node, do checks and assignments for the TableBuffer. */
  private void recordNodeSymbol(JPNode node, TableBuffer buffer) throws SemanticException {
    String nodeText = node.getText();
    if (buffer == null) {
      throw new TreeParserException("Could not resolve table '" + nodeText + "'", node.getFilename(), node.getLine(), node.getColumn());
    }
    ITable table = buffer.getTable();
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
  public void recordNameNode(JPNode anode, ContextQualifier contextQualifier) throws SemanticException {
    LOG.trace("Entering recordNameNode {} {}", anode, contextQualifier);
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
        ITable table = refSession.getSchema().lookupTable(anode.getText());
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
      default:
        break;
    }
    buffer.noteReference(contextQualifier);
  }

  @Override
  public void routineReturnDatatype(JPNode datatypeNode) {
    if (datatypeNode.getType() == ProParserTokenTypes.CLASS)
      datatypeNode = datatypeNode.nextNode();
    currentRoutine.setReturnDatatypeNode(datatypeNode);
  }

  /**
   * Called by the tree parser at the beginning of a RUN statement.
   * 
   * @author pcd
   */
  @Override
  public void runBegin(JPNode runNode) {
    LOG.trace("Entering runBegin {}", runNode);
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
  public void runEnd(JPNode node) {
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
  public void runInHandle(JPNode exprNode) {
    wipCalls.getFirst().setRunHandleNode(exprNode);
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
  public void runPersistentSet(JPNode fld) {
    wipCalls.getFirst().setPersistentHandleNode((JPNode) fld);
  }

  @Override
  public void scopeAdd(JPNode anode) {
    LOG.trace("Entering scopeAdd {}", anode);
    BlockNode blockNode = (BlockNode) anode;
    currentScope = currentScope.addScope();
    currentBlock = pushBlock(new Block(currentScope, blockNode));
    currentScope.setRootBlock(currentBlock);
    blockNode.setBlock(currentBlock);
  }

  @Override
  public void scopeClose(JPNode scopeRootNode) {
    LOG.trace("Entering scopeClose {}", scopeRootNode);
    currentScope = currentScope.getParentScope();
    blockEnd();
  }

  /**
   * In the case of a function definition that comes some time after a function forward declaration, we want to use the
   * scope that was created with the forward declaration, because it is the scope that has all of the parameter
   * definitions. We have to do this because the definition itself may have left out the parameter list - it's not
   * required - it just uses the parameter list from the declaration.
   */
  private void scopeSwap(TreeParserSymbolScope scope) {
    currentScope = scope;
    blockEnd(); // pop the unused block from the stack
    currentBlock = pushBlock(scope.getRootBlock());
  }

  @Override
  public void setSymbol(int symbolType, JPNode idNode) {
    idNode.setSymbol(currentScope.lookupSymbol(symbolType, idNode.getText()));
  }

  @Override
  public void noteReference(JPNode node, ContextQualifier cq) throws SemanticException {
    if ((node.getSymbol() != null) && ((cq == ContextQualifier.UPDATING) || (cq == ContextQualifier.REFUP))) {
      node.getSymbol().noteReference(cq);
    }
  }

  /**
   * Create a "strong" buffer scope. This is called within a DO FOR or REPEAT FOR statement.
   * 
   * @param anode Is the RECORD_NAME node. It must already have the BufferSymbol linked to it.
   */
  @Override
  public void strongScope(JPNode anode) {
    currentBlock.addStrongBufferScope((RecordNameNode) anode);
  }

  /** Constructor or destructor. */
  @Override
  public void structorBegin(JPNode blockAST) {
    /*
     * Since 'structors don't have a name, we don't add them to any sort of map in the parent scope.
     */
    scopeAdd(blockAST);
    BlockNode blockNode = (BlockNode) blockAST;
    TreeParserSymbolScope definingScope = currentScope.getParentScope();
    // 'structors don't have names, so use empty string.
    Routine r = new Routine("", definingScope, currentScope);
    r.setProgressType(blockNode.getType());
    r.setDefOrIdNode(blockNode);
    blockNode.setSymbol(r);
    currentRoutine = r;
  }

  /** End of constructor or destructor. */
  @Override
  public void structorEnd(JPNode blockAST) {
    scopeClose(blockAST);
    currentRoutine = rootRoutine;
  }

  /** Called at the end of a VIEW statement. */
  @Override
  public void viewState(JPNode headAST) {
    // The VIEW statement grammar uses gwidget, so we have to do some
    // special searching for FRAME to initialize.
    JPNode headNode = headAST;
    for (JPNode frameNode : headNode.query(ABLNodeType.FRAME)) {
      int parentType = frameNode.getParent().getType();
      if (parentType == ProParserTokenTypes.Widget_ref || parentType == ProParserTokenTypes.IN_KW) {
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

  public TreeParserSymbolScope getCurrentScope() {
    return currentScope;
  }

  public TreeParserRootSymbolScope getRootScope() {
    return rootScope;
  }

  /** Get the Table symbol linked from a RECORD_NAME AST. */
  private ITable astTableLink(JPNode tableAST) {
    LOG.trace("Entering astTableLink {}", tableAST);
    TableBuffer buffer = (TableBuffer) tableAST.getLink(IConstants.SYMBOL);
    assert buffer != null;
    return buffer.getTable();
  }

}
