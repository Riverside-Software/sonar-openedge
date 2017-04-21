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
