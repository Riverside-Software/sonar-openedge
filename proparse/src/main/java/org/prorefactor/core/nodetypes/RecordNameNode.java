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
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.TableBuffer;

public class RecordNameNode extends JPNode {
  private static final long serialVersionUID = 8045143516803910613L;

  /**
   * For creating from persistent storage
   */
  public RecordNameNode() {
    super();
  }

  public RecordNameNode(int file, int line, int column) {
    super(file, line, column);
  }

  public RecordNameNode(ProToken t) {
    super(t);
  }

  public boolean hasTableBuffer() {
    return getLink(IConstants.SYMBOL) != null;
  }

  public boolean hasBufferScope() {
    return getLink(IConstants.BUFFERSCOPE) != null;
  }

  public BufferScope getBufferScope() {
    BufferScope bufferScope = (BufferScope) getLink(IConstants.BUFFERSCOPE);
    assert bufferScope != null;
    return bufferScope;
  }

  /**
   * Every JPNode subtype has its own index. Used for persistent storage.
   */
  @Override
  public int getSubtypeIndex() {
    return 4;
  }

  public TableBuffer getTableBuffer() {
    TableBuffer buffer = (TableBuffer) getLink(IConstants.SYMBOL);
    assert buffer != null;
    return buffer;
  }

  public void setBufferScope(BufferScope bufferScope) {
    assert bufferScope != null;
    setLink(IConstants.BUFFERSCOPE, bufferScope);
  }

  public void setTableBuffer(TableBuffer buffer) {
    setLink(IConstants.SYMBOL, buffer);
  }

}
