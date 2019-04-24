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

import org.prorefactor.treeparser.DataType;
import org.prorefactor.treeparser.Primative;

/**
 * Field objects are created both by the Schema class and they are also created for temp and work table fields defined
 * within a 4gl compile unit.
 */
public class Field implements IField {
  private final String name;
  private int extent;
  private DataType dataType;
  private String className = null;
  private ITable table;

  /**
   * Standard constructor.
   * 
   * @param table Use null if you want to assign the field to a table as a separate step.
   */
  public Field(String inName, ITable table) {
    this.name = inName;
    this.table = table;
    if (table != null)
      table.add(this);
  }

  /** Constructor for temporary "lookup" fields. "Package" visibility. */
  Field(String inName) {
    this.name = inName;
    this.table = Constants.nullTable;
  }

  @Override
  public void assignAttributesLike(Primative likePrim) {
    dataType = likePrim.getDataType();
    className = likePrim.getClassName();
    extent = likePrim.getExtent();
  }

  /**
   * Copy the bare minimum attributes to a new Field object.
   * 
   * @param toTable The table that the field is being added to.
   * @return The newly created Field, though you may not need it for anything since it has already been added to the
   *         Table.
   */
  @Override
  public IField copyBare(ITable toTable) {
    Field f = new Field(this.name, toTable);
    f.dataType = this.dataType;
    f.extent = this.extent;
    f.className = this.className;
    return f;
  }

  @Override
  public String getClassName() {
    return className;
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

  @Override
  public Primative setClassName(String s) {
    this.className = s;
    return this;
  }

  @Override
  public Primative setDataType(DataType dataType) {
    this.dataType = dataType;
    return this;
  }

  @Override
  public Primative setExtent(int extent) {
    this.extent = extent;
    return this;
  }

  /** Use this to set the field to a table if you used null for the table in the constructor. */
  @Override
  public void setTable(ITable table) {
    this.table = table;
    table.add(this);
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
      if (table != null && table.length() > 0) {
        if (db != null && db.length() > 0) {
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
