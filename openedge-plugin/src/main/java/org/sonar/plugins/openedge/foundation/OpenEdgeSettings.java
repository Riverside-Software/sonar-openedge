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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.com.google.common.base.Joiner;
import org.sonar.plugins.openedge.api.com.google.common.base.Splitter;
import org.sonar.plugins.openedge.api.com.google.common.base.Strings;
import org.sonar.plugins.openedge.api.com.google.common.collect.ImmutableSet;
import org.sonar.plugins.openedge.api.eu.rssw.antlr.database.DumpFileUtils;
import org.sonar.plugins.openedge.api.eu.rssw.antlr.database.objects.DatabaseDescription;
import org.sonar.plugins.openedge.api.objects.DatabaseWrapper;
import org.sonar.plugins.openedge.api.org.prorefactor.core.schema.IDatabase;
import org.sonar.plugins.openedge.api.org.prorefactor.core.schema.Schema;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.RefactorSession;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.IProparseSettings;
import org.sonar.plugins.openedge.api.org.prorefactor.refactor.settings.ProparseSettings;
import org.sonarsource.api.sonarlint.SonarLintSide;

@ScannerSide
@SonarLintSide
@BatchSide
public class OpenEdgeSettings {
  private static final Logger LOG = Loggers.get(OpenEdgeSettings.class);

  private final List<String> sourceDirs = new ArrayList<>();
  private final File binariesDir;
  private final File pctDir;
  private final Settings settings;
  private final List<File> propath = new ArrayList<>();
  private final Set<String> cpdAnnotations = new HashSet<>();
  private final Set<String> cpdMethods = new HashSet<>();
  private final Set<String> cpdProcedures = new HashSet<>();
  private final RefactorSession proparseSession;
  private final Set<Integer> xrefBytes = new HashSet<>();

  public OpenEdgeSettings(Settings settings, FileSystem fileSystem) {
    this.settings = settings;

    initializeDirectories(settings, fileSystem);

    // And for .pct directory
    String binariesSetting = settings.getString(Constants.BINARIES);
    if (binariesSetting == null) {
      LOG.debug("Property {} not defined, using default value", Constants.BINARIES);
      binariesSetting = "build";
    }
    binariesDir = new File(fileSystem.baseDir(), binariesSetting);
    this.pctDir = new File(binariesDir, ".pct");

    initializePropath(settings, fileSystem);
    initializeCPD(settings);
    initializeXrefBytes(settings);
    LOG.info("Using backslash as escape character : {}", settings.getBoolean(Constants.BACKSLASH_ESCAPE));

    Schema sch = readSchema(settings, fileSystem);
    IProparseSettings ppSettings = new ProparseSettings(getPropathAsString(),
        settings.getBoolean(Constants.BACKSLASH_ESCAPE));
    proparseSession = new RefactorSession(ppSettings, sch, encoding());
  }

  private final void initializeDirectories(Settings settings, FileSystem fileSystem) {
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
  }

  private final void initializePropath(Settings settings, FileSystem fileSystem) {
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
  }

  private final void initializeCPD(Settings settings) {
    // CPD annotations
    for (String str : Strings.nullToEmpty(settings.getString(Constants.CPD_ANNOTATIONS)).split(",")) {
      LOG.debug("CPD annotation : '{}'", str);
      cpdAnnotations.add(str);
    }
    // CPD - Skip methods
    for (String str : Strings.nullToEmpty(settings.getString(Constants.CPD_METHODS)).split(",")) {
      LOG.debug("CPD skip method : '{}'", str);
      cpdMethods.add(str.toLowerCase(Locale.ENGLISH));
    }
    // CPD - Skip procedures and functions
    for (String str : Strings.nullToEmpty(settings.getString(Constants.CPD_PROCEDURES)).split(",")) {
      LOG.debug("CPD skip procedure : '{}'", str);
      cpdProcedures.add(str.toLowerCase(Locale.ENGLISH));
    }
  }

  private final void initializeXrefBytes(Settings settings) {
    // XREF invalid bytes
    for (String str : Strings.nullToEmpty(settings.getString(Constants.XREF_FILTER_BYTES)).split(",")) {
      try {
        if (str.indexOf('-') != -1) {
          for (int zz = Integer.parseInt(str.substring(0, str.indexOf('-'))); zz <= Integer.parseInt(
              str.substring(str.indexOf('-') + 1)); zz++) {
            xrefBytes.add(zz);
          }
        } else if (!str.isEmpty()) {
          xrefBytes.add(Integer.parseInt(str));
        }
      } catch (NumberFormatException caught) {
        throw new IllegalArgumentException("Invalid '" + Constants.XREF_FILTER_BYTES + "' property : " + str, caught);
      }
    }
  }

  public List<String> getSourceDirs() {
    return sourceDirs;
  }

  public File getPctDir() {
    return pctDir;
  }

  public File getBinariesDir() {
    return binariesDir;
  }

  public boolean skipCPD(String annotation) {
    return cpdAnnotations.contains(annotation);
  }

  /**
   * Returns true if method should be skipped by CPD engine
   * 
   * @param name Method name
   */
  public boolean skipMethod(String name) {
    if (name == null) {
      return false;
    }
    return cpdMethods.contains(name.toLowerCase(Locale.ENGLISH));
  }

  /**
   * Return File pointer to rcode in sonar.binaries directory if such rcode exists
   * 
   * @param fileName File name from profiler
   */
  public File getRCode(String fileName) {
    if (fileName.endsWith(".r"))
      return new File(fileName);

    File rCode = new File(binariesDir, FilenameUtils.removeExtension(fileName) + ".r");
    if (rCode.exists())
      return rCode;
    // Profiler also send file name as packagename.classname
    File rCode2 = new File(binariesDir, fileName.replace('.', '/') + ".r");
    if (rCode2.exists())
      return rCode2;

    return null;
  }

  /**
   * Returns absolute file name if found in work directory or in propath
   */
  public String getFilePath(String fileName) {
    if (new File(fileName).exists())
      return fileName;

    for (File file : propath) {
      File stdName = new File(file, fileName);
      if (stdName.exists())
        return stdName.getAbsolutePath();
      File clsName = new File(file, fileName.replace('.', '/') + ".cls");
      if (clsName.exists())
        return clsName.getAbsolutePath();
    }

    return fileName;
  }

  /**
   * Returns true if procedure or function should be skipped by CPD engine
   * 
   * @param name Procedure or function name
   */
  public boolean skipProcedure(String name) {
    if (name == null) {
      return false;
    }
    return cpdProcedures.contains(name.toLowerCase(Locale.ENGLISH));
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

  /**
   * @return False only if property is present and set to false
   */
  public boolean useAnalytics() {
    return !"false".equalsIgnoreCase(Strings.nullToEmpty(settings.getString(Constants.OE_ANALYTICS)));
  }

  public Set<Integer> getXrefBytes() {
    return ImmutableSet.copyOf(xrefBytes);
  }

  public String getXrefBytesAsString() {
    StringBuilder sb = new StringBuilder();
    for (Integer xx : xrefBytes) {
      if (sb.length() > 0) {
        sb.append(',');
      }
      sb.append(String.format("%#04x", xx));
    }

    return sb.toString();
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

  /**
   * Force usage of sonar.sourceEncoding property as SonarLint doesn't set correctly encoding
   */
  private Charset encoding() {
      String encoding = settings.getString(CoreProperties.ENCODING_PROPERTY);
      if (Strings.isNullOrEmpty(encoding)) {
        return Charset.defaultCharset();
      } else {
        return Charset.forName(encoding.trim());
      }
  }

  private Schema readSchema(Settings settings, FileSystem fileSystem) {
    String dbList = Strings.nullToEmpty(settings.getString(Constants.DATABASES));
    LOG.info("Using schema : {}", dbList);
    Collection<IDatabase> dbs = new ArrayList<>();

    for (String str : Splitter.on(',').trimResults().omitEmptyStrings().split(dbList)) {
      String dbName;
      int colonPos = str.lastIndexOf(':');
      if (colonPos == -1) {
        dbName = FilenameUtils.getBaseName(str);
      } else {
        dbName = str.substring(colonPos + 1);
        str = str.substring(0, colonPos);
      }

      LOG.debug("Parsing {} with alias {}", fileSystem.resolvePath(str), dbName);
      try {
        DatabaseDescription desc = DumpFileUtils.getDatabaseDescription(fileSystem.resolvePath(str));
        dbs.add(new DatabaseWrapper(desc));
      } catch (IOException caught) {
        LOG.error("Unable to parse " + str, caught);
      }
    }

    Schema sch = new Schema(dbs.toArray(new IDatabase[] {}));
    if (!sch.getDbSet().isEmpty()) {
      sch.createAlias("dictdb", sch.getDbSet().first().getName());
    }
    for (String str : Splitter.on(';').trimResults().omitEmptyStrings().split(
        Strings.nullToEmpty(settings.getString(Constants.ALIASES)))) {
      List<String> lst = Splitter.on(',').trimResults().splitToList(str);
      for (String alias : lst.subList(1, lst.size())) {
        LOG.debug("Adding {} aliases to database {}", new Object[] {alias, lst.get(0)});
        sch.createAlias(alias, lst.get(0));
      }
    }

    return sch;
  }
}
