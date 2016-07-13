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
 * A reference to a macro argument, i.e. {1} or {&amp;name}. Origin might be an include argument or an &amp;DEFINE.
 */
public class NamedMacroRef extends MacroRef {
  private final MacroDef macroDef;

  public NamedMacroRef(MacroDef macro, MacroRef parent, int line, int column) {
    super(parent, line, column);
    this.macroDef = macro;
  }

  public MacroDef getMacroDef() {
    return macroDef;
  }

  @Override
  public int getFileIndex() {
    return getParent().getFileIndex();
  }

}
