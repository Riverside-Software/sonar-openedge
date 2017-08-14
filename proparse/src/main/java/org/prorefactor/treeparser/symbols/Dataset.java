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
package org.prorefactor.treeparser.symbols;

import java.util.ArrayList;
import java.util.List;

import org.prorefactor.core.NodeTypes;
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

  @Override
  public Symbol copyBare(TreeParserSymbolScope scope) {
    return new Dataset(getName(), scope);
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
   * Returns NodeTypes.DATASET.
   */
  @Override
  public int getProgressType() {
    return NodeTypes.DATASET;
  }

}
