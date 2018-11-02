/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2018 Riverside Software
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
package org.prorefactor.core;

import static org.testng.Assert.assertTrue;

import org.prorefactor.proparse.ProParserTokenTypes;
import org.testng.annotations.Test;

public class NodeTypesTest {

  @Test
  public void testRange() {
    for (ABLNodeType type : ABLNodeType.values()) {
      assertTrue(type.getType() >= -1000);
      // assertTrue(type.getType() != 0);
      assertTrue(type.getType() < ProParserTokenTypes.Last_Token_Number);
    }
  }

  @Test(enabled = false)
  public void generateKeywordList() {
    // Only for proparse.g
    for (ABLNodeType nodeType : ABLNodeType.values()) {
      if (nodeType.isUnreservedKeywordType())
        System.out.println(" | " + nodeType);
    }
  }
}
