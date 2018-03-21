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

import static org.testng.Assert.assertNotNull;

import java.io.File;

import org.prorefactor.core.ABLNodeType;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

/**
 * Test the tree parsers against problematic syntax. These tests just run the tree parsers against the data/bugsfixed
 * directory. If no exceptions are thrown, then the tests pass. The files in the "bugsfixed" directories are subject to
 * change, so no other tests should be added other than the expectation that they parse clean.
 */
public class LegacyTest {
  private final static String TEMP_DIR = "target/nodes-lister/legacy";

  private RefactorSession session;
  private File tempDir = new File(TEMP_DIR);

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);

    tempDir.mkdirs();
  }

  @Test
  public void testAppendProgram() throws ANTLRException {
      ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/appendprogram/t01/test/t01.p"), session);
      pu1.treeParser01();
      assertNotNull(pu1.getTopNode());
      assertNotNull(pu1.getRootScope());
      // TODO Add assertions

      ParseUnit pu2 = new ParseUnit(new File("src/test/resources/legacy/appendprogram/t01/test/t01b.p"), session);
      pu2.treeParser01();
      assertNotNull(pu2.getTopNode());
      assertNotNull(pu2.getRootScope());
      // TODO Add assertions
  }

  @Test
  public void testBubbleProgram01() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/bubble/test/bubbledecs.p"), session);
    pu1.treeParser01();
    assertNotNull(pu1.getTopNode());
    assertNotNull(pu1.getRootScope());
    // TODO Add assertions

    ParseUnit pu2 = new ParseUnit(new File("src/test/resources/legacy/bubble/test/test2.p"), session);
    pu2.treeParser01();
    assertNotNull(pu2.getTopNode());
    assertNotNull(pu2.getRootScope());
    // TODO Add assertions
  }

  @Test
  public void testBubbleProgram02() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/bubble/test2/bubb2.p"), session);
    pu1.treeParser01();
    assertNotNull(pu1.getTopNode());
    assertNotNull(pu1.getRootScope());
    // TODO Add assertions
  }

  @Test
  public void testBubbleProgram03() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/bubble/x03_test/x03.p"), session);
    pu1.treeParser01();
    assertNotNull(pu1.getTopNode());
    assertNotNull(pu1.getRootScope());
    // TODO Add assertions
  }

  @Test
  public void testBubbleProgram04() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/bubble/x04/test/x04.p"), session);
    pu1.treeParser01();
    assertNotNull(pu1.getTopNode());
    assertNotNull(pu1.getRootScope());
    // TODO Add assertions
  }

  @Test
  public void testBubbleProgram05() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/bubble/x05/test/x05.p"), session);
    pu1.treeParser01();
    assertNotNull(pu1.getTopNode());
    assertNotNull(pu1.getRootScope());
    // TODO Add assertions
  }

  @Test
  public void testExtractMethod() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/extractmethod/t01/test/t01a.p"), session);
    pu1.treeParser01();
    assertNotNull(pu1.getTopNode());
    assertNotNull(pu1.getRootScope());
    // TODO Add assertions
  }

  @Test
  public void testNames() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/names/billto.p"), session);
    pu1.treeParser01();
    assertNotNull(pu1.getTopNode());
    assertNotNull(pu1.getRootScope());
    // TODO Add assertions
    ParseUnit pu2 = new ParseUnit(new File("src/test/resources/legacy/names/customer.p"), session);
    pu2.treeParser01();
    assertNotNull(pu2.getTopNode());
    assertNotNull(pu2.getRootScope());
    // TODO Add assertions
    ParseUnit pu3 = new ParseUnit(new File("src/test/resources/legacy/names/shipto.p"), session);
    pu3.treeParser01();
    assertNotNull(pu3.getTopNode());
    assertNotNull(pu3.getRootScope());
    // TODO Add assertions
  }

  @Test
  public void testQualifyFields() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01a.p"), session);
    ParseUnit pu2 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01b.p"), session);
    ParseUnit pu3 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01c.p"), session);
    ParseUnit pu4 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01d.p"), session);
    ParseUnit pu5 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01e.p"), session);
    ParseUnit pu6 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01f.p"), session);
    ParseUnit pu7 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01g.p"), session);
    ParseUnit pu8 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01h.p"), session);
    ParseUnit pu9 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01i.p"), session);
    ParseUnit pu10 = new ParseUnit(new File("src/test/resources/legacy/qualifyfields/t01/test/t01j.p"), session);
    pu1.treeParser01();
    pu2.treeParser01();
    pu3.treeParser01();
    pu4.treeParser01();
    pu5.treeParser01();
    pu6.treeParser01();
    pu7.treeParser01();
    pu8.treeParser01();
    pu9.treeParser01();
    pu10.treeParser01();
    // TODO Add assertions
  }

  @Test
  public void testAmbiguous() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/Sports2000/Customer/Name.cls"), session);
    pu1.treeParser01();
    // TODO Add assertions
  }

  @Test
  public void testWrapProcedure() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/wrapprocedure/t01/test/t01.p"), session);
    pu1.treeParser01();
    // TODO Add assertions
  }

  @Test
  public void testBaseDir() throws ANTLRException {
    ParseUnit pu1 = new ParseUnit(new File("src/test/resources/legacy/c-win.w"), session);
    ParseUnit pu3 = new ParseUnit(new File("src/test/resources/legacy/empty.p"), session);
    ParseUnit pu4 = new ParseUnit(new File("src/test/resources/legacy/hello2.p"), session);
    ParseUnit pu5 = new ParseUnit(new File("src/test/resources/legacy/jpplus1match.p"), session);
    ParseUnit pu6 = new ParseUnit(new File("src/test/resources/legacy/match.p"), session);
    ParseUnit pu7 = new ParseUnit(new File("src/test/resources/legacy/names.p"), session);
    ParseUnit pu8 = new ParseUnit(new File("src/test/resources/legacy/substitute.p"), session);
    ParseUnit pu9 = new ParseUnit(new File("src/test/resources/legacy/tw2sample.p"), session);
    pu1.treeParser01();
    pu3.treeParser01();
    pu4.treeParser01();
    pu5.treeParser01();
    pu6.treeParser01();
    pu7.treeParser01();
    pu8.treeParser01();
    pu9.treeParser01();

    Assert.assertEquals(pu1.getTopNode().query(ABLNodeType.BGCOLOR).size(), 1);
    Assert.assertNotNull(pu1.getTopNode().query(ABLNodeType.BGCOLOR).get(0));
    Assert.assertEquals(pu1.getTopNode().query(ABLNodeType.BGCOLOR).get(0).getAnalyzeSuspend(), "_CREATE-WINDOW");

    Assert.assertEquals(pu1.getTopNode().query(ABLNodeType.WAITFOR).size(), 1);
    Assert.assertNotNull(pu1.getTopNode().query(ABLNodeType.WAITFOR).get(0));
    Assert.assertEquals(pu1.getTopNode().query(ABLNodeType.WAITFOR).get(0).getAnalyzeSuspend(), "_UIB-CODE-BLOCK,_CUSTOM,_MAIN-BLOCK,C-Win");
}

}
