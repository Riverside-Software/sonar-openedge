/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.refactor;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.prorefactor.core.schema.ISchema;
import org.prorefactor.refactor.settings.IProparseSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import eu.rssw.pct.elements.ITypeInfo;

/**
 * This class provides an interface to an org.prorefactor.refactor session. Much of this class was originally put in
 * place for use of Proparse within an Eclipse environment, with references to multiple projects within Eclipse.
 */
public class RefactorSession {
  private static final Logger LOG = LoggerFactory.getLogger(RefactorSession.class);

  private final IProparseSettings proparseSettings;
  private final ISchema schema;
  private final Charset charset;

  // Structure from rcode
  private final Map<String, ITypeInfo> typeInfoMap = Collections.synchronizedMap(new HashMap<>());

  @Inject
  public RefactorSession(IProparseSettings proparseSettings, ISchema schema) {
    this(proparseSettings, schema, Charset.defaultCharset());
  }

  public RefactorSession(IProparseSettings proparseSettings, ISchema schema,
      Charset charset) {
    this.proparseSettings = proparseSettings;
    this.schema = schema;
    this.charset = charset;
  }

  public Charset getCharset() {
    return charset;
  }

  public ISchema getSchema() {
    return schema;
  }

  /**
   * Returns the Settings for the currently loaded project
   */
  public IProparseSettings getProparseSettings() {
    return proparseSettings;
  }

  @Nullable
  public ITypeInfo getTypeInfo(String clz) {
    if (clz == null) {
      return null;
    }
    ITypeInfo info = typeInfoMap.get(clz);
    if (info == null) {
      LOG.debug("No TypeInfo found for {}", clz);
    }

    return info;
  }

  public void injectTypeInfoCollection(Collection<ITypeInfo> units) {
    for (ITypeInfo info : units) {
      injectTypeInfo(info);
    }
  }

  public void injectTypeInfo(ITypeInfo unit) {
    if ((unit == null) || Strings.isNullOrEmpty(unit.getTypeName()))
      return;
    typeInfoMap.put(unit.getTypeName(), unit);
  }

  public File findFile3(String fileName) {
    
    // If we have an absolute path-filename, we don't search the path.
    // If we have a relative (starts with dot) path-filename, ditto.
    int len = fileName.length();
    
    // Windows drive letter, ex: "C:"
 // Relative path, "./" or "../"
    if ((len > 0 && (fileName.charAt(0) == '/' || fileName.charAt(0) == '\\')) || (len > 1 && fileName.charAt(1) == ':')
        || (len > 1 && fileName.charAt(0) == '.')) {
      if (new File(fileName).exists())
        return new File(fileName);
    }

    for (String p : proparseSettings.getPropathAsList()) {
      String tryPath = p + File.separatorChar + fileName;
      if (new File(tryPath).exists())
        return new File(tryPath);
    }

    return null;
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

    for (String p : proparseSettings.getPropathAsList()) {
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
    String propath = proparseSettings.getPropath();
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
