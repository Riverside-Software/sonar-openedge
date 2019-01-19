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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.prorefactor.core.unittest.util.AttributedWriter;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

public class TreeParser02Test {

  private RefactorSession session;

  private final static String SOURCEDIR = "src/test/resources/treeparser02/";
  private final static String TARGETDIR = "target/test-temp/treeparser02/";
  private final static String EXPECTDIR = "src/test/resources/treeparser02-expect/";

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().createAlias("foo", "sports2000");
    new File(TARGETDIR).mkdirs();
  }

  @Test
  public void test01() throws ANTLRException, IOException {
    genericTest("test01.p");
  }

  @Test
  public void test02() throws ANTLRException, IOException {
    genericTest("test02.p");
  }

  @Test
  public void test03() throws ANTLRException, IOException {
    genericTest("test03.p");
  }

  @Test
  public void test04() throws ANTLRException, IOException {
    genericTest("test04.p");
  }

  @Test
  public void test05() throws ANTLRException, IOException {
    genericTest("test05.p");
  }

  @Test
  public void test06() throws ANTLRException, IOException {
    genericTest("test06.p");
  }

  @Test
  public void test07() throws ANTLRException, IOException {
    genericTest("test07.p");
  }

  @Test
  public void test08() throws ANTLRException, IOException {
    genericTest("test08.p");
  }

  @Test
  public void test09() throws ANTLRException, IOException {
    genericTest("test09.p");
  }

  @Test
  public void test10() throws ANTLRException, IOException {
    genericTest("test10.p");
  }

  @Test
  public void test11() throws ANTLRException, IOException {
    genericTest("test11.p");
  }

  @Test
  public void test12() throws ANTLRException, IOException {
    genericTest("test12.p");
  }

  @Test
  public void test13() throws ANTLRException, IOException {
    genericTest("test13.p");
  }

  @Test
  public void test14() throws ANTLRException, IOException {
    genericTest("test14.p");
  }

  @Test
  public void test15() throws ANTLRException, IOException {
    genericTest("test15.p");
  }

  @Test
  public void test16() throws ANTLRException, IOException {
    genericTest("test16.p");
  }

  @Test
  public void test17() throws ANTLRException, IOException {
    genericTest("test17.p");
  }

  @Test
  public void test18() throws ANTLRException, IOException {
    genericTest("test18.p");
  }

  @Test
  public void test19() throws ANTLRException, IOException {
    genericTest("test19.p");
  }

  @Test
  public void test20() throws ANTLRException, IOException {
    genericTest("test20.p");
  }

  @Test
  public void test21() throws ANTLRException, IOException {
    genericTest("test21.p");
  }

  @Test
  public void test22() throws ANTLRException, IOException {
    genericTest("test22.p");
  }

  @Test
  public void test23() throws ANTLRException, IOException {
    genericTest("test23.p");
  }

  @Test
  public void test24() throws ANTLRException, IOException {
    genericTest("test24.p");
  }

  @Test
  public void test25() throws ANTLRException, IOException {
    genericTest("test25.p");
  }

  @Test
  public void test26() throws ANTLRException, IOException {
    genericTest("test26.p");
  }

  @Test
  public void test27() throws ANTLRException, IOException {
    genericTest("test27.p");
  }

  @Test
  public void test28() throws ANTLRException, IOException {
    genericTest("test28.p");
  }

  @Test
  public void test29() throws ANTLRException, IOException {
    genericTest("test29.p");
  }

  @Test
  public void test30() throws ANTLRException, IOException {
    genericTest("test30.p");
  }

  @Test
  public void test31() throws ANTLRException, IOException {
    genericTest("test31.p");
  }

  @Test
  public void test32() throws ANTLRException, IOException {
    genericTest("test32.p");
  }

  @Test
  public void test33() throws ANTLRException, IOException {
    genericTest("test33.p");
  }

  @Test
  public void test34() throws ANTLRException, IOException {
    genericTest("test34.p");
  }

  @Test
  public void test35() throws ANTLRException, IOException {
    genericTest("test35.p");
  }

  @Test
  public void test36() throws ANTLRException, IOException {
    genericTest("test36.p");
  }

  @Test
  public void test37() throws ANTLRException, IOException {
    genericTest("test37.p");
  }

  @Test
  public void test38() throws ANTLRException, IOException {
    genericTest("test38.p");
  }

  @Test
  public void test39() throws ANTLRException, IOException {
    genericTest("test39.p");
  }

  @Test
  public void test40() throws ANTLRException, IOException {
    genericTest("test40.p");
  }

  @Test
  public void test41() throws ANTLRException, IOException {
    genericTest("test41.p");
  }

  @Test
  public void test42() throws ANTLRException, IOException {
    genericTest("test42.p");
  }

  @Test
  public void test43() throws ANTLRException, IOException {
    genericTest("test43.p");
  }

  @Test
  public void test44() throws ANTLRException, IOException {
    genericTest("test44.p");
  }

  @Test
  public void test45() throws ANTLRException, IOException {
    genericTest("test45.p");
  }

  @Test
  public void test46() throws ANTLRException, IOException {
    genericTest("test46.p");
  }

  @Test
  public void test47() throws ANTLRException, IOException {
    genericTest("test47.p");
  }

  @Test
  public void test48() throws ANTLRException, IOException {
    genericTest("test48.p");
  }

  @Test
  public void test49() throws ANTLRException, IOException {
    genericTest("test49.p");
  }

  private void genericTest(String name) throws ANTLRException, IOException {
    AttributedWriter writer = new AttributedWriter();
    writer.write(SOURCEDIR + name, new File(TARGETDIR + name), session);
    assertTrue(FileUtils.contentEquals(new File(EXPECTDIR + name), new File(TARGETDIR + name)));
  }

}
