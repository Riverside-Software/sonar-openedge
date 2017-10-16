package org.prorefactor.core.unittest;

import java.io.File;
import java.io.UncheckedIOException;

import org.prorefactor.core.ProparseRuntimeException;
import org.prorefactor.core.unittest.util.UnitTestBackslashModule;
import org.prorefactor.core.unittest.util.UnitTestModule;
import org.prorefactor.refactor.RefactorSession;
import org.prorefactor.treeparser.ParseUnit;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import antlr.ANTLRException;

public class OpSysTest {
  private final static boolean IS_WINDOWS = (System.getenv("windir") != null);
  private final static String SRC_DIR = "src/test/resources/data/bugsfixed";

  @Test
  public void testBackslashNoEscape() throws ANTLRException {
    // Backslash not considered an escape character on Windows, so it has to fail on Windows
    // UNIX test not executed
    if (!IS_WINDOWS)
      return;
    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char.p"), session);
    try {
      pu.treeParser01();
      Assert.fail("Should have failed");
    } catch (ProparseRuntimeException caught) {

    }
  }

  @Test
  public void testBackslashEscape() throws ANTLRException {
    // Backslash considered an escape character on Windows, so it shouldn't fail on both Windows and Unix
    Injector injector = Guice.createInjector(new UnitTestBackslashModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char.p"), session);
    pu.treeParser01();
  }

  @Test
  public void testBackslashInIncludeWindows() throws ANTLRException {
    // Backslash considered an escape character on Windows, so include file will fail
    if (!IS_WINDOWS)
      return;

    Injector injector = Guice.createInjector(new UnitTestBackslashModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char2.p"), session);
    try {
      pu.treeParser01();
      Assert.fail("Should have failed");
    } catch (UncheckedIOException caught) {

    }
  }

  @Test
  public void test2BackslashInIncludeWindows() throws ANTLRException {
    // Backslash not considered an escape character on Windows, so include file is OK (standard behavior)
    if (!IS_WINDOWS)
      return;

    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char2.p"), session);
    pu.treeParser01();
  }

  @Test
  public void testBackslashInIncludeLinux() throws ANTLRException {
    // Always fail on Linux
    if (IS_WINDOWS)
      return;

    Injector injector = Guice.createInjector(new UnitTestModule());
    RefactorSession session = injector.getInstance(RefactorSession.class);
    ParseUnit pu = new ParseUnit(new File(SRC_DIR, "escape_char2.p"), session);
    try {
      pu.treeParser01();
      Assert.fail("Should have failed");
    } catch (UncheckedIOException caught) {

    }
  }

}
