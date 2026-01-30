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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Table objects are created both by the Schema class and also when temp and work tables are defined within a 4gl
 * compile unit. For temp and work tables, the database is Schema.nullDatabase.
 */
public class Table implements ITable {
  private final IDatabase database;
  private final String name;
  private final TableType type;
  private boolean parentNoUndo = false;
  private boolean undo = false;
  private boolean noUndo = false;

  private List<IIndex> indexes = new ArrayList<>();
  private Set<IField> fieldSet = new HashSet<>();

  /** Constructor for schema */
  public Table(@Nonnull String name, @Nonnull IDatabase database) {
    this.name = Objects.requireNonNull(name);
    this.database = Objects.requireNonNull(database);
    this.type = TableType.DB_TABLE;
  }

  /** Constructor for temp / work tables */
  @Deprecated
  public Table(String name, int storetype) {
    this(name, TableType.getTableType(storetype));
  }

  public Table(@Nonnull String name, @Nonnull TableType type) {
    this.name = Objects.requireNonNull(name);
    this.type = Objects.requireNonNull(type);
    this.database = Constants.nullDatabase;
  }

  public void add(IField field) {
    fieldSet.add(field);
  }

  public void add(IIndex index) {
    indexes.add(index);
  }

  @Override
  public IDatabase getDatabase() {
    return database;
  }

  @Override
  public List<IField> getFieldPosOrder() {
    return fieldSet.stream().sorted(Constants.FIELD_POSITION_ORDER).toList();
  }

  @Override
  public List<IField> getFieldSet() {
    return fieldSet.stream().sorted(Constants.FIELD_NAME_ORDER).toList();
  }

  @Override
  public List<IIndex> getIndexes() {
    return indexes;
  }

  @Override
  public IIndex lookupIndex(String name) {
    for (IIndex idx : indexes) {
      if (idx.getName().equalsIgnoreCase(name))
        return idx;
    }
    return null;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public TableType getTableType() {
    return type;
  }

  public void setParentNoUndo(boolean parentNoUndo) {
    this.parentNoUndo = parentNoUndo;
  }

  public void setNoUndo(boolean noUndo) {
    this.noUndo = noUndo;
  }

  public void setUndo(boolean undo) {
    this.undo = undo;
  }

  @Override
  public boolean isNoUndo() {
    if (undo)
      return false;
    else if (noUndo)
      return true;
    else 
      return parentNoUndo;
  }

  @Override
  public String toString() {
    return new StringBuilder(type.toString()).append(' ').append(name).toString();
  }

  /**
   * This is a convenience class for working with a string table name, where there may or may not be a database
   * qualifier in the name.
   */
  public static class Name {
    private final String db;
    private final String table;

    public Name(String dbPart, String tablePart) {
      db = dbPart;
      table = tablePart;
    }

    public Name(String name) {
      String[] parts = name.split("\\.");
      if (parts.length == 1) {
        db = null;
        table = parts[0];
      } else {
        db = parts[0];
        table = parts[1];
      }
    }

    public String getDb() {
      return db;
    }

    public String getTable() {
      return table;
    }

    public String generateName() {
      StringBuilder buff = new StringBuilder();
      if (db != null && db.length() > 0) {
        buff.append(db);
        buff.append(".");
      }
      buff.append(table);
      return buff.toString();
    }
  }
}
