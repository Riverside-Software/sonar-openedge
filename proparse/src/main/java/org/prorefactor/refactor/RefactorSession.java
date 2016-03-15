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
package org.prorefactor.refactor;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.prorefactor.core.schema.Schema;
import org.prorefactor.proparse.SymbolScope;
import org.prorefactor.refactor.settings.IProgressSettings;
import org.prorefactor.refactor.settings.IProparseSettings;

import com.google.inject.Inject;

/**
 * This class provides an interface to an org.prorefactor.refactor session. Much of this class was originally put in
 * place for use of Proparse within an Eclipse environment, with references to multiple projects within Eclipse.
 */
public class RefactorSession {
  public static final int OPSYS_WINDOWS = 1;
  public static final int OPSYS_UNIX = 2;

  private final IProgressSettings progressSettings;
  private final IProparseSettings proparseSettings;
  private final Schema schema;
  private final Charset charset;

  private final Map<String, SymbolScope> superCache = new HashMap<>();

  @Inject
  public RefactorSession(IProgressSettings progressSettings, IProparseSettings proparseSettings, Schema schema) {
    this(progressSettings, proparseSettings, schema, Charset.defaultCharset());
  }

  public RefactorSession(IProgressSettings progressSettings, IProparseSettings proparseSettings, Schema schema,
      Charset charset) {
    this.progressSettings = progressSettings;
    this.proparseSettings = proparseSettings;
    this.schema = schema;
    this.charset = charset;
  }

  public Charset getCharset() {
    return charset;
  }

  /**
   * This gets called by DoParse at cleanup time, if multiParse==false.
   */
  public void clearSuperCache() {
    superCache.clear();
  }

  /**
   * Adds an inheritance scope regardless of the multiParse flag. Deals with name's letter case.
   */
  public void addToSuperCache(String name, SymbolScope scope) {
    superCache.put(name.toLowerCase(), scope);
  }

  /**
   * The lookup deals with the name's letter case.
   */
  public SymbolScope lookupSuper(String superName) {
    return superCache.get(superName.toLowerCase());
  }

  public Schema getSchema() {
    return schema;
  }

  public void disableParserListing() {
    proparseSettings.disableParserListing();
  }

  public void enableParserListing() {
    proparseSettings.enableParserListing();
  }

  /**
   * Get the listing file name, makes sure the directory exists.
   */
  public String getListingFileName() {
    return "listingfile.txt";
  }

  /**
   * Are the project binaries (.pub, .msg) enabled?
   */
  public boolean getProjectBinariesEnabled() {
    return proparseSettings.getProjectBinaries();
  }

  public IProgressSettings getProgressSettings() {
    return progressSettings;
  }

  /**
   * Returns the Settings for the currently loaded project
   */
  public IProparseSettings getProparseSettings() {
    return proparseSettings;
  }

  /**
   * Disable the project directory binary output files (.pub, .msg).
   */
  @Deprecated
  public void setProjectBinariesEnabledOff() {
    proparseSettings.disableProjectBinaries();
  }

  /**
   * Enable the project directory binary output files (.pub, .msg).
   */
  @Deprecated
  public void setProjectBinariesEnabledOn() {
    proparseSettings.enableProjectBinaries();
  }

  public String findFile(String fileName) {
    // If we have an absolute path-filename, we don't search the path.
    // If we have a relative (starts with dot) path-filename, ditto.
    int len = fileName.length();
    
    // Windows drive letter, ex: "C:"
 // Relative path, "./" or "../"
    if ((len > 0 && (fileName.charAt(0) == '/' || fileName.charAt(0) == '\\')) || (len > 1 && fileName.charAt(1) == ':')
        || (len > 1 && fileName.charAt(0) == '.')) {
      if (new File(fileName).exists())
        return fileName;
    }

    for (String p : progressSettings.getPropathAsList()) {
      String tryPath = p + File.separatorChar + fileName;
      if (new File(tryPath).exists())
        return tryPath;
    }

    return "";
  }

  /**
   * Find a file (or directory) on the propath
   * 
   * TODO Method probably redundant with the previous one
   * 
   * @return null if not found
   */
  public File findFile2(String filename) {
    File inFile = new File(filename);
    // "absolute" on windows means drive letter (i.e. c:)
    // We don't search the path if it starts with '.', '/', or '\'
    char c = filename.charAt(0);
    if (inFile.isAbsolute() || c == '.' || c == '/' || c == '\\') {
      if (inFile.exists())
        return inFile;
      return null;
    }
    String propath = progressSettings.getPropath();
    String[] parts = propath.split(",");
    for (String part : parts) {
      File retFile = new File(part + File.separator + filename);
      if (retFile.exists())
        return retFile;
    }
    return null;
  } // findFile

  /**
   * Find a class file on propath, from the "package.classname"
   * 
   * @return null if not found.
   */
  public File findFileForClassName(String className) {
    return findFile2(className.replace('.', '/') + ".cls");
  }
}
