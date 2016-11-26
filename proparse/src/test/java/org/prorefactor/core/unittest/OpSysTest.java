package org.prorefactor.core.unittest;

import static org.testng.Assert.assertNotNull;

import java.io.File;

import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.unittest.util.UnitTestSports2000Module;
import org.prorefactor.core.unittest.util.UnitTestSports2000UnixModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class OpSysTest {
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";

  @Test
  public void testUnix() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestSports2000UnixModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);

    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char.p"), session);
    pu.treeParser01();
    assertNotNull(pu.getTopNode());
    assertNotNull(pu.getRootScope());
  }

  @Test(expectedExceptions = { ProparseRuntimeException.class })
  public void testWindows() throws Exception {
    Injector injector = Guice.createInjector(new UnitTestSports2000Module());
    RefactorSession session = injector.getInstance(RefactorSession.class);

    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char.p"), session);
    pu.treeParser01();
  }

}
