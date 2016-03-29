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

import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;

import com.google.inject.Guice;
import com.google.inject.Injector;

import junit.framework.TestCase;

public class AliasesT extends TestCase {
  private RefactorSession session;

  @Override
  public void setUp() throws Exception {
    super.setUp();

    Injector injector = Guice.createInjector(new UnitTestSports2000Module());
    session = injector.getInstance(RefactorSession.class);
    session.getSchema().aliasCreate("dictdb", "sports2000");
    session.getSchema().aliasCreate("foo", "sports2000");
  }

  public void test01() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/aliases.p"), session);
    assertNull(unit.getTopNode());
    assertNull(unit.getRootScope());
    unit.treeParser01();
    assertNotNull(unit.getTopNode());
    assertNotNull(unit.getRootScope());
  }

  public void test02() throws Exception {
    assertNotNull(session.getSchema().lookupDatabase("dictdb"));
    assertNotNull(session.getSchema().lookupDatabase("foo"));
    assertNull(session.getSchema().lookupDatabase("dictdb2"));
    assertNotNull(session.getSchema().lookupTable("_file"));
    assertNotNull(session.getSchema().lookupTable("dictdb", "_file"));
    assertNull(session.getSchema().lookupTable("dictdb", "_file2"));
  }

  public void test03() throws Exception {
    assertNull(session.getSchema().lookupDatabase("test"));
    session.getSchema().aliasCreate("test", "sports2000");
    assertNotNull(session.getSchema().lookupDatabase("test"));
    assertNotNull(session.getSchema().lookupTable("test", "customer"));
    session.getSchema().aliasDelete("test");
    assertNull(session.getSchema().lookupDatabase("test"));
  }

  public void test04() throws Exception {
    assertNotNull(session.getSchema().lookupField("sports2000", "customer", "custnum"));
    assertNotNull(session.getSchema().lookupField("dictdb", "customer", "custnum"));
    assertNotNull(session.getSchema().lookupField("foo", "customer", "custnum"));
  }
}
