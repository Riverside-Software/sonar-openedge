package org.prorefactor.core;

import static org.testng.Assert.assertTrue;

import org.prorefactor.proparse.ProParserTokenTypes;
import org.testng.annotations.Test;

public class NodeTypesTest {

  @Test
  public void testRange() {
    for (ABLNodeType type : ABLNodeType.values()) {
      assertTrue(type.getType() >= -1);
      assertTrue(type.getType() != 0);
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
