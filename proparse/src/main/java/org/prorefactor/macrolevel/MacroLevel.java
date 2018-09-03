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

/**
 * Static functions for working with an existing macro tree.
 */
public class MacroLevel {

  private MacroLevel() {

  }

  /**
   * Trace back nested macro definitions until we find the original source.
   * 
   * @return int[3] - file, line, column.
   */
  public static int[] getDefinitionPosition(MacroDef def) {
    int[] ret = new int[3];
    if (def.includeRef == null) {
      if (def.getParent() instanceof NamedMacroRef) {
        return getDefinitionPosition(((NamedMacroRef) def.getParent()).getMacroDef());
      }
      ret[0] = ((IncludeRef) def.getParent()).getFileIndex();
      ret[1] = def.getPosition().getLine();
      ret[2] = def.getPosition().getColumn();
    } else {
      // Include arguments don't get their file/line/col stored, so
      // we have to find the include reference source.
      if (!(def.includeRef.getParent() instanceof IncludeRef))
        return getDefinitionPosition(((NamedMacroRef) def.includeRef.getParent()).getMacroDef());
      ret[0] = ((IncludeRef) def.includeRef.getParent()).getFileIndex();
      ret[1] = def.includeRef.getLine();
      ret[2] = def.includeRef.getColumn();
    }
    return ret;
  }

  /**
   * Build and return an array of the MacroRef objects, which would map to the SOURCENUM attribute from JPNode. Built
   * simply by walking the tree and adding every MacroRef to the array.
   */
  public static MacroRef[] sourceArray(MacroRef top) {
    ArrayList<MacroRef> list = new ArrayList<>();
    sourceArray2(top, list);
    MacroRef[] ret = new MacroRef[list.size()];
    return list.toArray(ret);
  }

  private static void sourceArray2(MacroRef macroNode, ArrayList<MacroRef> list) {
    list.add(macroNode);
    for (MacroEvent event : macroNode.macroEventList) {
      if (event instanceof MacroRef) {
        sourceArray2((MacroRef) event, list);
      }
    }
  }

}
