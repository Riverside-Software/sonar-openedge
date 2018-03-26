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
