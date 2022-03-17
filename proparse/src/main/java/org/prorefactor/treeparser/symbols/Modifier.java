/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
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
package org.prorefactor.treeparser.symbols;

import org.prorefactor.core.ABLNodeType;

public enum Modifier {
  PUBLIC(ABLNodeType.PUBLIC),
  PROTECTED(ABLNodeType.PROTECTED),
  PRIVATE(ABLNodeType.PRIVATE),
  PACKAGE_PROTECTED(ABLNodeType.PACKAGEPROTECTED),
  PACKAGE_PRIVATE(ABLNodeType.PACKAGEPRIVATE),
  STATIC(ABLNodeType.STATIC),
  SERIALIZABLE(ABLNodeType.SERIALIZABLE),
  NON_SERIALIZABLE(ABLNodeType.NONSERIALIZABLE),
  ABSTRACT(ABLNodeType.ABSTRACT),
  OVERRIDE(ABLNodeType.OVERRIDE),
  FINAL(ABLNodeType.FINAL),
  INPUT(ABLNodeType.INPUT),
  OUTPUT(ABLNodeType.OUTPUT),
  INPUT_OUTPUT(ABLNodeType.INPUTOUTPUT);

  ABLNodeType type;

  private Modifier(ABLNodeType type) {
    this.type = type;
  }

  public static Modifier getModifier(ABLNodeType type) {
    for (Modifier m : values()) {
      if (m.type == type)
        return m;
    }
    return null;
  }

  public static Modifier getModifier(int type) {
    for (Modifier m : values()) {
      if (m.type.getType() == type)
        return m;
    }
    return null;
  }

}
