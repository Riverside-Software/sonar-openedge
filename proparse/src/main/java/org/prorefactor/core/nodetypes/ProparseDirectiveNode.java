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

import org.prorefactor.core.JPNode;
import org.prorefactor.core.ProToken;

public class ProparseDirectiveNode extends JPNode {
  private static final long serialVersionUID = -8215081305962889482L;

  private String directiveText = "";

  /** For creating from persistent storage */
  public ProparseDirectiveNode() {
    super();
  }

  public ProparseDirectiveNode(int file, int line, int column) {
    super(file, line, column);
  }

  public ProparseDirectiveNode(ProToken t) {
    super(t);
    directiveText = t.getText();
  }

  /**
   * Get the directive text. Might return empty, but should not return null.
   */
  public String getDirectiveText() {
    return directiveText;
  }

  /**
   * Every JPNode subtype has its own index. Used for persistent storage.
   */
  @Override
  public int getSubtypeIndex() {
    return 5;
  }

  public void setDirectiveText(String text) {
    directiveText = text;
  }

}
