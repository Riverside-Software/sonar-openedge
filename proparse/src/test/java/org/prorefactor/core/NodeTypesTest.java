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
package org.prorefactor.core;

import static org.testng.Assert.assertTrue;

import org.prorefactor.proparse.ProParserTokenTypes;
import org.testng.annotations.Test;

public class NodeTypesTest {

  @Test
  public void testRange() {
    for (ABLNodeType type : ABLNodeType.values()) {
      assertTrue(type.getType() >= -1);
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
