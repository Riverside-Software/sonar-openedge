/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2026 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
package org.prorefactor.core.schema;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;

import org.prorefactor.core.schema.MetaSchemaProvider.SchemaLineProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.io.Files;

import eu.rssw.pct.mapping.OpenEdgeVersion;

/**
 * Schema is a singleton with methods and fields for working with database schema names, and references to those from
 * 4gl compile units.
 */
public class Schema implements ISchema {
  private static final Logger LOGGER = LoggerFactory.getLogger(Schema.class);

  private static final Comparator<ITable> ALLTABLES_ORDER = (t1, t2) -> {
    var ret = t1.getName().compareToIgnoreCase(t2.getName());
    if (ret != 0)
      return ret;
    return t1.getDatabase().getName().compareToIgnoreCase(t2.getDatabase().getName());
  };

  private final Map<String, String> aliases = new HashMap<>();
  private final SortedSet<IDatabase> dbSet = new TreeSet<>(Constants.DB_NAME_ORDER);
  private final SortedSet<ITable> allTables = new TreeSet<>(ALLTABLES_ORDER);

  public Schema(@Nonnull Path path) throws IOException {
    this(path, false);
  }

  public Schema(@Nonnull Path path, boolean injectMetaSchema) throws IOException {
    loadSchema(path, injectMetaSchema);
    createAlias("dictdb", dbSet.iterator().next().getName());
  }

  public Schema(IDatabase... dbs) {
    for (var db : dbs) {
      dbSet.add(db);
      for (var tbl : db.getTableSet()) {
        allTables.add(tbl);
      }
    }
    if (dbs.length > 0)
      createAlias("dictdb", dbs[0].getName());
  }

  public Map<String, String> getAliases() {
    return Collections.unmodifiableMap(aliases);
  }

  @Override
  public Collection<IDatabase> getDatabases() {
    return Collections.unmodifiableCollection(dbSet);
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

  private final void loadSchema(Path file, boolean injectMetaSchema) throws IOException {
    var db = new Database(Files.getNameWithoutExtension(file.getFileName().toString()),
        injectMetaSchema ? OpenEdgeVersion.V128 : null);
    dbSet.add(db);
    var lineProcessor = new SchemaLineProcessor(db);
    Files.asCharSource(file.toFile(), Charset.defaultCharset()).readLines(lineProcessor);
    for (var tbl : lineProcessor.getResult()) {
      db.add(tbl);
    }
    for (var tbl : db.getTableSet()) {
      allTables.add(tbl);
    }
  }

  @Override
  public IDatabase lookupDatabase(String inName) {
    var db = lookupDatabase2(inName);
    if (db != null)
      return db;
    // Check for database alias
    var realName = aliases.get(inName.toLowerCase());
    if (realName == null)
      return null;
    return lookupDatabase2(realName);
  }

  @Override
  public ITable lookupTable(String inName) {
    if (inName.indexOf('.') > -1) {
      var firstTry = lookupTable2(inName);
      if (firstTry != null)
        return firstTry;
      return lookupMetaTable(inName);
    }
    return lookupTableCheckName(allTables.stream().toList(), inName);
  }

  @Override
  public ITable lookupTable(String dbName, String tableName) {
    var db = lookupDatabase(dbName);
    if (db == null)
      return null;
    return lookupTableCheckName(db.getTableSet(), tableName);
  }

  @Override
  public IField lookupField(String dbName, String tableName, String fieldName) {
    var table = lookupTable(dbName, tableName);
    if (table == null)
      return null;
    return table.lookupField(fieldName);
  }

  @Override
  public IField lookupUnqualifiedField(String name, boolean requireFullName) {
    for (var table : allTables) {
      var field = requireFullName ? table.lookupFullNameField(name) : table.lookupField(name);
      if (field != null)
        return field;
    }
    return null;
  }

  /**
   * Lookup database by name. Called twice by lookupDatabase().
   */
  private IDatabase lookupDatabase2(String inName) {
    return dbSet.stream().filter(it -> it.getName().equalsIgnoreCase(inName)).findFirst().orElse(null);
  }

  // It turns out that we *do* have to test for uniqueness - we can't just leave
  // that job to the compiler. That's because when looking up schema names for
  // a DEF..LIKE x, if x is non-unique in schema, then we move on to temp/work/buffer names.
  private ITable lookupTableCheckName(List<ITable> set, String name) {
    var lname = name.toLowerCase();
    var it = set.iterator();
    if (!it.hasNext())
      return null;
    var table = it.next();
    while (table.getName().toLowerCase().compareTo(lname) < 0) {
      if (it.hasNext())
        table = it.next();
      else
        return null;
    }
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
    if ((parts == null) || (parts.length == 0)) {
      // Only in the case 'inName' equals '.'
      return null;
    } else if (parts.length == 1) {
      return lookupTable(parts[0]);
    } else {
      return lookupTable(parts[0], parts[1]);
    }
  }

  /**
   * This is for looking up names like "sports._file". We return the dictdb Table.
   */
  private ITable lookupMetaTable(String inName) {
    String[] parts = inName.split("\\.");
    IDatabase db = parts.length >= 1 ? lookupDatabase(parts[0]) : null;
    if ((db == null) || (parts[1] == null) || (!parts[1].startsWith("_"))) {
      return null;
    }
    return lookupTableCheckName(db.getTableSet(), parts[1]);
  }

}
