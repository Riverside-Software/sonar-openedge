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

/**
 * Represents the list of all available db, aliases and tables in an OpenEdge session
 */
public interface ISchema {
  /**
   * Add a database alias.
   * 
   * @param aliasname The name for the alias
   * @param dbname The database's logical name
   */
  void createAlias(String aliasname, String dbname);
  
  /**
   * Delete a database alias.
   * 
   * @param aliasname The name for the alias, null or empty string to delete all.
   */
  void deleteAlias(String aliasname);

  /**
   * Returns the database with the given (or alias)
   * @param name Database name
   * @return Null if not found
   */
  IDatabase lookupDatabase(String name);

  /**
   * Lookup a Field, given the db, table, and field names
   * @param dbName
   * @param tableName
   * @param fieldName
   * @return
   */
  IField lookupField(String dbName, String tableName, String fieldName);

  /**
   * Lookup a table by name.
   * 
   * @param inName The string table name to lookup.
   * @return A Table, or null if not found. If a name like "db.table" fails on the first lookup try, we next search
   *         dictdb for the table, in case it's something like "sports._file". In that case, the Table from the "dictdb"
   *         database would be returned. We don't keep meta-schema records in the rest of the databases.
   */
  ITable lookupTable(String inName);

  /** Lookup a table, given a database name and a table name. */
  ITable lookupTable(String dbName, String tableName);

  /**
   * Lookup an unqualified schema field name. Does not test for uniqueness. That job is left to the compiler. In fact,
   * anywhere this is run, the compiler would check that the field name is also unique against temp/work tables.
   * 
   * @param name Unqualified schema field name
   * @return Null if nothing found
   */
  IField lookupUnqualifiedField(String name);
}
