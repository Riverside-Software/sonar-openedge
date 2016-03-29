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
package org.prorefactor.treeparser;

/**
 * A Symbol defined with DEFINE &lt;widget-type&gt; or any of the other various syntaxes which implicitly define a widget.
 * This includes FRAMEs, WINDOWs, MENUs, etc.
 */
public abstract class Widget extends Symbol implements WidgetI {

  protected Widget() {
    // Only to be used for persistence/serialization
  }

  public Widget(String name, SymbolScope scope) {
    super(scope);
    setName(name);
  }

  @Override
  public String fullName() {
    return getName();
  }

}
