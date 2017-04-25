package org.prorefactor.core.schema;

import java.util.SortedSet;

public interface IDatabase {
  public void add(ITable table);
  public String getName();
  public SortedSet<ITable> getTableSet();
}
