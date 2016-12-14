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
package org.prorefactor.core.schema;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.core.IConstants;
import org.prorefactor.treeparser.SymbolScopeRoot;

/**
 * Table objects are created both by the Schema class and also when temp and work tables are defined within a 4gl
 * compile unit. For temp and work tables, the database is Schema.nullDatabase.
 */
public class Table {
  /** Comparator for sorting by name. */
  public static final Comparator<Table> NAME_ORDER = new Comparator<Table>() {
    @Override
    public int compare(Table t1, Table t2) {
      return t1.name.compareToIgnoreCase(t2.name);
    }
  };

  private int storetype = IConstants.ST_DBTABLE;
  private List<Field> fieldPosOrder = new ArrayList<>();
  private String name;
  private Database database;
  private SortedSet<Field> fieldSet = new TreeSet<>(Field.NAME_ORDER);

  /** Constructor for schema */
  public Table(String name, Database database) {
    this.name = name;
    this.database = database;
    database.add(this);
  }

  /** Constructor for temp / work tables */
  public Table(String name, int storetype) {
    this.name = name;
    this.storetype = storetype;
    this.database = Schema.nullDatabase;
  }

  /** Constructor for temporary "comparator" objects. */
  public Table(String name) {
    this.name = name;
    database = Schema.nullDatabase;
  }

  /** Add a Field to this table. "Package" visibility only. */
  void add(Field field) {
    fieldSet.add(field);
    fieldPosOrder.add(field);
  }

  /**
   * Create a bare minimum copy of a Table definition. No-op if the table already exists in the requested scope. Copies
   * all of the field definitions as well.
   * 
   * @param scope The scope that this table is to be added to.
   * @return The newly created table, or the existing one from the scope if it has previously been defined.
   */
  public Table copyBare(SymbolScopeRoot scope) {
    Table t = scope.lookupTableDefinition(this.name);
    if (t != null)
      return t;
    t = new Table(this.name, this.storetype);
    for (Field field : this.fieldPosOrder) {
      field.copyBare(t);
    }
    return t;
  }

  public Database getDatabase() {
    return database;
  }

  /** Get the ArrayList of fields in field position order (rather than sorted alpha). */
  public List<Field> getFieldPosOrder() {
    return fieldPosOrder;
  }

  public SortedSet<Field> getFieldSet() {
    return fieldSet;
  }

  public String getName() {
    return name;
  }

  public int getStoretype() {
    return storetype;
  }

  /**
   * Lookup a field by name. We do not test for uniqueness. We leave that job to the compiler. This function expects an
   * unqualified field name (no name dots).
   */
  public Field lookupField(String lookupName) {
    java.util.SortedSet<Field> fieldTailSet = fieldSet.tailSet(new Field(lookupName));
    if (fieldTailSet.isEmpty())
      return null;
    Field field = fieldTailSet.first();
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
