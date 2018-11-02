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

import org.prorefactor.core.ProToken;

/**
 * Catch preprocessor events in order to generate a macro tree.
 */
public class PreprocessorEventListener implements IPreprocessorEventListener {
  private final IncludeRef root;

  // AppBuilder managed is read-only by default - Keep track of editable code sections
  private boolean appBuilderCode = false;
  private final List<EditableCodeSection> sections = new ArrayList<>();

  /* Temp stack of scopes, just used during tree creation */
  private Deque<Scope> scopeStack = new LinkedList<>();
  private IncludeRef currInclude;
  /* Temp stack of global defines, just used during tree creation */
  private Map<String, MacroDef> globalDefMap = new HashMap<>();
  private MacroRef currRef;
  /* Temp object for editable section */
  private EditableCodeSection currSection;

  public PreprocessorEventListener() {
    root = new IncludeRef(null, 0, 0);

    currRef = root;
    currInclude = root;
    scopeStack.addFirst(new Scope(root));
  }

  public IncludeRef getMacroGraph() {
    return root;
  }

  @Override
  public void define(int line, int column, String name, String value, int type) {
    MacroDef newDef = new MacroDef(currRef, type, line, column, name, value);
    if (type == MacroDef.GLOBAL)
      globalDefMap.put(name, newDef);
    if (type == MacroDef.SCOPED) {
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
  public void include(int line, int column, int currentFile, String incFile) {
    IncludeRef newRef = new IncludeRef(currRef, line, column, currentFile);
    scopeStack.addFirst(new Scope(newRef));
    currRef.macroEventList.add(newRef);
    currInclude = newRef;
    currRef = newRef;
    newRef.setFileRefName(incFile);
  }

  @Override
  public void includeArgument(String argName, String value) {
    int argNum = 0;
    try {
      argNum = Integer.parseInt(argName);
    } catch (NumberFormatException uncaught) {
    }
    MacroDef newArg;
    if ((argNum == 0) || (argNum != currInclude.numArgs() + 1)) {
      newArg = new MacroDef(currInclude.getParent(), MacroDef.NAMEDARG);
      newArg.setName(argName);
      currInclude.addNamedArg(newArg);
    } else {
      newArg = new MacroDef(currInclude.getParent(), MacroDef.NUMBEREDARG);
      currInclude.addNumberedArg(newArg);
    }
    newArg.setValue(value);
    newArg.setIncludeRef(currInclude);
  }

  @Override
  public void includeEnd() {
    scopeStack.removeFirst();
    currInclude = scopeStack.getFirst().includeRef;
    currRef = currRef.getParent();
  }

  @Override
  public void macroRef(int line, int column, String macroName) {
    NamedMacroRef newRef = new NamedMacroRef(findMacroDef(macroName), currRef, line, column);
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
    MacroDef newDef = new MacroDef(currRef, MacroDef.UNDEFINE, line, column, name, "");
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
      currSection = new EditableCodeSection();
      currSection.fileNum = currInclude.getFileIndex();
      currSection.startLine = line;
    }
  }

  @Override
  public void analyzeResume(int line) {
    if ((currSection != null) && (currInclude.getFileIndex() == currSection.fileNum)) {
      currSection.endLine = line;
      sections.add(currSection);
    }
    currSection = null;
  }

  public boolean isAppBuilderCode() {
    return appBuilderCode;
  }

  public boolean isLineInEditableSection(int file, int line) {
    for (EditableCodeSection range : sections) {
      if ((range.fileNum == file) && (range.startLine <= line) && (range.endLine >= line))
      return true;
    }
    return false;
  }

  /**
   * Find a MacroDef by name. NOTE: I have not yet implemented {*} and other such built-in macro reference tricks. Not
   * sure how soon I'll need those. There's a good chance that this function will return null.
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

  public List<EditableCodeSection> getEditableCodeSections() {
    return Collections.unmodifiableList(sections);
  }

  // These scopes are temporary, just used during tree creation
  private static class Scope {
    Map<String, MacroDef> defMap = new HashMap<>();
    IncludeRef includeRef;

    public Scope(IncludeRef ref) {
      this.includeRef = ref;
    }
  }

  public static class EditableCodeSection {
    private int fileNum;
    private int startLine;
    private int endLine;

    /**
     * @return Always 0 for now
     */
    public int getFileNum() {
      return fileNum;
    }

    /**
     * @return Starting line number of editable code sectin
     */
    public int getStartLine() {
      return startLine;
    }

    /**
     * @return Ending line number of editable code sectin
     */
    public int getEndLine() {
      return endLine;
    }
  }
}
