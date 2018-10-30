package eu.rssw.pct.elements;

public interface IMethodElement extends IAccessibleElement {
  DataType getReturnType();
  String getReturnTypeName();
  int getExtent();
  IParameter[] getParameters();

  boolean isProcedure();
  boolean isFunction();
  boolean isConstructor();
  boolean isDestructor();
  boolean isOverloaded();
}
