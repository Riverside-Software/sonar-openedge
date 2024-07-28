/********************************************************************************
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
package org.prorefactor.proparse;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.testng.annotations.Test;

import eu.rssw.pct.elements.ITypeInfo;

public class DotNetCatalogTest {

  @Test
  public void testDotNetCatalog() throws IOException {
    RefactorSession session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
    assertNull( session.getTypeInfo("Microsoft.Win32.Registry"));
    // Inject content of dotnet.json
    try (Reader reader = new FileReader("src/test/resources/dotnet.json")) {
      session.injectClassesFromDotNetCatalog(reader);
    }
    ITypeInfo info  = session.getTypeInfo("Microsoft.Win32.Registry");
    assertNotNull(info);
    assertEquals(info.getMethods().size(), 3);
    assertEquals(info.getProperties().size(), 0);
  }

}
