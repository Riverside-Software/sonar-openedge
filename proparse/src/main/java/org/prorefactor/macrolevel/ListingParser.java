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
package org.prorefactor.macrolevel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.prorefactor.refactor.RefactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For parsing Proparse's "preprocessor listing" file. Generates a "macro tree". The macro tree's root is an IncludeRef
 * object. The root IncludeRef represents the main.p source file.
 */
public class ListingParser {
  private final static Logger LOGGER = LoggerFactory.getLogger(ListingParser.class);

  /** Map of fileIndex (Integer) to fileName (String) */
  public Map<Integer, String> fileIndexes = new HashMap<Integer, String>();

  private IncludeRef root = null;

  /* Temp stack of scopes, just used during tree creation */
  private int column;
  private int line;
  private int listingFileLine = 0;
  private LinkedList<Scope> scopeStack = new LinkedList<Scope>();
  private IncludeRef currInclude;
  /* Temp stack of global defines, just used during tree creation */
  private Map<String, MacroDef> globalDefMap = new HashMap<String, MacroDef>();
  private MacroRef currRef;
  private String listingFile;

  public ListingParser(String listingFile) {
    this.listingFile = listingFile;
  }

  public void parse() throws IOException, RefactorException {
    LOGGER.trace("Entering ListingParser#parse()");
    try (BufferedReader reader = new BufferedReader(new FileReader(listingFile))) {
      createRootNode();
      while (true) {
        String currLine = reader.readLine();
        if (currLine == null)
          break;
        listingFileLine++;
        String[] parts = currLine.split("\\s", 6);
        String token = parts[3].intern();
        if ("globdef".equals(token)) {
          globdef(parts);
          continue;
        }
        if ("scopdef".equals(token)) {
          scopdef(parts);
          continue;
        }
        if ("macroref".equals(token)) {
          macroref(parts);
          continue;
        }
        if ("macrorefend".equals(token)) {
          macrorefend(parts);
          continue;
        }
        if ("undef".equals(token)) {
          undef(parts);
          continue;
        }
        if ("include".equals(token)) {
          include(parts);
          continue;
        }
        if ("incarg".equals(token)) {
          incarg(parts);
          continue;
        }
        if ("incend".equals(token)) {
          incend(parts);
          continue;
        }
        if ("ampif".equals(token)) {
          ampif(parts);
          continue;
        }
        if ("ampelseif".equals(token)) {
          ampelseif(parts);
          continue;
        }
        if ("ampelse".equals(token)) {
          ampelse(parts);
          continue;
        }
        if ("ampendif".equals(token)) {
          ampendif(parts);
          continue;
        }
        if ("fileindex".equals(token)) {
          fileindex(parts);
          continue;
        }
        // We might just be at an empty line at the end of the file.
        if (currLine.trim().length() == 0)
          continue;
        throw new RefactorException("Invalid token in Proparse listing file." + " Token: " + token + " Line: "
            + (new Integer(listingFileLine)).toString());
      }
    }
    LOGGER.trace("Exiting ListingParser#parse()");
  }

  /**
   * Get the macro tree's root - an IncludeRef object which represents the main.p source file.
   */
  public IncludeRef getRoot() {
    return root;
  }

  /**
   * Get an array of the MacroRef objects, which would map to the SOURCENUM attribute from JPNode.
   * 
   * @see org.prorefactor.macrolevel.MacroLevel#sourceArray(MacroRef)
   */
  public MacroRef[] sourceArray() {
    return MacroLevel.sourceArray(root);
  }

  /** Global or scoped define */
  private void ampdef(String[] parts, int type) {
    MacroDef newDef = new MacroDef();
    getPosition(parts);
    newDef.parent = currRef;
    newDef.line = line;
    newDef.column = column;
    newDef.name = parts[4];
    newDef.value = replaceEscapes(parts[5]);
    newDef.type = type;
    if (type == MacroDef.GLOBAL)
      globalDefMap.put(newDef.name, newDef);
    if (type == MacroDef.SCOPED) {
      Scope currScope = scopeStack.getFirst();
      currScope.defMap.put(newDef.name, newDef);
    }
    currRef.macroEventList.add(newDef);
  }

  private void ampelse(String[] parts) {
    return;
  }

  private void ampelseif(String[] parts) {
    return;
  }

  private void ampendif(String[] parts) {
    return;
  }

  private void ampif(String[] parts) {
    return;
  }

  private void createRootNode() {
    root = new IncludeRef(listingFileLine);
    currRef = root;
    currInclude = root;
    scopeStack.addFirst(new Scope(root));
  }

  private void fileindex(String[] parts) {
    fileIndexes.put(new Integer(Integer.parseInt(parts[4])), parts[5]);
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

  private void getPosition(String[] parts) {
    line = Integer.parseInt(parts[1]);
    column = Integer.parseInt(parts[2]);
  }

  private void globdef(String[] parts) {
    ampdef(parts, MacroDef.GLOBAL);
  }

  private void incarg(String[] parts) {
    MacroDef newArg = new MacroDef();
    newArg.value = replaceEscapes(parts[5]);
    newArg.includeRef = currInclude;
    newArg.parent = currInclude.parent;
    int argNum = 0;
    try {
      argNum = Integer.parseInt(parts[4]);
    } catch (NumberFormatException e) {
    }
    if (argNum == 0 || argNum != currInclude.numArgs() + 1) {
      newArg.name = parts[4];
      currInclude.usesNamedArgs = true;
      newArg.type = MacroDef.NAMEDARG;
      currInclude.addNamedArg(newArg);
    } else {
      newArg.type = MacroDef.NUMBEREDARG;
      currInclude.addNumberedArg(newArg);
    }
  }

  private void incend(String[] parts) {
    scopeStack.removeFirst();
    currInclude = scopeStack.getFirst().includeRef;
    currRef = currRef.parent;
  }

  private void include(String[] parts) {
    IncludeRef newRef = new IncludeRef(listingFileLine);
    scopeStack.addFirst(new Scope(newRef));
    currRef.macroEventList.add(newRef);
    newRef.parent = currRef; // not necessarily an include file!
    currInclude = newRef;
    currRef = newRef;
    getPosition(parts);
    newRef.refLine = line;
    newRef.refColumn = column;
    newRef.fileIndex = Integer.parseInt(parts[4]);
    // Backwards compatability note: The filename reference string
    // was not added to Proparse's preprocessor listing output until
    // version 3.1C.
    if (parts.length > 5)
      newRef.setFileRefName(parts[5]);
  }

  private void macroref(String[] parts) {
    NamedMacroRef newRef = new NamedMacroRef(listingFileLine);
    currRef.macroEventList.add(newRef);
    newRef.parent = currRef;
    currRef = newRef;
    getPosition(parts);
    newRef.refLine = line;
    newRef.refColumn = column;
    newRef.macroDef = findMacroDef(parts[4]);
  }

  private void macrorefend(String[] parts) {
    currRef = currRef.parent;
  }

  /**
   * Proparse's preprocess listing replaces '\n' with "\\n", '\r' with "\\r", and '\\' with "\\\\". This function gets
   * the string back to its original form.
   */
  private String replaceEscapes(String s) {
    StringBuilder r = new StringBuilder("");
    int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      if (c != '\\' || i == len - 1) {
        r.append(c);
        continue;
      }
      char c2 = s.charAt(i + 1);
      switch (c2) {
        case '\\':
          r.append('\\');
          i++;
          break;
        case 'n':
          r.append('\n');
          i++;
          break;
        case 'r':
          r.append('\r');
          i++;
          break;
        default:
          r.append(c);
      }
    }
    return r.toString();
  }

  private void scopdef(String[] parts) {
    ampdef(parts, MacroDef.SCOPED);
  }

  private void undef(String[] parts) {
    // Add an object for this macro event.
    String name = parts[4];
    MacroDef newDef = new MacroDef();
    currRef.macroEventList.add(newDef);
    getPosition(parts);
    newDef.parent = currRef;
    newDef.line = line;
    newDef.column = column;
    newDef.name = name;
    newDef.type = MacroDef.UNDEFINE;

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
    if (globalDefMap.containsKey(name))
      newDef.undefWhat = globalDefMap.remove(name);
  }

  // These scopes are temporary, just used during tree creation
  private static class Scope {
    Map<String, MacroDef> defMap = new HashMap<String, MacroDef>();
    IncludeRef includeRef;

    public Scope(IncludeRef ref) {
      this.includeRef = ref;
    }
  }

}
