package eu.rssw.pct.elements;

public interface IEventElement extends IAccessibleElement {
  DataType getReturnType();
  String getReturnTypeName();
  String getDelegateName();
  IParameter[] getParameters();
}
