/*******************************************************************************
 * Copyright (c) 2003-2015 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.schema;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.treeparser.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schema is a singleton with methods and fields for working with database schema names, and references to those from
 * 4gl compile units.
 */
public class Schema {
  private static final Logger LOGGER = LoggerFactory.getLogger(Schema.class);

  private static final Comparator<Table> ALLTABLES_ORDER = new Comparator<Table>() {
    @Override
    public int compare(Table s1, Table s2) {
      int ret = s1.getName().compareToIgnoreCase(s2.getName());
      if (ret != 0)
        return ret;
      return s1.getDatabase().getName().compareToIgnoreCase(s2.getDatabase().getName());
    }
  };

  public static final Database nullDatabase = new Database("");
  public static final Table nullTable = new Table("");

  private final Map<String, String> aliases = new HashMap<>();
  private final SortedSet<Database> dbSet = new TreeSet<>(Database.NAME_ORDER);
  private final SortedSet<Table> allTables = new TreeSet<>(ALLTABLES_ORDER);

  public Schema(String file) throws IOException {
    this(file, false);
  }

  public Schema(String file, boolean injectMetaSchema) throws IOException {
    loadSchema(file);
    if (injectMetaSchema) {
      injectMetaSchema();
    }
  }

  /** Get databases sorted by name. */
  public SortedSet<Database> getDbSet() {
    return dbSet;
  }

  /**
   * Add a database alias.
   * 
   * @param aliasname The name for the alias
   * @param dbname The database's logical name
   * @return Empty string.
   */
  public String aliasCreate(String aliasname, String dbname) {
    aliases.put(aliasname.toLowerCase(), dbname);
    return "";
  }

  /**
   * Delete a database alias.
   * 
   * @param aliasname The name for the alias, null or empty string to delete all.
   */
  public void aliasDelete(String aliasname) {
    if (aliasname == null || aliasname.length() == 0) {
      aliases.clear();
    } else {
      aliases.remove(aliasname.toLowerCase());
    }
  }

  /** Get an iterator through all tables, sorted by db.table name. */
  public Iterator<Table> getAllTablesIterator() {
    return allTables.iterator();
  }

  public final void injectMetaSchema() throws IOException {
    for (Database db : dbSet) {
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(this.getClass().getResourceAsStream("/meta.txt")))) {
        StreamTokenizer tokenstream = new StreamTokenizer(reader);
        tokenstream.eolIsSignificant(false);
        tokenstream.wordChars('!', 'z');

        Table currTable = null;
        while (tokenstream.nextToken() != StreamTokenizer.TT_EOF) {
          String theString = tokenstream.sval;
          if (":".equals(theString)) {
            // table name
            tokenstream.nextToken();
            String tablename = tokenstream.sval;
            tokenstream.nextToken(); // table recid is no longer stored
            currTable = new Table(tablename, db);
            allTables.add(currTable);
          } else {
            // field name
            String fieldname = tokenstream.sval;
            tokenstream.nextToken(); // field recid is no longer stored
            tokenstream.nextToken();
            String typeName = tokenstream.sval;
            Field field = new Field(fieldname, currTable);
            field.setDataType(DataType.getDataType(typeName));
            if (field.getDataType() == null)
              throw new IOException("Unknown datatype: " + typeName);
            tokenstream.nextToken();
            field.setExtent((int) tokenstream.nval);
            // Fields are not needed or used in Proparse.dll.
          }
        } // while
      }
    }
  }

  /**
   * Load schema names and RECID from a flat file.
   * 
   * @param from The filename to read from.
   */
  private final void loadSchema(String from) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(from));
    StreamTokenizer tokenstream = new StreamTokenizer(reader);
    tokenstream.eolIsSignificant(false);
    tokenstream.wordChars('!', 'z');
    Database currDatabase = null;
    Table currTable = null;
    while (tokenstream.nextToken() != StreamTokenizer.TT_EOF) {
      String theString = tokenstream.sval;
      if ("::".equals(theString)) {
        // database name
        tokenstream.nextToken();
        String dbname = tokenstream.sval;
        tokenstream.nextToken(); // database number is no longer stored
        currDatabase = new Database(dbname);
        dbSet.add(currDatabase);
      } else if (":".equals(theString)) {
        // table name
        tokenstream.nextToken();
        String tablename = tokenstream.sval;
        tokenstream.nextToken(); // table recid is no longer stored
        currTable = new Table(tablename, currDatabase);
        allTables.add(currTable);
      } else {
        // field name
        String fieldname = tokenstream.sval;
        tokenstream.nextToken(); // field recid is no longer stored
        tokenstream.nextToken();
        String typeName = tokenstream.sval;
        Field field = new Field(fieldname, currTable);
        field.setDataType(DataType.getDataType(typeName));
        if (field.getDataType() == null)
          throw new IOException("Unknown datatype: " + typeName);
        tokenstream.nextToken();
        field.setExtent((int) tokenstream.nval);
        // Fields are not needed or used in Proparse.dll.
      }
    } // while
    reader.close();
  } // loadSchema()

  /** Lookup Database, with alias checks. */
  public Database lookupDatabase(String inName) {
    Database db = lookupDatabase2(inName);
    if (db != null)
      return db;
    // Check for database alias
    String realName = aliases.get(inName.toLowerCase());
    if (realName == null)
      return null;
    return lookupDatabase2(realName);
  }

  /**
   * Lookup Database by name. Called twice by lookupDatabase().
   */
  private Database lookupDatabase2(String inName) {
    SortedSet<Database> dbTailSet = dbSet.tailSet(new Database(inName));
    if (dbTailSet.isEmpty())
      return null;
    Database db = dbTailSet.first();
    if (db == null || db.getName().compareToIgnoreCase(inName) != 0)
      return null;
    return db;
  }

  /** Lookup a Field, given the db, table, and field names */
  public Field lookupField(String dbName, String tableName, String fieldName) {
    Table table = lookupTable(dbName, tableName);
    if (table == null)
      return null;
    return table.lookupField(fieldName);
  }

  /**
   * Lookup a table by name.
   * 
   * @param inName The string table name to lookup.
   * @return A Table, or null if not found. If a name like "db.table" fails on the first lookup try, we next search
   *         dictdb for the table, in case it's something like "sports._file". In that case, the Table from the "dictdb"
   *         database would be returned. We don't keep meta-schema records in the rest of the databases.
   */
  public Table lookupTable(String inName) {
    if (inName.indexOf('.') > -1) {
      Table firstTry = lookupTable2(inName);
      if (firstTry != null)
        return firstTry;
      return lookupMetaTable(inName);
    }
    return lookupTableCheckName(allTables.tailSet(new Table(inName)), inName);
  }

  /** Lookup a table, given a database name and a table name. */
  public Table lookupTable(String dbName, String tableName) {
    Database db = lookupDatabase(dbName);
    if (db == null)
      return null;
    return lookupTableCheckName(db.getTableSet().tailSet(new Table(tableName)), tableName);
  }

  // It turns out that we *do* have to test for uniqueness - we can't just leave
  // that job to the compiler. That's because when looking up schema names for
  // a DEF..LIKE x, if x is non-unique in schema, then we move on to temp/work/buffer names.
  private Table lookupTableCheckName(SortedSet<Table> set, String name) {
    String lname = name.toLowerCase();
    Iterator<Table> it = set.iterator();
    if (!it.hasNext())
      return null;
    Table table = it.next();
    // test that we got a match
    if (!table.getName().toLowerCase().startsWith(lname))
      return null;
    // test that we got a unique match
    if (lname.length() < table.getName().length() && it.hasNext()) {
      Table next = it.next();
      if (next.getName().toLowerCase().startsWith(lname))
        return null;
    }
    return table;
  }

  /** Lookup a qualified table name */
  private Table lookupTable2(String inName) {
    String[] parts = inName.split("\\.");
    if (parts == null) {
      // Only in the case 'inName' equals '.'
      return null;
    }
    return lookupTable(parts[0], parts[1]);
  }

  /**
   * This is for looking up names like "sports._file". We return the dictdb Table.
   */
  private Table lookupMetaTable(String inName) {
    String[] parts = inName.split("\\.");
    Database db = lookupDatabase("dictdb");
    if (db == null)
      return null;
    return lookupTableCheckName(db.getTableSet().tailSet(new Table(parts[1])), parts[1]);
  }

  /**
   * Lookup an unqualified schema field name. Does not test for uniqueness. That job is left to the compiler. (In fact,
   * anywhere this is run, the compiler would check that the field name is also unique against temp/work tables.)
   * Returns null if nothing found.
   */
  public Field lookupUnqualifiedField(String name) {
    Field field;
    for (Object allTable : allTables) {
      Table table = (Table) allTable;
      field = table.lookupField(name);
      if (field != null)
        return field;
    }
    return null;
  }

}
