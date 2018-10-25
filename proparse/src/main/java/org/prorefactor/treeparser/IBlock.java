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
package org.prorefactor.treeparser;

import java.util.List;

import org.prorefactor.core.nodetypes.RecordNameNode;
import org.prorefactor.treeparser.symbols.ITableBuffer;
import org.prorefactor.treeparser.symbols.widgets.Frame;

/**
 * Represents objects that have a value.
 */
public interface IBlock {
  /**
   * Get the node for this block. Returns a node of one of these types:
   * Program_root/DO/FOR/REPEAT/EDITING/PROCEDURE/FUNCTION/ON/TRIGGERS.
   */
  Object getNode();

  ITreeParserSymbolScope getSymbolScope();

  /** Find or create a buffer for the input BufferSymbol */
  BufferScope getBufferForReference(ITableBuffer symbol);

  /**
   * Create a "weak" buffer scope. This is called within a FOR or PRESELECT statement.
   * 
   * @param symbol The RECORD_NAME node. It must already have the BufferSymbol linked to it.
   */
  BufferScope addWeakBufferScope(ITableBuffer symbol);

  /**
   * Create a "strong" buffer scope. This is called within a DO FOR or REPEAT FOR statement. A STRONG scope prevents the
   * scope from being raised to an enclosing block. Note that the compiler performs additional checks here that we
   * don't.
   * 
   * @param node The RECORD_NAME node. It must already have the BufferSymbol linked to it.
   */
  void addStrongBufferScope(RecordNameNode node);
  
  /**
   * A "hidden cursor" is a BufferScope which has no side-effects on surrounding blocks like strong, weak, and reference
   * scopes do. These are used within a CAN-FIND function. (2004.Sep:John: Maybe in triggers too? Haven't checked.)
   * 
   * @param node The RECORD_NAME node. Must have the BufferSymbol linked to it already.
   */
  void addHiddenCursor(RecordNameNode node);

  /**
   * Add a reference to a BufferScope to this and all outer blocks. These references are required for duplicating
   * Progress's scope and "raise scope" behaviours. BufferScope references are not added up past the symbol's scope.
   */
  void addBufferScopeReferences(BufferScope bufferScope);

  
  BufferScope getBufferForReferenceSub(ITableBuffer symbol);

  BufferScope getBufferScopeSub(ITableBuffer symbol, BufferScope.Strength creating);
  
  /** Find nearest BufferScope for a BufferSymbol, if any */
  BufferScope findBufferScope(ITableBuffer symbol);

  void setParent(IBlock parent);

  /** This returns the <em>block of the parent scope</em>. */
  IBlock getParent();

  /**
   * From the nearest frame scoping block, get the default (possibly unnamed) frame if it exists. Returns null if no
   * default frame has been established yet.
   */
  Frame getDefaultFrame();

  /** Get a copy of the list of frames scoped to this block. */
  List<Frame> getFrames();

  /**
   * Called by Frame.setFrameScopeBlock() - not intended to be called by any client code. This should only be called by
   * the Frame object itself. Adds a frame to this or the appropriate parent block. Returns the scoping block. Frames
   * are scoped to FOR and REPEAT blocks, or else to a symbol scoping block. They may also be scoped with a DO WITH
   * FRAME block, but that is handled elsewhere.
   */
  IBlock addFrame(Frame frame);

  /**
   * Explicitly set the default frame for this block. This should only be called by the Frame object itself. This is
   * especially important to be called for DO WITH FRAME statements because DO blocks do not normally scope frames. This
   * should also be called for REPEAT WITH FRAME and FOR WITH FRAME blocks.
   */
  void setDefaultFrameExplicit(Frame frame);

  /**
   * In the nearest frame scoping block, set the default implicit (unnamed) frame. This should only be called by the
   * Frame object itself. Returns the Block that scopes the frame.
   */
  IBlock setDefaultFrameImplicit(Frame frame);

  /**
   * Find a field based on buffers which are referenced in nearest enclosing blocks. Note that the compiler enforces
   * uniqueness here. We don't, we just find the first possible and return it.
   */
  FieldLookupResult lookupUnqualifiedField(String name);

  /**
   * General lookup for Field or Variable. Does not guarantee uniqueness. That job is left to the compiler.
   */
  FieldLookupResult lookupField(String name, boolean getBufferScope);

}
