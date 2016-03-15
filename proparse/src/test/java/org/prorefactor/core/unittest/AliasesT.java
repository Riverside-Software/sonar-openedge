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

}
