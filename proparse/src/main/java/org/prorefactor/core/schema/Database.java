/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
package org.prorefactor.core.schema;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Database objects are created by the Schema class, and they are used when looking up table names from 4gl compile
 * units.
 */
public class Database implements IDatabase {
  private final String name;
  private final SortedSet<ITable> tableSet = new TreeSet<>(Constants.TABLE_NAME_ORDER);

  /**
   * New Database object
   * @param name Main DB name
   */
  public Database(String name) {
    this.name = name;
  }

  @Override
  public void add(ITable table) {
    tableSet.add(table);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public SortedSet<ITable> getTableSet() {
    return tableSet;
  }

  @Override
  public String toString() {
    return new StringBuilder("DB ").append(name).toString();
  }
}
