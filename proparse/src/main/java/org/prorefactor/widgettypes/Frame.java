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
package org.prorefactor.widgettypes;

import org.prorefactor.core.NodeTypes;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.FieldContainer;
import org.prorefactor.treeparser.Symbol;
import org.prorefactor.treeparser.SymbolScope;

public class Frame extends FieldContainer {

  private boolean initialized = false;
  private Block frameScopeBlock = null;

  public Frame() {
    // Only to be used for persistence/serialization
  }

  /** Unlike other symbols, Frames are automatically added to the scope, right here at creation time. */
  public Frame(String name, SymbolScope scope) {
    super(name, scope);
    scope.add(this);
  }

  @Override
  public Symbol copyBare(SymbolScope scope) {
    // Frames cannot be inherited, so we don't have to worry about the other frame attributes.
    return new Frame(getName(), scope);
  }

  public Block getFrameScopeBlock() {
    return frameScopeBlock;
  }

  /**
   * @return NodeTypes.FRAME
   */
  @Override
  public int getProgressType() {
    return NodeTypes.FRAME;
  }

  /**
   * Initialize the frame and set the frame scope if not done already. Returns the frameScopeBlock.
   * 
   * @see #isInitialized()
   */
  public Block initialize(Block block) {
    if (initialized)
      return frameScopeBlock;
    initialized = true;
    if (frameScopeBlock == null)
      frameScopeBlock = block.addFrame(this);
    return frameScopeBlock;
  }

  /**
   * Has this frame been "referenced"? In other words, has it or any of its fields been displayed yet? Has its scope
   * been determined?
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * This should be called for a block with an explicit default. i.e. {DO|FOR|REPEAT} WITH FRAME.
   */
  public void setFrameScopeBlockExplicitDefault(Block block) {
    frameScopeBlock = block;
    block.setDefaultFrameExplicit(this);
  }

  /**
   * This should be called when we need to set a block with this unnamed frame as that block's implicit default. Returns
   * the block that this unnamed/default frame got scoped to. That would be a REPEAT or FOR block, or else the frame's
   * symbol scope.
   */
  public Block setFrameScopeUnnamedDefault(Block block) {
    frameScopeBlock = block.setDefaultFrameImplicit(this);
    return frameScopeBlock;
  }

}
