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

import org.prorefactor.treeparser.symbols.FieldBuffer;
import org.prorefactor.treeparser.symbols.ISymbol;

/**
 * For field lookups, we need to be able to pass back the BufferScope object as well as the Field object.
 */
public class FieldLookupResult {
  private boolean abbreviated;
  private boolean unqualified;
  private BufferScope bufferScope;
  private ISymbol symbol;

  private FieldLookupResult() {
    // Use Builder object
  }

  public boolean isAbbreviated() {
    return abbreviated;
  }

  public boolean isUnqualified() {
    return unqualified;
  }

  public BufferScope getBufferScope() {
    return bufferScope;
  }

  public ISymbol getSymbol() {
    return symbol;
  }

  public static class Builder {
    private boolean isAbbreviated;
    private boolean isUnqualified;
    private BufferScope bufferScope;
    private ISymbol symbol;

    public Builder setAbbreviated() {
      this.isAbbreviated = true;
      return this;
    }

    public Builder setUnqualified() {
      this.isUnqualified = true;
      return this;
    }

    public Builder setBufferScope(BufferScope bufferScope) {
      this.bufferScope = bufferScope;
      return this;
    }

    public Builder setSymbol(ISymbol symbol) {
      this.symbol = symbol;
      return this;
    }

    public FieldBuffer getField() {
      return (FieldBuffer) symbol;
    }

    public FieldLookupResult build() {
      if (symbol == null)
        throw new NullPointerException("Symbol can't be null in FieldLookupResult");
      FieldLookupResult result = new FieldLookupResult();
      result.abbreviated = isAbbreviated;
      result.unqualified = isUnqualified;
      result.bufferScope = bufferScope;
      result.symbol = symbol;

      return result;
    }
  }
}
