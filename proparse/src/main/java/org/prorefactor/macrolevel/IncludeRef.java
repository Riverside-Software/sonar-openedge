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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncludeRef extends MacroRef {
  public boolean usesNamedArgs;
  public int fileIndex;
  private List<MacroDef> includeArgs = new ArrayList<>();
  private Map<String, MacroDef> argMap = new HashMap<>();
  private String fileRefName = "";

  public IncludeRef(MacroRef parent, int line, int column) {
    super(parent, line, column);
  }

  public void addNamedArg(MacroDef arg) {
    includeArgs.add(arg);
    argMap.put(arg.name.toLowerCase(), arg);
  }

  public void addNumberedArg(MacroDef arg) {
    includeArgs.add(arg);
  }

  /**
   * Count from 1, the way that the arguments are referenced in ABL.
   */
  public MacroDef getArgNumber(int num) {
    if (num > 0 && num <= includeArgs.size())
      return includeArgs.get(num - 1);
    return null;
  }

  @Override
  public int getFileIndex() {
    return fileIndex;
  }

  /**
   * Get the string that was used for referencing the include file name. For example, if the code was {includeMe.i},
   * then the string "includeMe.i" is returned. Note: For Proparse versions earlier than 3.1C, this will return and
   * empty string.
   */
  public String getFileRefName() {
    return fileRefName;
  }

  public MacroDef lookupNamedArg(String name) {
    if (!usesNamedArgs)
      return null;
    return argMap.get(name.toLowerCase());
  }

  public int numArgs() {
    return includeArgs.size();
  }

  public void setFileRefName(String fileRefName) {
    this.fileRefName = fileRefName;
  }

  public MacroDef undefine(String name) {
    MacroDef theArg = argMap.get(name);
    if (theArg != null) {
      argMap.remove(name);
      argMap.put("", theArg);
      return theArg;
    }
    return null;
  }

}
