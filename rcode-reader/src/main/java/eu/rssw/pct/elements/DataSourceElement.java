package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class DataSourceElement extends AbstractAccessibleElement {
  private final String queryName;
  private final String keyComponentNames;
  private final String[] bufferNames;

  public DataSourceElement(String name, Set<AccessType> accessType, String queryName, String keyComponentNames,
      String[] bufferNames) {
    super(name, accessType);
    this.queryName = queryName;
    this.keyComponentNames = keyComponentNames;
    this.bufferNames = bufferNames;
  }

  public static DataSourceElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment,
      int currentPos, int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    // int flags = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int queryNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String queryName = queryNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + queryNameOffset);

    int keyComponentNamesOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    String keyComponentNames = keyComponentNamesOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + keyComponentNamesOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    return new DataSourceElement(name2, accessType, queryName, keyComponentNames, bufferNames);
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
