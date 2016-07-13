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
  public int type;
  /** For an UNDEFINE - undef what? */
  public MacroDef undefWhat = null;
  /** For an include argument - what include reference is it for? */
  public IncludeRef includeRef = null;
  /** The source where this definition can be found */
  public String name;
  public String value;

  public MacroDef(MacroRef parent) {
    this(parent, 0, 0);
  }

  public MacroDef(MacroRef parent, int line, int column) {
    this.parent = parent;
    this.line = line;
    this.column = column;
  }

  @Override
  public MacroRef getParent() {
    return parent;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
  }

  @Override
  public MacroPosition getPosition() {
    return new MacroPosition(parent.getPosition().getFileNum(), line, column);
  }

}
