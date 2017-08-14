package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.DataType;
import eu.rssw.pct.RCodeInfo;

public class VariableElement extends AbstractAccessibleElement {
  private static final int READ_ONLY = 1;
  private static final int WRITE_ONLY = 2;
  private static final int BASE_IS_DOTNET = 4;
  private static final int NO_UNDO = 8;

  private final int dataType;
  private final int extent;
  private final int flags;
  private final String typeName;
  // private int fullNameLength;
  // private int recordPosition;

  public VariableElement(String name, Set<AccessType> accessType, int dataType, int extent, int flags,
      String typeName) {
    super(name, accessType);
    this.dataType = dataType;
    this.extent = extent;
    this.flags = flags;
    this.typeName = typeName;
  }

  public static VariableElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment,
      int currentPos, int textAreaOffset, ByteOrder order) {
    int dataType = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    // int recordPosition = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int extent = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    // int fullNameLength = ByteBuffer.wrap(segment, currentPos + 10, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String typeName = typeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);

    return new VariableElement(name2, accessType, dataType, extent, flags, typeName);
  }

  protected String getTypeName() {
    return typeName;
  }

  public String toString() {
    return String.format("Variable %s [%d] - %s", name, extent, getDataType().toString());
  }

  public DataType getDataType() {
    return DataType.getDataType(dataType);
  }

  public int getExtent() {
    return this.extent;
  }

  @Override
  public int size() {
    return 24;
  }

  public boolean isReadOnly() {
    return (flags & READ_ONLY) != 0;
  }

  public boolean isWriteOnly() {
    return (flags & WRITE_ONLY) != 0;
  }

  public boolean isNoUndo() {
    return (flags & NO_UNDO) != 0;
  }

  public boolean baseIsDotNet() {
    return (flags & BASE_IS_DOTNET) != 0;
  }

}
