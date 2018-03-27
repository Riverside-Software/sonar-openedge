/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2018 Riverside Software
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.prorefactor.core.schema.IDatabase;
import org.prorefactor.core.schema.Schema;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.refactor.settings.ProparseSettings;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.openedge.api.Constants;
import org.sonar.plugins.openedge.api.objects.DatabaseWrapper;
import org.sonarsource.api.sonarlint.SonarLintSide;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.pct.FileEntry;
import eu.rssw.pct.PLReader;
import eu.rssw.pct.ProgressClasses;
import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.TypeInfo;

@ScannerSide
@SonarLintSide
public class OpenEdgeSettings {
  private static final Logger LOG = Loggers.get(OpenEdgeSettings.class);

  // IoC
  private final Configuration config;
  private final FileSystem fileSystem;

  // Internal use
  private final List<Path> sourcePaths = new ArrayList<>();
  private final List<Path> binariesDirs = new ArrayList<>();
  private final List<File> propath = new ArrayList<>();
  private final Set<String> includeExtensions = new HashSet<>();
  private final Set<String> cpdAnnotations = new HashSet<>();
  private final Set<String> cpdMethods = new HashSet<>();
  private final Set<String> cpdProcedures = new HashSet<>();
  private final Set<Integer> xrefBytes = new HashSet<>();

  private RefactorSession proparseSession;

  public OpenEdgeSettings(Configuration config, FileSystem fileSystem) {
    this.config = config;
    this.fileSystem = fileSystem;

    LOG.info("Loading OpenEdge settings for server ID '{}' '{}'", config.get(CoreProperties.SERVER_ID).orElse(""),
        config.get(CoreProperties.PERMANENT_SERVER_ID).orElse(""));
    initializeDirectories(config, fileSystem);
    initializePropath(config, fileSystem);
    initializeCPD(config);
    initializeXrefBytes(config);
    initializeIncludeExtensions(config);

    LOG.debug("Using backslash as escape character : {}", config.getBoolean(Constants.BACKSLASH_ESCAPE).orElse(false));
    if (useXrefFilter()) {
      LOG.info("XML XREF filter activated [{}]", getXrefBytesAsString());
    }
  }

  private final void initializeDirectories(Configuration config, FileSystem fileSystem) {
    // Looking for source directories
    Optional<String> sonarSources = config.get(ProjectDefinition.SOURCES_PROPERTY);
    if (sonarSources.isPresent()) {
      for (String str : Splitter.on(',').trimResults().split(sonarSources.get())) {
        sourcePaths.add(new File(fileSystem.baseDir(), str).toPath().normalize());
      }
    } else {
      sourcePaths.add(fileSystem.baseDir().toPath().normalize());
      LOG.debug("No sonar.sources property, defaults to base directory");
    }

    // Build directories
    Optional<String> binariesSetting = config.get(Constants.BINARIES);
    if (binariesSetting.isPresent()) {
      for (String str : Splitter.on(',').trimResults().split(binariesSetting.get())) {
        binariesDirs.add(fileSystem.baseDir().toPath().resolve(str));
      }
    } else {
      LOG.debug("No sonar.oe.binaries property, defaults to source directories");
      binariesDirs.addAll(sourcePaths);
    }
  }

  private final void initializePropath(Configuration config, FileSystem fileSystem) {
    // PROPATH definition
    String propathProp = config.get(Constants.PROPATH).orElse("");
    LOG.info("Using PROPATH : {}", propathProp);
    for (String str : Splitter.on(',').trimResults().split(propathProp)) {
      File entry = fileSystem.resolvePath(str);
      LOG.debug("Adding {} to PROPATH", entry.getAbsolutePath());
      propath.add(entry);
    }

    String dlcInstallDir = config.get(Constants.DLC).orElse(null);
    boolean dlcInPropath = config.getBoolean(Constants.PROPATH_DLC).orElse(false);
    if (dlcInPropath && !Strings.isNullOrEmpty(dlcInstallDir)) {
      File dlc = new File(dlcInstallDir);
      LOG.info("Adding DLC directory '{}' to PROPATH", dlc.getAbsolutePath());
      propath.add(new File(dlc, "gui"));
      propath.add(new File(dlc, "tty"));
      propath.add(new File(dlc, "src"));
      propath.add(dlc);
    }
  }

  private final void initializeCPD(Configuration config) {
    // CPD annotations
    for (String str : config.get(Constants.CPD_ANNOTATIONS).orElse("").split(",")) {
      LOG.debug("CPD annotation : '{}'", str);
      cpdAnnotations.add(str);
    }
    // CPD - Skip methods
    for (String str : config.get(Constants.CPD_METHODS).orElse("").split(",")) {
      LOG.debug("CPD skip method : '{}'", str);
      cpdMethods.add(str.toLowerCase(Locale.ENGLISH));
    }
    // CPD - Skip procedures and functions
    for (String str : config.get(Constants.CPD_PROCEDURES).orElse("").split(",")) {
      LOG.debug("CPD skip procedure : '{}'", str);
      cpdProcedures.add(str.toLowerCase(Locale.ENGLISH));
    }
  }

  private final void initializeXrefBytes(Configuration config) {
    // XREF invalid bytes
    for (String str : config.get(Constants.XREF_FILTER_BYTES).orElse("").split(",")) {
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

  private final void initializeIncludeExtensions(Configuration config) {
    includeExtensions.addAll(Splitter.on(',').trimResults().omitEmptyStrings().splitToList(
        config.get(Constants.INCLUDE_SUFFIXES).orElse("i")).stream().map(String::toLowerCase).collect(
            Collectors.toList()));
  }

  public final void parseHierarchy(InputFile file) {
    String relPath = getRelativePathToSourceDirs(file);
    LOG.debug("Parsing hierarchy of '{}' - Relative '{}'", file, relPath);
    if (relPath == null)
      return;
    File rcd = getRCode(relPath);
    LOG.debug("  RCode found: '{}'", rcd);
    if ((rcd != null) && rcd.exists()) {
      TypeInfo info = parseRCode(rcd);
      if (info != null) {
        parseHierarchy(info);
      }
    }
  }

  private final void parseHierarchy(TypeInfo info) {
    LOG.info("Injecting type info '{}'", info);
    proparseSession.injectTypeInfo(info);
    if (info.getParentTypeName() != null) {
      File rcd = getRCode(info.getParentTypeName());
      if (rcd != null) {
        TypeInfo inf = parseRCode(rcd);
        if (inf != null) {
          parseHierarchy(inf);
        }
      }
    }
    for (String str : info.getInterfaces()) {
      File rcd = getRCode(str);
      if (rcd != null) {
        TypeInfo inf = parseRCode(rcd);
        if (inf != null) {
          parseHierarchy(inf);
        }
      }
    }
  }

  public final void parseBuildDirectory() {
    if (config.getBoolean(Constants.SKIP_RCODE).orElse(false))
      return;

    AtomicInteger numClasses = new AtomicInteger(0);
    AtomicInteger numMethods = new AtomicInteger(0);
    AtomicInteger numProperties = new AtomicInteger(0);
    // Multi-threaded pool
    long currTime = System.currentTimeMillis();
    AtomicInteger numRCode = new AtomicInteger(0);
    ExecutorService service = Executors.newFixedThreadPool(4);
    for (Path binDir : binariesDirs) {
      Files.fileTraverser().depthFirstPreOrder(binDir.toFile()).forEach(f -> {
        if (f.getName().endsWith(".r")) {
          numRCode.incrementAndGet();
          service.submit(() -> {
            TypeInfo info = parseRCode(f);
            if (info != null) {
              numClasses.incrementAndGet();
              numMethods.addAndGet(info.getMethods().size());
              numProperties.addAndGet(info.getProperties().size());
              proparseSession.injectTypeInfo(info);
            }
          });
        }
      });
    }

    // Include PL files in $DLC/gui
    String dlcInstallDir = config.get(Constants.DLC).orElse(null);
    boolean dlcInPropath = config.getBoolean(Constants.PROPATH_DLC).orElse(false);
    if (dlcInPropath && !Strings.isNullOrEmpty(dlcInstallDir)) {
      File dlc = new File(dlcInstallDir);

      Files.fileTraverser().depthFirstPreOrder(new File(dlc, "gui")).forEach(f -> {
        if (f.getName().endsWith(".pl")) {
          service.submit(() -> parseLibrary(f));
        }
      });
    }
    
    try {
      service.shutdown();
      service.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException caught) {
      LOG.error("Unable to finish parsing rcode...", caught);
    }
    LOG.info("{} RCode read in {} ms - {} classes - {} methods - {} properties", numRCode.get(),
        System.currentTimeMillis() - currTime, numClasses.get(), numMethods.get(), numProperties.get());
  }

  private TypeInfo parseRCode(File file) {
    try (FileInputStream fis = new FileInputStream(file)) {
      LOG.debug("Parsing rcode {}", file.getAbsolutePath());
      RCodeInfo rci = new RCodeInfo(fis);
      if (rci.isClass()) {
        return rci.getTypeInfo();
      }
    } catch (InvalidRCodeException | IOException | RuntimeException caught) {
      LOG.error("Unable to parse rcode {} - Please open issue on GitHub - {}", file.getAbsolutePath(),
          caught.getClass().getName());
    }
    return null;
  }

  private void parseLibrary(File lib) {
    LOG.debug("Parsing PL " + lib.getAbsolutePath());
    PLReader pl = new PLReader(lib);
    for (FileEntry entry : pl.getFileList()) {
      if (entry.getFileName().endsWith(".r")) {
        try {
          RCodeInfo rci = new RCodeInfo(pl.getInputStream(entry));
          if (rci.isClass()) {
            proparseSession.injectTypeInfo(rci.getTypeInfo());
          }
        } catch (InvalidRCodeException | IOException caught) {
          LOG.error("Unable to open file " + entry.getFileName() + " in PL " + lib.getAbsolutePath(), caught);
        }
      }
    }
  }

  public File getPctDir() {
    return new File(binariesDirs.get(0).toFile(), ".pct");
  }

  public List<Path> getBinariesDirs() {
    return binariesDirs;
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
   * Return File pointer to rcode in sonar.oe.binaries directory if such rcode exists
   * 
   * @param fileName File name from profiler
   */
  public File getRCode(String fileName) {
    for (Path binariesDir : binariesDirs) {
      if (fileName.endsWith(".r"))
        return new File(fileName);

      Path rCode = binariesDir.resolve(FilenameUtils.removeExtension(fileName) + ".r");
      if (rCode.toFile().exists())
        return rCode.toFile();
      // Profiler also send file name as packagename.classname
      Path rCode2 = binariesDir.resolve(fileName.replace('.', '/') + ".r");
      if (rCode2.toFile().exists())
        return rCode2.toFile();
    }

    return null;
  }

  public String getRelativePathToSourceDirs(InputFile file) {
    for (Path p : sourcePaths) {
      try {
        String s = p.toAbsolutePath().relativize(Paths.get(file.uri())).toString();
        if (!Strings.isNullOrEmpty(s) && !s.startsWith(".."))
          return s;
      } catch (IllegalArgumentException uncaught) {
        // Path#relativize() can throw IllegalArgumentException
        // We just swallow it...
      }
    }
    return "";
  }

  public File getWarningsFile(InputFile file) {
    String s = getRelativePathToSourceDirs(file);
    if (!Strings.isNullOrEmpty(s))
      return new File(getPctDir(), s + ".warnings");
    return null;
  }

  public File getXrefFile(InputFile file) {
    String s = getRelativePathToSourceDirs(file);
    if (!Strings.isNullOrEmpty(s))
      return new File(getPctDir(), s + ".xref");
    return null;
  }

  public File getListingFile(InputFile file) {
    String s = getRelativePathToSourceDirs(file);
    if (!Strings.isNullOrEmpty(s))
      return new File(getPctDir(), s);
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
   * @return True if file name is defined as an include file
   */
  public boolean isIncludeFile(String name) {
    return includeExtensions.contains(Files.getFileExtension(name).toLowerCase()); 
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
    return config.getBoolean(Constants.SKIP_PROPARSE_PROPERTY).orElse(false);
  }

  public boolean useProparseDebug() {
    return config.getBoolean(Constants.PROPARSE_DEBUG).orElse(false);
  }

  public boolean useXrefFilter() {
    return config.getBoolean(Constants.XREF_FILTER).orElse(false);
  }

  /**
   * @return False only if property is present and set to false
   */
  public boolean useAnalytics() {
    return config.getBoolean(Constants.OE_ANALYTICS).orElse(true);
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

  public RefactorSession getProparseSession(boolean sonarLintSession) {
    if (proparseSession == null) {
      Schema sch = readSchema(config, fileSystem, sonarLintSession);
      ProparseSettings ppSettings = new ProparseSettings(getPropathAsString(),
          config.getBoolean(Constants.BACKSLASH_ESCAPE).orElse(false));

      // Some preprocessor values can be overridden at the project level
      Optional<String> opsys = config.get("sonar.oe.preprocessor.opsys");
      if (opsys.isPresent())
        ppSettings.setCustomOpsys(opsys.get());

      Optional<String> windowSystem = config.get("sonar.oe.preprocessor.window-system");
      if (windowSystem.isPresent())
        ppSettings.setCustomWindowSystem(windowSystem.get());

      Optional<String> proVersion = config.get("sonar.oe.preprocessor.proversion");
      if (proVersion.isPresent())
        ppSettings.setCustomProversion(proVersion.get());

      Optional<Boolean> batchMode = config.getBoolean("sonar.oe.preprocessor.batch-mode");
      if (batchMode.isPresent())
        ppSettings.setCustomBatchMode(batchMode.get());

      Optional<String> processArch = config.get("sonar.oe.preprocessor.process-architecture");
      if (processArch.isPresent())
        ppSettings.setCustomProcessArchitecture(processArch.get());

      proparseSession = new RefactorSession(ppSettings, sch, encoding());
      proparseSession.injectTypeInfoCollection(ProgressClasses.getProgressClasses());
      if (!sonarLintSession) {
        // Parse entire build directory if not in SonarLint
        parseBuildDirectory();
      }
    }

    return proparseSession;
  }

  /**
   * Force usage of sonar.sourceEncoding property as SonarLint doesn't set correctly encoding
   */
  private Charset encoding() {
    String encoding = config.get(CoreProperties.ENCODING_PROPERTY).orElse("");
    if (Strings.isNullOrEmpty(encoding)) {
      return Charset.defaultCharset();
    } else {
      return Charset.forName(encoding.trim());
    }
  }

  private Schema readSchema(Configuration config, FileSystem fileSystem, boolean useCache) {
    String dbList = config.get(Constants.DATABASES).orElse("");
    LOG.info("Using schema : {}", dbList);
    Collection<IDatabase> dbs = new ArrayList<>();

    for (String str : Splitter.on(',').trimResults().omitEmptyStrings().split(dbList)) {
      String dbName;
      int colonPos = str.lastIndexOf(':');
      if (colonPos <= 1) {
        dbName = FilenameUtils.getBaseName(str);
      } else {
        dbName = str.substring(colonPos + 1);
        str = str.substring(0, colonPos);
      }

      LOG.debug("Parsing {} with alias {}", fileSystem.resolvePath(str), dbName);
      File dfFile = fileSystem.resolvePath(str);
      File serFile = new File(fileSystem.baseDir(), ".sonarlint/" + str.replace(':', '_').replace('\\', '_').replace('/', '_') + ".bin");
      serFile.getParentFile().mkdir();
      DatabaseDescription desc = null;
      if (useCache && (dfFile.lastModified() < serFile.lastModified())) {
        LOG.debug("SonarLint side, using serialized file");
        try (InputStream is = new FileInputStream(serFile)) {
          desc = DatabaseDescription.deserialize(is, dbName);
        } catch (IOException caught) {
          LOG.error("Unable to deserialize from '" + serFile + "', deleting file", caught);
          FileUtils.deleteQuietly(serFile);
        }
      } else {
        try {
          desc = DumpFileUtils.getDatabaseDescription(fileSystem.resolvePath(str), dbName);
        } catch (IOException caught) {
          LOG.error("Unable to parse " + str, caught);
        }
        if ((desc != null) && useCache) {
          try (OutputStream os = new FileOutputStream(serFile)) {
            desc.serialize(os);
          } catch (IOException caught) {
            LOG.error("Unable to serialize to '" + serFile + "'", caught);
          }
        }
      }
      if (desc != null) {
        dbs.add(new DatabaseWrapper(desc));
      }
    }

    Schema sch = new Schema(dbs.toArray(new IDatabase[] {}));
    if (!sch.getDbSet().isEmpty()) {
      sch.createAlias("dictdb", sch.getDbSet().first().getName());
    }
    for (String str : Splitter.on(';').trimResults().omitEmptyStrings().split(
        config.get(Constants.ALIASES).orElse(""))) {
      List<String> lst = Splitter.on(',').trimResults().splitToList(str);
      for (String alias : lst.subList(1, lst.size())) {
        LOG.debug("Adding {} aliases to database {}", alias, lst.get(0));
        sch.createAlias(alias, lst.get(0));
      }
    }

    return sch;
  }
}
