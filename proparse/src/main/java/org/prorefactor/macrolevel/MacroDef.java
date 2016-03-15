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

import org.prorefactor.refactor.RefactorException;
import org.prorefactor.xfer.DataXferStream;

import java.io.IOException;

/**
 * A macro DEFINE (global or scoped) or UNDEFINE or an include argument (named or numbered/positional).
 */
public class MacroDef implements MacroEvent {
  private static final long serialVersionUID = 1071848152328641207L;

  public static final int GLOBAL = 1;
  public static final int SCOPED = 2;
  public static final int UNDEFINE = 3;
  public static final int NAMEDARG = 4;
  public static final int NUMBEREDARG = 5;

  public int column;
  public int line;
  /** One of this class's values: GLOBAL, SCOPED, UNDEFINE, NAMEDARG, NUMBEREDARG */
  public int type;
  /** For an UNDEFINE - undef what? */
  public MacroDef undefWhat = null;
  /** For an include argument - what include reference is it for? */
  public IncludeRef includeRef = null;
  /** The source where this definition can be found */
  public MacroRef parent;
  public String name;
  public String value;

  public MacroDef() {

  }

  @Override
  public MacroRef getParent() {
    return parent;
  }

  @Override
  public MacroPosition getPosition() throws RefactorException {
    return new MacroPosition(parent.getPosition().getFileNum(), line, column);
  }

  /** Implement Xferable. */
  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    out.writeInt(type);
    out.writeInt(line);
    out.writeInt(column);
    out.writeRef(name);
    out.writeRef(value);
    out.writeRef(parent);
    out.writeRef(undefWhat);
    out.writeRef(includeRef);
  }

  /** Implement Xferable. */
  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    out.schemaInt("type");
    out.schemaInt("line");
    out.schemaInt("column");
    out.schemaRef("name");
    out.schemaRef("value");
    out.schemaRef("parent");
    out.schemaRef("undefWhat");
    out.schemaRef("includeRef");
  }

}
