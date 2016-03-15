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
package org.prorefactor.proparse;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A preprocessor contains one or more IncludeFiles.
 * 
 * There is a special IncludeFile object created for the top-level program (ex: .p or .w).
 * 
 * Every time the lexer has to scan an include file, we create an IncludeFile object, for managing include file
 * arguments and pre-processor scopes.
 * 
 * We keep an InputSource object, which has an input stream.
 * 
 * Each IncludeFile object will have one or more InputSource objects.
 * 
 * The bottom InputSource object for an IncludeFile is the input for the include file itself.
 */
public class IncludeFile {
  final Map<String, String> defdNames = new HashMap<>();
  final LinkedList<InputSource> inputVector = new LinkedList<>();
  final List<String> numdArgs = new ArrayList<>();
  final Map<String, String> namedArgs = new HashMap<>();
  final List<NamedArgIn> namedArgsIn = new ArrayList<>();

  IncludeFile(String referencedWithName, InputSource is) {
    inputVector.add(is);
    // {0} must return the name that this include file was referenced with.
    numdArgs.add(referencedWithName);
  }

  void defNamedArg(String name, String arg) {
    namedArgsIn.add(new NamedArgIn(name, arg));
    String lname = name.toLowerCase();
    // The first one defined is the one that gets used
    if (!namedArgs.containsKey(lname))
      namedArgs.put(lname, arg);
    // Named include arguments can also be referenced by number.
    numdArgs.add(arg);
  }

  String getAllNamedArgs() {
    String allArgs = "";
    String currArg;
    for (NamedArgIn aNamedArgsIn : namedArgsIn) {
      if (allArgs.length() > 0)
        allArgs += " ";
      currArg = aNamedArgsIn.name;
      allArgs += "&" + currArg + "=\"";
      allArgs += aNamedArgsIn.arg + "\"";
    }
    return allArgs;
  }

  /**
   * Get a named arg.
   * 
   * @param name Arg name. If blank, returns first blank (undefined) named arg.
   * @return null if not found.
   */
  String getNamedArg(String name) {
    // If name is blank, return the first blank (undefined) named argument (if any).
    if (name.length() == 0) {
      for (NamedArgIn nargin : namedArgsIn) {
        if (nargin.name.length() == 0)
          return nargin.arg;
      }
      return null;
    }
    return namedArgs.get(name.toLowerCase());
  }

  boolean undefNamedArg(String name) {
    String lname = name.toLowerCase();
    // Find the first one and clobber it
    boolean found = false;
    for (NamedArgIn nargin : namedArgsIn) {
      if (nargin.name.equalsIgnoreCase(name)) {
        // Erase the argument name, which seems to be what Progress does.
        nargin.name = "";
        found = true;
        break;
      }
    }
    if (!found)
      return false;
    // Now see if that named argument got assigned more than once
    found = false;
    for (NamedArgIn nargin : namedArgsIn) {
      if (nargin.name.equalsIgnoreCase(name)) {
        namedArgs.put(lname, nargin.arg);
        found = true;
        break;
      }
    }
    if (!found)
      namedArgs.remove(lname);
    return true;
  }

  private static final class NamedArgIn {
    private String name;
    private String arg;

    NamedArgIn(String name, String arg) {
      this.name = name;
      this.arg = arg;
    }
  }

}
