package org.prorefactor.proparse;

import java.io.IOException;
import java.io.InputStream;

public interface XCodeWrapper {
  InputStream wrap(InputStream input, String fileName) throws IOException;
}
