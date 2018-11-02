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
package org.prorefactor.core.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.core.IConstants;

/**
 * Table objects are created both by the Schema class and also when temp and work tables are defined within a 4gl
 * compile unit. For temp and work tables, the database is Schema.nullDatabase.
 */
public class Table implements ITable {
  private final IDatabase database;
  private final String name;
  private final int storetype;

  private List<IField> fieldPosOrder = new ArrayList<>();
  private List<IIndex> indexes = new ArrayList<>();
  private SortedSet<IField> fieldSet = new TreeSet<>(Constants.FIELD_NAME_ORDER);

  /** Constructor for schema */
  public Table(String name, IDatabase database) {
    this.name = name;
    this.database = database;
    this.storetype = IConstants.ST_DBTABLE;
    database.add(this);
  }

  /** Constructor for temp / work tables */
  public Table(String name, int storetype) {
    this.name = name;
    this.storetype = storetype;
    this.database = Constants.nullDatabase;
  }

  /** Constructor for temporary "comparator" objects. */
  public Table(String name) {
    this.name = name;
    this.storetype = IConstants.ST_DBTABLE;
    database = Constants.nullDatabase;
  }

  @Override
  public void add(IField field) {
    fieldSet.add(field);
    fieldPosOrder.add(field);
  }

  @Override
  public void add(IIndex index) {
    indexes.add(index);
  }

  @Override
  public IDatabase getDatabase() {
    return database;
  }

  @Override
  public List<IField> getFieldPosOrder() {
    return fieldPosOrder;
  }

  @Override
  public SortedSet<IField> getFieldSet() {
    return fieldSet;
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
  public int getStoretype() {
    return storetype;
  }

  @Override
  public IField lookupField(String lookupName) {
    SortedSet<IField> fieldTailSet = fieldSet.tailSet(new Field(lookupName));
    if (fieldTailSet.isEmpty())
      return null;
    IField field = fieldTailSet.first();
    if (field == null || !field.getName().toLowerCase().startsWith(lookupName.toLowerCase()))
      return null;
    return field;
  }

  @Override
  public String toString() {
    return new StringBuilder(storetype == IConstants.ST_DBTABLE ? "DB Table"
        : storetype == IConstants.ST_TTABLE ? "Temp-table" : "Work-table").append(' ').append(name).toString();
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
