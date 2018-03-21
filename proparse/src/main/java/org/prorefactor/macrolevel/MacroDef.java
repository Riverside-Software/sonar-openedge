/*******************************************************************************
 * Original work Copyright (c) 2003-2015 John Green
 * Modified work Copyright (c) 2015-2018 Riverside Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Green - initial API and implementation and/or initial documentation
 *    Gilles Querret - Almost anything written after 2015
 *******************************************************************************/ 
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
  public MacroDef undefWhat = null;
  /** For an include argument - what include reference is it for? */
  public IncludeRef includeRef = null;
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

}
