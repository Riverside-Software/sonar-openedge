/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2024 Riverside Software
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
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TreeParser02Test {

  private RefactorSession session;

  private final static String SOURCEDIR = "src/test/resources/treeparser02/";
  private final static String TARGETDIR = "target/test-temp/treeparser02/";
  private final static String EXPECTDIR = "src/test/resources/treeparser02-expect/";

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    session.getSchema().createAlias("foo", "sports2000");
    new File(TARGETDIR).mkdirs();
  }

  @Test
  public void test01() throws IOException {
    genericTest("test01.p");
  }

  @Test
  public void test02() throws IOException {
    genericTest("test02.p");
  }

  @Test
  public void test03() throws IOException {
    genericTest("test03.p");
  }

  @Test
  public void test04() throws IOException {
    genericTest("test04.p");
  }

  @Test
  public void test05() throws IOException {
    genericTest("test05.p");
  }

  @Test
  public void test06() throws IOException {
    genericTest("test06.p");
  }

  @Test
  public void test07() throws IOException {
    genericTest("test07.p");
  }

  @Test
  public void test08() throws IOException {
    genericTest("test08.p");
  }

  @Test
  public void test09() throws IOException {
    genericTest("test09.p");
  }

  @Test
  public void test10() throws IOException {
    genericTest("test10.p");
  }

  @Test
  public void test11() throws IOException {
    genericTest("test11.p");
  }

  @Test
  public void test12() throws IOException {
    genericTest("test12.p");
  }

  @Test
  public void test13() throws IOException {
    genericTest("test13.p");
  }

  @Test
  public void test14() throws IOException {
    genericTest("test14.p");
  }

  @Test
  public void test15() throws IOException {
    genericTest("test15.p");
  }

  @Test
  public void test16() throws IOException {
    genericTest("test16.p");
  }

  @Test
  public void test17() throws IOException {
    genericTest("test17.p");
  }

  @Test
  public void test18() throws IOException {
    genericTest("test18.p");
  }

  @Test
  public void test19() throws IOException {
    genericTest("test19.p");
  }

  @Test
  public void test20() throws IOException {
    genericTest("test20.p");
  }

  @Test
  public void test21() throws IOException {
    genericTest("test21.p");
  }

  @Test
  public void test22() throws IOException {
    genericTest("test22.p");
  }

  @Test
  public void test23() throws IOException {
    genericTest("test23.p");
  }

  @Test
  public void test24() throws IOException {
    genericTest("test24.p");
  }

  @Test
  public void test25() throws IOException {
    genericTest("test25.p");
  }

  @Test
  public void test26() throws IOException {
    genericTest("test26.p");
  }

  @Test
  public void test27() throws IOException {
    genericTest("test27.p");
  }

  @Test
  public void test28() throws IOException {
    genericTest("test28.p");
  }

  @Test
  public void test29() throws IOException {
    genericTest("test29.p");
  }

  @Test
  public void test30() throws IOException {
    genericTest("test30.p");
  }

  @Test
  public void test31() throws IOException {
    genericTest("test31.p");
  }

  @Test
  public void test32() throws IOException {
    genericTest("test32.p");
  }

  @Test
  public void test33() throws IOException {
    genericTest("test33.p");
  }

  @Test
  public void test34() throws IOException {
    genericTest("test34.p");
  }

  @Test(enabled = false)
  public void test35() throws IOException {
    genericTest("test35.p");
  }

  @Test(enabled = false)
  public void test36() throws IOException {
    genericTest("test36.p");
  }

  @Test
  public void test37() throws IOException {
    genericTest("test37.p");
  }

  @Test
  public void test38() throws IOException {
    genericTest("test38.p");
  }

  @Test
  public void test39() throws IOException {
    genericTest("test39.p");
  }

  @Test
  public void test40() throws IOException {
    genericTest("test40.p");
  }

  @Test
  public void test41() throws IOException {
    genericTest("test41.p");
  }

  @Test
  public void test42() throws IOException {
    genericTest("test42.p");
  }

  @Test
  public void test43() throws IOException {
    genericTest("test43.p");
  }

  @Test
  public void test44() throws IOException {
    genericTest("test44.p");
  }

  @Test
  public void test45() throws IOException {
    genericTest("test45.p");
  }

  @Test
  public void test46() throws IOException {
    genericTest("test46.p");
  }

  @Test
  public void test47() throws IOException {
    genericTest("test47.p");
  }

  @Test
  public void test48() throws IOException {
    genericTest("test48.p");
  }

  @Test
  public void test49() throws IOException {
    genericTest("test49.p");
  }

  @Test
  public void test50() throws IOException {
    genericTest("test50.p");
  }

  @Test
  public void test51() throws IOException {
    genericTest("test51.p");
  }

  private void genericTest(String name) throws IOException {
    AttributedWriter writer = new AttributedWriter();
    writer.write(SOURCEDIR + name, new File(TARGETDIR + name), session);
    try (FileReader r1 = new FileReader(EXPECTDIR + name);
        FileReader r2 = new FileReader(TARGETDIR + name);
        BufferedReader br1 = new BufferedReader(r1);
        BufferedReader br2 = new BufferedReader(r2)) {
      assertTrue(contentEquals(br1, br2));
    } catch (IOException caught) {
      fail("Unable to find output file", caught);
    }
  }

  protected static boolean contentEquals(BufferedReader r1, BufferedReader r2) throws IOException {
    String s1 = r1.readLine();
    String s2 = r2.readLine();
    while (s1 != null) {
      if (!s1.equals(s2))
        return false;
      s1 = r1.readLine();
      s2 = r2.readLine();
    }

    return true;
  }
}
