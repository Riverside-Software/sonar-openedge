/********************************************************************************
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
package org.prorefactor.proparse.support;

import java.io.File;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import org.prorefactor.core.schema.ISchema;
import org.prorefactor.proparse.classdoc.ClassDocumentation;
import org.prorefactor.refactor.settings.IProparseSettings;

import eu.rssw.pct.elements.ITypeInfo;

/**
 * Required environment for parser: propath, rcode information, database schema, ...
 */
public interface IProparseEnvironment {

  /**
   * Inject description of PL files or external assemblies from a JSON reader
   */
  void injectClassesFromCatalog(Reader reader);

  /**
   * File encoding 
   */
  Charset getCharset();

  /**
   * Database schema
   */
  ISchema getSchema();

  /**
   * List of Proparse Settings
   */
  IProparseSettings getProparseSettings();

  /**
   * Return TypeInfo object from a class name (case sensitive search)
   */
  ITypeInfo getTypeInfo(String clz);

  /**
   * Return TypeInfo object from a class name (case insensitive search)
   */
  ITypeInfo getTypeInfoCI(String clz);

  /**
   * Return all class names from a given package name
   */
  List<String> getAllClassesFromPackage(String pkgName);

  /**
   * Add a given TypeInfo object 
   */
  void injectTypeInfo(ITypeInfo unit);

  /**
   * First version of findFile :-)
   */
  String findFile(String fileName);

  /**
   * Third version of findFile :-)
   */
  File findFile3(String fileName);

  /**
   * Return all classes defined in propath (as well as standard Progress classes)
   */
  Collection<ITypeInfo> getAllClassesInPropath();

  /**
   * Return all known classes in source directories
   */
  Collection<ITypeInfo> getAllClassesInSource();

  /**
   * Return ClassDocumentation for given class name. Null if not found
   */
  default ClassDocumentation getClassDocumentation(String className) {
    return null;
  }
}
