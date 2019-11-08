/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2019 Riverside Software
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
package org.prorefactor.core.nodetypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;
import org.prorefactor.treeparser.Block;

/**
 * Specialized type of JPNode for those token types: DO, FOR, REPEAT, FUNCTION, PROCEDURE, CONSTRUCTOR, DESTRUCTOR,
 * METHOD, CANFIND, CATCH, ON, PROPERTY_GETTER, PROPERTY_SETTER
 */
public class BlockNode extends JPNode {
  public BlockNode(ProToken t, JPNode parent, int num, boolean hasChildren) {
    super(t, parent, num, hasChildren);
  }

  @Nullable
  public Block getBlock() {
    return (Block) getLink(IConstants.BLOCK);
  }

  public void setBlock(@Nonnull Block block) {
    setLink(IConstants.BLOCK, block);
  }

}
