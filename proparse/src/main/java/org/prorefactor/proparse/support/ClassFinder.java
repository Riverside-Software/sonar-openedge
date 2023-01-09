/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2023 Riverside Software
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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassFinder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassFinder.class);

  private IProparseEnvironment session;
  private Map<String, String> namesMap = new HashMap<>();

  public ClassFinder(IProparseEnvironment session) {
    this.session = session;
  }

  /**
   * Add a USING class name or path glob to the class finder.
   */
  void addPath(String nodeText) {
    LOGGER.trace("Entering addPath {}", nodeText);
    String dequoted = dequote(nodeText);
    if (dequoted.length() == 0)
      return;
    if (dequoted.endsWith("*")) {
      String pkgName = dequoted.substring(0, dequoted.length() - 2);
      for (String str : session.getAllClassesFromPackage(pkgName)) {
        addQualifiedName(str);
      }
    } else {
      addQualifiedName(dequoted);
    }
  }

  private void addQualifiedName(String qName) {
    int dotPos = qName.lastIndexOf('.');
    String unqualified = dotPos > 0 ? qName.substring(dotPos + 1) : qName;
    unqualified = unqualified.toLowerCase();
    // First match takes precedence.
    namesMap.putIfAbsent(unqualified, qName);
  }

  /**
   * Returns a string with quotes and string attributes removed. Can't just use StringFuncs.qstringStrip because a class
   * name might have embedded quoted text, to quote embedded spaces and such in the file name. The embedded quotation
   * marks have to be stripped too.
   */
  static String dequote(String s1) {
    StringBuilder s2 = new StringBuilder();
    int len = s1.length();
    char[] c1 = s1.toCharArray();
    int numQuotes = 0;
    for (int i = 0; i < len; ++i) {
      char c = c1[i];
      if (c == '"' || c == '\'') {
        // If we have a colon after a quote, assume we have string
        // attributes at the end of a quoted class name, and we're done.
        if (++numQuotes > 1 && i + 1 < len && c1[i + 1] == ':')
          break;
      } else {
        s2.append(c);
      }
    }
    return s2.toString();
  }

  /**
   * Find a class file for a *qualified* class name.
   */
  String findClassFile(String qualClassName) {
    String slashName = qualClassName.replace('.', '/');
    return session.findFile(slashName + ".cls");
  }

  /**
   * Lookup a qualified class name on the USING list and/or PROPATH:<ul>
   * <li>If input name is already qualified, just returns that name dequoted</li>
   * <li>Checks for explicit USING</li>
   * <li>Checks for USING globs on PROPATH</li>
   * <li>Checks for "no package" class file on PROPATH</li>
   * <li>Returns empty String if all of the above fail</li>
   * </ul>
   */
  String lookup(String rawRefName) {
    LOGGER.trace("Entering lookup {}", rawRefName);
    String dequotedName = dequote(rawRefName);

    // If already qualified, then return the dequoted name, no check against USING.
    if (dequotedName.contains("."))
      return dequotedName;

    // Check if USING class name, or if the class file has already been found.
    String ret = namesMap.get(dequotedName.toLowerCase());
    if (ret != null)
      return ret;

    // The last chance is for a "no package" name in RefactorSession
    if (session.getTypeInfo(dequotedName) != null)
      return dequotedName;

    // No class source was found, return empty String.
    return "";
  }

}
