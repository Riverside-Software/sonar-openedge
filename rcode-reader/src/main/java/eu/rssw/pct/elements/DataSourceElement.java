package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class DataSourceElement extends AbstractAccessibleElement {
  // private int flags;
  // private int crc;

  private String queryName;
  private String keyComponentNames;
  private String[] bufferNames;

  public DataSourceElement(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    super(name, accessType);

    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    // this.flags = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    // this.crc = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int queryNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    this.queryName = queryNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + queryNameOffset);

    int keyComponentNamesOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    this.keyComponentNames = keyComponentNamesOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + keyComponentNamesOffset);

    bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      this.bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }
  }

  public String getQueryName() {
    return queryName;
  }

  public String getKeyComponents() {
    return keyComponentNames;
  }

  @Override
  public int size() {
    int size = 24 + (this.bufferNames.length * 4);
    return size + 7 & -8;
  }

  public String[] getBufferNames() {
    return bufferNames;
  }

}
