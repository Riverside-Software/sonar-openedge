/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
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
