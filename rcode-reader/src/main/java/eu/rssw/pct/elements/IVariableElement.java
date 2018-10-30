package eu.rssw.pct.elements;

public interface IVariableElement extends IAccessibleElement {

  int getExtent();
  DataType getDataType();
  String getTypeName();

  boolean isReadOnly();
  boolean isWriteOnly();
  boolean isNoUndo();
  boolean baseIsDotNet();
}
