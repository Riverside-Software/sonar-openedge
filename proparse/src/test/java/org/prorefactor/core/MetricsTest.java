/********************************************************************************
 * Copyright (c) 2015-2026 Riverside Software
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
import java.io.IOException;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class MetricsTest extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void test01() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/data/preprocessor/preprocessor14.p"), session);
    unit.parse();
    assertFalse(unit.hasSyntaxError());

    assertEquals(unit.getMetrics().getLoc(), 2);
    assertEquals(unit.getMetrics().getComments(), 6);
    assertEquals(unit.getParseTreeSize(), 50);
    assertEquals(unit.getJPNodeSize(), 22);
  }

  @Test
  public void test02() {
    ParseUnit unit = getParseUnit(new File("src/test/resources/data/inc3.i"), session);
    unit.lexAndGenerateMetrics();

    assertEquals(unit.getMetrics().getLoc(), 1);
    assertEquals(unit.getMetrics().getComments(), 2);
  }

}
