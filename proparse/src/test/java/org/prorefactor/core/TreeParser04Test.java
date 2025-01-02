/********************************************************************************
 * Copyright (c) 2003-2015 John Green
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.TP01FramesTreeLister;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.proparse.support.JPNodeLister;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.Test;

/**
 * Test frame scopes and implicit field associations to frames.
 */
public class TreeParser04Test extends AbstractProparseTest {
  String expectName = "src/test/resources/treeparser04-expect/frames.p";
  String inName = "src/test/resources/treeparser04/frames.p";
  File outFile = new File("target/test-temp/treeparser04/frames.p");

  @Test(enabled = false)
  public void test01() throws IOException {
    // TODO Re-enable test when FrameStack implementation is 100% identical to previous one
    RefactorSession session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    outFile.getParentFile().mkdirs();

    ParseUnit pu = getParseUnit(new File(inName), session);
    pu.treeParser01();
    assertFalse(pu.hasSyntaxError());

    PrintWriter writer = new PrintWriter(new FileWriter(outFile));
    JPNodeLister nodeLister = new TP01FramesTreeLister(pu.getTopNode(), writer);
    nodeLister.print(' ');
    writer.close();

    assertEquals(Files.readAllBytes(new File(expectName).toPath()), Files.readAllBytes(outFile.toPath()));
  }

}
