package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.RCodeInfo;

public class DataRelationElement extends AbstractElement {
  // private int pairCount;
  // private int flags;

  private final String parentBufferName;
  private final String childBufferName;
  private final String fieldPairs;

  public DataRelationElement(String name, String parentBuffer, String childBuffer, String fieldPairs) {
    super(name);
    this.parentBufferName = parentBuffer;
    this.childBufferName = childBuffer;
    this.fieldPairs = fieldPairs;
  }

  public static DataRelationElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    // int pairCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    // int flags = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();

    int parentBufferNameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    String parentBufferName = parentBufferNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + parentBufferNameOffset);

    int childBufferNameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String childBufferName = childBufferNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + childBufferNameOffset);

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int fieldPairsOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    String fieldPairs = fieldPairsOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + fieldPairsOffset);

    return new DataRelationElement(name, parentBufferName, childBufferName, fieldPairs);
  }

  public String getParentBufferName() {
    return parentBufferName;
  }

  public String getChildBufferName() {
    return childBufferName;
  }

  public String getFieldPairs() {
    return fieldPairs;
  }

  @Override
  public int size() {
    return 24;
  }
}
