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
package eu.rssw.pct;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import eu.rssw.pct.elements.DataType;
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
    this(input, null);
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
  public RCodeInfo(InputStream input, PrintStream out) throws InvalidRCodeException, IOException {
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
    if (out != null) {
      out.printf("%n*********%nSIGNATURE%n*********%n");
      printByteBuffer(out, header);
    }

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
    if (out != null) {
      out.printf("%n*******%nSEGMENT%n*******%n");
      printByteBuffer(out, header);
    }

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
    if (out != null) {
      out.printf("%n**********%nTYPE BLOCK%n***********%n");
      printByteBuffer(out, segment);
    }

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
    if (out != null) {
      out.printf("%n**********%nINITIAL VALUES%n***********%n");
      printByteBuffer(out, segment);
    }
  }

  void processDebugSegment(InputStream input, PrintStream out) throws IOException, InvalidRCodeException {
    byte[] segment = new byte[debugSegmentSize];
    int bytesRead = input.read(segment);
    if (bytesRead != debugSegmentSize) {
      throw new InvalidRCodeException("Not enough bytes in debug segment block");
    }
    if (out != null) {
      out.printf("%n*******%nDEBUG%n*******%n");
      printByteBuffer(out, segment);
    }

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

  @SuppressWarnings("unused")
  private static Function parseProcSignature(String str) {
    Function fn = new Function();
    
    // String is a comma-separated list of at least 3 elements
    Iterator<String> lst =  Splitter.on(',').trimResults().split(str).iterator();
    
    // First element is the description, a space-separated list of 3 sub-elements
    String desc = lst.next();
    // Sub-element 1 is the type
    fn.type = FunctionType.getTypeFromString(desc.substring(0, desc.indexOf(' ')));
    // Sub-element 2 is the name (may contain spaces...)
    fn.name = desc.substring(desc.indexOf(' ') + 1, desc.lastIndexOf(' '));
    // Sub-element 3 is the flag
    fn.accessTypes = SigAccessType.getTypeFromString(desc.substring(desc.lastIndexOf(' ') + 1));
    
    // Second element is return type, which may be empty (if entry doesn't return anything)
    String retType = lst.next();
    if (retType.isEmpty()) {
      fn.returnType = DataType.VOID;
    } else {
      // Generics handling, which break comma-separated list...
      if (retType.contains("[[")) {
        while (!retType.contains("]]")) {
          retType = retType + lst.next();
        }
      }
      // If not empty, then two entries separated by a space, first is data type, then extent number
      fn.returnExtent = Integer.parseInt(retType.substring(retType.lastIndexOf(' ') + 1));
      fn.returnType = DataType.getDataType(retType.substring(0, retType.lastIndexOf(' ')));
      if (fn.returnType == DataType.CLASS) {
        fn.classReturnType = retType.substring(0, retType.indexOf(' '));
      }
    }
    
    // Parameters
    while (lst.hasNext()) {
      String str2 = lst.next();
      if (!str2.isEmpty()) {
        // Generics handling, which break comma-separated list...
        if (str2.contains("[[")) {
          while (!str2.contains("]]")) {
            str2 = str2 + lst.next();
          }
        }

        fn.parameters.add(parseParameter(str2));
      }
    }
    
    return fn;
  }

  private static Parameter parseParameter(String str) {
    Parameter param = new Parameter();
    List<String> prm = Splitter.on(' ').trimResults().splitToList(str);
    if (prm.size() != 4) {
      return param;
    }
    param.name = prm.get(1);
    param.type = ParameterType.getTypeFromString(prm.get(0));
    param.datatype = DataType.getDataType(prm.get(2));
    param.extent = Integer.parseInt(prm.get(3));
    param.classType = prm.get(2);

    return param;
  }

  public static class Function {
    private Set<SigAccessType> accessTypes;
    private String name;
    private List<Parameter> parameters = new ArrayList<>();
    private DataType returnType;
    private String classReturnType;
    private int returnExtent;
    private FunctionType type;

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      for (SigAccessType type : accessTypes) {
        sb.append(type).append(' ');
      }
      sb.append(type).append(' ').append(returnType);
      if (returnType == DataType.CLASS) {
        sb.append('{').append(classReturnType).append('}');
      }
      if (returnExtent > 0) {
        sb.append(" EXTENT ").append(returnExtent);
      }
      sb.append(' ').append(name).append(" (");

      StringBuilder sb2 = new StringBuilder();
      for (Parameter p : parameters) {
        if (sb2.length() > 0) {
          sb2.append(", ");
        }
        sb2.append(p.toString());
      }
      sb.append(sb2.toString()).append(')');

      return sb.toString();
    }
  }

  public enum FunctionType {
    MAIN,
    CONSTRUCTOR,
    METHOD,
    FUNCTION,
    PROCEDURE,
    EXTERNAL_PROCEDURE,
    DLL_PROCEDURE,
    DESTRUCTOR;

    public static FunctionType getTypeFromString(String str) {
      switch (str.toUpperCase()) {
        case "CONST": return CONSTRUCTOR;
        case "MAIN": return MAIN;
        case "METH": return METHOD;
        case "FUNC": return FUNCTION;
        case "PROC": return PROCEDURE;
        case "EXT": return EXTERNAL_PROCEDURE;
        case "DLL": return DLL_PROCEDURE;
        case "DEST": return DESTRUCTOR;
        default: return null;
      }
    }
  }

  public enum SigAccessType {
    PUBLIC,
    PRIVATE,
    PROTECTED,
    STATIC,
    ABSTRACT,
    FINAL,
    OVERRIDE;

    public static Set<SigAccessType> getTypeFromString(String str) {
      int val = Integer.valueOf(str);
      EnumSet<SigAccessType> set = EnumSet.noneOf(SigAccessType.class);
      switch (val & 0x07) {
        case 1:
          set.add(PUBLIC);
          break;
        case 2:
          set.add(PRIVATE);
          break;
        case 4:
          set.add(PROTECTED);
          break;
        default:
          break;
      }
      if ((val & 0x08) != 0)
        set.add(STATIC);
      if ((val & 0x10) != 0)
        set.add(ABSTRACT);
      if ((val & 0x20) != 0)
        set.add(FINAL);
      if ((val & 0x40) != 0)
        set.add(OVERRIDE);

      return set;
    }
  }

  public enum ParameterType {
    INPUT,
    OUTPUT,
    INPUT_OUTPUT,
    BUFFER;

    public static ParameterType getTypeFromString(String str) {
      switch (str) {
        case "1":
          return INPUT;
        case "2":
          return OUTPUT;
        case "3":
          return INPUT_OUTPUT;
        case "4":
          return BUFFER;
        default:
          return null;
      }
    }
  }

  public static class Parameter {
    private ParameterType type;
    private String name;
    private DataType datatype;
    private int extent;
    private String classType;

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      if (type != null) {
        sb.append(type.toString());
      }
      sb.append(" PARAMETER ");
      if (type == ParameterType.BUFFER) {
        sb.append(" FOR ").append(name);
      } else {
        sb.append(name).append(" AS ").append(datatype);
        if (datatype == DataType.CLASS) {
          sb.append('{').append(classType).append('}');
        }
        if (extent > 0) {
          sb.append(" EXTENT ").append(extent);
        }
      }

      return sb.toString();
    }
  }

}
