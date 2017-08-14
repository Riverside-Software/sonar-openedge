package org.prorefactor.core.schema;

import java.util.List;
import java.util.SortedSet;

import org.prorefactor.treeparser.TreeParserRootSymbolScope;

public interface ITable {
  IDatabase getDatabase();
  String getName();

  /**
   * Lookup a field by name. We do not test for uniqueness. We leave that job to the compiler. This function expects an
   * unqualified field name (no name dots).
   */
  IField lookupField(String name);

  /** Add a Field to this table. "Package" visibility only. */
  void add(IField field);

  SortedSet<IField> getFieldSet();

  /**
   * Get the ArrayList of fields in field position order (rather than sorted alpha)
   */
  List<IField> getFieldPosOrder();

  // TODO Document return values
  int getStoretype();

  /**
   * Create a bare minimum copy of a Table definition. No-op if the table already exists in the requested scope. Copies
   * all of the field definitions as well.
   * 
   * @param scope The scope that this table is to be added to.
   * @return The newly created table, or the existing one from the scope if it has previously been defined.
   */
  // TODO Remove dependency
  ITable copyBare(TreeParserRootSymbolScope scope);
}
