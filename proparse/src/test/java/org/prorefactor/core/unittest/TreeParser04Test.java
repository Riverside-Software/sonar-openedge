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
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test frame scopes and implicit field associations to frames.
 */
public class TreeParser04Test {
  String expectName = "src/test/resources/treeparser04-expect/frames.p";
  String inName = "src/test/resources/treeparser04/frames.p";
  File outFile = new File("target/test-temp/treeparser04/frames.p");

  @Test
  public void test01() throws IOException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    outFile.getParentFile().mkdirs();

    ParseUnit pu = new ParseUnit(new File(inName), session);
    pu.treeParser01();

    PrintWriter writer = new PrintWriter(new FileWriter(outFile));
    JPNodeLister nodeLister = new TP01FramesTreeLister(pu.getTopNode(), writer);
    nodeLister.print(' ');
    writer.close();

    assertTrue(FileUtils.contentEquals(new File(expectName), outFile));
  }

}
