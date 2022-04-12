/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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
