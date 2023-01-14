/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2023 Riverside Software
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
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Read and extract content of a Progress Library file.
 * 
 * Note: calls to ByteBuffer.position(int) are changed to Buffer.position() in order to keep compatibility with Java 8.
 * This can be removed later.
 */
public class PLReader {
  private static final int MAGIC = 0xd707;
  private static final int MAGIC_V11 = 0xd70b;
  private static final int MAGIC_V11_MM = 0xd70c;
  private static final int ENCODING_OFFSET = 0x02;
  private static final int ENCODING_SIZE = 20;
  private static final int FILE_LIST_OFFSET = 0x1e;
  private static final int FILE_LIST_OFFSET_V11 = 0x22;

  private Path library;
  private List<FileEntry> files = null;
  private boolean mm = false;

  public PLReader(Path file) {
    this.library = file;
  }

  /**
   * Return entries contained in this procedure library
   * 
   * @throws RuntimeException If file is not a valid procedure library
   */
  public List<FileEntry> getFileList() {
    if (files == null)
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

  public boolean isMemoryMapped() {
    return mm;
  }

  public InputStream getInputStream(FileEntry entry) throws IOException {
    ByteBuffer buf = null;
    try (SeekableByteChannel channel = Files.newByteChannel(library, StandardOpenOption.READ)) {
      buf = ByteBuffer.allocate(entry.getSize());
      channel.position(entry.getOffset());
      channel.read(buf);
    }

    return new ByteBufferBackedInputStream((ByteBuffer) ((Buffer) buf).position(0));
  }

  private void readFileList() {
    ILibVersion version;
    try (SeekableByteChannel channel = Files.newByteChannel(library, StandardOpenOption.READ)) {
      ByteBuffer magic = ByteBuffer.allocate(2);
      channel.read(magic);
      int magicVal = magic.getShort(0) & 0xffff;
      if (magicVal == MAGIC) {
        version = new Version10();
      } else if (magicVal == MAGIC_V11) {
        version = new Version11();
      } else if (magicVal == MAGIC_V11_MM) {
        version = new Version11();
        mm = true;
      } else {
        throw new InvalidLibraryException("Invalid magic number");
      }

      Charset charset = getCharset(channel);
      int offset = getTOCOffset(channel, version);
      files = new ArrayList<>();
      FileEntry fe = null;
      while ((fe = readEntry(channel, offset, charset, version)) != null) {
        if (fe.isValid())
          files.add(fe);
        offset += fe.getTocSize();
      }
    } catch (IOException caught) {
      throw new InvalidLibraryException(caught);
    }
  }

  private Charset getCharset(SeekableByteChannel channel) throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(ENCODING_SIZE);
    channel.position(ENCODING_OFFSET);
    if (channel.read(buf) != ENCODING_SIZE)
      throw new InvalidLibraryException("Can't read charset");

    StringBuilder sbEncoding = new StringBuilder();
    int zz = 0;
    while ((zz < ENCODING_SIZE) && (buf.get(zz) != 0)) {
      sbEncoding.append((char) buf.get(zz++));
    }
    try {
      return Charset.forName(sbEncoding.toString());
    } catch (IllegalArgumentException iae) {
      return StandardCharsets.US_ASCII;
    }
  }

  private int getTOCOffset(SeekableByteChannel channel, ILibVersion version) throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(4);
    channel.position(version.getFileListOffset());
    if (channel.read(buf) != 4)
      throw new InvalidLibraryException("Can't read table of contents");

    return buf.getInt(0);
  }

  private FileEntry readEntry(SeekableByteChannel channel, int offset, Charset charset, ILibVersion version)
      throws IOException {
    ByteBuffer buf1 = ByteBuffer.allocate(1);
    channel.position(offset);
    channel.read(buf1);

    if (buf1.get(0) == (byte) 0xFE) {
      while ((channel.read((ByteBuffer) ((Buffer) buf1).position(0)) != -1) && (buf1.get(0) != (byte) 0xFF)) {
        // Just read until EOF or next 0xFF
      }
      return new FileEntry((int) (channel.position() - offset - 1));
    } else if (buf1.get(0) == (byte) 0xFF) {
      channel.read((ByteBuffer) ((Buffer) buf1).position(0));
      int fNameSize = buf1.get(0) & 0xFF;
      if (fNameSize == 0)
        return new FileEntry(version.getEmptyEntrySize());

      ByteBuffer buf2 = ByteBuffer.allocate(fNameSize);
      channel.read(buf2);
      String fName = charset.decode((ByteBuffer) ((Buffer) buf2).position(0)).toString();

      ByteBuffer buf3 = ByteBuffer.allocate(version.getEntryMinSize());
      channel.read(buf3);
      int fileOffset = buf3.getInt(version.getEntryFileNameOffset());
      int fileSize = buf3.getInt(version.getEntryFileSizeOffset());
      long added = buf3.getInt(version.getEntryFileAddedOffset());
      long modified = buf3.getInt(version.getEntryFileModifiedOffset());

      int tocSize = (buf3.get(version.getEntryMinSize() - 1) == 0 ? (version.getEntryMinSize() + 2)
          : (version.getEntryMinSize() + 1)) + fNameSize;
      return new FileEntry(fName, modified, added, fileOffset, fileSize, tocSize);
    } else {
      return null;
    }
  }


  public static class InvalidLibraryException extends RuntimeException {
    private static final long serialVersionUID = -5636414187086107273L;

    public InvalidLibraryException(String msg) {
      super(msg);
    }

    public InvalidLibraryException(Throwable cause) {
      super(cause);
    }
  }

  private interface ILibVersion {
    // File list offset in header
    int getFileListOffset();
    // Size of empty file list entries
    int getEmptyEntrySize();
    // File list entry minimum size
    int getEntryMinSize();
    // File name offset in entry
    int getEntryFileNameOffset();
    // File size offset in entry
    int getEntryFileSizeOffset();
    // File added timestamp offset in entry
    int getEntryFileAddedOffset();
    // File modified timestamp offset in entry
    int getEntryFileModifiedOffset();
  }

  private class Version10 implements ILibVersion {
    @Override
    public int getFileListOffset() {
      return FILE_LIST_OFFSET;
    }

    @Override
    public int getEmptyEntrySize() {
      return 29;
    }

    @Override
    public int getEntryMinSize() {
      return 28;
    }

    @Override
    public int getEntryFileNameOffset() {
      return 2;
    }

    @Override
    public int getEntryFileSizeOffset() {
      return 7;
    }

    @Override
    public int getEntryFileAddedOffset() {
      return 11;
    }

    @Override
    public int getEntryFileModifiedOffset() {
      return 15;
    }
  }

  private class Version11 implements ILibVersion {
    @Override
    public int getFileListOffset() {
      return FILE_LIST_OFFSET_V11;
    }

    @Override
    public int getEmptyEntrySize() {
      return 49;
    }

    @Override
    public int getEntryMinSize() {
      return 48;
    }

    @Override
    public int getEntryFileNameOffset() {
      return 6;
    }

    @Override
    public int getEntryFileSizeOffset() {
      return 11;
    }

    @Override
    public int getEntryFileAddedOffset() {
      return 15;
    }

    @Override
    public int getEntryFileModifiedOffset() {
      return 19;
    }
  }

  private class ByteBufferBackedInputStream extends InputStream {
    private ByteBuffer buf;

    public ByteBufferBackedInputStream(ByteBuffer buf) {
      this.buf = buf;
    }

    @Override
    public int read() throws IOException {
      if (!buf.hasRemaining()) {
        return -1;
      }
      return buf.get() & 0xFF;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
      if (!buf.hasRemaining()) {
        return -1;
      }

      len = Math.min(len, buf.remaining());
      buf.get(bytes, off, len);
      return len;
    }
  }
}
