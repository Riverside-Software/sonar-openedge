/********************************************************************************
 * Copyright (c) 2003-2015 John Green
 * Copyright (c) 2015-2020 Riverside Software
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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.File;

import org.prorefactor.core.schema.ISchema;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AliasesTest {
  private RefactorSession session;
  // Just a shortcut for schema
  private ISchema schema;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
    schema = session.getSchema();
    schema.createAlias("dictdb", "sports2000");
    schema.createAlias("foo", "sports2000");
  }

  @Test
  public void test01() {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/aliases.p"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  @Test
  public void test02() {
    assertNotNull(schema.lookupDatabase("dictdb"));
    assertNotNull(schema.lookupDatabase("foo"));
    assertNull(schema.lookupDatabase("dictdb2"));
    assertNotNull(schema.lookupTable("_file"));
    assertNotNull(schema.lookupTable("dictdb", "_file"));
    assertNull(schema.lookupTable("dictdb", "_file2"));
  }

  @Test
  public void test03() {
    assertNull(schema.lookupDatabase("test"));
    schema.createAlias("test", "sports2000");
    assertNotNull(schema.lookupDatabase("test"));
    assertNotNull(schema.lookupTable("test", "customer"));
    schema.deleteAlias("test");
    assertNull(schema.lookupDatabase("test"));
  }

  @Test
  public void test04() {
    assertNotNull(schema.lookupField("sports2000", "customer", "custnum"));
    assertNotNull(schema.lookupField("dictdb", "customer", "custnum"));
    assertNotNull(schema.lookupField("foo", "customer", "custnum"));
  }

  @Test
  public void test05()  {
    // Issue #27
    assertNotNull(schema.lookupTable("salesrep"), "Salesrep table exists");
    assertNull(schema.lookupTable("salesrepp"), "Typo, table doesn't exist");
    assertNull(schema.lookupTable("salesrep.salesrep"), "Table salesrep.salesrep doesn't exist (is a field)");

    assertNotNull(schema.lookupTable("_file"), "Table _file exists");
    assertNotNull(schema.lookupTable("foo._file"), "Table foo._file exists");
    assertNotNull(schema.lookupTable("sports2000._file"), "Table sports2000._file exists");
  }
}
