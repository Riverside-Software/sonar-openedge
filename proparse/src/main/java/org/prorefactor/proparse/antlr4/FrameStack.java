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
package org.prorefactor.proparse.antlr4;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.IField;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.FieldLookupResult;
import org.prorefactor.treeparser.TreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.FieldContainer;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.TableBuffer;
import org.prorefactor.treeparser.symbols.Variable;
import org.prorefactor.treeparser.symbols.widgets.Browse;
import org.prorefactor.treeparser.symbols.widgets.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps a stack of most recently "referenced" frames. A frame may be "referenced" at up to two different occassions.
 * Once when the frame is created (like in a DEFINE FRAME statement), and once when the frame is "initialized" (like in
 * a DISPLAY statement). The frame's scope is determined at the time it is initialized. Also deals with BROWSE widgets
 * and the fields in those.
 */
public class FrameStack {
  private static final Logger LOG = LoggerFactory.getLogger(FrameStack.class);

  private boolean currStatementIsEnabler = false;
  private Deque<Frame> frameMRU = new LinkedList<>();
  private FieldContainer containerForCurrentStatement = null;
  private JPNode currStatementWholeTableFormItemNode = null;

  protected FrameStack() {
    // Only from TreeParser
  }

  /**
   * The ID node in a BROWSE ID pair. The ID node might have already had the symbol assigned to it at the point where
   * the statement head was processed.
   */
  void browseRefNode(JPNode idNode, TreeParserSymbolScope symbolScope) {
    LOG.debug("Enter FrameStack#browseRefNode");

    if (idNode.getSymbol() == null)
      browseRefSet(idNode, symbolScope);
  }

  private Browse browseRefSet(JPNode idNode, TreeParserSymbolScope symbolScope) {
    Browse browse = (Browse) symbolScope.lookupFieldLevelWidget(idNode.getText());
    idNode.setLink(IConstants.SYMBOL, browse);
    return browse;
  }

  /**
   * For a Form_item node which is for a whole table reference, get a list of the FieldBuffers that would be added to
   * the frame, respecting any EXCEPT fields list.
   */
  private List<FieldBuffer> calculateFormItemTableFields(JPNode formItemNode) {
    assert formItemNode.getType() == ProParserTokenTypes.Form_item;
    assert formItemNode.getFirstChild().getType() == ProParserTokenTypes.RECORD_NAME;
    RecordNameNode recordNameNode = (RecordNameNode) formItemNode.getFirstChild();
    TableBuffer tableBuffer = recordNameNode.getTableBuffer();
    HashSet<IField> fieldSet = new HashSet<>(tableBuffer.getTable().getFieldSet());
    JPNode exceptNode = formItemNode.getParent().findDirectChild(ProParserTokenTypes.EXCEPT);
    if (exceptNode != null)
      for (JPNode n = exceptNode.getFirstChild(); n != null; n = n.getNextSibling()) {
        if (!(n instanceof FieldRefNode))
          continue;
        IField f = ((FieldBuffer) ((FieldRefNode) n).getSymbol()).getField();
        fieldSet.remove(f);
      }
    ArrayList<FieldBuffer> returnList = new ArrayList<>();
    for (IField field : fieldSet) {
      returnList.add(tableBuffer.getFieldBuffer(field));
    }
    return returnList;
  }

  /**
   * Create a frame object. Adds the new frame object to the MRU list.
   */
  private Frame createFrame(String frameName, TreeParserSymbolScope symbolScope) {
    Frame frame = new Frame(frameName, symbolScope);
    frameMRU.addFirst(frame);
    return frame;
  }

  /**
   * Recieve a Form_item node for a field which should be referenceable on the frame|browse. This checks for LEXAT
   * (DISPLAY thisField @ anotherField) which would keep thisField from being added to the frame. The LEXAT is dealt
   * with in a separate call. This must be called <b>after</b> any Field_ref symbols have been resolved. This only does
   * anything if the first child of the Form_item is RECORD_NAME or Field_ref. Tree parser rules like display_item and
   * form_item sometimes get used in statements that don't actually affect frames. In those cases,
   * containerForCurrentStatement==null, and this function is a no-op.
   */
  void formItem(JPNode formItemNode) {
    LOG.debug("Enter FrameStack#formItem");

    if (containerForCurrentStatement == null)
      return;
    assert formItemNode.getType() == ProParserTokenTypes.Form_item;
    JPNode firstChild = formItemNode.getFirstChild();
    if (firstChild.getType() == ProParserTokenTypes.RECORD_NAME) {
      // Delay processing until the end of the statement. We need any EXCEPT fields resolved first.
      currStatementWholeTableFormItemNode = formItemNode;
    } else {
      FieldRefNode fieldRefNode = null;
      JPNode tempNode = formItemNode.findDirectChild(ProParserTokenTypes.Format_phrase);
      if (tempNode != null) {
        tempNode = tempNode.findDirectChild(ProParserTokenTypes.LEXAT);
        if (tempNode != null)
          return;
      }
      if (fieldRefNode == null && firstChild.getType() == ProParserTokenTypes.Field_ref) {
        fieldRefNode = (FieldRefNode) firstChild;
      }
      if (fieldRefNode != null)
        containerForCurrentStatement.addSymbol(fieldRefNode.getSymbol(), currStatementIsEnabler);
    }
  }

  /**
   * The ID node in a FRAME ID pair. For "WITH FRAME id", the ID was already set when we processed the statement head.
   */
  void frameRefNode(JPNode idNode, TreeParserSymbolScope symbolScope) {
    LOG.debug("Enter FrameStack#frameRefNode");

    if (idNode.getSymbol() == null)
      frameRefSet(idNode, symbolScope);
  }

  private Frame frameRefSet(JPNode idNode, TreeParserSymbolScope symbolScope) {
    String frameName = idNode.getText();
    Frame frame = (Frame) symbolScope.lookupWidget(ProParserTokenTypes.FRAME, frameName);
    if (frame == null)
      frame = createFrame(frameName, symbolScope);
    idNode.setLink(IConstants.SYMBOL, frame);
    return frame;
  }

  /** For a statement that might have #(WITH ... #([FRAME|BROWSE] ID)), get the FRAME|BROWSE node. */
  private JPNode getContainerTypeNode(JPNode stateNode) {
    JPNode withNode = stateNode.findDirectChild(ProParserTokenTypes.WITH);
    if (withNode == null)
      return null;
    JPNode typeNode = withNode.findDirectChild(ProParserTokenTypes.FRAME);
    if (typeNode == null)
      typeNode = withNode.findDirectChild(ProParserTokenTypes.BROWSE);
    return typeNode;
  }

  /** Create the frame if necessary, set its scope if that hasn't already been done. */
  private Frame initializeFrame(Frame frame, Block currentBlock) {
    // If we don't have a frame then get or create the unnamed default frame for the block.
    if (frame == null)
      frame = currentBlock.getDefaultFrame();
    boolean newFrame = frame == null;
    if (newFrame) {
      frame = createFrame("", currentBlock.getSymbolScope());
      frame.setFrameScopeUnnamedDefault(currentBlock);
    }
    if (!frame.isInitialized()) {
      frame.initialize(currentBlock);
      if (!newFrame) {
        frameMRU.remove(frame);
        frameMRU.addFirst(frame);
      }
    }
    return frame;
  }

  /**
   * Deals with frame fields referenced by INPUT and USING. For a Field_ref node where it matches #(Field_ref INPUT
   * ...), determine which frame field is being referenced. This is also called for #(RECORD_NAME ... #(USING
   * #(Field_ref...))). Sets the FieldContainer attribute (a Frame or Browse object) on the Field_ref node.
   * 
   * @see org.prorefactor.core.JPNode#getFieldContainer().
   */
  FieldLookupResult inputFieldLookup(FieldRefNode fieldRefNode, TreeParserSymbolScope currentScope) {
    JPNode idNode = fieldRefNode.getIdNode();
    Field.Name inputName = new Field.Name(idNode.getText().toLowerCase());
    FieldContainer fieldContainer = null;
    Symbol fieldOrVariable = null;
    JPNode tempNode = fieldRefNode.getFirstChild();
    int tempType = tempNode.getType();
    if (tempType == ProParserTokenTypes.INPUT) {
      tempNode = tempNode.getNextSibling();
      tempType = tempNode.getType();
    }
    if (tempType == ProParserTokenTypes.BROWSE || tempType == ProParserTokenTypes.FRAME) {
      fieldContainer = (FieldContainer) tempNode.nextNode().getSymbol();
      fieldOrVariable = fieldContainer.lookupFieldOrVar(inputName);
    } else {
      for (Frame frame : frameMRU) {
        if (!frame.getScope().isActiveIn(currentScope))
          continue;
        fieldOrVariable = frame.lookupFieldOrVar(inputName);
        if (fieldOrVariable != null) {
          fieldContainer = frame;
          break;
        }
      }
    }
    if (fieldOrVariable == null) {
      LOG.error("Could not find input field {} {}:{}", idNode.getText(), idNode.getFileIndex(), idNode.getLine());
      return null;
    }
    fieldRefNode.setFieldContainer(fieldContainer);
    FieldLookupResult.Builder result = new FieldLookupResult.Builder().setSymbol(fieldOrVariable);
    if (!(fieldOrVariable instanceof Variable)) {
      Field.Name resName = new Field.Name(fieldOrVariable.fullName());
      if (inputName.getTable() == null)
        result.setUnqualified();
      if (inputName.getField().length() < resName.getField().length()
          || (inputName.getTable() != null && (inputName.getTable().length() < resName.getTable().length())))
        result.setAbbreviated();
    }
    return result.build();
  }

  /** Receive the node (will be a Field_ref) that follows an @ in a frame phrase. */
  void lexAt(JPNode fieldRefNode) {
    LOG.debug("Enter FrameStack#lexAt");

    if (containerForCurrentStatement != null)
      containerForCurrentStatement.addSymbol(fieldRefNode.getSymbol(), currStatementIsEnabler);
  }

  /** FOR|REPEAT|DO blocks need to be checked for explicit WITH FRAME phrase. */
  void nodeOfBlock(JPNode blockNode, Block currentBlock) {
    LOG.debug("Enter FrameStack#nodeOfBlock");

    JPNode containerTypeNode = getContainerTypeNode(blockNode);
    if (containerTypeNode == null)
      return;
    // No such thing as DO WITH BROWSE...
    assert containerTypeNode.getType() == ProParserTokenTypes.FRAME;
    JPNode frameIDNode = containerTypeNode.nextNode();
    assert frameIDNode.getType() == ProParserTokenTypes.ID;
    Frame frame = frameRefSet(frameIDNode, currentBlock.getSymbolScope());
    frame.setFrameScopeBlockExplicitDefault(((BlockNode) blockNode).getBlock());
    blockNode.setFieldContainer(frame);
    containerForCurrentStatement = frame;
  }

  /** Called at tree parser DEFINE BROWSE statement. */
  void nodeOfDefineBrowse(Browse newBrowseSymbol, JPNode defNode, ParseTree defNode2) {
    LOG.debug("Enter FrameStack#nodeOfDefineBrowse");

    containerForCurrentStatement = newBrowseSymbol;
    containerForCurrentStatement.addStatement(defNode2);
  }

  /**
   * Called at tree parser DEFINE FRAME statement. A DEFINE FRAME statement might hide a frame symbol from a higher
   * symbol scope. A DEFINE FRAME statement is legal for a frame symbol already in use, sort of like how you can have
   * multiple FORM statements, I suppose. A DEFINE FRAME statement does not initialize the frame's scope.
   */
  void nodeOfDefineFrame(ParseTree defNode2, JPNode defNode, JPNode idNode, String frameName, TreeParserSymbolScope currentSymbolScope) {
    LOG.debug("Enter FrameStack#nodeOfDefineFrame");

    Frame frame = (Frame) currentSymbolScope.lookupSymbolLocally(ProParserTokenTypes.FRAME, frameName);
    if (frame == null)
      frame = createFrame(frameName, currentSymbolScope);
    frame.setDefinitionNode(defNode.getIdNode());
    defNode.setLink(IConstants.SYMBOL, frame);
    defNode.setFieldContainer(frame);
    containerForCurrentStatement = frame;
    containerForCurrentStatement.addStatement(defNode2);
  }

  /**
   * For an IO/UI statement which would initialize a frame, compute the frame and set the frame attribute on the
   * statement head node. This is not used from DEFINE FRAME, HIDE FRAME, or any other "frame" statements which would
   * not count as a "reference" for frame scoping purposes.
   */
  void nodeOfInitializingStatement(ParseTree stateNode2, JPNode stateNode, Block currentBlock) {
    LOG.debug("Enter FrameStack#nodeOfInitializingStatement");

    JPNode containerTypeNode = getContainerTypeNode(stateNode);
    JPNode idNode = null;
    if (containerTypeNode != null) {
      idNode = containerTypeNode.nextNode();
      assert idNode.getType() == ProParserTokenTypes.ID;
    }
    if (containerTypeNode != null && containerTypeNode.getType() == ProParserTokenTypes.BROWSE) {
      containerForCurrentStatement = browseRefSet(idNode, currentBlock.getSymbolScope());
    } else {
      Frame frame = null;
      if (idNode != null)
        frame = frameRefSet(idNode, currentBlock.getSymbolScope());
      // This returns the frame whether it already exists or it creates it new.
      frame = initializeFrame(frame, currentBlock);
      containerForCurrentStatement = frame;
    }
    stateNode.setFieldContainer(containerForCurrentStatement);
    containerForCurrentStatement.addStatement(stateNode2);
  }

  /**
   * For frame init statements like VIEW and CLEAR which have no frame phrase. Called at the end of the statement, after
   * all symbols (including FRAME ID) have been resolved.
   */
  void simpleFrameInitStatement(ParseTree headNode2, JPNode headNode, JPNode frameIDNode, Block currentBlock) {
    LOG.debug("Enter FrameStack#simpleFrameInitStatement");

    Frame frame = (Frame) frameIDNode.nextNode().getSymbol();
    assert frame != null;
    initializeFrame(frame, currentBlock);
    headNode.setFieldContainer(frame);
    frame.addStatement(headNode2);
  }

  /** Called at the end of a frame affecting statement. */
  void statementEnd() {
    LOG.debug("Enter FrameStack#statementEnd");

    // For something like DISPLAY customer, we delay adding the fields to the frame until the end of the statement.
    // That's because any fields in an EXCEPT fields phrase need to have their symbols resolved first.
    if (currStatementWholeTableFormItemNode != null) {
      List<FieldBuffer> fields = calculateFormItemTableFields(currStatementWholeTableFormItemNode);
      for (FieldBuffer fieldBuffer : fields) {
        containerForCurrentStatement.addSymbol(fieldBuffer, currStatementIsEnabler);
      }
      currStatementWholeTableFormItemNode = null;
    }
    containerForCurrentStatement = null;
    currStatementIsEnabler = false;
  }

  /** Used only by the tree parser, for ENABLE|UPDATE|PROMPT-FOR. */
  void statementIsEnabler() {
    currStatementIsEnabler = true;
  }

}
