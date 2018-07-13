package org.prorefactor.core.unittest.util;

import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.ITable;

public class EmptyDatabase implements IDatabase {
  private final SortedSet<ITable> tables = new TreeSet<>();

  @Override
  public void add(ITable table) {
    // No implementation
  }

  @Override
  public String getName() {
    return "dictdb";
  }

  @Override
  public SortedSet<ITable> getTableSet() {
    return Collections.unmodifiableSortedSet(tables);
  }

}
