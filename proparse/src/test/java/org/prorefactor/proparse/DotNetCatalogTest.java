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
package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.FileReader;
import java.io.IOException;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.Test;

public class DotNetCatalogTest {

  @Test
  public void testDotNetCatalog01() throws IOException {
    var session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    assertNull(session.getTypeInfo("Microsoft.Win32.Registry"));
    // Inject content of dotnet.json
    try (var reader = new FileReader("src/test/resources/dotnet.json")) {
      session.injectClassesFromDotNetCatalog(reader);
    }
    var info = session.getTypeInfo("Microsoft.Win32.Registry");
    assertNotNull(info);
    assertEquals(info.getMethods().size(), 3);
    assertEquals(info.getProperties().size(), 0);
  }

  @Test
  public void testDotNetCatalog02() throws IOException {
    // Same, but use static method from RefactorSession
    try (var reader = new FileReader("src/test/resources/dotnet.json")) {
      var list = RefactorSession.getClassesFromDotNetCatalog(reader);

      var info01 = list.stream() //
        .filter(it -> "Microsoft.Win32.Registry".equals(it.getTypeName())) //
        .findFirst();
      assertTrue(info01.isPresent());
      assertEquals(info01.get().getMethods().size(), 3);
      assertEquals(info01.get().getProperties().size(), 0);

      var info02 = list.stream() //
        .filter(it -> "System.Environment+SpecialFolder".equals(it.getTypeName())) //
        .findFirst();
      assertTrue(info02.isPresent());
      assertEquals(info02.get().getMethods().size(), 0);
      assertEquals(info02.get().getProperties().size(), 0);
      assertEquals(info02.get().getVariables().size(), 47);
    }
  }

}
