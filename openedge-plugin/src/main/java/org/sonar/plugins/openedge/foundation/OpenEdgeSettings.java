/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2014 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.foundation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.openedge.OpenEdgePlugin;
import org.sonar.plugins.openedge.api.org.prorefactor.core.schema.Schema;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.antlr.database.objects.Field;
import eu.rssw.antlr.database.objects.Table;

@BatchSide
public class OpenEdgeSettings {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeSettings.class);

  private final List<String> sourceDirs = new ArrayList<String>();
  private final File pctDir, dbgDir;
  private final Settings settings;
  private final List<File> propath = new ArrayList<File>();
  /* XXX private final Map<String, List<IDatabaseTable>> dbDesc = new TreeMap<String, List<IDatabaseTable>>(); */
  private final Schema ppSchema;
  /* XXX private final Map<String, ClassInformation> genClasses = new HashMap<String, ClassInformation>();
  private final Map<String, ClassInformation> ppClasses = new HashMap<String, ClassInformation>();*/

  public OpenEdgeSettings(Settings settings, FileSystem fileSystem) {
    this.settings = settings;

    // Looking for source directories
    String sonarSources = settings.getString("sonar.sources");
    if (sonarSources == null) {
      sourceDirs.add(FilenameUtils.normalizeNoEndSeparator(fileSystem.baseDir().getAbsolutePath(), true));
    } else {
      for (String str : Splitter.on(',').trimResults().split(sonarSources)) {
        String dir = FilenameUtils.normalizeNoEndSeparator(new File(fileSystem.baseDir(), str).getAbsolutePath(), true);
        sourceDirs.add(dir);
      }
    }

    // And for .pct directory
    String binariesSetting = settings.getString(OpenEdgePlugin.BINARIES);
    if (binariesSetting == null) {
      LOG.warn("Property {} not defined, using default value", OpenEdgePlugin.BINARIES);
      binariesSetting = "build";
    }
    File binaries = new File(fileSystem.baseDir(), binariesSetting);
    this.pctDir = new File(binaries, ".pct");
    this.dbgDir = new File(binaries, ".dbg");
    // Getting ClassInformation objects from rcode in binaries dir
    /* XXX if (binaries.exists() && binaries.isDirectory()) {
      readRCodeFromDirectory(binaries, genClasses);
    }
    LOG.info("{} classes read from {} directory", genClasses.keySet().size(), binariesSetting); */

    // PROPATH definition
    String propathProp = settings.getString(OpenEdgePlugin.PROPATH);
    LOG.info("Using PROPATH : {}", propathProp);
    if (propathProp != null) {
      for (String str : Splitter.on(',').trimResults().split(propathProp)) {
        File entry = fileSystem.resolvePath(str);
        LOG.debug("Adding {} to PROPATH", entry.getAbsolutePath());
        propath.add(entry);
      }
    }
    // Getting ClassInformation objects from rcode in propath
    /* XXX for (File entry : propath) {
      if (entry.isDirectory()) {
        readRCodeFromDirectory(entry, ppClasses);
      } else if (entry.isFile() && "pl".equalsIgnoreCase(Files.getFileExtension(entry.getName()))) {
        readRCodeFromPL(entry, ppClasses);
      }
    }
    LOG.info("{} classes read from PROPATH entries", ppClasses.keySet().size(), binariesSetting);*/

    // File definition for temporary .schema file
    File dbFile;
    int dbId = 1, recid = 1000;
    try {
      dbFile = File.createTempFile("proparse", ".schema");
    } catch (IOException caught) {
      LOG.error("Unable to create proparse.schema file", caught);
      throw new RuntimeException(caught);
    }

    // Database definitions
    String dbs = settings.getString(OpenEdgePlugin.DATABASES);
    LOG.info("Using schema : {}", dbs);
    if (dbs != null) {
      try (BufferedWriter writer = Files.newWriter(dbFile, Charsets.UTF_8);
          BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/schema/meta.txt")))) {
        for (String str : Splitter.on(',').trimResults().split(dbs)) {
          String dbName = "";
          int colonPos = str.lastIndexOf(':');
          if (colonPos == -1) {
            dbName = FilenameUtils.getBaseName(str);
          } else {
            dbName = str.substring(colonPos + 1);
          }
          LOG.debug("Parsing {} with alias {}", fileSystem.resolvePath(str), dbName);
          DatabaseDescription desc = DumpFileUtils.getDatabaseDescription(fileSystem.resolvePath(str));
          // XXX dbDesc.put(dbName, mapToDatabaseKeyword(desc, dbName).getTables());
          writer.write(":: " + dbName + " " + dbId++);
          writer.newLine();
          for (Table tbl : desc.getTables()) {
            writer.write(": " + tbl.getName() + " " + (recid++));
            writer.newLine();
            for (Field fld : tbl.getFields()) {
              writer.write(fld.getName() + " " + (recid++) + " " + fld.getDataType().toUpperCase() + " "
                  + (fld.getExtent() == null ? "0" : fld.getExtent()));
              writer.newLine();
            }
          }
        }
        String str = null;
        while ((str = reader.readLine()) != null) {
          writer.write(str);
          writer.newLine();
        }
      } catch (IOException caught) {
        LOG.error("Unable to write proparse.schema file", caught);
      }
    }

    ppSchema = new Schema(dbFile.getAbsolutePath());
    if (ppSchema.getDbSet().size() > 0) {
      ppSchema.aliasCreate("dictdb", ppSchema.getDbSet().first().getName());
    }
    dbFile.delete();

  }

  public List<String> getSourceDirs() {
    return sourceDirs;
  }

  public File getPctDir() {
    return pctDir;
  }

  public File getDbgDir() {
    return dbgDir;
  }

  public boolean skipParserSensor() {
    return settings.getBoolean(OpenEdgePlugin.SKIP_PARSER_PROPERTY);
  }

  public boolean skipProparseSensor() {
    return settings.getBoolean(OpenEdgePlugin.SKIP_PROPARSE_PROPERTY);
  }

  public boolean useProparseDebug() {
    return settings.getBoolean(OpenEdgePlugin.PROPARSE_DEBUG);
  }

  public List<File> getPropath() {
    return propath;
  }

  public String getPropathAsString() {
    String str = settings.getString(OpenEdgePlugin.PROPATH);
    return (str == null ? "" : str);
  }

  public Schema getProparseSchema() {
    return ppSchema;
  }

  /* XXX public Map<String, List<IDatabaseTable>> getDatabases() {
    return dbDesc;
  }

  public Map<String, ClassInformation> getBinariesClassInfo() {
    return genClasses;
  }

  public Map<String, ClassInformation> getPropathClassInfo() {
    return ppClasses;
  }

  private DatabaseKeyword mapToDatabaseKeyword(DatabaseDescription desc, String name) {
    DatabaseKeyword db = new DatabaseKeyword(name);
    for (Table t : desc.getTables()) {
      LOG.trace("Adding table {}.{}", new Object[] {name, t.getName()});
      DatabaseTableKeyword tbl = new DatabaseTableKeyword(db, t.getName());
      for (Field f : t.getFields()) {
        LOG.trace("Adding field {}.{}.{} {}", new Object[] {name, t.getName(), f.getName(), f.getDataType()});
        new DatabaseFieldKeyword(tbl, f.getName(), DataTypes.getDataType(f.getDataType()), f.getDescription(),
            (f.getExtent() == null ? 0 : f.getExtent().intValue()), "", false, 10);
      }
      for (Index i : t.getIndexes()) {
        DatabaseIndexFieldKeyword[] flds = new DatabaseIndexFieldKeyword[i.getFields().size()];
        for (int zz = 0; zz < i.getFields().size(); zz++) {
          LOG.trace("Adding index field {}.{}.{}.{}",
              new Object[] {name, t.getName(), i.getName(), i.getFields().get(zz).getField().getName()});
          flds[zz] = new DatabaseIndexFieldKeyword(tbl, i.getName(), i.getFields().get(zz).getField().getName(),
              i.getFields().get(zz).isAscending());
        }
        new DatabaseIndexKeyword(tbl, i.getName(), flds, i.isUnique(), i.isPrimary());
      }
    }

    return db;
  }

  private void readRCodeFromDirectory(File dir, Map<String, ClassInformation> infos) {
    for (File f : FileUtils.listFiles(dir, new String[] {"r"}, true)) {
      try {
        RCodeObject obj = new RCodeObject(f);
        if (obj.isClass()) {
          infos.put(obj.getClassInfo().getName(), new ClassInformation(obj.getClassInfo()));
        }
        obj.dispose();
      } catch (IOException | RCodeReadException e) {
        LOG.error("Unable to read rcode {}", f.getName());
      }
    }
  }

  private void readRCodeFromPL(File lib, Map<String, ClassInformation> infos) {
    File tmpFile = null;
    try {
      tmpFile = File.createTempFile("rcode", ".r");
      PLReader reader = new PLReader(lib);
      for (FileEntry e : reader.getFileList()) {
        // Extracts rcode from PL into temporary file
        OutputStream os = new FileOutputStream(tmpFile);
        IOUtils.copy(reader.getInputStream(e), os);
        // Then read content
        RCodeObject obj = new RCodeObject(tmpFile);
        if (obj.isClass()) {
          infos.put(obj.getClassInfo().getName(), new ClassInformation(obj.getClassInfo()));
        }
        obj.dispose();
      }
    } catch (IOException caught) {

    } catch (RCodeReadException caught) {

    } finally {
      if (tmpFile != null)
        tmpFile.delete();
    }
  }*/
}
