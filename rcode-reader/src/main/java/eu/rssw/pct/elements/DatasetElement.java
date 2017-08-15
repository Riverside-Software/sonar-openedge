package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class DatasetElement extends AbstractAccessibleElement {
  private final int prvte;
  private final String[] bufferNames;
  private final DataRelationElement[] relations;

  public DatasetElement(String name, Set<AccessType> accessType, int prvte, String[] bufferNames,
      DataRelationElement[] relations) {
    super(name, accessType);
    this.prvte = prvte;
    this.bufferNames = bufferNames;
    this.relations = relations;
  }

  public static DatasetElement fromDebugSegment(String name, Set<AccessType> accessType, byte[] segment, int currentPos,
      int textAreaOffset, ByteOrder order) {
    int bufferCount = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    int relationshipCount = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int prvte = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();
    // int flags = ByteBuffer.wrap(segment, currentPos + 6, Short.BYTES).order(order).getShort();
    // int crc = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    String name2 = nameOffset == 0 ? name : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    String[] bufferNames = new String[bufferCount];
    for (int zz = 0; zz < bufferCount; zz++) {
      bufferNames[zz] = RCodeInfo.readNullTerminatedString(segment,
          textAreaOffset + ByteBuffer.wrap(segment, currentPos + 24 + (zz * 4), Integer.BYTES).order(order).getInt());
    }

    int currPos = currentPos + 4 * bufferCount;
    DataRelationElement[] relations = new DataRelationElement[relationshipCount];
    for (int zz = 0; zz < relationshipCount; zz++) {
      DataRelationElement param = DataRelationElement.fromDebugSegment(segment, currPos, textAreaOffset, order);
      currPos += param.size();
      relations[zz] = param;
    }

    return new DatasetElement(name2, accessType, prvte, bufferNames, relations);
  }

  @Override
  public String toString() {
    return String.format("Dataset %s for %d buffer(s) and %d relations", name, bufferNames.length, relations.length);
  }

  @Override
  public int size() {
    int size = 24 + (bufferNames.length * 4);
    for (DataRelationElement elem : relations) {
      size += elem.size();
    }
    return size + 7 & -8;
  }

  public DataRelationElement[] getDataRelations() {
    return this.relations;
  }

  public String[] getBufferNames() {
    return bufferNames;
  }
}
