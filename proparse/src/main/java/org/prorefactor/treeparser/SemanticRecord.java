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
