package org.sonar.plugins.openedge.sensor;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Filter specific characters which can be found in XML XREF files, especially CHR(1), CHR(2) and CHR(4).
 * Those characters are used in ADM2 applications, and are hard-coded in some procedures.
 */
public class InvalidXMLFilterStream extends FilterInputStream {
  private final Set<Integer> bytes;

  protected InvalidXMLFilterStream(Set<Integer> skippedBytes, InputStream in) {
    super(in);
    this.bytes = skippedBytes;
  }

  @Override
  public int read() throws IOException {
    // Discard any 0x01, 0x02 and 0x04 character from the stream
    int xx = super.read();
    if ((xx == 0x01) || (xx == 0x02) || (xx == 0x04)) {
      return read();
    }

    return xx;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int xx = super.read(b, off, len);
    int zz = off;
    while (zz < off + xx) {
      if (bytes.contains((int) b[zz])) {
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