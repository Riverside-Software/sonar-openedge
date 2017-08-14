package eu.rssw.pct.elements;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.Set;

import eu.rssw.pct.AccessType;
import eu.rssw.pct.RCodeInfo;

public class PropertyElement extends AbstractAccessibleElement {
  private static final int PUBLIC_GETTER = 1;
  private static final int PROTECTED_GETTER = 2;
  private static final int PRIVATE_GETTER = 4;
  private static final int PUBLIC_SETTER = 8;
  private static final int PROTECTED_SETTER = 16;
  private static final int PRIVATE_SETTER = 32;
  private static final int HAS_GETTER = 256;
  private static final int HAS_SETTER = 512;
  private static final int PROPERTY_AS_VARIABLE = 1024;
  private static final int PROPERTY_IS_INDEXED = 8192;
  private static final int PROPERTY_IS_DEFAULT = 16384;

  private int flags;
  private VariableElement variable;
  private MethodElement getter;
  private MethodElement setter;
  private int extent;

  public PropertyElement(String name, Set<AccessType> accessType, byte[] segment, int currentPos, int textAreaOffset, ByteOrder order) {
    super(name, accessType);

    this.flags = ByteBuffer.wrap(segment, currentPos, Short.BYTES).order(order).getShort();

    int nameOffset = ByteBuffer.wrap(segment, currentPos + 4, Integer.BYTES).order(order).getInt();
    this.name = nameOffset == 0 ? "" : RCodeInfo.readNullTerminatedString(segment, textAreaOffset + nameOffset);

    int currPos = currentPos + 8;
    if (this.propertyAsVariable()) {
      this.variable = VariableElement.fromDebugSegment("", accessType, segment, currPos, textAreaOffset, order);
      currPos += this.variable.size();
    }

    if (this.hasGetter()) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if (isGetterPublic())
        atp.add(AccessType.PUBLIC);
      if (isGetterProtected())
        atp.add(AccessType.PROTECTED);
      this.getter = MethodElement.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
      currPos += this.getter.size();
    }
    if (this.hasSetter()) {
      Set<AccessType> atp = EnumSet.noneOf(AccessType.class);
      if (isGetterPublic())
        atp.add(AccessType.PUBLIC);
      if (isGetterProtected())
        atp.add(AccessType.PROTECTED);
      this.setter = MethodElement.fromDebugSegment("", atp, segment, currPos, textAreaOffset, order);
    }
  }

  @Override
  public int size() {
    int size = 8;
    if (this.propertyAsVariable()) {
      size += this.variable.size();
    }
    if (this.hasGetter()) {
      size += this.getter.size();
    }
    if (this.hasSetter()) {
      size += this.setter.size();
    }
    return size;
  }

  public VariableElement getVariable() {
    return this.variable;
  }

  public MethodElement getGetter() {
    return this.getter;
  }

  public MethodElement getSetter() {
    return this.setter;
  }

  public boolean propertyAsVariable() {
    return (flags & PROPERTY_AS_VARIABLE) != 0;
  }

  public boolean hasGetter() {
    return (flags & HAS_GETTER) != 0;
  }

  public boolean hasSetter() {
    return (flags & HAS_SETTER) != 0;
  }

  public boolean isGetterPublic() {
    return (flags & PUBLIC_GETTER) != 0;
  }

  public boolean isGetterProtected() {
    return (flags & PROTECTED_GETTER) != 0;
  }

  public boolean isGetterPrivate() {
    return (flags & PRIVATE_GETTER) != 0;
  }

  public boolean isSetterPublic() {
    return (flags & PUBLIC_SETTER) != 0;
  }

  public boolean isSetterProtected() {
    return (flags & PROTECTED_SETTER) != 0;
  }

  public boolean isSetterPrivate() {
    return (flags & PRIVATE_SETTER) != 0;
  }

  public boolean isIndexed() {
    return (flags & PROPERTY_IS_INDEXED) != 0;
  }

  public boolean isDefault() {
    return (flags & PROPERTY_IS_DEFAULT) != 0;
  }

  public int getExtent() {
    return this.extent;
  }

  public boolean canRead() {
    return (isGetterPrivate() || isGetterProtected() || isGetterPublic());
  }

  public boolean canWrite() {
    return (isSetterPrivate() || isSetterProtected() || isSetterPublic());
  }

}
