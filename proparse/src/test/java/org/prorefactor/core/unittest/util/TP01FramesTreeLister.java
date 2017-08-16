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
package org.prorefactor.core.unittest.util;

import java.io.PrintWriter;

import org.prorefactor.core.JPNode;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.nodetypes.BlockNode;
import org.prorefactor.core.nodetypes.FieldRefNode;
import org.prorefactor.treeparser.Block;
import org.prorefactor.treeparser.symbols.FieldContainer;
import org.prorefactor.treeparser.symbols.widgets.Frame;

public class TP01FramesTreeLister extends JPNodeLister {

	public TP01FramesTreeLister(JPNode topNode, PrintWriter writer) {
		super(topNode, writer);
	}

	private void appendName(StringBuffer buff, FieldContainer container) {
		if (container.getName().length()==0)
			buff.append('"').append(container.getName()).append('"');
		else
			buff.append(container.getName());
	}

	
//	@Override
//	protected String generateLineText(JPNode node) {
//		StringBuffer buff = new StringBuffer();
//		buff
//			.append(indent())
//			.append(tokenTypes.getName(node.getType()))
//			.append('|')
//			.append(node.getText())
//			.append('|')
//			;
//		if (node instanceof BlockNode) blockNode(buff, (BlockNode)node);
//		if (node instanceof FieldRefNode) {
//			fieldRefNode(buff, (FieldRefNode)node);
//			fieldContainer(buff, node);
//		}
//		if (node.isStateHead()) fieldContainer(buff, node);
//		return buff.toString();
//	}

	@Override
	protected String getExtraInfo(JPNode node, char spacer) {
    StringBuffer buff = new StringBuffer();
//    buff.append(indent(1));
    if (node instanceof BlockNode) blockNode(buff, (BlockNode)node, spacer);
    if (node instanceof FieldRefNode) {
      fieldRefNode(buff, (FieldRefNode)node, spacer);
      fieldContainer(buff, node, spacer);
    }
    if (node.isStateHead()) fieldContainer(buff, node, spacer);
    return buff.toString();
	}

	private void blockNode(StringBuffer buff, BlockNode blockNode, char spacer) {
		Block block = blockNode.getBlock();
		if (block.getDefaultFrame()!=null) {
			buff
//				.append(spacer)
				.append("defaultFrame:").append(spacer)
				;
			appendName(buff, block.getDefaultFrame());
		}
		buff
//			.append(spacer)
			.append("frames:").append(spacer)
			;
		for (Frame frame : block.getFrames()) {
			buff.append(" ");
			appendName(buff, frame);
		}
	}
	
	
	private void fieldContainer(StringBuffer buff, JPNode node, char spacer) {
		FieldContainer fieldContainer = node.getFieldContainer();
		if (fieldContainer==null) return;
		buff
			.append(spacer)
			.append(NodeTypes.getTypeName(fieldContainer.getProgressType()))
			.append("=")
			;
		appendName(buff, fieldContainer);
	}

	
	private void fieldRefNode(StringBuffer buff, FieldRefNode refNode, char spacer) {
		buff
			.append(spacer)
			.append(refNode.getSymbol().fullName())
			;
	}
	
	
}
