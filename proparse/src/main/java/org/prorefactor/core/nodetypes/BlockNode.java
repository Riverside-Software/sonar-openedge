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
package org.prorefactor.core.nodetypes;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.Block;

public class BlockNode extends JPNode {
  private static final long serialVersionUID = 6062037678978630381L;

  public BlockNode(ProToken t) {
    super(t);
  }

  public Block getBlock() {
    Block block = (Block) getLink(IConstants.BLOCK);
    assert block != null;
    return block;
  }

  public void setBlock(Block block) {
    setLink(IConstants.BLOCK, block);
  }

}
