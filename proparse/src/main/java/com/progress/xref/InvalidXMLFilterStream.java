/********************************************************************************
 * Copyright (c) 2015-2022 Riverside Software
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

package com.progress.xref;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Filter specific characters which can be found in XML XREF files, especially CHR(1), CHR(2) and CHR(4).
 * Those characters are used in ADM2 applications, and are hard-coded in some procedures.
 */
public class InvalidXMLFilterStream extends FilterInputStream {

  public InvalidXMLFilterStream(InputStream in) {
    super(in);
  }

  @Override
  public int read() throws IOException {
    // Discard any non-printable character (except CR, LF and TAB) from the stream
    int xx = super.read();
    if ((xx >= 0x01) && (xx <= 0x1F) && (xx != 0x09) && (xx != 0x0A) && (xx != 0x0D)) {
      return read();
    }

    return xx;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int xx = super.read(b, off, len);
    int zz = off;
    while (zz < off + xx) {
      if ((b[zz] >= 0x01) && (b[zz] <= 0x1F) && (b[zz] != 0x09) && (b[zz] != 0x0A) && (b[zz] != 0x0D)) {
        // Shift all subsequent bytes by one position left
        for (int zz2 = zz; zz2 < off + xx - 1; zz2++) {
          b[zz2] = b[zz2 + 1];
        }
        // One less character read
        xx--;
      } else {
        zz++;
      }
    }
    return xx;
  }
}