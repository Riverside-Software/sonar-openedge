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
package org.prorefactor.core.nodetypes;

import org.prorefactor.core.ProToken;

public class ProgramRootNode extends BlockNode {
  private static final long serialVersionUID = 7160983003100786995L;

  public ProgramRootNode() {
    super();
  }

  public ProgramRootNode(int file, int line, int column) {
    super(file, line, column);
  }

  public ProgramRootNode(ProToken t) {
    super(t);
  }

  /** Every JPNode subtype has its own index. Used for persistent storage. */
  @Override
  public int getSubtypeIndex() {
    return 6;
  }

}
