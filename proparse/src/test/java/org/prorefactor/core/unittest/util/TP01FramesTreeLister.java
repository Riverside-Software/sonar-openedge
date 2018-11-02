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
package org.prorefactor.core.unittest.util;

import java.io.PrintWriter;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.JPNode;
import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.symbols.FieldContainer;
import org.prorefactor.treeparser.symbols.Symbol;
import org.prorefactor.treeparser.symbols.widgets.Frame;

public class TP01FramesTreeLister extends JPNodeLister {

  public TP01FramesTreeLister(JPNode topNode, PrintWriter writer) {
    super(topNode, writer);
  }

  private void appendName(StringBuffer buff, FieldContainer container) {
    if (container.getName().length() == 0)
      buff.append('"').append(container.getName()).append('"');
    else
      buff.append(container.getName());
  }

  @Override
  protected String getExtraInfo(JPNode node, char spacer) {
    StringBuffer buff = new StringBuffer();
    // buff.append(indent(1));
    if (node instanceof BlockNode)
      blockNode(buff, (BlockNode) node, spacer);
    if (node instanceof FieldRefNode) {
      fieldRefNode(buff, (FieldRefNode) node, spacer);
      fieldContainer(buff, node, spacer);
    }
    if (node.isStateHead())
      fieldContainer(buff, node, spacer);
    return buff.toString();
  }

  private void blockNode(StringBuffer buff, BlockNode blockNode, char spacer) {
    Block block = blockNode.getBlock();
    if (block.getDefaultFrame() != null) {
      buff
        // .append(spacer)
        .append("defaultFrame:").append(spacer);
      appendName(buff, block.getDefaultFrame());
    }
    buff
      // .append(spacer)
      .append("frames:").append(spacer);
    for (Frame frame : block.getFrames()) {
      buff.append(" ");
      appendName(buff, frame);
    }
  }

  private void fieldContainer(StringBuffer buff, JPNode node, char spacer) {
    FieldContainer fieldContainer = node.getFieldContainer();
    if (fieldContainer == null)
      return;
    buff.append(spacer).append(ABLNodeType.getNodeType(fieldContainer.getProgressType())).append("=");
    appendName(buff, fieldContainer);
  }

  private void fieldRefNode(StringBuffer buff, FieldRefNode refNode, char spacer) {
    Symbol symbol = refNode.getSymbol();
    buff.append(spacer).append(symbol == null ? "" : symbol.fullName());
  }

}
