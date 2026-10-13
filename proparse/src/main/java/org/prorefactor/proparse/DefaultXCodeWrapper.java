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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Default implementation: xcode'd streams are either replaced with a single space character (when skipXCode is true),
 * or rejected with {@link XCodedFileException}. Clear text streams are returned as is.
 */
public class DefaultXCodeWrapper implements XCodeWrapper {
  private final boolean skipXCode;

  public DefaultXCodeWrapper(boolean skipXCode) {
    this.skipXCode = skipXCode;
  }

  @Override
  public InputStream wrap(InputStream input, String fileName) throws IOException {
    var stream = new PushbackInputStream(input, 1);
    int firstByte = stream.read();
    if (isXCoded(firstByte)) {
      if (skipXCode)
        return new ByteArrayInputStream(" ".getBytes(StandardCharsets.US_ASCII));
      else
        throw new XCodedFileException(fileName);
    }
    if (firstByte != -1)
      stream.unread(firstByte);
    return stream;
  }

  private static boolean isXCoded(int firstByte) {
    return (firstByte == 0x11) || (firstByte == 0x13);
  }
}
