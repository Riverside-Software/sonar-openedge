package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class BufferElement extends AbstractAccessibleElement {
  private static final int TEMP_TABLE = 4;

  private String tableName;
  private String databaseName;

  private int flags;

  public BufferElement(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    super(name, accessType);

    int nameOffset = ByteBuffer.wrap(segment, currentPos, Integer.BYTES).order(order).getInt();
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int tableNameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    this.tableName = tableNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + tableNameOffset);

    int databaseNameOffset = ByteBuffer.wrap(segment, currentPos + 8, Integer.BYTES).order(order).getInt();
    this.databaseName = databaseNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + databaseNameOffset);

    this.flags = ByteBuffer.wrap(segment, currentPos + 12, Short.BYTES).order(order).getShort();
    // this.crc = ByteBuffer.wrap(segment, currentPos + 14, Short.BYTES).order(order).getShort();
    // this.prvt = ByteBuffer.wrap(segment, currentPos + 16, Short.BYTES).order(order).getShort();
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
