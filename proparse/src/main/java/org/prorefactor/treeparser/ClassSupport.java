/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2019 Riverside Software
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU Lesser General Public License v3.0
 * which is available at https://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-3.0
 ********************************************************************************/
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
