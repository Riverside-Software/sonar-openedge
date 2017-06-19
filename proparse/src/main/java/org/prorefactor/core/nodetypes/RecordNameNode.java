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
import org.prorefactor.proparse.SymbolScope.FieldType;
import org.prorefactor.treeparser.BufferScope;
import org.prorefactor.treeparser.symbols.TableBuffer;

public class RecordNameNode extends JPNode {
  private static final long serialVersionUID = 8045143516803910613L;

  public RecordNameNode(ProToken t) {
    super(t);
  }

  public BufferScope getBufferScope() {
    BufferScope bufferScope = (BufferScope) getLink(IConstants.BUFFERSCOPE);
    assert bufferScope != null;
    return bufferScope;
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

  /** Set the 'store type' attribute on a RECORD_NAME node. */
  public void setStoreType(FieldType tabletype) {
    switch (tabletype) {
      case DBTABLE:
        attrSet(IConstants.STORETYPE, IConstants.ST_DBTABLE);
        break;
      case TTABLE:
        attrSet(IConstants.STORETYPE, IConstants.ST_TTABLE);
        break;
      case WTABLE:
        attrSet(IConstants.STORETYPE, IConstants.ST_WTABLE);
        break;
      case VARIABLE:
        // Never happens
        break;
    }
  }

}
