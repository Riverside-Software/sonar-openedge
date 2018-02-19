package org.prorefactor.core.schema;

import java.util.List;
import java.util.SortedSet;

import org.prorefactor.core.IConstants;

public interface ITable {
  /**
   * @return Parent IDatabase object, or {@link Constants#nullDatabase} for temp-tables or work-tables
   */
  IDatabase getDatabase();
  
  /**
   * @return Table name
   */
  String getName();

  /**
   * @return {@link IConstants#ST_DBTABLE} for DB table, {@link IConstants#ST_TTABLE} for temp-tables or
   *         {@link IConstants#ST_WTABLE} for work-tables
   */
  int getStoretype();

  /**
   * Lookup a field by name. We do not test for uniqueness. We leave that job to the compiler. This function expects an
   * unqualified field name (no name dots).
   */
  IField lookupField(String name);

  /** Add a Field to this table. "Package" visibility only. */
  void add(IField field);

  /** Add a new index to this table. "Package" visibility only. */
  void add(IIndex index);

  /**
   * @return Sorted (by field name) list of fields
   */
  SortedSet<IField> getFieldSet();

  /**
   * Get the ArrayList of fields in field position order (rather than sorted alpha)
   */
  List<IField> getFieldPosOrder();

  List<IIndex> getIndexes();
}
