package eu.rssw.pct.elements.fixed;

import eu.rssw.pct.elements.DataType;
import eu.rssw.pct.elements.IPropertyElement;
import eu.rssw.pct.elements.ITypeInfo;

/**
 * Only for Progress.Lang.Enum:GetValue()
 */
public class EnumGetValueMethodElement extends MethodElement {
  private final ITypeInfo parent;

  public EnumGetValueMethodElement(ITypeInfo parent) {
    super("GetValue", false, DataType.UNKNOWN);
    this.parent = parent;
  }

  public ITypeInfo getParent() {
    return parent;
  }

  @Override
  public DataType getReturnType() {
    if (parent == null)
      return DataType.INTEGER;

    // If more than 2^32 values, then it's INT64. But getProperties().size() returns an int,
    // and anyway it's unlikely that the compiler will accept more than 2 billion entries
    // So we just directly check if any Enum value is larger than 2b
    for (IPropertyElement prop : parent.getProperties()) {
      if ((prop.getEnumDescriptor() != null) && (prop.getEnumDescriptor().getValue() > Integer.MAX_VALUE)) {
          return DataType.INT64;
      }
    }
    return DataType.INTEGER;
  }
}