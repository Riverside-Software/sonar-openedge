/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
package org.prorefactor.proparse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.prorefactor.refactor.RefactorSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassFinder {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassFinder.class);

  private RefactorSession session;
  private List<String> paths = new ArrayList<>();
  private Map<String, String> namesMap = new HashMap<>();

  public ClassFinder(RefactorSession session) {
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
      paths.add(dequoted.replace('.', '/').substring(0, dequoted.length() - 1));
    } else {
      int dotPos = dequoted.lastIndexOf('.');
      String unqualified = dotPos > 0 ? dequoted.substring(dotPos + 1) : dequoted;
      unqualified = unqualified.toLowerCase();
      // First match takes precedence.
      if (!namesMap.containsKey(unqualified))
        namesMap.put(unqualified, dequoted);
    }
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
   * Lookup a qualified class name on the USING list and/or PROPATH. - If input name is already qualified, just returns
   * that name dequoted. - Checks for explicit USING. - Checks for USING globs on PROPATH. - Checks for "no package"
   * class file on PROPATH. - Returns empty String if all of the above fail.
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

    // Check USING package globs and files on the PROPATH.
    String withExtension = dequotedName + ".cls";
    for (String path : paths) {
      String classFile = session.findFile(path + withExtension);
      if (classFile.length() != 0) {
        ret = path.replace('/', '.') + dequotedName;
        namesMap.put(dequotedName.toLowerCase(), ret);
        return ret;
      }
    }

    // The last chance is for a "no package" name on the path.
    if (session.findFile(dequotedName + ".cls").length() > 0) {
      namesMap.put(dequotedName.toLowerCase(), dequotedName);
      return dequotedName;
    }

    // No class source was found, return empty String.
    return "";
  }

  // TEMP-ANTLR4
  public int compareTo(ClassFinder other) {
    if (!String.join(",", paths).equals(String.join(",", other.paths))) {
      System.err.println("ClassFinder paths: " + String.join(",", paths) + " *** " + String.join(",", other.paths));
      return 1;
    }

    return 0;
  }

}
