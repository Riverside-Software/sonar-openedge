package org.prorefactor.core;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.prorefactor.core.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class ParserFailureTest {
  private RefactorSession session;

  @BeforeTest
  public void setUp() {
    Injector injector = Guice.createInjector(new UnitTestModule());
    session = injector.getInstance(RefactorSession.class);
  }

  @Test
  public void testFailure01() {
    ParseUnit unit = new ParseUnit("MESSAGE 'Hello' VIEW-AS", session);
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
    ParseUnit unit = new ParseUnit("FIND customer. FIND sp2k.plopmachin. ", session);
    try {
      unit.treeParser01();
      fail("Invalid table name, should have failed");
    } catch (ParseCancellationException caught) {
      assertTrue(unit.hasSyntaxError());
      assertNull(unit.getTopNode());
    }
  }

}
