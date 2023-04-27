/********************************************************************************
 * Copyright (c) 2015-2023 Riverside Software
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

import org.prorefactor.core.util.StatementFlowWriter;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class StatementFlowTest {

  private RefactorSession session;

  private final static String SOURCEDIR = "src/test/resources/treeparser06/";
  private final static String TARGETDIR = "target/test-temp/treeparser06/";
  private final static String EXPECTDIR = "src/test/resources/treeparser06-expect/";

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().createAlias("foo", "sports2000");
    new File(TARGETDIR).mkdirs();
  }

  @Test
  public void test01() throws IOException {
    genericTest("test01.p");
  }

  @Test
  public void test02() throws IOException {
    genericTest("test02.cls");
  }

  @Test
  public void test03() throws IOException {
    genericTest("test03.p");
  }

  @Test
  public void test04() throws IOException {
    genericTest("test04.p");
  }

  private void genericTest(String name) throws IOException {
    StatementFlowWriter writer = new StatementFlowWriter();
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
