/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2025 Riverside Software
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.prorefactor.core.schema.ISchema;
import org.prorefactor.proparse.AssemblyCatalog;
import org.prorefactor.proparse.AssemblyCatalog.Event;
import org.prorefactor.proparse.classdoc.ClassDocumentation;
import org.prorefactor.proparse.support.IProparseEnvironment;
import org.prorefactor.refactor.settings.IProparseSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import eu.rssw.pct.elements.BuiltinClasses;
import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.PrimitiveDataType;
import eu.rssw.pct.elements.fixed.EventElement;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
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

  // ClassDocumentation
  private final Map<String, ClassDocumentation> classDoc = new HashMap<>();

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
        for (MethodInfo methd : info.methods) {
          typeInfo.addMethod(methd.toMethodElement(false));
        }
      }
      if (info.staticMethods != null) {
        for (MethodInfo methd : info.staticMethods) {
          typeInfo.addMethod(methd.toMethodElement(true));
        }
      }
      if (info.properties != null) {
        for (Property str : info.properties) {
          typeInfo.addProperty(new PropertyElement(str.name, false, toDataType(str.dataType)));
        }
      }
      if (info.staticProperties != null) {
        for (Property str : info.staticProperties) {
          typeInfo.addProperty(new PropertyElement(str.name, true, toDataType(str.dataType)));
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

  private static ITypeInfo assemblyCatalogEntryToTypeInfo(AssemblyCatalog.Entry info) {
    String parentType = info.baseTypes != null && info.baseTypes.length > 0 ? info.baseTypes[0] : null;
    String[] interfaces = info.baseTypes != null && info.baseTypes.length > 1
        ? Arrays.copyOfRange(info.baseTypes, 1, info.baseTypes.length) : new String[] {};
    TypeInfo typeInfo = new TypeInfo(info.name, info.isInterface, info.isAbstract, parentType, "", interfaces);
    if (info.methods != null) {
      for (org.prorefactor.proparse.AssemblyCatalog.Method methd : info.methods) {
        typeInfo.addMethod(toMethodElement(methd));
      }
    }
    if (info.properties != null) {
      for (org.prorefactor.proparse.AssemblyCatalog.Property prop : info.properties) {
        typeInfo.addProperty(new PropertyElement(prop.name, prop.isStatic, toDataType(prop.dataType)));
      }
    }
    if (info.events != null) {
      for (Event event : info.events) {
        typeInfo.addEvent(new EventElement(event.name));
      }
    }

    return typeInfo;
  }

  public static List<ITypeInfo> getClassesFromDotNetCatalog(Reader reader) {
    List<ITypeInfo> list = new ArrayList<>();
    Gson gson = new GsonBuilder().create();
    AssemblyCatalog catalog = gson.fromJson(reader, AssemblyCatalog.class);
    if (catalog.version != 1) {
      LOG.info("Outdated JSON catalog, file should be regenerated using the latest version of the tool");
      return list;
    }
    for (AssemblyCatalog.Entry info : catalog.entries) {
      list.add(assemblyCatalogEntryToTypeInfo(info));
    }
    return list;
  }

  public void injectClassesFromDotNetCatalog(Reader reader) {
    for (ITypeInfo typeInfo: getClassesFromDotNetCatalog(reader)) {
      injectClassInfo(typeInfo);
    }
  }

  private static IMethodElement toMethodElement(org.prorefactor.proparse.AssemblyCatalog.Method method) {
    List<IParameter> params = new ArrayList<>();
    int offset = 0;
    for (org.prorefactor.proparse.AssemblyCatalog.Parameter methd : method.parameters) {
      int extent = methd.dataType.endsWith("[]") ? 1 : 0;
      String dataType = extent == 1 ? methd.dataType.substring(0, methd.dataType.length() - 2) : methd.dataType;
      ParameterMode mode = "IO".equalsIgnoreCase(methd.mode) ? ParameterMode.INPUT_OUTPUT
          : ("O".equalsIgnoreCase(methd.mode) ? ParameterMode.OUTPUT : ParameterMode.INPUT);
      params.add(new Parameter(offset++, methd.name, extent, mode, toDataType(dataType)));
    }

    return new MethodElement(method.name, method.isStatic, toDataType(method.returnType),
        params.toArray(new IParameter[0]));
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
      LOG.trace("No TypeInfo found for {}", clz);
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
      LOG.trace("No TypeInfo found for {}", clz);
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

  public void injectClassDocumentation(ClassDocumentation classDoc) {
    this.classDoc.put(classDoc.getClassName(), classDoc);
  }

  @Override
  public ClassDocumentation getClassDocumentation(String className) {
    return classDoc.get(className);
  }

  /**
   * Inject TypeInfo object into the structure managing build directory
   */
  public void injectTypeInfo(ITypeInfo typeInfo) {
    if ((typeInfo == null) || Strings.isNullOrEmpty(typeInfo.getTypeName()))
      return;
    typeInfoMap.put(typeInfo.getTypeName(), typeInfo);
    lcTypeInfoMap.put(typeInfo.getTypeName().toLowerCase(), typeInfo);

    int dotPos = typeInfo.getTypeName().lastIndexOf('.');
    String pkgName = dotPos >= 1 ? typeInfo.getTypeName().substring(0, dotPos) : "";
    synchronized(pkgLock) { 
      classesPerPkg.computeIfAbsent(pkgName, key -> new ArrayList<>()).add(typeInfo);
    }
  }

  /**
   * Inject TypeInfo object into the structure managing builtin classes and .Net catalog
   */
  public void injectClassInfo(ITypeInfo typeInfo) {
    if ((typeInfo == null) || Strings.isNullOrEmpty(typeInfo.getTypeName()))
      return;

    classInfo.put(typeInfo.getTypeName(), typeInfo);
    lcClassInfo.put(typeInfo.getTypeName().toLowerCase(), typeInfo);

    int dotPos = typeInfo.getTypeName().lastIndexOf('.');
    String pkgName = dotPos >= 1 ? typeInfo.getTypeName().substring(0, dotPos) : "";
    synchronized(pkgLock) { 
      classesPerPkg.computeIfAbsent(pkgName, key -> new ArrayList<>()).add(typeInfo);
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
    Property[] properties;
    MethodInfo[] methods;
    MethodInfo[] staticMethods;
    Property[] staticProperties;
  }
  public static class Property {
    @SerializedName(value = "name")
    public String name;
    @SerializedName(value = "dataType")
    public String dataType;
  }

  private class MethodInfo {
    String name;
    String returnType;

    public IMethodElement toMethodElement(boolean staticM) {
      int leftBracket = name.indexOf('(');
      String methdName = name.substring(0, leftBracket).trim();
      String params = name.substring(leftBracket + 1, name.indexOf(')'));
      String[] prms = params.split(",");
      List<IParameter> pp = new ArrayList<>();
      int offset = 0;
      for (String str : prms) {
        str = str.trim();
        if (!str.isEmpty()) {
          pp.add(toParameter(str, offset++));
        }
      }
      return new MethodElement(methdName, staticM, toDataType(returnType), pp.toArray(new IParameter[0]));
    }

    private IParameter toParameter(String str, int num) {
      if (str.charAt(str.length() - 1) == '&')
        str = str.substring(0, str.length() - 1);
      int spacePos = str.indexOf(' ');
      ParameterMode mode = spacePos == -1 ? ParameterMode.INPUT : ParameterMode.OUTPUT;
      str = str.substring(spacePos + 1);
      int extent = str.endsWith("[]") ? 1 : 0;
      if (extent == 1)
        str = str.substring(0, str.length() - 2);

      return new Parameter(num, "prm" + num, extent, mode, toDataType(str));
    }

  }

  private static DataType toDataType(String str) {
    DataType dt = DataType.get(str);
    return dt.getPrimitive() == PrimitiveDataType.UNKNOWN ? new DataType(str) : dt;
  }

  @Override
  public Collection<ITypeInfo> getAllClassesInPropath() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Collection<ITypeInfo> getAllClassesInSource() {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public Collection<ITypeInfo> getAllClassesInAssemblies() {
    throw new UnsupportedOperationException("Not implemented");
  }
}
