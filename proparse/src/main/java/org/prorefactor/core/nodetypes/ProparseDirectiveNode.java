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

import com.google.common.base.Strings;

public class ProparseDirectiveNode extends JPNode {
  private final String directiveText;

  public ProparseDirectiveNode(ProToken t) {
    super(t);
    directiveText = Strings.nullToEmpty(t.getText());
  }

  /**
   * Get the directive text. Might return empty, but should not return null.
   */
  public String getDirectiveText() {
    return directiveText;
  }

}
