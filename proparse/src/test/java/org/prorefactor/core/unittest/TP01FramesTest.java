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

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.unittest.util.JPNodeLister;
import org.prorefactor.core.unittest.util.TP01FramesTreeLister;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

/**
 * Test frame scopes and implicit field associations to frames.
 */
public class TP01FramesTest {
  private RefactorSession session;

  String expectFileName = "src/test/resources/data/tp01tests/frames.expect.txt";
  String inFileName = "src/test/resources/data/tp01tests/frames.p";
  File outFileName = new File("target/test-temp/tp01tests/frames.out.txt");

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);

    // Create target directory for output result
    outFileName.getParentFile().mkdirs();
  }

  @Test
  public void test01() throws ANTLRException, IOException {
    ParseUnit pu = new ParseUnit(new File(inFileName), session);
    pu.treeParser01();

    PrintWriter writer = new PrintWriter(new FileWriter(outFileName));
    JPNodeLister nodeLister = new TP01FramesTreeLister(pu.getTopNode(), writer);
    nodeLister.print(' ');
    writer.close();

    assertTrue(FileUtils.contentEquals(new File(expectFileName), outFileName),
        "Differences in: " + expectFileName + " " + outFileName);
  }

}
