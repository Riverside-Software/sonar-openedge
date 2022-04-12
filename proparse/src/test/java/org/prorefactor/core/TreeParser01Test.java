/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2022 Riverside Software
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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.prorefactor.core.util.AttributedWriter;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TreeParser01Test {

  String expectName = "src/test/resources/treeparser01-expect/test01.p";
  String inName = "src/test/resources/treeparser01/test01.p";
  File outFile = new File("target/test-temp/treeparser01/test01.p");

  @Test
  public void test01() throws IOException {
    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    outFile.getParentFile().mkdirs();

    AttributedWriter writer = new AttributedWriter();
    writer.write(inName, outFile, session);
    
    try (FileReader r1 = new FileReader(expectName);
        FileReader r2 = new FileReader(outFile);
        BufferedReader br1 = new BufferedReader(r1);
        BufferedReader br2 = new BufferedReader(r2)) {
      assertTrue(TreeParser02Test.contentEquals(br1, br2));
    } catch (IOException caught) {
      fail("Unable to find output file", caught);
    }
  }

}
