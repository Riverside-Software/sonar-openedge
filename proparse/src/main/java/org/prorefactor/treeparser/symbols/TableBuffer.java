/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.IConstants;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ITable;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/**
 * A TableBuffer is a Symbol which provides a link from the syntax tree to a Table object.
 */
public class TableBuffer extends Symbol {
  private final ITable table;
  private final boolean isDefault;
  private final Map<IField, FieldBuffer> fieldBuffers = new HashMap<>();

  /**
   * Constructor for a named buffer.
   * 
   * @param name Input "" for an unnamed or default buffer
   */
  public TableBuffer(String name, TreeParserSymbolScope scope, ITable table) {
    super(name, scope);
    this.table = table;
    this.isDefault = name.isEmpty();
  }

  void addFieldBuffer(FieldBuffer fieldBuffer) {
    fieldBuffers.put(fieldBuffer.getField(), fieldBuffer);
  }

  /**
   * Return fully qualified table name (with DB) of the table buffer is pointing to
   */
  public String getTargetFullName() {
    if (table.getStoretype() == IConstants.ST_DBTABLE)
      return new StringBuilder(table.getDatabase().getName()).append(".").append(table.getName()).toString();
    else
      return table.getName();
  }

  /**
   * Get the "database.buffer" name for schema buffers, get "buffer" for temp/work table buffers.
   */
  @Override
  public String fullName() {
    if (table.getStoretype() != IConstants.ST_DBTABLE)
      return getName();

    return new StringBuilder(table.getDatabase().getName()).append(".").append(getName()).toString();
  }

  /** Get a list of FieldBuffer symbols that have been created for this TableBuffer. */
  public Collection<FieldBuffer> getFieldBufferList() {
    return fieldBuffers.values();
  }

  /**
   * Always returns BUFFER, whether this is a named buffer or a default buffer.
   * 
   * @see org.prorefactor.treeparser.symbols.Symbol#getProgressType()
   * @see org.prorefactor.core.schema.ITable#getStoretype()
   */
  @Override
  public ABLNodeType getNodeType() {
    return ABLNodeType.BUFFER;
  }

  @Override
  public int getProgressType() {
    return getNodeType().getType();
  }

  /** Get or create a FieldBuffer for a Field. */
  public FieldBuffer getFieldBuffer(IField field) {
    assert field.getTable() == this.table;
    FieldBuffer ret = fieldBuffers.get(field);
    if (ret != null)
      return ret;
    ret = new FieldBuffer(this.getScope(), this, field);
    fieldBuffers.put(field, ret);
    return ret;
  }

  /**
   * Get the name of the buffer (overrides Symbol.getName). Returns the name of the table for default (unnamed) buffers.
   */
  @Override
  public String getName() {
    if (super.getName().isEmpty()) {
      return table.getName();
    }

    return super.getName();
  }

  public ITable getTable() {
    return table;
  }

  /** Is this the default (unnamed) buffer? */
  public boolean isDefault() {
    return isDefault;
  }

  /** Is this a default (unnamed) buffer for a schema table? */
  public boolean isDefaultSchema() {
    return isDefault && table.getStoretype() == IConstants.ST_DBTABLE;
  }

}
