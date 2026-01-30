/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2026 Riverside Software
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
package org.prorefactor.core.schema;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.prorefactor.treeparser.Primitive;

import eu.rssw.pct.elements.DataType;

/**
 * Field objects are created both by the Schema class and they are also created for temp and work table fields defined
 * within a 4gl compile unit.
 */
public class Field implements IField {
  private final String name;
  private final int extent;
  private final DataType dataType;
  private final ITable table;

  /**
   * Standard constructor.
   */
  private Field(@Nonnull String inName, @Nonnull ITable table, @Nonnull DataType dataType, int extent) {
    this.name = Objects.requireNonNull(inName);
    this.table = Objects.requireNonNull(table);
    this.dataType = Objects.requireNonNull(dataType);
    this.extent = extent;
  }


  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public int getExtent() {
    return extent;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public ITable getTable() {
    return table;
  }

  public static class Builder {
    private String name;
    private ITable parent;
    private DataType dataType;
    private int extent = 0;

    public Builder(String name) {
      this.name = name;
    }

    public Builder setParent(ITable parent) {
      this.parent = parent;
      return this;
    }

    public Builder setDataType(DataType dataType) {
      this.dataType = dataType;
      return this;
    }

    public Builder setExtent(int extent) {
      this.extent = extent;
      return this;
    }

    public Builder assignAttributesLike(Primitive likePrim) {
      this.dataType = likePrim.getDataType();
      this.extent = likePrim.getExtent();
      return this;
    }

    public Field build() {
      return new Field(name, parent, dataType, extent);
    }
  }

  /**
   * This is a convenience class for working with a string field name, where there may or may not be a database or table
   * qualifier in the name.
   */
  public static class Name {
    private final String db;
    private final String table;
    private final String field;

    public Name(String dbPart, String tablePart, String fieldPart) {
      db = dbPart;
      table = tablePart;
      field = fieldPart;
    }

    public Name(String name) {
      String[] parts = name.split("\\.");
      if (parts.length == 1) {
        db = null;
        table = null;
        field = parts[0];
      } else if (parts.length == 2) {
        db = null;
        table = parts[0];
        field = parts[1];
      } else {
        db = parts[0];
        table = parts[1];
        field = parts[2];
      }
    }

    public String getDb() {
      return db;
    }

    public String getTable() {
      return table;
    }

    public String getField() {
      return field;
    }

    public String generateName() {
      StringBuilder buff = new StringBuilder();
      if (table != null && !table.isEmpty()) {
        if (db != null && !db.isEmpty()) {
          buff.append(db);
          buff.append(".");
        }
        buff.append(table);
        buff.append(".");
      }
      buff.append(field);
      return buff.toString();
    }
  }

}
