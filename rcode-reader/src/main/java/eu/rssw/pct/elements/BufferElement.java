package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class BufferElement extends AbstractAccessibleElement {
  private static final int TEMP_TABLE = 4;

  private final String tableName;
  private final String databaseName;
  private final int flags;

  public BufferElement(String name, Set<AccessType> accessType, String tableName, String dbName, int flags) {
    super(name, accessType);
    this.tableName = tableName;
    this.databaseName = dbName;
    this.flags = flags;
  }

  public static BufferElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int tableNameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    String tableName = tableNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + tableNameOffset);

    int databaseNameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    String databaseName = databaseNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + databaseNameOffset);

    int flags = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(order).getShort();
    // int prvt = ByteBuffer.wrap(segment, currentPos + 16, Short.BYTES).order(order).getShort();

    return new BufferElement(name2, accessType, tableName, databaseName, flags);
  }

  @Override
  public String toString() {
    return String.format("Buffer %s for %s.%s", name, databaseName, tableName);
  }

  public String getTableName() {
    return tableName;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  @Override
  public int size() {
    return 24;
  }

  public boolean isTempTableBuffer() {
    return (flags & TEMP_TABLE) != 0;
  }

}
