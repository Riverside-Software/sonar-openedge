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

import java.io.IOException;

import org.prorefactor.xfer.DataXferStream;

/**
 * A reference to a macro argument, i.e. {1} or {&name}. Origin might be an include argument or an &DEFINE.
 */
public class NamedMacroRef extends MacroRef {
  private static final long serialVersionUID = -7576618416022482176L;

  public MacroDef macroDef;

  /** Only to be used for persistence/serialization. */
  public NamedMacroRef() {
  }

  NamedMacroRef(int listingFileLine) {
    super(listingFileLine);
  }

  @Override
  public int getFileIndex() {
    return parent.getFileIndex();
  }

  @Override
  public void writeXferBytes(DataXferStream out) throws IOException {
    super.writeXferBytes(out);
    out.writeRef(macroDef);
  }

  @Override
  public void writeXferSchema(DataXferStream out) throws IOException {
    super.writeXferSchema(out);
    out.schemaRef("macroDef");
  }

}
