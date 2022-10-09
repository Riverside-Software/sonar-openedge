/*
 * OpenEdge plugin for SonarQube
 * Copyright (c) 2015-2022 Riverside Software
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.v11.TypeInfoV11;
import eu.rssw.pct.elements.v12.TypeInfoV12;

/**
 * Import debug segment information from rcode.
 */
public class RCodeInfo {
  // Magic number, followed by same magic number written as little-endian
  private static final int MAGIC1 = 0x56CED309;
  private static final int MAGIC2 = 0x09D3CE56;

  // Header values
  private static final int HEADER_SIZE = 68;
  private static final int HEADER_OFFSET_MAGIC = 0;
  private static final int HEADER_OFFSET_TIMESTAMP = 4;
  private static final int HEADER_OFFSET_DIGEST = 10;
  private static final int HEADER_OFFSET_DIGEST_V12 = 22;
  private static final int HEADER_OFFSET_RCODE_VERSION = 14;
  private static final int HEADER_OFFSET_SEGMENT_TABLE_SIZE = 0x1E;
  private static final int HEADER_OFFSET_SIGNATURE_SIZE = 56;
  private static final int HEADER_OFFSET_TYPEBLOCK_SIZE = 60;
  private static final int HEADER_OFFSET_RCODE_SIZE = 64;

  // Segment table values
  private static final int SEGMENT_TABLE_OFFSET_INITIAL_VALUE_SEGMENT_OFFSET = 0;
  private static final int SEGMENT_TABLE_OFFSET_ACTION_SEGMENT_OFFSET = 4;
  private static final int SEGMENT_TABLE_OFFSET_ECODE_SEGMENT_OFFSET = 8;
  private static final int SEGMENT_TABLE_OFFSET_DEBUG_SEGMENT_OFFSET = 12;
  private static final int SEGMENT_TABLE_OFFSET_INITIAL_VALUE_SEGMENT_SIZE = 16;
  private static final int SEGMENT_TABLE_OFFSET_ACTION_SEGMENT_SIZE = 20;
  private static final int SEGMENT_TABLE_OFFSET_ECODE_SEGMENT_SIZE = 24;
  private static final int SEGMENT_TABLE_OFFSET_DEBUG_SEGMENT_SIZE = 28;
  private static final int SEGMENT_TABLE_OFFSET_IPACS_TABLE_SIZE = 32;
  private static final int SEGMENT_TABLE_OFFSET_FRAME_SEGMENT_TABLE_SIZE = 34;
  private static final int SEGMENT_TABLE_OFFSET_TEXT_SEGMENT_TABLE_SIZE = 36;

  protected ByteOrder order;
  protected int version;
  protected boolean sixtyFourBits;
  protected long timeStamp;
  protected int digestOffset;

  protected int segmentTableSize;
  protected int signatureSize;
  protected int typeBlockSize;
  protected int rcodeSize;
  protected int initialValueSegmentOffset;
  protected int initialValueSegmentSize;
  protected int debugSegmentOffset;
  protected int debugSegmentSize;
  protected int actionSegmentOffset;
  protected int actionSegmentSize;
  protected int ecodeSegmentOffset;
  protected int ecodeSegmentSize;
  protected int ipacsTableSize;
  protected int frameSegmentTableSize;
  protected int textSegmentTableSize;

  // From type block
  private boolean isClass = false;

  private ITypeInfo typeInfo;

  public RCodeInfo(InputStream input) throws InvalidRCodeException, IOException {
    this(input, new PrintStream(new NullOutputStream()));
  }

  /**
   * Parse InputStream and store debug segment information
   * 
   * @param input Has to be closed by caller
   * @param out Output stream for debug. Can be null
   * 
   * @throws InvalidRCodeException
   * @throws IOException
   */
  public RCodeInfo(InputStream input, @Nonnull PrintStream out) throws InvalidRCodeException, IOException {
    processHeader(input, out);
    processSignatureBlock(input, out);
    processSegmentTable(input, out);

    if ((initialValueSegmentOffset >= 0) && (initialValueSegmentSize > 0)) {
      long bytesRead = input.skip(initialValueSegmentOffset);
      if (bytesRead != initialValueSegmentOffset) {
        throw new InvalidRCodeException("Not enough bytes to reach initial values segment");
      }
      processInitialValueSegment(input, out);
    }

    if ((debugSegmentOffset > 0) && (debugSegmentSize > 0)) {
      long bytesRead = input.skip((long) debugSegmentOffset - initialValueSegmentSize);
      if (bytesRead != debugSegmentOffset - initialValueSegmentSize) {
        throw new InvalidRCodeException("Not enough bytes to reach debug segment");
      }
      processDebugSegment(input, out);
    }

    if (typeBlockSize > 0) {
      int skip = debugSegmentOffset > 0 ? rcodeSize - debugSegmentOffset - debugSegmentSize
          : rcodeSize - initialValueSegmentSize - debugSegmentSize;
      long bytesRead = input.skip(skip);
      if (bytesRead != skip) {
        throw new InvalidRCodeException("Not enough bytes to reach type block");
      }
      processTypeBlock(input, out);
      isClass = true;
    }

    input.close();
  }

  private final void processHeader(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] header = new byte[HEADER_SIZE];
    int bytesRead = input.read(header);
    if (bytesRead != HEADER_SIZE) {
      throw new InvalidRCodeException("Not enough bytes in header");
    }

    out.printf("%n******%nHEADER%n******%n");
    printByteBuffer(out, header);

    long magic = ByteBuffer.wrap(header, HEADER_OFFSET_MAGIC, Integer.BYTES).getInt();
    if (magic == MAGIC1) {
      order = ByteOrder.BIG_ENDIAN;
    } else if (magic == MAGIC2) {
      order = ByteOrder.LITTLE_ENDIAN;
    } else {
      throw new InvalidRCodeException("Can't find magic number");
    }

    version = ByteBuffer.wrap(header, HEADER_OFFSET_RCODE_VERSION, Short.BYTES).order(order).getShort();
    sixtyFourBits = (version & 0x4000) != 0;
    if ((version & 0x3FFF) >= 1200) {
      byte[] header2 = new byte[16];
      if (input.read(header2) != 16) {
        throw new InvalidRCodeException("Not enough bytes in OE12 header");
      }
      
      timeStamp = ByteBuffer.wrap(header, HEADER_OFFSET_TIMESTAMP, Integer.BYTES).order(order).getInt();
      digestOffset = ByteBuffer.wrap(header, HEADER_OFFSET_DIGEST_V12, Short.BYTES).order(order).getShort();
      segmentTableSize = ByteBuffer.wrap(header, HEADER_OFFSET_SEGMENT_TABLE_SIZE, Short.BYTES).order(order).getShort();
      signatureSize = ByteBuffer.wrap(header, HEADER_OFFSET_SIGNATURE_SIZE, Integer.BYTES).order(order).getInt();
      typeBlockSize = ByteBuffer.wrap(header, HEADER_OFFSET_TYPEBLOCK_SIZE, Integer.BYTES).order(order).getInt();
      rcodeSize = ByteBuffer.wrap(header2, 0xc, Integer.BYTES).order(order).getInt();
    } else if ((version & 0x3FFF) >= 1100) {
      timeStamp = ByteBuffer.wrap(header, HEADER_OFFSET_TIMESTAMP, Integer.BYTES).order(order).getInt();
      digestOffset = ByteBuffer.wrap(header, HEADER_OFFSET_DIGEST, Short.BYTES).order(order).getShort();
      segmentTableSize = ByteBuffer.wrap(header, HEADER_OFFSET_SEGMENT_TABLE_SIZE, Short.BYTES).order(order).getShort();
      signatureSize = ByteBuffer.wrap(header, HEADER_OFFSET_SIGNATURE_SIZE, Integer.BYTES).order(order).getInt();
      typeBlockSize = ByteBuffer.wrap(header, HEADER_OFFSET_TYPEBLOCK_SIZE, Integer.BYTES).order(order).getInt();
      rcodeSize = ByteBuffer.wrap(header, HEADER_OFFSET_RCODE_SIZE, Integer.BYTES).order(order).getInt();
    } else {
      throw new InvalidRCodeException("Only v11 rcode is supported");
    }
  }

  private final void processSignatureBlock(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] header = new byte[signatureSize];
    int bytesRead = input.read(header);
    if (bytesRead != signatureSize) {
      throw new InvalidRCodeException("Not enough bytes in signature block");
    }
    out.printf("%n*********%nSIGNATURE%n*********%n");
    printByteBuffer(out, header);

    int preambleSize = readAsciiEncodedNumber(header, 0, 4);
    int numElements = readAsciiEncodedNumber(header, 4, 4);
    // Version of signature block : offset 8, 4 bytes
    // Encoding : offset 12, null-terminated string

    int pos = preambleSize;
    for (int kk = 0; kk < numElements; kk++) {
      String str = readNullTerminatedString(header, pos);
      pos += str.length() + 1;

      // Datasets and temp-tables not read for now
      if (str.startsWith("DSET") || str.startsWith("TTAB")) {
        continue;
      }

      // Will probably be skipped
      // Function fn = parseProcSignature(str);
      // if ((unit.mainProcedure == null) && (fn.type == FunctionType.MAIN)) {
      // unit.mainProcedure = fn;
      // } else {
      // unit.funcs.add(fn);
      // }
    }
  }

  private final void processSegmentTable(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] header = new byte[segmentTableSize];
    int bytesRead = input.read(header);
    if (bytesRead != segmentTableSize) {
      throw new InvalidRCodeException("Not enough bytes in segment table block");
    }
    out.printf("%n*******%nSEGMENT%n*******%n");
    printByteBuffer(out, header);

    initialValueSegmentOffset = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_INITIAL_VALUE_SEGMENT_OFFSET, Integer.BYTES).order(order).getInt();
    initialValueSegmentSize = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_INITIAL_VALUE_SEGMENT_SIZE, Integer.BYTES).order(order).getInt();
    actionSegmentOffset = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_ACTION_SEGMENT_OFFSET, Integer.BYTES).order(order).getInt();
    actionSegmentSize = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_ACTION_SEGMENT_SIZE, Integer.BYTES).order(order).getInt();
    ecodeSegmentOffset = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_ECODE_SEGMENT_OFFSET, Integer.BYTES).order(order).getInt();
    ecodeSegmentSize = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_ECODE_SEGMENT_SIZE, Integer.BYTES).order(order).getInt();
    debugSegmentOffset = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_DEBUG_SEGMENT_OFFSET, Integer.BYTES).order(order).getInt();
    debugSegmentSize = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_DEBUG_SEGMENT_SIZE, Integer.BYTES).order(order).getInt();
    
    ipacsTableSize = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_IPACS_TABLE_SIZE, Short.BYTES).order(order).getShort();
    frameSegmentTableSize = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_FRAME_SEGMENT_TABLE_SIZE, Short.BYTES).order(order).getShort();
    textSegmentTableSize = ByteBuffer.wrap(header, SEGMENT_TABLE_OFFSET_TEXT_SEGMENT_TABLE_SIZE, Short.BYTES).order(order).getShort();
  }

  void processTypeBlock(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] segment = new byte[typeBlockSize];
    int bytesRead = input.read(segment);
    if (bytesRead != typeBlockSize) {
      throw new InvalidRCodeException("Not enough bytes in type block");
    }
    out.printf("%n**********%nTYPE BLOCK%n***********%n");
    printByteBuffer(out, segment);

    if ((version & 0x3FFF) >= 1200) {
      this.typeInfo = TypeInfoV12.newTypeInfo(segment, order);
    } else {
      this.typeInfo = TypeInfoV11.newTypeInfo(segment, order);
    }
  }

  private final void processInitialValueSegment(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] segment = new byte[initialValueSegmentSize];
    int bytesRead = input.read(segment);
    if (bytesRead != initialValueSegmentSize) {
      throw new InvalidRCodeException("Not enough bytes in initial value segment block");
    }
    out.printf("%n**********%nINITIAL VALUES%n***********%n");
    printByteBuffer(out, segment);
  }

  void processDebugSegment(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] segment = new byte[debugSegmentSize];
    int bytesRead = input.read(segment);
    if (bytesRead != debugSegmentSize) {
      throw new InvalidRCodeException("Not enough bytes in debug segment block");
    }
    out.printf("%n*******%nDEBUG%n*******%n");
    printByteBuffer(out, segment);
  }

  public ITypeInfo getTypeInfo() {
    return typeInfo;
  }

  public static void printByteBuffer(PrintStream writer, byte[] block) {
    StringBuilder sb = new StringBuilder();
    int pos = 0;
    while (pos < block.length) {
      if ((pos % 16) == 0) {
        writer.print(String.format("%010X | ", pos));
      }
      writer.print(String.format("%02X ", block[pos]));
      if (Character.isISOControl((char) block[pos])) {
        sb.append('.');
      } else {
        sb.append((char) block[pos]);
      }
      if ((pos > 0) && (((pos + 1) % 16) == 0)) {
        writer.println(" | " + sb.toString());
        sb = new StringBuilder();
      }
      pos++;
    }
    if ((pos % 16) != 0) {
      writer.print(Strings.repeat("   ", 16 - (pos % 16)));
      writer.println(" | " + sb.toString());
    }
  }

  /**
   * Returns r-code compiler version
   * 
   * @return Version
   */
  public long getVersion() {
    return version;
  }

  /**
   * Returns r-code timestamp (in milliseconds)
   * 
   * @return Timestamp
   */
  public long getTimeStamp() {
    return timeStamp;
  }

  public boolean is64bits() {
    return sixtyFourBits;
  }

  public boolean isClass() {
    return isClass;
  }

  public static String readNullTerminatedString(byte[] array, int offset) {
    return readNullTerminatedString(array, offset, Charset.defaultCharset());
  }

  public static String readNullTerminatedString(byte[] array, int offset, Charset charset) {
    int zz = 0;
    while ((zz + offset < array.length) && (array[zz + offset] != 0)) {
      zz++;
    }

    return charset.decode(ByteBuffer.wrap(array, offset, zz)).toString();
  }

  private static int readAsciiEncodedNumber(byte[] array, int pos, int length) throws InvalidRCodeException {
    try {
      return Integer.valueOf(new String(Arrays.copyOfRange(array, pos, pos + length)), 16);
    } catch (NumberFormatException caught) {
      throw new InvalidRCodeException(caught);
    }
  }

  public static class InvalidRCodeException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidRCodeException(String s) {
      super(s);
    }

    public InvalidRCodeException(Throwable caught) {
      super(caught);
    }
  }

  // Use OutputStream.NullOutputStream in Java 11
  private static class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {
      // Nothing
    }
    @Override
    public void write(byte[] b) throws IOException {
      // Nothing
    }
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      // Nothing
    }
  }

}
