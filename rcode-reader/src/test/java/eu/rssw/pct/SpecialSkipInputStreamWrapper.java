/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2026 Riverside Software
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
package eu.rssw.pct;

import java.io.IOException;
import java.io.InputStream;

/**
 * InputStream wrapper for issue #1005. Skip() method never returns the full number of bytes asked.
 */
public class SpecialSkipInputStreamWrapper extends InputStream {
  private final InputStream delegate;
  private int value = 1;

  public SpecialSkipInputStreamWrapper(InputStream stream) {
    this.delegate = stream;
  }

  @Override
  public long skip(long n) throws IOException {
    if (n < value) {
      delegate.read(new byte[(int) n]);
      return n;
    } else {
      delegate.read(new byte[value]);
      int oldVal = value;
      value = value >= 4 ? 1 : value++;
      return oldVal;
    }
  }

  // Just pass all other calls
  // *************************

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return delegate.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return delegate.read(b, off, len);
  }

  @Override
  public int available() throws IOException {
    return delegate.available();
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }

  @Override
  public synchronized void mark(int readlimit) {
    delegate.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return delegate.markSupported();
  }

  @Override
  public synchronized void reset() throws IOException {
    delegate.reset();
  }
}