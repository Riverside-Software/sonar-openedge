package org.prorefactor.treeparser;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.prorefactor.proparse.support.IProparseEnvironment;

public abstract class AbstractProparseTest {

  public ParseUnit getParseUnit(File file, IProparseEnvironment session) {
    return new ParseUnit(file, file.getPath(), session, StandardCharsets.UTF_8);
  }

  public ParseUnit getParseUnit(String source, IProparseEnvironment session) {
    return new ParseUnit(source, "unnamed.p", session);
  }

}
