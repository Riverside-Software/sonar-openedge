package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import eu.rssw.pct.RCodeInfo;

public class IndexElement extends AbstractElement {
  private static final int UNIQUE_INDEX = 2;
  private static final int WORD_INDEX = 8;
  private static final int DEFAULT_INDEX = 16;

  private final int primary;
  private final int flags;
  private final IndexComponentElement[] indexComponents;

  public IndexElement(String name, int primary, int flags, IndexComponentElement[] indexComponents) {
    super(name);
    this.primary = primary;
    this.flags = flags;
    this.indexComponents = indexComponents;
  }

  protected static IndexElement fromDebugSegment(byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    int primary = segment[currentPos];
    int flags = segment[currentPos + 1];

    int componentCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int nameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    String name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int currPos = currentPos + 16;
    IndexComponentElement[] indexComponents = new IndexComponentElement[componentCount];
    for (int zz = 0; zz < componentCount; zz++) {
      IndexComponentElement component = IndexComponentElement.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += component.size();
      indexComponents[zz] = component;
    }

    return new IndexElement(name, primary, flags, indexComponents);
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
