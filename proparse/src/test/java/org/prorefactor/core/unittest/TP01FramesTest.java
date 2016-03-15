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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.unittest.util.JPNodeLister;
import org.prorefactor.core.unittest.util.TP01FramesTreeLister;
import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

/**
 * Test frame scopes and implicit field associations to frames.
 */
public class TP01FramesTest extends TestCase {
  private RefactorSession session;

  String expectFileName = "src/test/resources/data/tp01tests/frames.expect.txt";
  String inFileName = "src/test/resources/data/tp01tests/frames.p";
  File outFileName = new File("target/test-temp/tp01tests/frames.out.txt");

  @Override
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestSports2000Module());
    session = injector.getInstance(RefactorSession.class);

    // Create target directory for output result
    outFileName.getParentFile().mkdirs();
  }

  public void test01() throws Exception {
    ParseUnit pu = new ParseUnit(new File(inFileName), session);
    pu.treeParser01();

    PrintWriter writer = new PrintWriter(new FileWriter(outFileName));
    JPNodeLister nodeLister = new TP01FramesTreeLister(pu.getTopNode(), writer);
    nodeLister.print(' ');
    writer.close();

    assertTrue("Differences in: " + expectFileName + " " + outFileName,
        FileUtils.contentEquals(new File(expectFileName), outFileName));
  }

}
