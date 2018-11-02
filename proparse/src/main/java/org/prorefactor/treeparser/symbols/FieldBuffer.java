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

import org.prorefactor.core.schema.Field;
import org.prorefactor.core.schema.IField;
import org.prorefactor.core.schema.ISchema;
import org.prorefactor.proparse.ProParserTokenTypes;
import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;
import org.prorefactor.treeparser.TreeParserSymbolScope;

/**
 * FieldBuffer is the Symbol object linked to from the AST for schema, temp, and work table fields, and FieldBuffer
 * provides the link to the Field object.
 */
public class FieldBuffer extends Symbol implements Primative {
  private final TableBuffer buffer;
  private final IField field;

  /**
   * When you create a FieldBuffer object, you do not set the name, because that comes from the Field object.
   */
  public FieldBuffer(TreeParserSymbolScope scope, TableBuffer buffer, IField field) {
    super("", scope);
    this.buffer = buffer;
    this.field = field;
    buffer.addFieldBuffer(this);
  }

  @Override
  public void assignAttributesLike(Primative likePrim) {
    field.assignAttributesLike(likePrim);
  }

  /**
   * Could this FieldBuffer be referenced by the input name? Input Field.Name must already be all lowercase. Deals with
   * abbreviations, unqualified table/database, and db aliases.
   */
  public boolean canMatch(Field.Name input) {
    // Assert that the input name is already lowercase.
    assert input.generateName().equalsIgnoreCase(input.generateName());
    Field.Name self = new Field.Name(this.fullName().toLowerCase());
    if (input.getDb() != null) {
      ISchema schema = getScope().getRootScope().getRefactorSession().getSchema();
      if (this.buffer.getTable().getDatabase() != schema.lookupDatabase(input.getDb()))
        return false;
    }
    if (input.getTable() != null) {
      if (buffer.isDefaultSchema()) {
        if (!self.getTable().startsWith(input.getTable()))
          return false;
      } else {
        // Temp/work/buffer names can't be abbreviated.
        if (!self.getTable().equals(input.getTable()))
          return false;
      }
    }
    if (!self.getField().startsWith(input.getField()))
      return false;
    return true;
  }

  /**
   * Get "database.buffer.field" for schema fields, or "buffer.field" for temp/work table fields.
   */
  @Override
  public String fullName() {
    StringBuilder buff = new StringBuilder(buffer.fullName());
    buff.append(".");
    buff.append(field.getName());
    return buff.toString();
  }

  public TableBuffer getBuffer() {
    return buffer;
  }

  /**
   * Gets the underlying Field's className (or null if not a class).
   * 
   * @see Primative#getClassName()
   */
  @Override
  public String getClassName() {
    return field.getClassName();
  }

  /** Gets the underlying Field's dataType. */
  @Override
  public DataType getDataType() {
    return field.getDataType();
  }

  /** The extent comes from the underlying Field. */
  @Override
  public int getExtent() {
    return field.getExtent();
  }

  public IField getField() {
    return field;
  }

  /** Returns the Field name. There is no "field buffer name". */
  @Override
  public String getName() {
    return field.getName();
  }

  /**
   * Always returns FIELD.
   * 
   * @see org.prorefactor.treeparser.symbols.Symbol#getProgressType() To see if this field buffer is for a schema table,
   *      temp-table, or work-table, see Table.getStoreType().
   * @see org.prorefactor.core.schema.ITable#getStoretype()
   */
  @Override
  public int getProgressType() {
    return ProParserTokenTypes.FIELD;
  }

  /** Sets the underlying Field's className. */
  @Override
  public Primative setClassName(String className) {
    field.setClassName(className);
    return this;
  }

  /** Sets the underlying Field's dataType. */
  @Override
  public Primative setDataType(DataType dataType) {
    field.setDataType(dataType);
    return this;
  }

  /** Sets the extent of the underlying Field. */
  @Override
  public Primative setExtent(int extent) {
    field.setExtent(extent);
    return this;
  }

}
