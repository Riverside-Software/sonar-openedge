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

  private int extent;
  // private int crc;
  private int flags;
  private int parameterType;
  private int paramMode;
  private int dataType;
  // private int fullNameLength;
  private int paramNum;

  private String argumentName;

  protected MethodParameter(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    this.parameterType = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    this.paramMode = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    this.extent = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    this.dataType = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    // this.crc = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();
    this.flags = ByteBuffer.wrap(segment, currentPos + 10, Short.BYTES).order(order).getShort();
    // this.fullNameLength = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort();
    int argumentNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    int nameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();

    this.argumentName = argumentNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + argumentNameOffset);
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);
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
      return argumentName;
    return getABLDataType().name();
  }

  public String getArgumentName() {
    return argumentName;
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
