package org.prorefactor.core;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.util.SportsSchema;
import org.prorefactor.core.util.UnitTestProparseSettings;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.AbstractProparseTest;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ParserFailureTest extends AbstractProparseTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() throws IOException {
    session = new RefactorSession(new UnitTestProparseSettings(), new SportsSchema());
  }

  @Test
  public void testFailure01() {
    ParseUnit unit = getParseUnit("MESSAGE 'Hello' VIEW-AS", session);
    try {
      unit.treeParser01();
      fail("Missing keyword, should have failed");
    } catch (ParseCancellationException caught) {
      assertTrue(unit.hasSyntaxError());
      assertNull(unit.getTopNode());
    }
  }

  @Test
  public void testFailure02() {
    ParseUnit unit = getParseUnit("FIND customer. FIND sp2k.plopmachin. ", session);
    try {
      unit.treeParser01();
      fail("Invalid table name, should have failed");
    } catch (ParseCancellationException caught) {
      assertTrue(unit.hasSyntaxError());
      assertNull(unit.getTopNode());
    }
  }

}
