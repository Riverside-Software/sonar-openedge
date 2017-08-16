/*
 * RCode library - OpenEdge plugin for SonarQube
 * Copyright (C) 2017 Riverside Software
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for reading and extracting contents of a Progress Library file.
 * 
 * @author <a href="mailto:g.querret+PCT@gmail.com">Gilles QUERRET</a>
 */
public class PLReader {
  private static final int MAGIC_V11 = 0xd70b;
  private static final int ENCODING_OFFSET = 0x02;
  private static final int ENCODING_SIZE = 20;
  private static final int FILE_LIST_OFFSET_V11 = 0x22;

  private File pl;
  private List<FileEntry> files = null;

  public PLReader(File file) {
    String name = file.getPath();
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
      sm.checkRead(name);
    }

    this.pl = file;
  }

  /**
   * Returns entries contained in this procedure library
   * 
   * @throws RuntimeException If file is not a valid procedure library
   */
  public List<FileEntry> getFileList() {
    if (this.files == null)
      readFileList();
    return files;
  }

  public FileEntry getEntry(String name) {
    for (FileEntry entry : getFileList()) {
      if (entry.getFileName().equals(name))
        return entry;
    }

    return null;
  }

  private void readFileList() {
    try (RandomAccessFile raf = new RandomAccessFile(pl, "r")) {
      FileChannel fc = raf.getChannel();
      ByteBuffer magic = ByteBuffer.allocate(2);
      fc.read(magic);
      if ((magic.getShort(0) & 0xffff) != MAGIC_V11)
        throw new RuntimeException("Not a valid PL file");

      Charset charset = getCharset(fc);
      int offset = getTOCOffset(fc);
      files = new ArrayList<>();
      FileEntry fe = null;
      while ((fe = readEntry(fc, offset, charset)) != null) {
        if (fe.isValid())
          files.add(fe);
        offset += fe.getTocSize();
      }
    } catch (IOException caught) {
      throw new RuntimeException(caught);
    }
  }

  public InputStream getInputStream(FileEntry fe) throws IOException {
    ByteBuffer bb = null;
    try (RandomAccessFile raf = new RandomAccessFile(pl, "r")) {
      FileChannel fc = raf.getChannel();
      bb = ByteBuffer.allocate(fe.getSize());
      fc.read(bb, fe.getOffset());
    }

    return new ByteArrayInputStream(bb.array());
  }

  private Charset getCharset(FileChannel fc) throws IOException {
    ByteBuffer bEncoding = ByteBuffer.allocate(ENCODING_SIZE);
    if (fc.read(bEncoding, ENCODING_OFFSET) != ENCODING_SIZE)
      throw new RuntimeException("Invalid PL file");
    bEncoding.position(0);
    StringBuilder sbEncoding = new StringBuilder();
    int zz = 0;
    while ((zz < 20) && (bEncoding.get(zz) != 0)) {
      sbEncoding.append((char) bEncoding.get(zz++));
    }
    try {
      return Charset.forName(sbEncoding.toString());
    } catch (IllegalArgumentException iae) {
      return Charset.forName("US-ASCII");
    }
  }

  private int getTOCOffset(FileChannel fc) throws IOException {
    ByteBuffer bTOC = ByteBuffer.allocate(4);
    if (fc.read(bTOC, FILE_LIST_OFFSET_V11) != 4)
      throw new RuntimeException("Invalid PL file");
    return bTOC.getInt(0);
  }

  private FileEntry readEntry(FileChannel fc, int offset, Charset charset) throws IOException {
    ByteBuffer b1 = ByteBuffer.allocate(1);
    fc.read(b1, offset);

    if (b1.get(0) == (byte) 0xFE) {
      boolean stop = false;
      int zz = 0;
      while (!stop) {
        b1.position(0);
        int kk = fc.read(b1, offset + ++zz);
        stop = (kk == -1) || (b1.get(0) == (byte) 0xFF);
      }

      return new FileEntry(zz);
    } else if (b1.get(0) == (byte) 0xFF) {
      b1.position(0);
      fc.read(b1, offset + 1);
      int fNameSize = (int) b1.get(0) & 0xFF;
      if (fNameSize == 0)
        return new FileEntry(29);
      ByteBuffer b2 = ByteBuffer.allocate(fNameSize);
      fc.read(b2, offset + 2);
      b2.position(0);
      String fName = charset.decode(b2).toString();
      ByteBuffer b3 = ByteBuffer.allocate(48); // Ou 47
      fc.read(b3, offset + 2 + fNameSize);
      int fileOffset = b3.getInt(6); // 7
      int fileSize = b3.getInt(11); // 12
      long added = b3.getInt(15) * 1000L; // 16
      long modified = b3.getInt(19) * 1000L; // 20

      int tocSize = (b3.get(47) == 0 ? 50 : 49) + fNameSize;
      return new FileEntry(fName, modified, added, fileOffset, fileSize, tocSize);
    } else {
      return null;
    }

  }
}
