package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.DataType;
import eu.rssw.pct.IParameter;
import eu.rssw.pct.ParameterMode;
import eu.rssw.pct.ParameterType;
import eu.rssw.pct.RCodeInfo;

public class MethodParameter extends AbstractElement implements IParameter {
  private static final int PARAMETER_APPEND = 1;
  private static final int PARAMETER_HANDLE = 2;
  private static final int PARAMETER_BIND = 4;

  public static final int PARAMETER_INPUT = 6028;
  public static final int PARAMETER_INOUT = 6110;
  public static final int PARAMETER_OUTPUT = 6049;
  public static final int PARAMETER_BUFFER = 1070;
  
  private final int paramNum;
  private final int extent;
  private final int flags;
  private final int parameterType;
  private final int paramMode;
  private final int dataType;
  private final String dataTypeName;
  // private int crc;
  // private int fullNameLength;

  public MethodParameter(int num, String name, int type, int mode, int flags, int dataType, String dataTypeName,
      int extent) {
    super(name);
    this.paramNum = num;
    this.parameterType = type;
    this.paramMode = mode;
    this.dataType = dataType;
    this.dataTypeName = dataTypeName;
    this.flags = flags;
    this.extent = extent;
  }

  protected static MethodParameter fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int parameterType = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int paramMode = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    int dataType = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 10, Short.BYTES).order(order).getShort();
    // int fullNameLength = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort();
    int argumentNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    int nameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();

    String dataTypeName = argumentNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + argumentNameOffset);
    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    return new MethodParameter(0, name, parameterType, paramMode, flags, dataType, dataTypeName, extent);
  }

  @Override
  public int size() {
    return 24;
  }

  @Override
  public int getExtent() {
    return extent;
  }

  public DataType getABLDataType() {
    return DataType.getDataType(dataType);
  }

  @Override
  public String getDataType() {
    if (dataType == DataType.CLASS.getNum())
      return dataTypeName;
    return getABLDataType().name();
  }

  public String getArgumentName() {
    return dataTypeName;
  }

  public ParameterMode getABLMode() {
    return ParameterMode.getParameterMode(paramMode);
  }

  @Override
  public ParameterType getParameterType() {
    return ParameterType.getParameterType(this.parameterType);
  }

  @Override
  public String getMode() {
    return getABLMode().getName();
  }

  @Override
  public String getName() {
    return name.isEmpty() ? "arg" + this.paramNum : name;
  }

  @Override
  public boolean isClassDataType() {
    return dataType == DataType.CLASS.getNum();
  }

  public boolean isBind() {
    return (flags & PARAMETER_BIND) != 0;
  }

  public boolean isAppend() {
    return (flags & PARAMETER_APPEND) != 0;
  }

  public boolean isHandle() {
    return (flags & PARAMETER_HANDLE) != 0;
  }
}
