/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2020 Riverside Software
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.InvalidPathException;
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
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Configuration;
import org.sonar.api.platform.Server;
import org.sonar.api.scanner.ScannerSide;
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
import com.google.common.primitives.Ints;

import eu.rssw.antlr.database.DumpFileUtils;
import eu.rssw.antlr.database.objects.DatabaseDescription;
import eu.rssw.pct.FileEntry;
import eu.rssw.pct.PLReader;
import eu.rssw.pct.RCodeInfo;
import eu.rssw.pct.RCodeInfo.InvalidRCodeException;
import eu.rssw.pct.elements.ITypeInfo;

@ScannerSide
@SonarLintSide
public class OpenEdgeSettings {
  private static final Logger LOG = Loggers.get(OpenEdgeSettings.class);

  // IoC
  private final Configuration config;
  private final FileSystem fileSystem;
  private final SonarRuntime runtime;
  private final Server server;

  // Internal use
  private boolean init = false;
  private final List<Path> sourcePaths = new ArrayList<>();
  private final List<Path> binariesDirs = new ArrayList<>();
  private final List<Path> pctDirs = new ArrayList<>();
  private final List<File> propath = new ArrayList<>();
  private final List<File> propathDlc = new ArrayList<>();
  private final List<File> propathFull = new ArrayList<>();
  private final Set<String> includeExtensions = new HashSet<>();
  private final Set<String> cpdAnnotations = new HashSet<>();
  private final Set<String> cpdMethods = new HashSet<>();
  private final Set<String> cpdProcedures = new HashSet<>();
  private final Set<Integer> xrefBytes = new HashSet<>();

  private RefactorSessionEnv sessionsEnv;
  private RefactorSession defaultSession;
  private String oePluginVersion;

  public OpenEdgeSettings(Configuration config, FileSystem fileSystem, SonarRuntime runtime) {
    this(config, fileSystem, runtime, null);
  }

  public OpenEdgeSettings(Configuration config, FileSystem fileSystem, SonarRuntime runtime, Server server) {
    this.config = config;
    this.fileSystem = fileSystem;
    this.runtime = runtime;
    this.server = server;
  }

  public final void init() {
    // init() is called from every sensor, and there's no guarantee in sensor order of execution
    if (init)
      return;

    oePluginVersion = readPluginVersion(this.getClass().getClassLoader(), "sonar-openedge.txt");
    LOG.info("OpenEdge plugin version: {}", oePluginVersion);
    LOG.info("Loading OpenEdge settings for server ID '{}'", server == null ? "" : server.getId());
    initializeDirectories(config, fileSystem);
    initializePropathDlc(config);
    initializeDefaultPropath(config, fileSystem);
    initializeCPD(config);
    initializeXrefBytes(config);
    initializeIncludeExtensions(config);

    LOG.debug("Using backslash as escape character : {}", config.getBoolean(Constants.BACKSLASH_ESCAPE).orElse(false));
    if (useXrefFilter()) {
      LOG.info("XML XREF filter activated [{}]", getXrefBytesAsString());
    }
    init = true;
  }

  private final void initializeDirectories(Configuration config, FileSystem fileSystem) {
    // Looking for source directories
    Optional<String> sonarSources = config.get("sonar.sources");
    if (sonarSources.isPresent()) {
      initializeDirectory(fileSystem, sonarSources.get(), "source", sourcePaths);
    } else {
      sourcePaths.add(fileSystem.baseDir().toPath().normalize());
      LOG.debug("No sonar.sources property, defaults to base directory");
    }

    // Build directories
    Optional<String> binariesSetting = config.get(Constants.BINARIES);
    if (binariesSetting.isPresent()) {
      initializeDirectory(fileSystem, binariesSetting.get(), "binaries", binariesDirs);
    } else {
      LOG.debug("No sonar.oe.binaries property, defaults to source directories");
      binariesDirs.addAll(sourcePaths);
    }

    // .PCT directories
    Optional<String> dotPctSetting = config.get(Constants.DOTPCT);
    if (dotPctSetting.isPresent()) {
      initializeDirectory(fileSystem, dotPctSetting.get(), ".pct", pctDirs);
    } else {
      LOG.debug("No sonar.oe.dotpct property, defaults to <binaries>/.pct directories");
      binariesDirs.forEach(dir -> pctDirs.add(Paths.get(dir.toString(), ".pct")));
    }
  }

  private final void initializeDirectory(FileSystem fileSystem, String prop, String type, List<Path> paths) {
    for (String str : Splitter.on(',').trimResults().split(prop)) {
      LOG.debug("Adding {} directory '{}' ...", type, str);
      try {
        Path p = fileSystem.baseDir().toPath().resolve(str).normalize();
        LOG.debug("  ... resolved to '{}'", p);
        paths.add(p);
      } catch (InvalidPathException caught) {
        LOG.error("Unable to resolve {} directory '{}'", type, str);
      }
    }
  }

  private final void initializePropathDlc(Configuration config) {
    String dlcInstallDir = config.get(Constants.DLC).orElse(null);
    boolean dlcInPropath = config.getBoolean(Constants.PROPATH_DLC).orElse(false);
    if (dlcInPropath && !Strings.isNullOrEmpty(dlcInstallDir)) {
      File dlc = new File(dlcInstallDir);
      LOG.info("DLC directory '{}' will be added to all PROPATH", dlc.getAbsolutePath());
      propathDlc.add(new File(dlc, "gui"));
      propathDlc.add(new File(dlc, "tty"));
      propathDlc.add(new File(dlc, "src"));
      propathDlc.add(dlc);
    }
  }

  private final void initializeDefaultPropath(Configuration config, FileSystem fileSystem) {
    propath.addAll(readPropath(fileSystem, config.get(Constants.PROPATH).orElse("")));
    propathFull.addAll(propath);
    propathFull.addAll(propathDlc);
  }

  private final List<File> readPropath(FileSystem fileSystem, String propValue) {
    List<File> retVal = new ArrayList<>();
    LOG.info("Using PROPATH : {}", propValue);
    for (String str : Splitter.on(',').trimResults().split(propValue)) {
      File entry = resolvePath(str);
      LOG.debug("Adding {} to PROPATH", entry.getAbsolutePath());
      retVal.add(entry);
    }

    return retVal;
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
      ITypeInfo info = parseRCode(rcd);
      if (info != null) {
        parseHierarchy(info);
      }
    }
  }

  private final void parseHierarchy(ITypeInfo info) {
    LOG.info("Injecting type info '{}'", info);
    defaultSession.injectTypeInfo(info);
    if (info.getParentTypeName() != null) {
      File rcd = getRCode(info.getParentTypeName());
      if (rcd != null) {
        ITypeInfo inf = parseRCode(rcd);
        if (inf != null) {
          parseHierarchy(inf);
        }
      }
    }
    for (String str : info.getInterfaces()) {
      File rcd = getRCode(str);
      if (rcd != null) {
        ITypeInfo inf = parseRCode(rcd);
        if (inf != null) {
          parseHierarchy(inf);
        }
      }
    }
  }

  private final void parseBuildDirectory() {
    if (config.getBoolean(Constants.SKIP_RCODE).orElse(false))
      return;
    long currTime = System.currentTimeMillis();
    RCodeInjectorService srv = new RCodeInjectorService();
    for (Path binDir : binariesDirs) {
      parseRCodeInPath(binDir.toFile(), srv);
    }

    // Include files in $DLC/gui
    String dlcInstallDir = config.get(Constants.DLC).orElse(null);
    boolean dlcInPropath = config.getBoolean(Constants.PROPATH_DLC).orElse(false);
    if (dlcInPropath && !Strings.isNullOrEmpty(dlcInstallDir)) {
      File dlc = new File(dlcInstallDir);
      parseRCodeInPath(new File(dlc, "gui"), srv);
    }

    // Include files in propath
    for (File entry : propath) {
      if (entry.getName().endsWith(".pl")) {
        srv.service.submit(() -> {
          parseLibrary(entry);
        });
      } else {
        parseRCodeInPath(entry, srv);
      }
    }

    try {
      srv.service.shutdown();
      srv.service.awaitTermination(10, TimeUnit.MINUTES);
    } catch (InterruptedException caught) {
      LOG.error("Unable to finish parsing rcode...", caught);
    }
    LOG.info("{} RCode read in {} ms - {} classes - {} methods - {} properties", srv.numRCode.get(),
        System.currentTimeMillis() - currTime, srv.numClasses.get(), srv.numMethods.get(), srv.numProperties.get());
  }

  private void parseRCodeInPath(File path, RCodeInjectorService srv) {
    LOG.debug("Parsing rcode in directory {}", path.getAbsolutePath());
    Files.fileTraverser().depthFirstPreOrder(path).forEach(f -> {
      if (f.getName().endsWith(".r")) {
        srv.numRCode.incrementAndGet();
        srv.service.submit(() -> {
          ITypeInfo info = parseRCode(f);
          if (info != null) {
            srv.numClasses.incrementAndGet();
            srv.numMethods.addAndGet(info.getMethods().size());
            srv.numProperties.addAndGet(info.getProperties().size());
            defaultSession.injectTypeInfo(info);
          }
        });
      } else if (f.getName().endsWith(".pl")) {
        srv.service.submit(() -> {
          parseLibrary(f);
        });
      }
    });
  }

  private ITypeInfo parseRCode(File file) {
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
    LOG.debug("Parsing PL {}", lib.getAbsolutePath());
    PLReader pl = new PLReader(lib);
    for (FileEntry entry : pl.getFileList()) {
      if (entry.getFileName().endsWith(".r")) {
        try {
          RCodeInfo rci = new RCodeInfo(pl.getInputStream(entry));
          if (rci.isClass()) {
            defaultSession.injectTypeInfo(rci.getTypeInfo());
          }
        } catch (InvalidRCodeException | IOException caught) {
          LOG.error("Unable to open file " + entry.getFileName() + " in PL " + lib.getAbsolutePath(), caught);
        }
      }
    }
  }

  public File getSonarLintXrefDir() {
    return resolvePath(config.get(Constants.SLINT_XREF).orElse(""));
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

  public boolean displayStackTraceOnError() {
    // Default is to hide stack traces in SonarLint, unless overidden by property
    return config.getBoolean(Constants.PROPARSE_ERROR_STACKTRACE).orElse(
        runtime.getProduct() == SonarProduct.SONARQUBE);
  }

  public boolean parseXrefDocument() {
    return config.getBoolean(Constants.XML_DOCUMENT_RULES).orElse(false);
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

  public File getWarningsFile(InputFile file) {
    String relPath = getRelativePathToSourceDirs(file);
    if (Strings.isNullOrEmpty(relPath))
      return null;
    else
      return getFileFromPctDirs(relPath + ".warnings");
  }

  public File getXrefFile(InputFile file) {
    String relPath = getRelativePathToSourceDirs(file);
    if (Strings.isNullOrEmpty(relPath))
      return null;
    else
      return getFileFromPctDirs(relPath + ".xref");
  }

  public File getSonarlintXrefFile(InputFile file) {
    String s = getRelativePathToSourceDirs(file);
    if (!Strings.isNullOrEmpty(s) && !s.endsWith(".") && (s.indexOf('.') > -1))
      return new File(getSonarLintXrefDir(), s.subSequence(0, s.lastIndexOf('.')) + ".xref.xml");
    return null;
  }

  public File getListingFile(InputFile file) {
    String relPath = getRelativePathToSourceDirs(file);
    if (Strings.isNullOrEmpty(relPath))
      return null;
    else
      return getFileFromPctDirs(relPath);
  }

  private File getFileFromPctDirs(String relPath) {
    for (Path dir : pctDirs) {
      Path path = dir.resolve(relPath);
      if (path.toFile().exists())
        return path.toFile();
    }

    return null;
  }

  private String getRelativePathToSourceDirs(InputFile file) {
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

  /**
   * Returns absolute file name if found in work directory or in propath
   */
  public String getFilePath(String fileName) {
    File f = new File(fileName);
    if (f.exists())
      return f.getAbsolutePath();

    for (File file : propathFull) {
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

  public boolean skipXCode() {
    return config.getBoolean(Constants.SKIP_XCODE).orElse(true);
  }

  public boolean useProparseDebug() {
    return config.getBoolean(Constants.PROPARSE_DEBUG).orElse(false);
  }

  public boolean useXrefFilter() {
    return config.getBoolean(Constants.XREF_FILTER).orElse(false);
  }

  public boolean useANTLR4() {
    return config.getBoolean(Constants.ANTLR4_TEST).orElse(false);
  }

  public boolean useANTLR4Profiler() {
    return config.getBoolean(Constants.ANTLR4_PROFILER).orElse(false);
  }

  public boolean useSimpleCPD() {
    return config.getBoolean(Constants.USE_SIMPLE_CPD).orElse(false);
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
    return propathFull;
  }

  public String getOpenEdgePluginVersion() {
    return this.oePluginVersion;
  }

  public String getServerId() {
    if (server == null)
      return "";
    String str = server.getId();
    int dashIndex = str.indexOf('-');

    return (dashIndex == 8) && (str.length() >= 20) ? str.substring(dashIndex + 1) : str;
  }

  public String getPropathAsString() {
    return Joiner.on(',').skipNulls().join(propathFull);
  }

  public IRefactorSessionEnv getProparseSessions() {
    if (sessionsEnv == null) {
      sessionsEnv = new RefactorSessionEnv(getProparseSession());
      if (runtime.getProduct() == SonarProduct.SONARLINT)
        // Only one session in SonarLint for now
        return sessionsEnv;

      int modNum = 1;
      while (true) {
        LOG.info("Looking for submodule #{}", modNum);
        String prefix = "sonar.oe.module" + modNum;
        String modDatabases = config.get(prefix + ".databases").orElse(
            config.get(Constants.DATABASES).orElse("")).trim();
        String modAliases = config.get(prefix + ".aliases").orElse(config.get(Constants.ALIASES).orElse("")).trim();
        String modulePropath = config.get(prefix + ".propath").orElse(config.get(Constants.PROPATH).orElse("")).trim();
        String modPattern = config.get(prefix + ".pattern").orElse("").trim();

        if ("".equals(modPattern)) {
          LOG.info("  No pattern found - Leaving...");
          return sessionsEnv;
        }
        // As long as we have a pattern, we contine the list
        LOG.info(" Found pattern '{}' for submodule #{}", modPattern, modNum);

        List<File> pp = readPropath(fileSystem, modulePropath);
        pp.addAll(propathDlc);
        ProparseSettings ppSettings = new ProparseSettings(Joiner.on(',').skipNulls().join(pp),
            config.getBoolean(Constants.BACKSLASH_ESCAPE).orElse(false));
        Schema sch = readSchema(config, fileSystem, modDatabases, modAliases);
        RefactorSession rf = new RefactorSession(ppSettings, sch, encoding(), getProparseSession());
        sessionsEnv.addSession(rf, modPattern);

        modNum++;
      }
    }

    return sessionsEnv;
  }

  private RefactorSession getProparseSession() {
    if (defaultSession == null) {
      Schema sch = readSchema(config, fileSystem, config.get(Constants.DATABASES).orElse(""),
          config.get(Constants.ALIASES).orElse(""));
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
      Integer processArchInt = processArch.isPresent() ? Ints.tryParse(processArch.get()) : null;
      if (processArchInt != null)
        ppSettings.setCustomProcessArchitecture(processArchInt);

      Optional<Boolean> skipXCode = config.getBoolean(Constants.SKIP_XCODE);
      if (skipXCode.isPresent())
        ppSettings.setCustomSkipXCode(skipXCode.get());

      // ANTLR Token Deletion
      ppSettings.setAntlrTokenDeletion(config.getBoolean("sonar.oe.proparse.token.deletion").orElse(false));
      // ANTLR Token Insertion
      ppSettings.setAntlrTokenInsertion(config.getBoolean("sonar.oe.proparse.token.insertion").orElse(false));
      // ANTLR Recover
      ppSettings.setAntlrRecover(config.getBoolean("sonar.oe.proparse.recover").orElse(false));

      defaultSession = new RefactorSession(ppSettings, sch, encoding());
      Optional<String> assemblyCatalog = config.get(Constants.ASSEMBLY_CATALOG);
      if (assemblyCatalog.isPresent()) {
        try (Reader reader = new FileReader(assemblyCatalog.get())) {
          defaultSession.injectClassesFromCatalog(reader);
        } catch (IOException caught) {
          LOG.error("Unable to read assembly catalog '" + assemblyCatalog.get() + "'", caught);
        }
      }

      if (runtime.getProduct() == SonarProduct.SONARQUBE) {
        // Parse entire build directory if not in SonarLint
        parseBuildDirectory();
      }
    }

    return defaultSession;
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

  private Collection<IDatabase> readSchemaFromProp1(FileSystem fileSystem, String dbList) {
    Collection<IDatabase> dbs = new ArrayList<>();
    LOG.info("Using schema : {}", dbList);

    for (String str : Splitter.on(',').trimResults().omitEmptyStrings().split(dbList)) {
      String dbName;
      int colonPos = str.lastIndexOf(':');
      if (colonPos <= 1) {
        dbName = FilenameUtils.getBaseName(str);
      } else {
        dbName = str.substring(colonPos + 1);
        str = str.substring(0, colonPos);
      }

      LOG.debug("Parsing {} with alias {}", resolvePath(str), dbName);
      File dfFile = resolvePath(str);
      File serFile = new File(fileSystem.baseDir(),
          ".sonarlint/" + str.replace(':', '_').replace('\\', '_').replace('/', '_') + ".bin");
      serFile.getParentFile().mkdir();
      DatabaseDescription desc = null;
      if ((runtime.getProduct() == SonarProduct.SONARLINT) && (dfFile.lastModified() < serFile.lastModified())) {
        LOG.debug("SonarLint side, using serialized file");
        try (InputStream is = new FileInputStream(serFile)) {
          desc = DatabaseDescription.deserialize(is, dbName);
        } catch (IOException caught) {
          LOG.error("Unable to deserialize from '" + serFile + "', deleting file", caught);
          FileUtils.deleteQuietly(serFile);
        }
      } else {
        try {
          desc = DumpFileUtils.getDatabaseDescription(resolvePath(str), dbName);
        } catch (IOException caught) {
          // Interrupt SonarLint analysis as this is the only way to have a notification for invalid DF file
          // By default, analysis log is not visible
          if (runtime.getProduct() == SonarProduct.SONARLINT) {
            throw new RuntimeException(
                "Unable to read database schema from '" + dfFile.getName() + "', property value is '" + dbList + "'",
                caught);
          } else {
            LOG.error("Unable to parse " + str, caught);
          }
        }
        if ((desc != null) && (runtime.getProduct() == SonarProduct.SONARLINT)) {
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

    return dbs;
  }

  private Collection<IDatabase> readSchemaFromProp2(Configuration config) {
    Collection<IDatabase> dbs = new ArrayList<>();
    for (String str : Splitter.on(',').trimResults().omitEmptyStrings().split(
        config.get(Constants.SLINT_DATABASES).orElse(""))) {
      String dbName = FilenameUtils.getBaseName(str);
      LOG.debug("Parsing '{}' with db name {}", str, dbName);
      try (InputStream is = new FileInputStream(new File(str))) {
        dbs.add(new DatabaseWrapper(DatabaseDescription.deserialize(is, dbName)));
      } catch (IOException caught) {
        LOG.error("Unable to deserialize from '" + str + "'", caught);
      }
    }

    return dbs;
  }

  private Schema readSchema(Configuration config, FileSystem fileSystem, String dbPropValue, String aliasPropValue) {
    Collection<IDatabase> dbs = new ArrayList<>();

    // First use sonar.oe.databases property, even on SonarLint (for compatibility reasons)
    if (dbPropValue.length() > 0) {
      dbs = readSchemaFromProp1(fileSystem, dbPropValue);
    } else if ((runtime.getProduct() == SonarProduct.SONARLINT)
        && (config.get(Constants.SLINT_DATABASES).orElse("").length() > 0)) {
      dbs = readSchemaFromProp2(config);
    }

    Schema sch = new Schema(dbs.toArray(new IDatabase[] {}));
    if (!sch.getDbSet().isEmpty()) {
      sch.createAlias("dictdb", sch.getDbSet().first().getName());
    }
    for (String str : Splitter.on(';').trimResults().omitEmptyStrings().split(aliasPropValue)) {
      List<String> lst = Splitter.on(',').trimResults().splitToList(str);
      for (String alias : lst.subList(1, lst.size())) {
        LOG.debug("Adding {} aliases to database {}", alias, lst.get(0));
        sch.createAlias(alias, lst.get(0));
      }
    }

    return sch;
  }

  public String readPluginVersion(ClassLoader cl, String file) {
    String retVal = "";
    try (InputStream inp = cl.getResourceAsStream(file);
        Reader r1 = new InputStreamReader(inp);
        BufferedReader r2 = new BufferedReader(r1)) {
      retVal = r2.readLine();
    } catch (IOException caught) {
      LOG.debug("Unable to read '" + file + "'", caught);
      retVal = file + " not found";
    }

    return retVal;
  }

  private File resolvePath(String path) {
    File file = new File(path);
    if (file.isAbsolute())
      return file;

    try {
      file = new File(fileSystem.baseDir(), path).getCanonicalFile();
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to resolve path '" + path + "'", e);
    }
    return file;
  }

  private class RCodeInjectorService {
    AtomicInteger numClasses = new AtomicInteger(0);
    AtomicInteger numMethods = new AtomicInteger(0);
    AtomicInteger numProperties = new AtomicInteger(0);
    AtomicInteger numRCode = new AtomicInteger(0);
    ExecutorService service = Executors.newFixedThreadPool(4);
  }
}
