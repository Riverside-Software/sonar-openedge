package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.DataType;
import eu.rssw.pct.IParameter;
import eu.rssw.pct.RCodeInfo;

public class MethodElement extends AbstractAccessibleElement {
  protected static final int METHOD_DESCRIPTOR_SIZE = 24;
  protected static final int FINAL_METHOD = 1;
  protected static final int PROTECTED_METHOD = 2;
  protected static final int PUBLIC_METHOD = 4;
  protected static final int PRIVATE_METHOD = 8;
  protected static final int PROCEDURE_METHOD = 16;
  protected static final int FUNCTION_METHOD = 32;
  protected static final int CONSTRUCTOR_METHOD = 64;
  protected static final int DESTRUCTOR_METHOD = 128;
  protected static final int OVERLOADED_METHOD = 256;
  protected static final int STATIC_METHOD = 512;

  protected int flags;
  protected int returnType;

  protected String typeName;
  protected int extent;
  protected IParameter[] parameters;

  public MethodElement(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset,
      ByteOrder order) {
    super(name, accessType);

    this.flags = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    this.returnType = ByteBuffer.wrap(segment, currentPos + 2, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    int paramCount = ByteBuffer.wrap(segment, currentPos + 4, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();
    this.extent = ByteBuffer.wrap(segment, currentPos + 8, Short.BYTES).order(ByteOrder.LITTLE_ENDIAN).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 12, Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).getInt();
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int typeNameOffset = ByteBuffer.wrap(segment, currentPos + 16, Integer.BYTES).order(
        ByteOrder.LITTLE_ENDIAN).getInt();
    this.typeName = typeNameOffset == 0 ? ""
        : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + typeNameOffset);

    int currPos = currentPos + 24;
    parameters = new IParameter[paramCount];
    for (int zz = 0; zz < paramCount; zz++) {
      MethodParameter param = new MethodParameter(segment, currPos, textAreaOffset, order);
      currPos += param.size();
      parameters[zz] = param;
    }
  }

  @Override
  public String toString() {
    return String.format("Method %s(%d arguments) returns %s", name, parameters.length, returnType); 
  }

  protected String getReturnTypeName() {
    return typeName;
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

  public IParameter[] getParameters() {
    return this.parameters;
  }

  @Override
  public boolean isProtected() {
    if (accessType != null)
      return accessType.contains(AccessType.PROTECTED);
    else
      return (flags & PROTECTED_METHOD) != 0;
  }

  @Override
  public boolean isPublic() {
    if (accessType != null)
      return accessType.contains(AccessType.PUBLIC);
    else
      return (flags & PUBLIC_METHOD) != 0;
  }

  @Override
  public boolean isPrivate() {
    if (accessType != null)
      return accessType.contains(AccessType.PRIVATE);
    else
      return (flags & PRIVATE_METHOD) != 0;
  }

  @Override
  public boolean isStatic() {
    return (flags & STATIC_METHOD) != 0;
  }

  public boolean isProcedure() {
    return (flags & PROCEDURE_METHOD) != 0;
  }

  public boolean isFinal() {
    return (flags & FINAL_METHOD) != 0;
  }

  public boolean isFunction() {
    return (flags & FUNCTION_METHOD) != 0;
  }

  public boolean isConstructor() {
    return (flags & CONSTRUCTOR_METHOD) != 0;
  }

  public boolean isDestructor() {
    return (flags & DESTRUCTOR_METHOD) != 0;
  }

  public boolean isOverloaded() {
    return (flags & OVERLOADED_METHOD) != 0;
  }

  public int getExtent() {
    if (this.extent == 32769) {
      return -1;
    }
    return this.extent;
  }
}
