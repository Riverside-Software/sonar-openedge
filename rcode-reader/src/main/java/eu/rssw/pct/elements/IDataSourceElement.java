package eu.rssw.pct.elements;

public interface IDataSourceElement extends IAccessibleElement {
  String getQueryName();
  String getKeyComponents();
  String[] getBufferNames();
}
