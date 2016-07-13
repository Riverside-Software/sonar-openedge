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

import org.prorefactor.core.schema.ISchema;
import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

public class AliasesT extends TestCase {
  private RefactorSession session;
  // Just a shortcut for schema
  private ISchema schema;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    Injector injector = Guice.createInjector(new UnitTestSports2000Module());
    session = injector.getInstance(RefactorSession.class);
    schema = session.getSchema();
    schema.createAlias("dictdb", "sports2000");
    schema.createAlias("foo", "sports2000");
  }

  public void test01() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/aliases.p"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  public void test02() {
    assertNotNull(schema.lookupDatabase("dictdb"));
    assertNotNull(schema.lookupDatabase("foo"));
    assertNull(schema.lookupDatabase("dictdb2"));
    assertNotNull(schema.lookupTable("_file"));
    assertNotNull(schema.lookupTable("dictdb", "_file"));
    assertNull(schema.lookupTable("dictdb", "_file2"));
  }

  public void test03() {
    assertNull(schema.lookupDatabase("test"));
    schema.createAlias("test", "sports2000");
    assertNotNull(schema.lookupDatabase("test"));
    assertNotNull(schema.lookupTable("test", "customer"));
    schema.deleteAlias("test");
    assertNull(schema.lookupDatabase("test"));
  }

  public void test04() {
    assertNotNull(schema.lookupField("sports2000", "customer", "custnum"));
    assertNotNull(schema.lookupField("dictdb", "customer", "custnum"));
    assertNotNull(schema.lookupField("foo", "customer", "custnum"));
  }

  public void test05() throws Exception {
    // Issue #27
    assertNotNull("Salesrep table exists", schema.lookupTable("salesrep"));
    assertNull("Typo, table doesn't exist", schema.lookupTable("salesrepp"));
    assertNull("Table salesrep.salesrep doesn't exist (is a field)", schema.lookupTable("salesrep.salesrep"));

    assertNotNull("Table _file exists", schema.lookupTable("_file"));
    assertNotNull("Table foo._file exists", schema.lookupTable("foo._file"));
    assertNotNull("Table sports2000._file exists", schema.lookupTable("sports2000._file"));
  }
}
