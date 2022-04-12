/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.prorefactor.core.schema.ISchema;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.refactor.settings.IProparseSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.PropertyElement;
import eu.rssw.pct.elements.fixed.TypeInfo;

/**
 * This class provides an interface to an org.prorefactor.refactor session. Much of this class was originally put in
 * place for use of Proparse within an Eclipse environment, with references to multiple projects within Eclipse.
 */
public class RefactorSession implements IProparseEnvironment {
  private static final Logger LOG = LoggerFactory.getLogger(RefactorSession.class);

  private final IProparseSettings proparseSettings;
  private final ISchema schema;
  private final Charset charset;

  // Structure from rcode
  private final Map<String, ITypeInfo> typeInfoMap;
  private final Map<String, ITypeInfo> lcTypeInfoMap;
  // Read from internal classes list and assembly catalog
  private final Map<String, ITypeInfo> classInfo;
  private final Map<String, ITypeInfo> lcClassInfo;
  // List of classes per package
  private final Map<String, List<ITypeInfo>> classesPerPkg;
  private final Object pkgLock = new Object();

  // Cached entries from propath
  private final Map<String, File> propathCache = new HashMap<>();
  // Cached entries from propath again
  private final Map<String, String> propathCache2 = new HashMap<>();

  @Inject
  public RefactorSession(IProparseSettings proparseSettings, ISchema schema) {
    this(proparseSettings, schema, Charset.defaultCharset());
  }

  public RefactorSession(IProparseSettings proparseSettings, ISchema schema,
      Charset charset) {
    this.proparseSettings = proparseSettings;
    this.schema = schema;
    this.charset = charset;

    typeInfoMap = Collections.synchronizedMap(new HashMap<>());
    lcTypeInfoMap = Collections.synchronizedMap(new HashMap<>());
    classInfo = new HashMap<>();
    lcClassInfo = new HashMap<>();
    classesPerPkg = new HashMap<>();

    initializeProgressClasses();
  }

  public RefactorSession(IProparseSettings proparseSettings, ISchema schema,
      Charset charset, RefactorSession copy) {
    this.proparseSettings = proparseSettings;
    this.schema = schema;
    this.charset = charset;

    typeInfoMap = copy.typeInfoMap;
    lcTypeInfoMap = copy.lcTypeInfoMap;
    classInfo = copy.classInfo;
    lcClassInfo = copy.lcClassInfo;
    classesPerPkg = copy.classesPerPkg;

    initializeProgressClasses();
  }

  private void initializeProgressClasses() {
    for (ITypeInfo typeInfo : BuiltinClasses.getBuiltinClasses()) {
      classInfo.put(typeInfo.getTypeName(), typeInfo);
      lcClassInfo.put(typeInfo.getTypeName().toLowerCase(), typeInfo);
      int dotPos = typeInfo.getTypeName().lastIndexOf('.');
      String pkgName = dotPos >= 1 ? typeInfo.getTypeName().substring(0, dotPos) : "";
      synchronized (pkgLock) {
        classesPerPkg.computeIfAbsent(pkgName, key -> new ArrayList<>()).add(typeInfo);
      }
    }
  }

  public void injectClassesFromCatalog(Reader reader) {
    Gson gson = new GsonBuilder().create();
    for (ClassInfo info : gson.fromJson(reader, ClassInfo[].class)) {
      String parentType = info.baseTypes != null && info.baseTypes.length > 0 ? info.baseTypes[0] : null;
      String[] interfaces = info.baseTypes != null && info.baseTypes.length > 1
          ? Arrays.copyOfRange(info.baseTypes, 1, info.baseTypes.length) : new String[] {};
      TypeInfo typeInfo = new TypeInfo(info.name, info.isInterface, info.isAbstract, parentType, "", interfaces);
      if (info.methods != null) {
        for (String str : info.methods) {
          typeInfo.addMethod(new MethodElement(str, false, DataType.VOID));
        }
      }
      if (info.staticMethods != null) {
        for (String str : info.staticMethods) {
          typeInfo.addMethod(new MethodElement(str, true, DataType.VOID));
        }
      }
      if (info.properties != null) {
        for (String str : info.properties) {
          typeInfo.addProperty(new PropertyElement(str, false));
        }
      }
      if (info.staticProperties != null) {
        for (String str : info.staticProperties) {
          typeInfo.addProperty(new PropertyElement(str, true));
        }
      }
      classInfo.put(typeInfo.getTypeName(), typeInfo);
      lcClassInfo.put(typeInfo.getTypeName().toLowerCase(), typeInfo);

      int dotPos = info.name.lastIndexOf('.');
      String pkgName = dotPos >= 1 ? info.name.substring(0, dotPos) : "";
      synchronized(pkgLock) {
        classesPerPkg.computeIfAbsent(pkgName, key -> new ArrayList<>()).add(typeInfo);
      }
    }
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
      info = classInfo.get(clz);
    }
    if (info == null) {
      LOG.debug("No TypeInfo found for {}", clz);
    }

    return info;
  }

  @Nullable
  public ITypeInfo getTypeInfoCI(String clz) {
    if (clz == null) {
      return null;
    }
    ITypeInfo info = lcTypeInfoMap.get(clz.toLowerCase());
    if (info == null) {
      info = lcClassInfo.get(clz.toLowerCase());
    }
    if (info == null) {
      LOG.debug("No TypeInfo found for {}", clz);
    }

    return info;
  }

  public List<String> getAllClassesFromPackage(String pkgName) {
    if (Strings.isNullOrEmpty(pkgName))
      return Collections.emptyList();
    List<ITypeInfo> clsList = classesPerPkg.getOrDefault(pkgName, Collections.emptyList());

    List<String> retVal = new ArrayList<>();
    for (ITypeInfo info : clsList) {
      retVal.add(info.getTypeName());
    }

    return retVal;
  }

  public void injectTypeInfo(ITypeInfo unit) {
    if ((unit == null) || Strings.isNullOrEmpty(unit.getTypeName()))
      return;
    typeInfoMap.put(unit.getTypeName(), unit);
    lcTypeInfoMap.put(unit.getTypeName().toLowerCase(), unit);

    int dotPos = unit.getTypeName().lastIndexOf('.');
    String pkgName = dotPos >= 1 ? unit.getTypeName().substring(0, dotPos) : "";
    synchronized(pkgLock) { 
      classesPerPkg.computeIfAbsent(pkgName, key -> new ArrayList<>()).add(unit);
    }
  }

  public File findFile3(String fileName) {
    File f = propathCache.get(fileName);
    if (f != null)
      return f;

    if (isRelativePath(fileName)) {
      File f2 = new File(fileName);
      if (f2.exists()) {
        propathCache.put(fileName, f2);
        return f2;
      }
    }

    for (String p : proparseSettings.getPropathAsList()) {
      String tryPath = p + File.separatorChar + fileName;
      if (new File(tryPath).exists()) {
        propathCache.put(fileName, new File(tryPath));
        return new File(tryPath);
      }
    }

    return null;
  }

  public String findFile(String fileName) {
    if (propathCache2.containsKey(fileName))
      return propathCache2.get(fileName);

    if (isRelativePath(fileName) && new File(fileName).exists()) {
      propathCache2.put(fileName, fileName);
      return fileName;
    }

    for (String p : proparseSettings.getPropathAsList()) {
      String tryPath = p + File.separatorChar + fileName;
      if (new File(tryPath).exists()) {
        propathCache2.put(fileName, tryPath);
        return tryPath;
      }
    }

    propathCache2.put(fileName, "");
    return "";
  }

  private boolean isRelativePath(String fileName) {
    // Windows drive letter, ex: "C:"
    // Relative path, "./" or "../"
    int len = fileName.length();
    return ((len > 0) && (fileName.charAt(0) == '/' || fileName.charAt(0) == '\\'))
        || ((len > 1) && (fileName.charAt(1) == ':' || fileName.charAt(0) == '.'));
  }

  private class ClassInfo {
    String name;
    String[] baseTypes;
    boolean isAbstract;
    @SuppressWarnings("unused")
    boolean isClass;
    @SuppressWarnings("unused")
    boolean isEnum;
    boolean isInterface;
    String[] properties;
    String[] methods;
    String[] staticMethods;
    String[] staticProperties;
  }
}
