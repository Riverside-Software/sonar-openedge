/********************************************************************************
 * Copyright (c) 2015-2025 Riverside Software
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.macrolevel.IncludeRef;
import org.prorefactor.macrolevel.MacroDef;
import org.prorefactor.macrolevel.MacroDefinitionType;
import org.prorefactor.macrolevel.MacroEvent;
import org.prorefactor.macrolevel.MacroRef;
import org.prorefactor.macrolevel.NamedMacroRef;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class MacroGraphTest extends AbstractProparseTest {
  private static final String SRC_DIR = "src/test/resources/data/preprocessor";

  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  private void testVariable(JPNode topNode, String variable) {
    for (JPNode node : topNode.query(ABLNodeType.ID)) {
      if (node.getText().equals(variable)) {
        return;
      }
    }
    fail("Variable " + variable + " not found");
  }

  private void testNoVariable(JPNode topNode, String variable) {
    for (JPNode node : topNode.query(ABLNodeType.ID)) {
      if (node.getText().equals(variable)) {
        fail("Variable " + variable + " not found");
      }
    }
  }

  @Test
  public void testGlobalDefine() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor02.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    testVariable(unit.getTopNode(), "var1");
    testVariable(unit.getTopNode(), "var2");
    testNoVariable(unit.getTopNode(), "var3");
  }

  @Test
  public void testScopedDefine() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor02.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    testNoVariable(unit.getTopNode(), "var4");
  }

  @Test
  public void testMacroGraph() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor02.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    assertEquals(unit.getMacroGraph().findExternalMacroReferences().size(), 4);
    List<MacroEvent> list = unit.getMacroGraph().findExternalMacroReferences(new int[] {1,1}, new int[] {5,1});
    assertEquals(list.size(), 1);
    assertTrue(list.get(0) instanceof MacroRef);
    MacroRef ref0 = (MacroRef) list.get(0);
    assertFalse(ref0.contains(0, 2));
    assertFalse(ref0.contains(3, 1));
    assertTrue(ref0.contains(3, 10));
    assertFalse(ref0.contains(3, 50));
    assertFalse(ref0.contains(6, 1));
    assertEquals(unit.getMacroGraph().findExternalMacroReferences(new int[] {1,1}, new int[] {2,1}).size(), 0);
    assertEquals(unit.getMacroGraph().findIncludeReferences(0).size(), 1);
    assertEquals(unit.getMacroGraph().findIncludeReferences(1).size(), 1);
    assertEquals(unit.getMacroGraph().findIncludeReferences(2).size(), 1);
    assertEquals(unit.getMacroGraph().findIncludeReferences(3).size(), 2);
    assertEquals(unit.getMacroGraph().findIncludeReferences(4).size(), 0);

    assertEquals(unit.getMacroGraph().findIncludeReferences(2).get(0).getArgNumber(0), null);
    assertEquals(unit.getMacroGraph().findIncludeReferences(2).get(0).getArgNumber(1).getValue(), "123");
    assertEquals(unit.getMacroGraph().findIncludeReferences(2).get(0).getArgNumber(2).getValue(), "456");
    assertEquals(unit.getMacroGraph().findIncludeReferences(2).get(0).getArgNumber(3), null);
    int ppDirectives = 0;
    int incInMainFile = 0;
    for (MacroEvent evt : unit.getMacroGraph().macroEventList) {
      if (evt instanceof NamedMacroRef) {
        if (((NamedMacroRef) evt).getMacroDef() == null)
          ppDirectives ++;
      } else if (evt instanceof IncludeRef) {
        incInMainFile++;
      }
    }
    assertEquals(ppDirectives, 3);
    assertEquals(incInMainFile, 4);
  }

  @Test
  public void testMacroGraphPosition() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor02.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<MacroEvent> list = unit.getMacroGraph().findExternalMacroReferences();
    assertTrue(list.get(0) instanceof IncludeRef);
    assertEquals(((MacroRef) list.get(0)).getFileIndex(), 1);
    assertEquals(((MacroRef) list.get(0)).getLine(), 3);
    assertEquals(((MacroRef) list.get(0)).getColumn(), 4);
    assertEquals(((MacroRef) list.get(0)).getEndLine(), 3);
    assertEquals(((MacroRef) list.get(0)).getEndColumn(), 40);
    assertTrue(list.get(1) instanceof IncludeRef);
    assertEquals(((MacroRef) list.get(1)).getFileIndex(), 2);
    assertEquals(((MacroRef) list.get(1)).getLine(), 20);
    assertEquals(((MacroRef) list.get(1)).getColumn(), 0);
    assertEquals(((MacroRef) list.get(1)).getEndLine(), 20);
    assertEquals(((MacroRef) list.get(1)).getEndColumn(), 44);
    assertTrue(list.get(2) instanceof IncludeRef);
    assertEquals(((MacroRef) list.get(2)).getFileIndex(), 3);
    assertEquals(((MacroRef) list.get(2)).getLine(), 22);
    assertEquals(((MacroRef) list.get(2)).getColumn(), 0);
    assertEquals(((MacroRef) list.get(2)).getEndLine(), 22);
    assertEquals(((MacroRef) list.get(2)).getEndColumn(), 36);
    assertTrue(list.get(3) instanceof IncludeRef);
    assertEquals(((MacroRef) list.get(3)).getFileIndex(), 3);
    assertEquals(((MacroRef) list.get(3)).getLine(), 28);
    assertEquals(((MacroRef) list.get(3)).getColumn(), 0);
    assertEquals(((MacroRef) list.get(3)).getEndLine(), 28);
    assertEquals(((MacroRef) list.get(3)).getEndColumn(), 36);

    MacroRef ref = (MacroRef) unit.getMacroGraph().macroEventList.get(5);
    assertEquals(ref.getLine(), 24);
    assertEquals(ref.getEndLine(), 24);
    assertEquals(ref.getColumn(), 0);
    assertEquals(ref.getEndColumn(), 49);
    ref = (MacroRef) unit.getMacroGraph().macroEventList.get(6);
    assertEquals(ref.getLine(), 25);
    assertEquals(ref.getEndLine(), 25);
    assertEquals(ref.getColumn(), 2);
    assertEquals(ref.getEndColumn(), 53);
    ref = (MacroRef) unit.getMacroGraph().macroEventList.get(7);
    assertEquals(ref.getLine(), 26);
    assertEquals(ref.getEndLine(), 27);
    assertEquals(ref.getColumn(), 0);
    assertEquals(ref.getEndColumn(), 1);
  }

  @Test
  public void testIncludeParameter01() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor18.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<MacroEvent> list = unit.getMacroGraph().findExternalMacroReferences();
    assertNotNull(list);
    assertEquals(list.size(), 1);
    assertTrue(list.get(0) instanceof IncludeRef);
    IncludeRef ref = (IncludeRef) list.get(0);
    assertEquals(ref.getLine(), 1);
    assertEquals(ref.getEndLine(), 3);
    assertEquals(ref.getColumn(), 0);
    assertEquals(ref.getEndColumn(), 44);
    assertNotNull(ref.getArgNumber(1));
    assertEquals(ref.getArgNumber(1).getName(), "param1");
    assertEquals(ref.getArgNumber(1).getValue(), "value1");
    assertNotNull(ref.getArgNumber(2));
    assertEquals(ref.getArgNumber(2).getName(), "param2");
    assertEquals(ref.getArgNumber(2).getValue(), "value2");
    assertNull(ref.getArgNumber(3));
  }

  @Test
  public void testIncludeParameter02() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor18-2.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<MacroEvent> list = unit.getMacroGraph().findExternalMacroReferences();
    assertNotNull(list);
    assertEquals(list.size(), 1);
    assertTrue(list.get(0) instanceof IncludeRef);
    IncludeRef ref = (IncludeRef) list.get(0);
    assertEquals(ref.getLine(), 1);
    assertEquals(ref.getEndLine(), 5);
    assertEquals(ref.getColumn(), 0);
    assertEquals(ref.getEndColumn(), 33);
    assertNotNull(ref.getArgNumber(1));
    assertEquals(ref.getArgNumber(1).getName(), "param1");
    assertEquals(ref.getArgNumber(1).getValue(), "value1");
    assertNotNull(ref.getArgNumber(2));
    assertEquals(ref.getArgNumber(2).getName(), "&param2");
    assertEquals(ref.getArgNumber(2).getValue(), "value2");
    assertNotNull(ref.getArgNumber(3));
    assertEquals(ref.getArgNumber(3).getName(), "pa&ram3");
    assertEquals(ref.getArgNumber(3).getValue(), "value3");
    assertNull(ref.getArgNumber(4));
  }

  @Test
  public void testIncludeParameter03() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor20.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<MacroEvent> list = unit.getMacroGraph().findExternalMacroReferences();
    assertNotNull(list);
    assertEquals(list.size(), 3);

    /* First include file */
    IncludeRef ref1 = (IncludeRef) list.get(0);
    assertEquals(ref1.getLine(), 6);
    assertEquals(ref1.getEndLine(), 6);
    assertEquals(ref1.getColumn(), 17);
    assertEquals(ref1.getEndColumn(), 58);
    assertEquals(ref1.numArgs(), 1);
    MacroDef ref1Arg1 = ref1.getArgNumber(1);
    assertEquals(ref1Arg1.getType(), MacroDefinitionType.NUMBEREDARG);
    assertEquals(ref1Arg1.getValue(), "BAR");

    /* Second include file */
    IncludeRef ref2 = (IncludeRef) list.get(1);
    assertEquals(ref2.getLine(), 2);
    assertEquals(ref2.getEndLine(), 8);
    assertEquals(ref2.getColumn(), 0);
    assertEquals(ref2.getEndColumn(), 7);
    assertEquals(ref2.numArgs(), 5);
    MacroDef ref2Arg1 = ref2.lookupNamedArg("param1");
    assertEquals(ref2Arg1.getValue(), "");
    MacroDef ref2Arg2 = ref2.lookupNamedArg("param2");
    assertEquals(ref2Arg2.getValue(), "");
    MacroDef ref2Arg3 = ref2.lookupNamedArg("param3");
    assertEquals(ref2Arg3.getValue(), " ");
    MacroDef ref2Arg4 = ref2.lookupNamedArg("param4");
    assertEquals(ref2Arg4.getValue(), "XXX BAR BAR XXX ");
    MacroDef ref2Arg5 = ref2.lookupNamedArg("param5");
    assertEquals(ref2Arg5.getValue(), " test ");

    /* Third include file */
    IncludeRef ref3 = (IncludeRef) list.get(2);
    assertEquals(ref3.getLine(), 9);
    assertEquals(ref3.getEndLine(), 9);
    assertEquals(ref3.getColumn(), 0);
    assertEquals(ref3.getEndColumn(), 82);
    assertEquals(ref3.numArgs(), 6);
    MacroDef ref3Arg1 = ref3.getArgNumber(1);
    assertEquals(ref3Arg1.getValue(), "\"");
    MacroDef ref3Arg2 = ref3.getArgNumber(2);
    assertEquals(ref3Arg2.getValue(), "value1");
    MacroDef ref3Arg3 = ref3.getArgNumber(3);
    assertEquals(ref3Arg3.getValue(), "value2");
    MacroDef ref3Arg4 = ref3.getArgNumber(4);
    assertEquals(ref3Arg4.getValue(), "\"value3\":U");
    MacroDef ref3Arg5 = ref3.getArgNumber(5);
    assertEquals(ref3Arg5.getValue(), "\"value4\"");
    MacroDef ref3Arg6 = ref3.getArgNumber(6);
    assertEquals(ref3Arg6.getValue(), " ");
  }

  @Test
  public void testIncludeParameter04() {
    ParseUnit unit = getParseUnit(new File(SRC_DIR, "preprocessor25.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    List<MacroEvent> list = unit.getMacroGraph().findExternalMacroReferences();
    assertNotNull(list);
    assertEquals(list.size(), 1);

    /* First include file */
    IncludeRef ref1 = (IncludeRef) list.get(0);
    assertEquals(ref1.getLine(), 1);
    assertEquals(ref1.getEndLine(), 1);
    assertEquals(ref1.getColumn(), 0);
    assertEquals(ref1.getEndColumn(), 56);
    assertEquals(ref1.numArgs(), 1);
    MacroDef ref1Arg1 = ref1.getArgNumber(1);
    assertEquals(ref1Arg1.getType(), MacroDefinitionType.NAMEDARG);
    assertEquals(ref1Arg1.getName(), "DefaultValue");
    assertEquals(ref1Arg1.getValue(), "\"-\"");
  }
}
