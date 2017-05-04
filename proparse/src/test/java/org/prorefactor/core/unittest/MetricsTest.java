/*******************************************************************************
 * Copyright (c) 2016 Gilles Querret
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gilles Querret - initial API and implementation and/or initial documentation
 *******************************************************************************/ 
package org.prorefactor.core.unittest;

import static org.testng.Assert.assertEquals;

import java.io.File;

import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class MetricsTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void test01() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/include.p"), session);
    unit.treeParser01();

    assertEquals(unit.getMetrics().getLoc(), 2);
    assertEquals(unit.getMetrics().getComments(), 6);
  }

  @Test
  public void test02() throws Exception {
    ParseUnit unit = new ParseUnit(new File("src/test/resources/data/inc3.i"), session);
    unit.lexAndGenerateMetrics();

    assertEquals(unit.getMetrics().getLoc(), 1);
    assertEquals(unit.getMetrics().getComments(), 2);
  }

}
