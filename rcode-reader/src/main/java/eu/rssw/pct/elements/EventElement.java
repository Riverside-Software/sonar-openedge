package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.DataType;
import eu.rssw.pct.IParameter;
import eu.rssw.pct.RCodeInfo;

public class EventElement extends AbstractAccessibleElement {
  // private int flags;
  private int returnType;
  private IParameter[] parameters;

  private String returnTypename;
  private String delegateName;

  public EventElement(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    super(name, accessType);
    
    // this.flags = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();
    this.returnType = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(order).getShort();
    int parameterCount = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(order).getInt();
    this.name = RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(order).getInt();
    this.returnTypename = typeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);

    int delegateNameOffset = ByteBuffer.wrap(segment, currentPos + 20, Integer.BYTES).order(order).getInt();
    this.delegateName = delegateNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + delegateNameOffset);

    int currPos = currentPos + 24;
    parameters = new IParameter[parameterCount];
    for (int zz = 0; zz < parameterCount; zz++) {
      MethodParameter param = new MethodParameter(segment, currPos, textAreaOffset, order);
      currPos += param.size();
      parameters[zz] = param;
    }
  }

  public IParameter[] getParameters() {
    return this.parameters;
  }

  @Override
  public int size() {
    int size = 24;
    for (IParameter p : parameters) {
      size += ((MethodParameter) p).size();
    }

    return size;
  }

  public DataType getReturnType() {
    return DataType.getDataType(returnType);
  }

  public String getDelegateName() {
    return delegateName;
  }

  protected String getReturnTypeName() {
    return returnTypename;
  }

}
