package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IndexComponentElement extends AbstractElement {
  protected int ascending;
  protected int flags;
  protected int position;

  protected IndexComponentElement(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    this.ascending = segment[currentPos];
    this.flags = segment[currentPos + 1];
    this.position = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
  }

  @Override
  public int size() {
    return 8;
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
