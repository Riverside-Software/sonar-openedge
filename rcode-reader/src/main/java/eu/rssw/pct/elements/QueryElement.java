package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class QueryElement extends AbstractAccessibleElement {
  private final String[] bufferNames;
  private final int prvte;
  private final int flags;
  // protected int cacheSize;
  // protected int crc;

  public QueryElement(String name, Set<AccessType> accessType, String[] buffers, int flags, int prvte) {
    super(name, accessType);
    this.bufferNames = buffers;
    this.flags = flags;
    this.prvte = prvte;
  }

  public static QueryElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int prvte = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    // int cacheSize = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    return new QueryElement(name2, accessType, bufferNames, flags, prvte);
  }

  @Override
  public int size() {
    return (24 + 4 * bufferNames.length) + 7 & -8;
  }

  public String[] getBufferNames() {
    return bufferNames;
  }

}
