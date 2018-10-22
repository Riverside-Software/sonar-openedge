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
package org.prorefactor.treeparser.symbols;

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/** A Symbol defined with DEFINE DATASET. */
public class Dataset extends Symbol {
  // Keep the buffers, in order, as part of the DATASET signature
  private final List<TableBuffer> buffers = new ArrayList<>();

  public Dataset(String name, TreeParserSymbolScope scope) {
    super(name, scope);
  }

  /**
   * The treeparser calls this at RECORD_NAME in <code>RECORD_NAME in FOR RECORD_NAME (COMMA RECORD_NAME)*</code>.
   */
  public void addBuffer(TableBuffer buff) {
    buffers.add(buff);
  }

  /** For this subclass of Symbol, fullName() returns the same value as getName(). */
  @Override
  public String fullName() {
    return getName();
  }

  /** Get the list of buffers (in order) which make up this dataset's signature. */
  public List<TableBuffer> getBuffers() {
    return buffers;
  }

  /**
   * Returns ABLNodeType.DATASET
   */
  @Override
  public ABLNodeType getProgressType() {
    return ABLNodeType.DATASET;
  }

}
