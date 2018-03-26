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
package org.prorefactor.proparse.antlr4;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <ul>
 * <li>Every time the lexer has to scan an include file, we create an IncludeFile object, for managing include file arguments and preprocessor scopes.</li>
 * <li>We keep an InputSource object, which has an input stream.</li>
 * <li>Each IncludeFile object will have one or more InputSource objects.</li>
 * <li>The bottom InputSource object for an IncludeFile is the input for the include file itself.</li>
 * </ul>
 */
public class IncludeFile {
  private static final Logger LOGGER = LoggerFactory.getLogger(IncludeFile.class);

  private final Map<String, String> defdNames = new HashMap<>();
  private final Deque<InputSource> inputVector = new LinkedList<>();

  private final List<String> numberedArgs = new ArrayList<>();
  private final Map<String, String> namedArgs = new HashMap<>();
  private final List<NamedArgument> namedArgsIn = new ArrayList<>();

  public IncludeFile(String referencedWithName, InputSource is) {
    LOGGER.trace("New IncludeFile object for '{}'", referencedWithName);

    inputVector.add(is);
    // {0} must return the name that this include file was referenced with.
    numberedArgs.add(referencedWithName);
  }

  public void addInputSource(InputSource source) {
    inputVector.add(source);
  }

  public InputSource pop() {
    if (inputVector.size() > 1) {
      inputVector.removeLast();
      return inputVector.getLast();
    } else {
      return null;
    }
  }

  public InputSource getLastSource() {
    return inputVector.getLast();
  }

  public void addArgument(String arg) {
    numberedArgs.add(arg);
  }

  public void addNamedArgument(String name, String arg) {
    namedArgsIn.add(new NamedArgument(name, arg));
    String lname = name.toLowerCase();
    // The first one defined is the one that gets used
    if (!namedArgs.containsKey(lname))
      namedArgs.put(lname, arg);
    // Named include arguments can also be referenced by number.
    numberedArgs.add(arg);
  }

  public String getAllNamedArgs() {
    StringBuilder out = new StringBuilder();
    for (NamedArgument arg : namedArgsIn) {
      if (out.length() > 0) {
        out.append(' ');
      }
      out.append('&').append(arg.name).append("=\"").append(arg.arg).append("\"");
    }
    return out.toString();
  }

  /**
   * Returns space-separated list of all include arguments
   */
  public String getAllArguments() {
    if (numberedArgs.size() <= 1)
      return "";

    StringBuilder sb = new StringBuilder();
    // Note: starts from 1. Doesn't include arg[0], which is the filename.
    for (String str : numberedArgs.subList(1, numberedArgs.size())) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(str);
    }
    return sb.toString();
  }


  /**
   * Get value of numbered argument (i.e. {&amp;2})
   * 
   * @param num Arg number
   * @return String value
   */
  public String getNumberedArgument(int num) {
    if (num >= numberedArgs.size()) {
      return "";
    } else {
      return numberedArgs.get(num);
    }
  }

  /**
   * Get value of named argument (i.e. {&amp;FOO}).
   * 
   * @param name Arg name. If blank, returns first blank (undefined) named arg.
   * @return null if not found.
   */
  public String getNamedArg(String name) {
    // If name is blank, return the first blank (undefined) named argument (if any).
    if (name.length() == 0) {
      for (NamedArgument nargin : namedArgsIn) {
        if (nargin.name.length() == 0)
          return nargin.arg;
      }
      return null;
    }
    return namedArgs.get(name.toLowerCase());
  }

  /**
   * Returns value of scope-defined variable
   */
  public String getValue(String name) {
    return defdNames.get(name);
  }

  public boolean isNameDefined(String name) {
    return defdNames.containsKey(name);
  }

  public void scopeDefine(String name, String value) {
    defdNames.put(name, value);
  }

  public void removeVariable(String name) {
    defdNames.remove(name);
  }

  boolean undefNamedArg(String name) {
    String lname = name.toLowerCase();
    // Find the first one and clobber it
    boolean found = false;
    for (NamedArgument nargin : namedArgsIn) {
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
    for (NamedArgument nargin : namedArgsIn) {
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

  private static final class NamedArgument {
    private String name;
    private String arg;

    NamedArgument(String name, String arg) {
      this.name = name;
      this.arg = arg;
    }
  }

}
