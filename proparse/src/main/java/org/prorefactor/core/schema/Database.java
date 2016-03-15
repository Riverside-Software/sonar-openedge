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

import java.io.IOException;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.xfer.DataXferStream;
import org.prorefactor.xfer.Xferable;

/**
 * Database objects are created by the Schema class, and they are used when looking up table names from 4gl comile
 * units. "id" field is a database number, starting at one. Might be the logical database number - depends on how you
 * use this.
 */
public class Database implements Xferable {
  /** Comparator for sorting by name. */
  public static final Comparator<Database> NAME_ORDER = new Comparator<Database>() {
    @Override
    public int compare(Database d1, Database d2) {
      return d1.getName().compareToIgnoreCase(d2.getName());
    }
  };

  private String name;
  private SortedSet<Table> tableSet = new TreeSet<>(Table.NAME_ORDER);

  public Database() {
    // For Xferable
  }

  public Database(String name) {
    this.setName(name);
  }

  public void add(Table table) {
    tableSet.add(table);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SortedSet<Table> getTableSet() {
    return tableSet;
  }

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    out.writeRef(name);
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    out.schemaRef("name");
  }
}
