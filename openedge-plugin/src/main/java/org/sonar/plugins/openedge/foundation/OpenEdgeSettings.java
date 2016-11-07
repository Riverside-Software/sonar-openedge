/*
 * OpenEdge plugin for SonarQube
 * Copyright (C) 2013-2016 Riverside Software
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.com.google.common.base.Charsets;
import org.sonar.plugins.openedge.api.com.google.common.base.Joiner;
import org.sonar.plugins.openedge.api.com.google.common.base.Splitter;
import org.sonar.plugins.openedge.api.com.google.common.base.Strings;
import org.sonar.plugins.openedge.api.com.google.common.io.Files;
import org.sonar.plugins.openedge.api.eu.rssw.antlr.database.DumpFileUtils;
import org.sonar.plugins.openedge.api.eu.rssw.antlr.database.objects.DatabaseDescription;
import org.sonar.plugins.openedge.api.eu.rssw.antlr.database.objects.Field;
import org.sonar.plugins.openedge.api.eu.rssw.antlr.database.objects.Table;
import org.sonar.plugins.openedge.api.org.prorefactor.core.schema.Schema;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorSession;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.IProgressSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.IProparseSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.ProgressSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.ProparseSettings;

@BatchSide
public class OpenEdgeSettings {
  private static final Logger LOG = LoggerFactory.getLogger(OpenEdgeSettings.class);

  private final List<String> sourceDirs = new ArrayList<>();
  private final File pctDir, dbgDir;
  private final Settings settings;
  private final List<File> propath = new ArrayList<>();
  private final Set<String> cpdAnnotations = new HashSet<>();
  private final RefactorSession proparseSession;

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
    String binariesSetting = settings.getString(Constants.BINARIES);
    if (binariesSetting == null) {
      LOG.warn("Property {} not defined, using default value", Constants.BINARIES);
      binariesSetting = "build";
    }
    File binaries = new File(fileSystem.baseDir(), binariesSetting);
    this.pctDir = new File(binaries, ".pct");
    this.dbgDir = new File(binaries, ".dbg");

    // PROPATH definition
    String propathProp = settings.getString(Constants.PROPATH);
    LOG.info("Using PROPATH : {}", propathProp);
    if (propathProp != null) {
      for (String str : Splitter.on(',').trimResults().split(propathProp)) {
        File entry = fileSystem.resolvePath(str);
        LOG.debug("Adding {} to PROPATH", entry.getAbsolutePath());
        propath.add(entry);
      }
    }
    String dlcInstallDir = settings.getString(Constants.DLC);
    boolean dlcInPropath = settings.getBoolean(Constants.PROPATH_DLC);
    if (dlcInPropath && !Strings.isNullOrEmpty(dlcInstallDir)) {
      File dlc = new File(dlcInstallDir);
      LOG.info("Adding DLC directory '{}' to PROPATH", dlc.getAbsolutePath());
      propath.add(new File(dlc, "gui"));
      propath.add(new File(dlc, "tty"));
      propath.add(new File(dlc, "src"));
      propath.add(dlc);
    }

    // File definition for temporary .schema file
    File dbFile;
    try {
      dbFile = File.createTempFile("proparse", ".schema");
    } catch (IOException caught) {
      LOG.error("Unable to create proparse.schema file", caught);
      throw new RuntimeException(caught);
    }

    // Database definitions
    String dbs = settings.getString(Constants.DATABASES);
    LOG.info("Using schema : {}", dbs);
    if (dbs != null) {
      try (BufferedWriter writer = Files.newWriter(dbFile, Charsets.UTF_8)) {
        for (String str : Splitter.on(',').trimResults().split(dbs)) {
          String dbName = "";
          int colonPos = str.lastIndexOf(':');
          if (colonPos == -1) {
            dbName = FilenameUtils.getBaseName(str);
          } else {
            dbName = str.substring(colonPos + 1);
            str = str.substring(0, colonPos);
          }
          LOG.debug("Parsing {} with alias {}", fileSystem.resolvePath(str), dbName);
          DatabaseDescription desc = DumpFileUtils.getDatabaseDescription(fileSystem.resolvePath(str));
          // XXX dbDesc.put(dbName, mapToDatabaseKeyword(desc, dbName).getTables());
          writer.write(":: " + dbName);
          writer.newLine();
          for (Table tbl : desc.getTables()) {
            writer.write(": " + tbl.getName() + " ");
            writer.newLine();
            for (Field fld : tbl.getFields()) {
              writer.write(fld.getName() + " " + fld.getDataType().toUpperCase() + " "
                  + (fld.getExtent() == null ? "0" : fld.getExtent()));
              writer.newLine();
            }
          }
        }
      } catch (IOException caught) {
        LOG.error("Unable to write proparse.schema file", caught);
      }
    }

    Schema sch = null;
    try {
      sch = new Schema(dbFile.getAbsolutePath(), true);
      if (!sch.getDbSet().isEmpty()) {
        sch.createAlias("dictdb", sch.getDbSet().first().getName());
      }
      if (settings.getString(Constants.ALIASES) != null) {
        for (String str : Splitter.on(';').trimResults().split(settings.getString(Constants.ALIASES))) {
          List<String> lst = Splitter.on(',').trimResults().splitToList(str);
          for (String alias : lst.subList(1, lst.size())) {
            LOG.debug("Adding {} aliases to database {}", new Object[] {alias, lst.get(0)});
            sch.createAlias(alias, lst.get(1));
          }
        }
      }
    } catch (IOException caught) {
      LOG.error("Unable to read proparse.schema file", caught);
    }
    dbFile.delete();

    // CPD annotations
    for (String str : Strings.nullToEmpty(settings.getString(Constants.CPD_ANNOTATIONS)).split(",")) {
      LOG.debug("CPD annotation : '{}'", str);
      cpdAnnotations.add(str);
    }

    IProgressSettings settings1 = new ProgressSettings(true, "", "WIN32", getPropathAsString(), "11.5", "MS-WIN95");
    IProparseSettings settings2 = new ProparseSettings();
    proparseSession = new RefactorSession(settings1, settings2, sch, fileSystem.encoding());
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

  public boolean skipCPD(String annotation) {
    return cpdAnnotations.contains(annotation);
  }

  public boolean skipProparseSensor() {
    return settings.getBoolean(Constants.SKIP_PROPARSE_PROPERTY);
  }

  public boolean useProparseDebug() {
    return settings.getBoolean(Constants.PROPARSE_DEBUG);
  }

  public boolean useCpdDebug() {
    return settings.getBoolean(Constants.CPD_DEBUG);
  }

  public boolean useXrefFilter() {
    return settings.getBoolean(Constants.XREF_FILTER);
  }

  public List<File> getPropath() {
    return propath;
  }

  public String getPropathAsString() {
    return Joiner.on(',').skipNulls().join(propath);
  }

  public RefactorSession getProparseSession() {
    return proparseSession;
  }

}
