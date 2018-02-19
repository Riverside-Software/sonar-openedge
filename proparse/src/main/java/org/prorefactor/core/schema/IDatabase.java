package org.prorefactor.core.schema;

import java.util.SortedSet;

/**
 * Database definition
 */
public interface IDatabase {
  /**
   * Adds table definition
   */
  public void add(ITable table);

  /**
   * @return Database name
   */
  public String getName();

  /**
   * @return Sorted (by table name) list of tables
   */
  public SortedSet<ITable> getTableSet();
}
