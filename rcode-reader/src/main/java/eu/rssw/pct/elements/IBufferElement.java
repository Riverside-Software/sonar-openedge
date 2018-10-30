package eu.rssw.pct.elements;

public interface IBufferElement extends IAccessibleElement {
  String getDatabaseName();
  String getTableName();
  boolean isTempTableBuffer();
}
