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
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertEquals;

import org.prorefactor.core.ProgressString;
import org.testng.annotations.Test;

public class ProgressStringTest {

  @Test
  public void testBasicFunctions() {
    ProgressString pstring = new ProgressString("\"No more 'Hello world'!\":T");
    assertEquals("No more 'Hello world'!", pstring.getText(), "Pstring.justText() failed");
  }

}
