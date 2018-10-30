package eu.rssw.pct.elements;

public interface IDatasetElement extends IAccessibleElement {
  IDataRelationElement[] getDataRelations();
  String[] getBufferNames();
}
