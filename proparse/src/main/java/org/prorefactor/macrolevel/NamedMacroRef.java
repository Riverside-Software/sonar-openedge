/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2023 Riverside Software
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

/**
 * A reference to a macro argument, i.e. {1} or {&amp;name}. Origin might be an include argument or an &amp;DEFINE.
 */
public class NamedMacroRef extends MacroRef {
  private final MacroDef macroDef;

  public NamedMacroRef(MacroDef macro, MacroRef parent, int line, int column, int endLine, int endColumn) {
    super(parent, line, column, endLine, endColumn);
    this.macroDef = macro;
  }

  public MacroDef getMacroDef() {
    return macroDef;
  }

  @Override
  public int getFileIndex() {
    return getParent().getFileIndex();
  }

  @Override
  public String toString() {
    if (macroDef == null) {
      return "Reference to unknown macro";
    } else {
      return "Reference to " + macroDef.getName() + " defined in file #" + macroDef.getParent().getFileIndex();
    }
  }
}
