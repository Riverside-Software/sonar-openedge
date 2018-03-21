/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
package org.prorefactor.treeparser;

import org.prorefactor.core.JPNode;

/**
 * Represents a record used in semantic processing. It is a base class for more specific semantic records, which can be
 * definitions that appear in the SymbolTable, references to previously defined items or other things of semantic
 * significance.
 * 
 * @author pcd
 *
 */
public class SemanticRecord {

  protected JPNode node;

  public SemanticRecord() {
    node = null;
  }

  public SemanticRecord(JPNode node) {
    this.node = node;
  }

  public int getColumn() {
    return node.getColumn();
  }

  public String getFilename() {
    return node.getFilename();
  }

  public int getLine() {
    return node.getLine();
  }

}
