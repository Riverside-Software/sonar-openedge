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
package org.prorefactor.macrolevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.antlr.v4.runtime.misc.IntervalSet;
import org.prorefactor.core.ProToken;

/**
 * Catch preprocessor events in order to generate a macro tree.
 */
public class PreprocessorEventListener implements IPreprocessorEventListener {
  private final IncludeRef root;

  // AppBuilder managed is read-only by default - Keep track of editable code sections
  private boolean appBuilderCode = false;
  private final Map<Integer, IntervalSet> appBuilderSections = new HashMap<>();

  /* Temp stack of scopes, just used during tree creation */
  private Deque<Scope> scopeStack = new LinkedList<>();
  private IncludeRef currInclude;
  /* Temp stack of global defines, just used during tree creation */
  private Map<String, MacroDef> globalDefMap = new HashMap<>();
  private MacroRef currRef;
  /* Temp object for editable section */
  private boolean inEditableSection;
  private int editableSectionFirstLine = -1;
  private List<String> messages = new ArrayList<>();

  public PreprocessorEventListener() {
    root = new IncludeRef(null, 0, 0, 0, 0);

    currRef = root;
    currInclude = root;
    scopeStack.addFirst(new Scope(root));
  }

  public IncludeRef getMacroGraph() {
    return root;
  }

  public List<String> getMessages() {
    return messages;
  }

  @Override
  public void define(int line, int column, String name, String value, MacroDefinitionType type) {
    MacroDef newDef = new MacroDef(currRef, type, line, column, name, value);
    if (type == MacroDefinitionType.GLOBAL) {
      globalDefMap.put(name, newDef);
    } else if (type == MacroDefinitionType.SCOPED) {
      Scope currScope = scopeStack.getFirst();
      currScope.defMap.put(name, newDef);
    }
    currRef.macroEventList.add(newDef);
  }

  @Override
  public void preproElse(int line, int column) {
    // Nothing for now
  }

  @Override
  public void preproElseIf(int line, int column) {
    // Nothing for now
  }

  @Override
  public void preproEndIf(int line, int column) {
    // Nothing for now
  }

  @Override
  public void preproIf(int line, int column, boolean value) {
    // Nothing for now
  }

  @Override
  public void include(int line, int column, int endLine, int endColumn, int currentFile, String incFile) {
    IncludeRef newRef = new IncludeRef(currRef, line, column, endLine, endColumn, currentFile);
    scopeStack.addFirst(new Scope(newRef));
    currRef.macroEventList.add(newRef);
    currInclude = newRef;
    currRef = newRef;
    newRef.setFileRefName(incFile);
  }

  @Override
  public void includeArgument(String argName, String value, boolean undefined) {
    int argNum = 0;
    try {
      argNum = Integer.parseInt(argName);
    } catch (NumberFormatException uncaught) {
    }
    MacroDef newArg;
    if ((argNum == 0) || (argNum != currInclude.numArgs() + 1)) {
      newArg = new MacroDef(currInclude.getParent(), MacroDefinitionType.NAMEDARG, argName);
      currInclude.addNamedArg(newArg);
    } else {
      newArg = new MacroDef(currInclude.getParent(), MacroDefinitionType.NUMBEREDARG);
      currInclude.addNumberedArg(newArg);
    }
    newArg.setValue(value);
    newArg.setUndefined(undefined);
    newArg.setIncludeRef(currInclude);
  }

  @Override
  public void includeEnd() {
    scopeStack.removeFirst();
    currInclude = scopeStack.getFirst().includeRef;
    currRef = currRef.getParent();
  }

  @Override
  public void macroRef(int line, int column, int endLine, int endColumn, String macroName) {
    NamedMacroRef newRef = new NamedMacroRef(findMacroDef(macroName), currRef, line, column, endLine, endColumn);
    currRef.macroEventList.add(newRef);
    currRef = newRef;
  }

  @Override
  public void macroRefEnd() {
    currRef = currRef.getParent();
  }

  @Override
  public void undefine(int line, int column, String name) {
    // Add an object for this macro event.
    MacroDef newDef = new MacroDef(currRef, MacroDefinitionType.UNDEFINE, line, column, name, "");
    currRef.macroEventList.add(newDef);

    // Now process the undefine.
    Scope currScope = scopeStack.getFirst();
    // First look for local SCOPED define
    if (currScope.defMap.containsKey(name)) {
      newDef.setUndefWhat(currScope.defMap.remove(name));
      return;
    }
    // Second look for a named include file argument
    MacroDef tmp = currInclude.undefine(name);
    if (tmp != null) {
      newDef.setUndefWhat(tmp);
      return;
    }
    // Third look for a non-local SCOPED define
    Iterator<Scope> it = scopeStack.iterator();
    it.next(); // skip the current scope - already checked.
    while (it.hasNext()) {
      currScope = it.next();
      if (currScope.defMap.containsKey(name)) {
        newDef.setUndefWhat(currScope.defMap.remove(name));
        return;
      }
    }
    // Fourth look for a GLOBAL define
    newDef.setUndefWhat(globalDefMap.remove(name.toLowerCase(Locale.ENGLISH)));
  }

  public void analyzeSuspend(String str, int line) {
    appBuilderCode = true;
    if ((currInclude.getFileIndex() == 0) && ProToken.isTokenEditableInAB(str)) {
      inEditableSection = true;
      editableSectionFirstLine = line;
    }
  }

  @Override
  public void analyzeResume(int line) {
    if (inEditableSection && (currInclude.getFileIndex() == 0)) {
      appBuilderSections.computeIfAbsent(0, it -> new IntervalSet()).add(editableSectionFirstLine, line);
    }
    inEditableSection = false;
    editableSectionFirstLine = -1;
  }

  @Override
  public void message(String str) {
    messages.add(str);
  }

  public boolean isAppBuilderCode() {
    return appBuilderCode;
  }

  /**
   * Find a MacroDef by name. NOTE: {*} and other such built-in macro reference are not yet implemented.
   */
  private MacroDef findMacroDef(String name) {
    MacroDef ret;
    Scope currScope = scopeStack.getFirst();
    // First look for local SCOPED define
    ret = currScope.defMap.get(name);
    if (ret != null)
      return ret;
    // Second look for a named include file argument
    ret = currInclude.lookupNamedArg(name);
    if (ret != null)
      return ret;
    // Third look for a non-local SCOPED define
    Iterator<Scope> it = scopeStack.iterator();
    it.next(); // skip the current scope - already checked.
    while (it.hasNext()) {
      currScope = it.next();
      ret = currScope.defMap.get(name);
      if (ret != null)
        return ret;
    }
    // Fourth look for a GLOBAL define
    ret = globalDefMap.get(name);
    return ret;
  }

  public Map<Integer, IntervalSet> getEditableCodeSections() {
    return Collections.unmodifiableMap(appBuilderSections);
  }

  // These scopes are temporary, just used during tree creation
  private static class Scope {
    Map<String, MacroDef> defMap = new HashMap<>();
    IncludeRef includeRef;

    public Scope(IncludeRef ref) {
      this.includeRef = ref;
    }
  }

}
