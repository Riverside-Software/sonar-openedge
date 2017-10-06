/*******************************************************************************
 * Copyright (c) 2003-2016 John Green
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Not going through listingfile.txt anymore
 *******************************************************************************/ 
package org.prorefactor.macrolevel;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

import org.prorefactor.core.ProToken;

/**
 * For parsing Proparse's "preprocessor listing" file. Generates a "macro tree". The macro tree's root is an IncludeRef
 * object. The root IncludeRef represents the main.p source file.
 */
public class ListingParser implements ListingListener {
  /** Map of fileIndex (Integer) to fileName (String) */
  private final Map<Integer, String> fileIndexes = new HashMap<>();

  private boolean appBuilderCode = false;
  private List<EditableTextRange> sections = new ArrayList<>();
  private EditableTextRange currSection;

  private IncludeRef root = null;

  /* Temp stack of scopes, just used during tree creation */
  private Deque<Scope> scopeStack = new LinkedList<>();
  private IncludeRef currInclude;
  /* Temp stack of global defines, just used during tree creation */
  private Map<String, MacroDef> globalDefMap = new HashMap<>();
  private MacroRef currRef;

  public ListingParser() {
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
    newArg.includeRef = currInclude;
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
  public void fileIndex(int num, String name) {
    fileIndexes.put(num, name);
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
      newDef.undefWhat = currScope.defMap.remove(name);
      return;
    }
    // Second look for a named include file argument
    newDef.undefWhat = currInclude.undefine(name);
    if (newDef.undefWhat != null)
      return;
    // Third look for a non-local SCOPED define
    Iterator<Scope> it = scopeStack.iterator();
    it.next(); // skip the current scope - already checked.
    while (it.hasNext()) {
      currScope = it.next();
      if (currScope.defMap.containsKey(name)) {
        newDef.undefWhat = currScope.defMap.remove(name);
        return;
      }
    }
    // Fourth look for a GLOBAL define
    newDef.undefWhat = globalDefMap.remove(name.toLowerCase(Locale.ENGLISH));
  }

  public void analyzeSuspend(String str, int line) {
    appBuilderCode = true;
    if (ProToken.isEditableInAB(str)) {
      currSection = new EditableTextRange();
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
    for (EditableTextRange range : sections) {
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

  public List<EditableTextRange> getSections() {
    return sections;
  }

  // These scopes are temporary, just used during tree creation
  private static class Scope {
    Map<String, MacroDef> defMap = new HashMap<>();
    IncludeRef includeRef;

    public Scope(IncludeRef ref) {
      this.includeRef = ref;
    }
  }

  public static class EditableTextRange {
    private int fileNum;
    private int startLine;
    private int endLine;
    
    public int getFileNum() {
      return fileNum;
    }
    public int getStartLine() {
      return startLine;
    }
    public int getEndLine() {
      return endLine;
    }
  }
}
