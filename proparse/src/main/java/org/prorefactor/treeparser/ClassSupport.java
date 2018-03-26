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
package org.prorefactor.treeparser;

import org.prorefactor.core.IConstants;
import org.prorefactor.core.JPNode;

public class ClassSupport {

  private ClassSupport() {
    // Shouldn't be instantiated
  }

  /** This little method is used during tree parsing by both Variable and Field. */
  public static String qualifiedClassName(JPNode typeNameNode) {
    String s = typeNameNode.attrGetS(IConstants.QUALIFIED_CLASS_INT);
    if (s != null && s.length() > 0) {
      return s;
    } else {
      return typeNameNode.getText();
    }
  }

}
