/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2025 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.api.objects;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.core.schema.Constants;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.ITable;

import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Table;

public class DatabaseWrapper implements IDatabase {
  private final DatabaseDescription dbDesc;

  private final SortedSet<ITable> sortedTables = new TreeSet<>(Constants.TABLE_NAME_ORDER);

  public DatabaseWrapper(DatabaseDescription dbDesc) {
    this.dbDesc = dbDesc;

    for (Table fld : dbDesc.getTables()) {
      ITable iFld = new TableWrapper(this, fld);
      sortedTables.add(iFld);
    }
  }

  public DatabaseDescription getDbDesc() {
    return dbDesc;
  }

  @Override
  public String getName() {
    return dbDesc.getDbName();
  }

  @Override
  public SortedSet<ITable> getTableSet() {
    return Collections.unmodifiableSortedSet(sortedTables);
  }

  @Override
  public void add(ITable table) {
    if (table.getName().startsWith("_") || table.getName().startsWith("SYS")) {
      sortedTables.add(table);
    } else {
      throw new UnsupportedOperationException("Unable to add table " + table.getName());
    }
  }

}
