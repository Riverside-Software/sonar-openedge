package eu.rssw.pct.elements;


public interface IPropertyElement extends IAccessibleElement {
  public static final int PUBLIC_GETTER = 1;
  public static final int PROTECTED_GETTER = 2;
  public static final int PRIVATE_GETTER = 4;
  public static final int PUBLIC_SETTER = 8;
  public static final int PROTECTED_SETTER = 16;
  public static final int PRIVATE_SETTER = 32;
  public static final int HAS_GETTER = 256;
  public static final int HAS_SETTER = 512;
  public static final int PROPERTY_AS_VARIABLE = 1024;
  public static final int PROPERTY_IS_INDEXED = 8192;
  public static final int PROPERTY_IS_DEFAULT = 16384;
  public static final int PROPERTY_IS_ENUM = 65536;

  IVariableElement getVariable();
  IMethodElement getGetter();
  IMethodElement getSetter();
}
