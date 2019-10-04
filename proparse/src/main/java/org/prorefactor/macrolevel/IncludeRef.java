/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2019 Riverside Software
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IncludeRef extends MacroRef {
  private List<MacroDef> includeArgs = new ArrayList<>();
  private Map<String, MacroDef> argMap = new HashMap<>();
  private String fileRefName = "";
  private int fileIndex;
  private boolean usesNamedArgs;

  public IncludeRef(MacroRef parent, int line, int column) {
    super(parent, line, column);
  }

  public IncludeRef(MacroRef parent, int line, int column, int fileIndex) {
    super(parent, line, column);
    this.fileIndex = fileIndex;
  }

  public void addNamedArg(MacroDef arg) {
    usesNamedArgs = true;
    includeArgs.add(arg);
    argMap.put(arg.getName().toLowerCase(), arg);
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
   * then the string "includeMe.i" is returned.
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

  @Override
  public String toString() {
    return "Include file at line " + getLine();
  }

  public void printMacroEvents(PrintStream stream) {
    stream.println("Include #" + fileIndex + " - " + fileRefName);
    for (MacroEvent event : macroEventList) {
      stream.println("  " + event.toString());
      if (event instanceof IncludeRef) {
        ((IncludeRef) event).printMacroEvents(stream);
      }
    }
  }
}
