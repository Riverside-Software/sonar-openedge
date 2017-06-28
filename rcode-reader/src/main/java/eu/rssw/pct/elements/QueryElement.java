package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class QueryElement extends AbstractAccessibleElement {
  protected int prvte;
  protected int cacheSize;
  protected int flags;
  protected int crc;
  private String[] bufferNames;

  public QueryElement(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    super(name, accessType);

    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    this.prvte = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    this.cacheSize = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    this.flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    this.crc = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      this.bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }
  }

  @Override
  public int size() {
    return (24 + 4 * bufferNames.length) + 7 & -8;
  }

  public String[] getBufferNames() {
    return bufferNames;
  }

}
