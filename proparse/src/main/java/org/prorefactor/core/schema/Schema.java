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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.prorefactor.treeparser.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;

/**
 * Schema is a singleton with methods and fields for working with database schema names, and references to those from
 * 4gl compile units.
 */
public class Schema implements ISchema {
  private static final Logger LOGGER = LoggerFactory.getLogger(Schema.class);

  private static final Comparator<ITable> ALLTABLES_ORDER = new Comparator<ITable>() {
    @Override
    public int compare(ITable s1, ITable s2) {
      int ret = s1.getName().compareToIgnoreCase(s2.getName());
      if (ret != 0)
        return ret;
      return s1.getDatabase().getName().compareToIgnoreCase(s2.getDatabase().getName());
    }
  };


  private final Map<String, String> aliases = new HashMap<>();
  private final SortedSet<IDatabase> dbSet = new TreeSet<>(Constants.DB_NAME_ORDER);
  private final SortedSet<ITable> allTables = new TreeSet<>(ALLTABLES_ORDER);

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
  public SortedSet<IDatabase> getDbSet() {
    return dbSet;
  }

  /**
   * Add a database alias.
   * 
   * @param aliasName The name for the alias
   * @param dbName The database's logical name
   */
  @Override
  public void createAlias(String aliasName, String dbName) {
    if (lookupDatabase2(dbName) == null) {
      LOGGER.error("Creating alias {} for unknown database {}", aliasName, dbName);
    }
    aliases.put(aliasName.toLowerCase(Locale.ENGLISH), dbName);
  }

  /**
   * Delete a database alias.
   * 
   * @param aliasName The name for the alias, null or empty string to delete all.
   */
  @Override
  public void deleteAlias(String aliasName) {
    if (Strings.isNullOrEmpty(aliasName)) {
      aliases.clear();
    } else {
      aliases.remove(aliasName.toLowerCase(Locale.ENGLISH));
    }
  }

  /** Get an iterator through all tables, sorted by db.table name. */
  public Iterator<ITable> getAllTablesIterator() {
    return allTables.iterator();
  }

  public final void injectMetaSchema() throws IOException {
    for (IDatabase db : dbSet) {
      SchemaLineProcessor lineProcessor = new SchemaLineProcessor(db);
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(this.getClass().getResourceAsStream("/meta.txt")))) {
        String line;
        while (((line = reader.readLine()) != null) && lineProcessor.processLine(line)) {
        }
      }
    }
  }

  private final void loadSchema(File file) throws IOException {
    Files.readLines(file, Charset.defaultCharset(), new SchemaLineProcessor());
  }

  /**
   * Load schema names from a flat file.
   * 
   * @param from The filename to read from.
   */
  private final void loadSchema(String from) throws IOException {
    loadSchema(new File(from));
  }

  @Override
  public IDatabase lookupDatabase(String inName) {
    IDatabase db = lookupDatabase2(inName);
    if (db != null)
      return db;
    // Check for database alias
    String realName = aliases.get(inName.toLowerCase());
    if (realName == null)
      return null;
    return lookupDatabase2(realName);
  }

  @Override
  public IField lookupField(String dbName, String tableName, String fieldName) {
    ITable table = lookupTable(dbName, tableName);
    if (table == null)
      return null;
    return table.lookupField(fieldName);
  }

  @Override
  public ITable lookupTable(String inName) {
    if (inName.indexOf('.') > -1) {
      ITable firstTry = lookupTable2(inName);
      if (firstTry != null)
        return firstTry;
      return lookupMetaTable(inName);
    }
    return lookupTableCheckName(allTables.tailSet(new Table(inName)), inName);
  }

  @Override
  public ITable lookupTable(String dbName, String tableName) {
    IDatabase db = lookupDatabase(dbName);
    if (db == null)
      return null;
    return lookupTableCheckName(db.getTableSet().tailSet(new Table(tableName)), tableName);
  }

  @Override
  public IField lookupUnqualifiedField(String name) {
    IField field;
    for (Object allTable : allTables) {
      Table table = (Table) allTable;
      field = table.lookupField(name);
      if (field != null)
        return field;
    }
    return null;
  }

  /**
   * Lookup Database by name. Called twice by lookupDatabase().
   */
  private IDatabase lookupDatabase2(String inName) {
    SortedSet<IDatabase> dbTailSet = dbSet.tailSet(new Database(inName));
    if (dbTailSet.isEmpty())
      return null;
    IDatabase db = dbTailSet.first();
    if (db == null || db.getName().compareToIgnoreCase(inName) != 0)
      return null;
    return db;
  }

  // It turns out that we *do* have to test for uniqueness - we can't just leave
  // that job to the compiler. That's because when looking up schema names for
  // a DEF..LIKE x, if x is non-unique in schema, then we move on to temp/work/buffer names.
  private ITable lookupTableCheckName(SortedSet<ITable> set, String name) {
    String lname = name.toLowerCase();
    Iterator<ITable> it = set.iterator();
    if (!it.hasNext())
      return null;
    ITable table = it.next();
    // test that we got a match
    if (!table.getName().toLowerCase().startsWith(lname))
      return null;
    // test that we got a unique match
    if (lname.length() < table.getName().length() && it.hasNext()) {
      ITable next = it.next();
      if (next.getName().toLowerCase().startsWith(lname))
        return null;
    }
    return table;
  }

  /** Lookup a qualified table name */
  private ITable lookupTable2(String inName) {
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
  private ITable lookupMetaTable(String inName) {
    String[] parts = inName.split("\\.");
    IDatabase db = lookupDatabase(parts[0]);
    if ((db == null) || (parts[1] == null) || (!parts[1].startsWith("_"))) {
      return null;
    }
    return lookupTableCheckName(db.getTableSet().tailSet(new Table(parts[1])), parts[1]);
  }

  private class SchemaLineProcessor implements LineProcessor<Void> {
    private IDatabase currDatabase;
    private Table currTable;

    public SchemaLineProcessor() {
      this(null);
    }

    public SchemaLineProcessor(IDatabase currDatabase) {
      this.currDatabase = currDatabase;
    }

    @Override
    public boolean processLine(String line) throws IOException {
      List<String> list = Splitter.on(' ').omitEmptyStrings().trimResults().splitToList(line);
      // Stop processing on empty line
      if ((list == null) || (list.size() < 2)) {
        return false;
      }
      switch (list.get(0)) {
        case "::":
          currDatabase = new Database(list.get(1));
          dbSet.add(currDatabase);
          break;
        case ":":
          currTable = new Table(list.get(1), currDatabase);
          allTables.add(currTable);
          break;
        default:
          Field field = new Field(list.get(0), currTable);
          field.setDataType(DataType.getDataType(list.get(1)));
          if (field.getDataType() == null)
            throw new IOException("Unknown datatype: " + list.get(1));
          field.setExtent(Integer.parseInt(list.get(2)));
      }
      return true;
    }

    @Override
    public Void getResult() {
      return null;
    }
  }

}
