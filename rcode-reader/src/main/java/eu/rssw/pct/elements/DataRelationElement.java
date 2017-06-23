package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.RCodeInfo;

public class DataRelationElement extends AbstractElement {
  // private int pairCount;
  // private int flags;

  private String parentBufferName;
  private String childBufferName;
  private String fieldPairs;

  public DataRelationElement(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    // this.pairCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    // this.flags = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();

    int parentBufferNameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    this.parentBufferName = parentBufferNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + parentBufferNameOffset);

    int childBufferNameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    this.childBufferName = childBufferNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + childBufferNameOffset);

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int fieldPairsOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    this.fieldPairs = fieldPairsOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + fieldPairsOffset);
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
