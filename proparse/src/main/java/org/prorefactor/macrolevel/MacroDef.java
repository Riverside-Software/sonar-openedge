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

/**
 * A macro DEFINE (global or scoped) or UNDEFINE or an include argument (named or numbered/positional).
 */
public class MacroDef implements MacroEvent {
  public static final int GLOBAL = 1;
  public static final int SCOPED = 2;
  public static final int UNDEFINE = 3;
  public static final int NAMEDARG = 4;
  public static final int NUMBEREDARG = 5;

  private final MacroRef parent;
  private final int column;
  private final int line;
  /** One of this class's values: GLOBAL, SCOPED, UNDEFINE, NAMEDARG, NUMBEREDARG */
  private int type;

  /** For an UNDEFINE - undef what? */
  private MacroDef undefWhat = null;
  /** For an include argument - what include reference is it for? */
  private IncludeRef includeRef = null;
  private String name;
  private String value;

  public MacroDef(MacroRef parent, int type) {
    this(parent, type, 0, 0);
  }

  public MacroDef(MacroRef parent, int type, int line, int column) {
    this(parent, type, line, column, "", "");
  }

  public MacroDef(MacroRef parent, int type, int line, int column, String name, String value) {
    this.parent = parent;
    this.type = type;
    this.line = line;
    this.column = column;
    this.name = name;
    this.value = value;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public MacroRef getParent() {
    return parent;
  }

  public int getType() {
    return type;
  }

  @Override
  public MacroPosition getPosition() {
    return new MacroPosition(parent.getPosition().getFileNum(), line, column);
  }

  public void setUndefWhat(MacroDef macroDef) {
    this.undefWhat = macroDef;
  }

  public MacroDef getUndefWhat() {
    return undefWhat;
  }

  public void setIncludeRef(IncludeRef includeRef) {
    this.includeRef = includeRef;
  }

  public IncludeRef getIncludeRef() {
    return includeRef;
  }
}
