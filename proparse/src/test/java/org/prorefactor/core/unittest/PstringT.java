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
package org.prorefactor.core.unittest;

import junit.framework.TestCase;

import org.prorefactor.core.Pstring;

public class PstringT extends TestCase {

  public void testBasicFunctions() {
    Pstring pstring = new Pstring("\"No more 'Hello world'!\":T");
    assertEquals("Pstring.justText() failed", "No more 'Hello world'!", pstring.getText());
  }

}
