/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2021 Riverside Software
 * contact AT riverside DASH software DOT fr
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.openedge.sensor;

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