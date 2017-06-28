package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.RCodeInfo;

public class IndexElement extends AbstractElement {
  private static final int UNIQUE_INDEX = 2;
  private static final int WORD_INDEX = 8;
  private static final int DEFAULT_INDEX = 16;

  protected int primary;
  protected int flags;
  protected IndexComponentElement[] indexComponents;

  protected IndexElement(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    this.primary = segment[currentPos];
    this.flags = segment[currentPos + 1];

    int componentCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int nameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int currPos = currentPos + 16;
    indexComponents = new IndexComponentElement[componentCount];
    for (int zz = 0; zz < componentCount; zz++) {
      IndexComponentElement component = new IndexComponentElement(segment, currPos, textAreaOffset, order);
      currPos += component.size();
      indexComponents[zz] = component;
    }
  }

  @Override
  public int size() {
    int size = 16;
    for (IndexComponentElement elem : indexComponents) {
      size += elem.size();
    }
    return size;
  }

  public IndexComponentElement[] getIndexComponents() {
    return this.indexComponents;
  }

  public boolean isPrimary() {
    return primary == 1;
  }

  public boolean isUnique() {
    return (flags & UNIQUE_INDEX) != 0;
  }

  public boolean isWordIndex() {
    return (flags & WORD_INDEX) != 0;
  }

  public boolean isDefaultIndex() {
    return (flags & DEFAULT_INDEX) != 0;
  }
}
