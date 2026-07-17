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
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.prorefactor.core.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IMethodElement;
import eu.rssw.pct.elements.IParameter;
import eu.rssw.pct.elements.ITypeInfo;
import eu.rssw.pct.elements.ParameterMode;
import eu.rssw.pct.elements.fixed.MethodElement;
import eu.rssw.pct.elements.fixed.Parameter;
import eu.rssw.pct.elements.v11.TypeInfoV11;
import eu.rssw.pct.elements.v12.TypeInfoV12;

/**
 * Extract rcode information
 */
public class RCodeInfo {
  private static final Logger LOGGER = LoggerFactory.getLogger(RCodeInfo.class);

  private static final String BUILTIN_CLASSES_PREFIX = "Progress.Lang.";

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

  // IVS
  private static final int IVS_CRC_OFFSET_V10 = 0x6E;
  private static final int IVS_CRC_OFFSET_V11 = 0xA4;
  private static final int IVS_CRC_OFFSET_V12 = 0xAE;
  private static final int IVS_GLOBAL_SHARED_OFFSET = 0x08;
  private static final int IVS_SHARED_OFFSET = 0x10;
  private static final int IVS_OO_INFO_OFFSET = 0x68;

  protected ByteOrder order;
  protected int version;
  protected boolean sixtyFourBits;
  protected long timeStamp;
  protected int digestOffset;
  private long crc;
  private String digest;

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

  protected int textSegmentOffset;
  protected int textSegmentSize;
  protected Charset textSegmentCharset;

  protected int attributeElementOffset;
  protected int referencedTypesOffset;
  protected int attributeElementCount;
  protected int olSigList;
  protected int typeTableSize;
  
  // From type block
  private boolean isClass = false;

  private ITypeInfo typeInfo;
  private IMethodElement mainBlock;

  private List<String> newGlobalSharedVars;
  private List<String> newSharedVars;
  private List<String> refdClasses;
  private List<String> refdMethods;
  private List<String> refdProperties;

  public RCodeInfo(InputStream input) throws InvalidRCodeException, IOException {
    this(input, null);
  }

  /**
   * Parse InputStream and store debug segment information
   * 
   * @param input Has to be closed by caller
   * @param out   Output stream for debug. Can be null
   * 
   * @throws InvalidRCodeException
   * @throws IOException
   */
  public RCodeInfo(InputStream input, PrintStream out) throws InvalidRCodeException, IOException {
    try {
      processHeader(input, out);
      processSignatureBlock(input, out);
      processSegmentTable(input, out);

      var rcodeBlock = input.readNBytes(rcodeSize);
      readDigest(rcodeBlock);
      if (typeBlockSize > 0) {
        processTypeBlock(input.readNBytes(typeBlockSize), out);
      }
      if ((version & 0x3FFF) >= 1100) {
        processRCodeBlock(rcodeBlock, out);
      }
    } catch (IndexOutOfBoundsException caught) {
      // Prevent RuntimeException from bubbling up
      throw new InvalidRCodeException(caught);
    }
  }

  private final void processHeader(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] header = input.readNBytes(HEADER_SIZE);

    if (out != null) {
      out.printf("%n******%nHEADER%n******%n");
      printByteBuffer(out, header);
    }

    long magic = ByteBuffer.wrap(header, HEADER_OFFSET_MAGIC, Integer.BYTES).getInt();
    if (magic == MAGIC1) {
      order = ByteOrder.BIG_ENDIAN;
    } else if (magic == MAGIC2) {
      order = ByteOrder.LITTLE_ENDIAN;
    } else {
      LOGGER.error("Invalid magic number in rcode");
      throw new InvalidRCodeException("Can't find magic number");
    }

    version = ByteBuffer.wrap(header, HEADER_OFFSET_RCODE_VERSION, Short.BYTES).order(order).getShort();
    sixtyFourBits = (version & 0x4000) != 0;
    if ((version & 0x3FFF) >= 1200) {
      byte[] extraHeaderSegmentOE12 = input.readNBytes(16);
      timeStamp = ByteBuffer.wrap(header, HEADER_OFFSET_TIMESTAMP, Integer.BYTES).order(order).getInt();
      digestOffset = ByteBuffer.wrap(header, HEADER_OFFSET_DIGEST_V12, Short.BYTES).order(order).getShort();
      segmentTableSize = ByteBuffer.wrap(header, HEADER_OFFSET_SEGMENT_TABLE_SIZE, Short.BYTES).order(order).getShort();
      signatureSize = ByteBuffer.wrap(header, HEADER_OFFSET_SIGNATURE_SIZE, Integer.BYTES).order(order).getInt();
      typeBlockSize = ByteBuffer.wrap(header, HEADER_OFFSET_TYPEBLOCK_SIZE, Integer.BYTES).order(order).getInt();
      rcodeSize = ByteBuffer.wrap(extraHeaderSegmentOE12, 0xc, Integer.BYTES).order(order).getInt();
    } else if ((version & 0x3FFF) >= 1100) {
      timeStamp = ByteBuffer.wrap(header, HEADER_OFFSET_TIMESTAMP, Integer.BYTES).order(order).getInt();
      digestOffset = ByteBuffer.wrap(header, HEADER_OFFSET_DIGEST, Short.BYTES).order(order).getShort();
      segmentTableSize = ByteBuffer.wrap(header, HEADER_OFFSET_SEGMENT_TABLE_SIZE, Short.BYTES).order(order).getShort();
      signatureSize = ByteBuffer.wrap(header, HEADER_OFFSET_SIGNATURE_SIZE, Integer.BYTES).order(order).getInt();
      typeBlockSize = ByteBuffer.wrap(header, HEADER_OFFSET_TYPEBLOCK_SIZE, Integer.BYTES).order(order).getInt();
      rcodeSize = ByteBuffer.wrap(header, HEADER_OFFSET_RCODE_SIZE, Integer.BYTES).order(order).getInt();
    } else if ((version & 0x3FFF) >= 1000) {
      timeStamp = ByteBuffer.wrap(header, HEADER_OFFSET_TIMESTAMP, Integer.BYTES).order(order).getInt();
      digestOffset = ByteBuffer.wrap(header, HEADER_OFFSET_DIGEST, Short.BYTES).order(order).getShort();
      segmentTableSize = ByteBuffer.wrap(header, 30, Short.BYTES).order(order).getShort();
      signatureSize = Short.toUnsignedInt(ByteBuffer.wrap(header, 8, Short.BYTES).order(order).getShort());
      typeBlockSize = 0;
      rcodeSize = ByteBuffer.wrap(header, HEADER_OFFSET_RCODE_SIZE, Integer.BYTES).order(order).getInt();
    } else {
      LOGGER.error("Only v10+ rcode is supported");
      throw new InvalidRCodeException("Only v10+ rcode is supported");
    }

    if (out != null) {
      out.printf("%nSig Sz: %08X -- SegTbl Sz: %08X -- TypeBlock Sz: %08X -- RCode Sz: %08X%n", signatureSize,
          segmentTableSize, typeBlockSize, rcodeSize);
    }
  }

  void processSignatureBlock(InputStream input, PrintStream out)
      throws IOException, InvalidRCodeException {
    byte[] header = input.readNBytes(signatureSize);
    if (out != null) {
      out.printf("%n*********%nSIGNATURE%n*********%n");
      printByteBuffer(out, header);
    }
    if ((version & 0x3FFF) < 1100)
      return;
    if (header.length == 0)
      return;
    int preambleSize = readAsciiEncodedNumber(header, 0, 4);
    // Number of elements in block at offset 0x04
    int sigBlockVersion = readAsciiEncodedNumber(header, 8, 4);
    if (sigBlockVersion < 5) 
      return ;
    var cs = oeCharset(readNullTerminatedString(header, 12));

    var pos = preambleSize;
    var str0 = readNullTerminatedString(header, pos, cs);
    if (!str0.isBlank())
      parseSignature(str0);

    // Signature block contains the signature of all internal procedures / functions
    // Not parsed as there's no value for CABL or the language server
  }

  private final void processSegmentTable(InputStream input, PrintStream out) throws IOException {
    byte[] header = input.readNBytes(segmentTableSize);
    if (out != null) {
      out.printf("%n**************%nSEGMENTS TABLE%n**************%n");
      printByteBuffer(out, header);
    }
    if ((version & 0x3FFF) < 1100)
      return;

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

    var offset = round8(
        SEGMENT_TABLE_OFFSET_TEXT_SEGMENT_TABLE_SIZE + 4 + (12 * ipacsTableSize) + (8 * frameSegmentTableSize));
    if ((header.length > offset + 12)) {
      var languages = readNullTerminatedString(header, offset);
      offset += languages.length() + 1;
      textSegmentOffset = ByteBuffer.wrap(header, offset, Integer.BYTES).order(order).getInt();
      textSegmentSize = ByteBuffer.wrap(header, offset + 4, Integer.BYTES).order(order).getInt();
      textSegmentCharset = oeCharset(readNullTerminatedString(header, offset + 8));
    }

    if (out != null) {
      out.printf("%nInitVal segment: %08X %08X%n", initialValueSegmentOffset, initialValueSegmentSize);
      out.printf("Action segment:  %08X %08X%n", actionSegmentOffset, actionSegmentSize);
      out.printf("Ecode segment:   %08X %08X%n", ecodeSegmentOffset, ecodeSegmentSize);
      out.printf("Text segment:    %08X %08X%n", textSegmentOffset, textSegmentSize);
      out.printf("Debug segment:   %08X %08X%n", debugSegmentOffset, debugSegmentSize);
      out.printf("IPACS Sz:                 %08X%n", ipacsTableSize);
      out.printf("FrameSeg Sz:              %08X%n", frameSegmentTableSize);
      out.printf("TextSeg Sz:               %08X%n", textSegmentTableSize);
    }
  }

  private final void readDigest(byte[] rcodeBlock) {
    if ((version & 0x3FFF) >= 1200) {
      crc = ByteBuffer.wrap(rcodeBlock, IVS_CRC_OFFSET_V12, Short.BYTES).order(order).getShort() & 0xFFFF;
      digest = Base64.getEncoder().encodeToString(
          Arrays.copyOfRange(rcodeBlock, digestOffset + 16, digestOffset + 16 + 32));
    } else if ((version & 0x3FFF) >= 1100) {
      crc = ByteBuffer.wrap(rcodeBlock, IVS_CRC_OFFSET_V11, Short.BYTES).order(order).getShort() & 0xFFFF;
      if (digestOffset > 0)
        digest = bufferToHex(Arrays.copyOfRange(rcodeBlock, digestOffset, digestOffset + 16));
    } else {
      crc = ByteBuffer.wrap(rcodeBlock, IVS_CRC_OFFSET_V10, Short.BYTES).order(order).getShort() & 0xFFFF;
      if (digestOffset > 0)
        digest = bufferToHex(Arrays.copyOfRange(rcodeBlock, digestOffset, digestOffset + 16));
    }
  }

  private final void processRCodeBlock(byte[] rcodeBlock, PrintStream out) throws InvalidRCodeException {
    if ((textSegmentOffset >= 0) && (textSegmentSize > 0)) {
      processTextSegment(Arrays.copyOfRange(rcodeBlock, textSegmentOffset, textSegmentOffset + textSegmentSize),
          out);
    }
    if ((initialValueSegmentOffset >= 0) && (initialValueSegmentSize > 0)) {
      processInitialValueSegment(Arrays.copyOfRange(rcodeBlock, initialValueSegmentOffset,
          initialValueSegmentOffset + initialValueSegmentSize), Arrays.copyOfRange(rcodeBlock, textSegmentOffset, textSegmentOffset + textSegmentSize), out);
    }
    if ((actionSegmentOffset >= 0) && (actionSegmentSize > 0)) {
      processActionSegment(
          Arrays.copyOfRange(rcodeBlock, actionSegmentOffset, actionSegmentOffset + actionSegmentSize), Arrays.copyOfRange(rcodeBlock, textSegmentOffset, textSegmentOffset + textSegmentSize), out);
    }
    if ((ecodeSegmentOffset >= 0) && (ecodeSegmentSize > 0)) {
      processEcodeSegment(Arrays.copyOfRange(rcodeBlock, ecodeSegmentOffset, ecodeSegmentOffset + ecodeSegmentSize),
          out);
    }
    if ((debugSegmentOffset > 0) && (debugSegmentSize > 0)) {
      processDebugSegment(Arrays.copyOfRange(rcodeBlock, debugSegmentOffset, debugSegmentOffset + debugSegmentSize),
          out);
    }

  }

  void processTypeBlock(byte[] segment, PrintStream out) throws InvalidRCodeException {
    if (out != null) {
      out.printf("%n**********%nTYPE BLOCK%n***********%n");
      printByteBuffer(out, segment);
    }

    isClass = true;
    if ((version & 0x3FFF) >= 1200) {
      this.typeInfo = TypeInfoV12.newTypeInfo(segment, order);
    } else {
      this.typeInfo = TypeInfoV11.newTypeInfo(segment, order);
    }
  }

  void processInitialValueSegment(byte[] segment, byte[] textSeg, PrintStream out) {
    if (out != null) {
      out.printf("%n**********%nIVS%n***********%n");
      printByteBuffer(out, segment);
    }

    // New global shared variables
    var currOffset = ByteBuffer.wrap(segment, IVS_GLOBAL_SHARED_OFFSET, Integer.BYTES).order(order).getInt();
    List<String> list = new ArrayList<>();
    while (currOffset > 0) {
      var textOffset = ByteBuffer.wrap(segment, currOffset + 4, Integer.BYTES).order(order).getInt();
      var varName = readNullTerminatedString(textSeg, textOffset, textSegmentCharset);
      list.add(varName);
      currOffset = ByteBuffer.wrap(segment, currOffset, Integer.BYTES).order(order).getInt();
    }
    newGlobalSharedVars = List.of(list.toArray(new String[] {}));

    // New shared variables
    currOffset = ByteBuffer.wrap(segment, IVS_SHARED_OFFSET, Integer.BYTES).order(order).getInt();
    list = new ArrayList<>();
    while (currOffset > 0) {
      var textOffset = ByteBuffer.wrap(segment, currOffset + 4, Integer.BYTES).order(order).getInt();
      var varName = readNullTerminatedString(textSeg, textOffset, textSegmentCharset);
      list.add(varName);
      currOffset = ByteBuffer.wrap(segment, currOffset, Integer.BYTES).order(order).getInt();
    }
    newSharedVars = List.of(list.toArray(new String[] {}));

    var ooInfoOffset = ByteBuffer.wrap(segment, IVS_OO_INFO_OFFSET, Integer.BYTES).order(order).getInt();
    // ooInfoOffset is *sometimes* 0, so make sure we don't read garbage data
    if (ooInfoOffset > 0) {
      attributeElementOffset = ByteBuffer.wrap(segment, ooInfoOffset, Integer.BYTES).order(order).getInt();
      referencedTypesOffset = ByteBuffer.wrap(segment, ooInfoOffset + 4, Integer.BYTES).order(order).getInt();
      olSigList = ByteBuffer.wrap(segment, ooInfoOffset + 20, Integer.BYTES).order(order).getInt();
      if ((version & 0x3FFF) >= 1200) {
        attributeElementCount = ByteBuffer.wrap(segment, ooInfoOffset + 36, Short.BYTES).order(order).getShort();
        typeTableSize = ByteBuffer.wrap(segment, ooInfoOffset + 38, Short.BYTES).order(order).getShort();
      } else {
        attributeElementCount = ByteBuffer.wrap(segment, ooInfoOffset + 32, Short.BYTES).order(order).getShort();
        typeTableSize = ByteBuffer.wrap(segment, ooInfoOffset + 34, Short.BYTES).order(order).getShort();
      }
    }
  }

  void processTextSegment(byte[] segment, PrintStream out) {
    // No-op
    if (out != null) {
      out.printf("%n**********%nTEXT%n***********%n");
      printByteBuffer(out, segment);
    }
  }

  void processActionSegment(byte[] segment, byte[] textSegment, PrintStream out) {
    if (out != null) {
      out.printf("%n**********%nACTION%n***********%n");
      printByteBuffer(out, segment);
    }

    var clsList = new String[typeTableSize];
    var sz1 = ((version & 0x3FFF) >= 1200) ? 40 : 24;
    for (int zz = 0; zz < typeTableSize; zz++) {
      var typeNameOffset = ByteBuffer.wrap(segment, referencedTypesOffset + (zz * sz1), Integer.BYTES).order(
          order).getInt();
      var typeName = readNullTerminatedString(textSegment, typeNameOffset);
      clsList[zz] = typeName;
    }

    var methdList = new String[attributeElementCount];
    var propList = new String[attributeElementCount];
    sz1 = ((version & 0x3FFF) >= 1200) ? 40 : 32;
    for (int zz = 0; zz < attributeElementCount; zz++) {
      var elemNameOffset = ByteBuffer.wrap(segment, attributeElementOffset + (zz * sz1), Integer.BYTES).order(
          order).getInt();
      var typeTableIndex = ByteBuffer.wrap(segment, attributeElementOffset + (zz * sz1) + (((version & 0x3FFF) >= 1200) ? 22 : 4), Short.BYTES).order(
          order).getShort();
      var elementType = ByteBuffer.wrap(segment, attributeElementOffset + (zz * sz1) + (((version & 0x3FFF) >= 1200) ? 38:30), Byte.BYTES).order(
          order).get();
      var elemName = readNullTerminatedString(textSegment, elemNameOffset);
      if (elementType == 1) {
        methdList[zz] = clsList[typeTableIndex] + ":" + elemName;
      } else if ((elementType == 2) || (elementType == 8)) {
        propList[zz] = clsList[typeTableIndex] + ":" + elemName;
      }

      refdClasses = Arrays.stream(clsList) //
        .filter(it -> !isClass || !it.equalsIgnoreCase(typeInfo.getTypeName())) //
        .filter(it -> !it.startsWith(BUILTIN_CLASSES_PREFIX)) //
        .toList();
      refdMethods = Arrays.stream(methdList) //
        .filter(Objects::nonNull) //
        .filter(it -> !isClass || !it.startsWith(typeInfo.getTypeName() + ":")) //
        .filter(it -> !it.startsWith(BUILTIN_CLASSES_PREFIX)) //
        .toList();
      refdProperties = Arrays.stream(propList) //
        .filter(Objects::nonNull) //
        .filter(it -> !isClass || !it.startsWith(typeInfo.getTypeName() + ":")) //
        .filter(it -> !it.startsWith(BUILTIN_CLASSES_PREFIX)) //
        .toList();
    }
  }

  void processEcodeSegment(byte[] segment, PrintStream out) throws InvalidRCodeException {
    // No-op
  }

  void processDebugSegment(byte[] segment, PrintStream out) throws InvalidRCodeException {
    // No-op
  }

  void printByteBuffer(PrintStream writer, byte[] block) {
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
      writer.print("   ".repeat(16 - (pos % 16)));
      writer.println(" | " + sb.toString());
    }
  }

  public ITypeInfo getTypeInfo() {
    return typeInfo;
  }

  public List<String> getNewGlobalSharedVars() {
    return newGlobalSharedVars;
  }

  public List<String> getNewSharedVars() {
    return newSharedVars;
  }

  public List<String> getReferencedClasses() {
    return refdClasses == null ? List.of() : refdClasses;
  }

  public List<String> getReferencedMethods() {
    return refdMethods == null ? List.of() : refdMethods;
  }

  public List<String> getReferencedProperties() {
    return refdProperties == null ? List.of() : refdProperties;
  }

  public Optional<IMethodElement> getMainBlock() {
    if (isClass)
      return Optional.empty();
    return Optional.ofNullable(mainBlock);
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

  public long getCrc() {
    return crc;
  }

  public String getDigest() {
    return digest;
  }

  public int getRCodeSize() {
    return rcodeSize;
  }

  private void parseSignature(String str) {
    var pos = str.indexOf(' ');
    var kind = str.substring(0, pos);

    if ("MAIN".equals(kind)) {
      mainBlock = parseProcedureSignature(str.substring(pos + 1));
    }
  }

  private static IMethodElement parseProcedureSignature(String str) {
    var pos = str.indexOf(',');
    var name = str.substring(0, pos);

    var lastSpace = name.lastIndexOf(' ');
    name = name.substring(0, lastSpace);

    var nextPos = nextComma(str, pos + 1);
    var returnType = parseReturnType(str.substring(pos + 1, nextPos));

    pos = nextPos;
    nextPos = nextComma(str, pos + 1);
    List<IParameter> params = new ArrayList<>();
    while (nextPos != -1) {
      if (str.startsWith("0", pos + 1))
        break;
      params.add(parseParameter(str.substring(pos + 1, nextPos)));
      pos = nextPos;
      nextPos = nextComma(str, pos + 1);
    }

    return new MethodElement(name, false, returnType.getO1(), params.toArray(new IParameter[0]));
  }

  private static IParameter parseParameter(String str) {
    var pos = str.indexOf(' ');

    var mode = str.substring(0, pos);
    if ("1".equals(mode))
      mode = "INPUT";
    else if ("2".equals(mode))
      mode = "OUTPUT";
    else if ("3".equals(mode))
      mode = "INPUT-OUTPUT";
    else if ("4".equals(mode))
      mode = "BUFFER";

    str = str.substring(pos + 1);
    pos = str.indexOf(' ');
    var name = str.substring(0, pos);
    var type = parseReturnType(str.substring(pos + 1));

    return new Parameter(0, name, type.getO2(), ParameterMode.getParameterMode(mode), type.getO1());
  }

  private static Pair<DataType, Integer> parseReturnType(String str) {
    var pos = str.indexOf(' ');
    if (pos == -1) {
      var dt = parseDataType(str);
      return Pair.of(dt, 0);
    } else {
      var dt = parseDataType(str.substring(0, pos));
      var extent = Integer.parseInt(str.substring(pos + 1));
      return Pair.of(dt, extent);
    }
  }

  private static DataType parseDataType(String str) {
    if ((str == null) || str.isBlank())
      return DataType.VOID;

    try {
      var dataType = DataType.get(Integer.parseInt(str));
      if (dataType != DataType.UNKNOWN)
        return dataType;
    } catch (NumberFormatException caught) {
      // No-op
    }

    return new DataType(str);
  }

  private static int nextComma(String str, int fromIndex) {
    var len = str.length();
    if (fromIndex >= len)
      return -1;
    var idx = str.indexOf(',', fromIndex);
    // Return end of string if no comma found
    return idx == -1 ? len : idx;
  }

  public static String readNullTerminatedString(byte[] array, int offset) {
    return readNullTerminatedString(array, offset, Charset.defaultCharset());
  }

  static String bufferToHex(byte[] buf) {
    StringBuilder hexString = new StringBuilder(2 * buf.length);
    for (int i = 0; i < buf.length; i++) {
      hexString.append(String.format("%02X", buf[i]));
    }

    return hexString.toString();
  }

  static String readNullTerminatedString(byte[] array, int offset, Charset charset) {
    int zz = 0;
    while ((zz + offset < array.length) && (array[zz + offset] != 0)) {
      zz++;
    }

    return charset.decode(ByteBuffer.wrap(array, offset, zz)).toString();
  }

  static int readAsciiEncodedNumber(byte[] array, int pos, int length) throws InvalidRCodeException {
    try {
      return Integer.valueOf(new String(Arrays.copyOfRange(array, pos, pos + length)), 16);
    } catch (NumberFormatException caught) {
      throw new InvalidRCodeException(caught);
    }
  }

  private static final Charset oeCharset(String str) {
    try {
      return Charset.forName(str);
    } catch (IllegalCharsetNameException | UnsupportedCharsetException caught) {
      return StandardCharsets.UTF_8;
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

  private static int round8(int i) {
    return (i + 7) & ~7;
  }

}
