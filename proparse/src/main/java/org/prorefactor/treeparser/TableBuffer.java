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
package org.prorefactor.treeparser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.NodeTypes;
import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.Table;

/**
 * A TableBuffer is a Symbol which provides a link from the syntax tree to a Table object.
 */
public class TableBuffer extends Symbol {
  private final Table table;
  private final boolean isDefault;
  private final Map<Field, FieldBuffer> fieldBuffers = new HashMap<>();

  /**
   * Constructor for a named buffer.
   * 
   * @param name Input "" for an unnamed or default buffer
   */
  public TableBuffer(String name, SymbolScope scope, Table table) {
    super(name, scope);
    this.table = table;
    this.isDefault = name.isEmpty();
  }

  void addFieldBuffer(FieldBuffer fieldBuffer) {
    fieldBuffers.put(fieldBuffer.getField(), fieldBuffer);
  }

  /** For temp/work table, also adds Table definition to the scope if it doesn't already exist. */
  @Override
  public Symbol copyBare(SymbolScope scope) {
    Table t;
    if (this.table.getStoretype() == IConstants.ST_DBTABLE) {
      t = this.table;
    } else {
      // Make sure temp/work table definition exists in target root scope.
      SymbolScopeRoot rootScope = scope.getRootScope();
      t = rootScope.lookupTableDefinition(table.getName());
      if (t == null)
        t = table.copyBare(rootScope);
    }
    String useName = this.isDefault ? "" : super.getName();
    return new TableBuffer(useName, scope, t);
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
   * @see org.prorefactor.treeparser.Symbol#getProgressType()
   * @see org.prorefactor.core.schema.Table#getStoretype()
   */
  @Override
  public int getProgressType() {
    return NodeTypes.BUFFER;
  }

  /** Get or create a FieldBuffer for a Field. */
  public FieldBuffer getFieldBuffer(Field field) {
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

  public Table getTable() {
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
