package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IndexComponentElement extends AbstractElement {
  private final int ascending;
  private final int flags;
  private final int position;

  public IndexComponentElement(int position, int flags, int ascending) {
    this.position = position;
    this.flags = flags;
    this.ascending = ascending;
  }

  protected static IndexComponentElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int ascending = segment[currentPos];
    int flags = segment[currentPos + 1];
    int position = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();

    return new IndexComponentElement(position, flags, ascending);
  }

  @Override
  public int size() {
    return 8;
  }

  public int getFlags() {
    return flags;
  }

  public int getFieldPosition() {
    return this.position;
  }

  public boolean getAscending() {
    return this.ascending == 105;
  }

  public boolean getDescending() {
    return this.ascending == 106;
  }

}
