/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2021 Riverside Software
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
package org.prorefactor.core.session;

import static org.testng.Assert.assertTrue;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.proparse.antlr4.Proparse;
import org.testng.annotations.Test;

public class NodeTypesTest {

  @Test
  public void testRange() {
    for (ABLNodeType type : ABLNodeType.values()) {
      assertTrue(type.getType() >= -1020);
      // This test ensures that no token is created during the parser generation (i.e. tokens found in proparse.g4,
      // but not found in BaseTokenTypes.tokens)
      assertTrue(type.getType() < Proparse.Last_Token_Number, "Invalid token: " + type.toString());
    }
  }

}
