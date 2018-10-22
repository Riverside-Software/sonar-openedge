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
package org.prorefactor.treeparser.symbols.widgets;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.ITreeParserSymbolScope;
import org.prorefactor.treeparser.symbols.FieldContainer;

public class Frame extends FieldContainer {

  private boolean initialized = false;
  private Block frameScopeBlock = null;

  /** Unlike other symbols, Frames are automatically added to the scope, right here at creation time. */
  public Frame(String name, ITreeParserSymbolScope scope) {
    super(name, scope);
    scope.add(this);
  }

  public Block getFrameScopeBlock() {
    return frameScopeBlock;
  }

  /**
   * @return ABLNodeType.FRAME
   */
  @Override
  public ABLNodeType getProgressType() {
    return ABLNodeType.FRAME;
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
