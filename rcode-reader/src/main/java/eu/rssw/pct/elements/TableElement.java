package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class TableElement extends AbstractAccessibleElement {
  protected int flags;
  protected int crc;
  protected int prvte;
  protected VariableElement[] fields;
  protected IndexElement[] indexes;
  private String beforeTableName;

  public TableElement(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    super(name, accessType);

    int fieldCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int indexCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    this.flags = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    this.crc = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    this.prvte = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    // int indexComponentCount = ByteBuffer.wrap(segment, currentPos + 10, Short.BYTES).order(
    //    ByteOrder.LITTLE_ENDIAN).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
    this.name = RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);
    int beforeNameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(
        ByteOrder.LITTLE_ENDIAN).getInt();
    this.beforeTableName = beforeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + beforeNameOffset);

    fields = new VariableElement[fieldCount];
    int currPos = currentPos + 24;
    for (int zz = 0; zz < fieldCount; zz++) {
      VariableElement var = new VariableElement("", null, segment, currPos, textAreaOffset, order);
      currPos += var.size();
      fields[zz] = var;
    }

    indexes = new IndexElement[indexCount];
    for (int zz = 0; zz < indexCount; zz++) {
      IndexElement idx = new IndexElement(segment, currPos, textAreaOffset, order);
      currPos += idx.size();
      indexes[zz] = idx;
    }
  }

  public String getBeforeTableName() {
    return beforeTableName;
  }

  @Override
  public int size() {
    int size = 24;
    for (VariableElement e : fields) {
      size += e.size();
    }
    for (IndexElement e : indexes) {
      size += e.size();
    }
    return size;
  }

  public VariableElement[] getFields() {
    return fields;
  }

  public IndexElement[] getIndexes() {
    return indexes;
  }

}
